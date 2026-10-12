package com.example.demo.service;

import com.example.demo.dto.DepositRequestDTO;
import com.example.demo.service.IdempotencyService;
import com.example.demo.service.FailedTransactionService;
import com.example.demo.dto.TransactionResponseDTO;
import com.example.demo.dto.TransferRequestDTO;
import com.example.demo.dto.WalletResponseDTO;
import com.example.demo.entity.IdempotencyStatus;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.TransactionStatus;
import com.example.demo.entity.User;
import com.example.demo.entity.Wallet;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.service.IdempotencyClaimService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final FailedTransactionService failedTransactionService;
    private final IdempotencyService idempotencyService;
    private final IdempotencyClaimService idempotencyClaimService;
    private final WalletTransferWorker walletTransferWorker;
    public WalletService(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            FailedTransactionService failedTransactionService,
            IdempotencyService idempotencyService,
            IdempotencyClaimService idempotencyClaimService,
            WalletTransferWorker walletTransferWorker) {

        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.failedTransactionService = failedTransactionService;
        this.idempotencyService = idempotencyService;
        this.idempotencyClaimService = idempotencyClaimService;
        this.walletTransferWorker = walletTransferWorker;
    }
    public Wallet createWallet(User user) {
        Wallet wallet = new Wallet();

        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUser(user);

        return walletRepository.save(wallet);
    }
    public WalletResponseDTO getWalletBalance(String email) {

        Wallet wallet = walletRepository.findByUserEmailForUpdate(email)
                .orElseThrow(() ->
                        new RuntimeException("Wallet not found"));

        WalletResponseDTO response = new WalletResponseDTO();
        response.setBalance(wallet.getBalance());

        return response;
    }
    @Transactional
    public WalletResponseDTO deposit(String email, DepositRequestDTO request) {

        Wallet wallet = walletRepository.findByUserEmailForUpdate(email)
                .orElseThrow(() ->
                        new RuntimeException("Wallet not found"));

        BigDecimal amount = request.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Deposit amount must be greater than zero");
        }

        wallet.setBalance(wallet.getBalance().add(amount));

        Wallet savedWallet = walletRepository.save(wallet);

        WalletResponseDTO response = new WalletResponseDTO();
        response.setBalance(savedWallet.getBalance());

        return response;
    }
    
    public Transaction transfer(String senderEmail, TransferRequestDTO request) {
    	
        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero");
        }

        if (senderEmail.equalsIgnoreCase(request.getReceiverEmail())) {
            throw new IllegalArgumentException(
                    "Cannot transfer money to yourself");
        }
        if (request.getIdempotencyKey() == null ||
    	        request.getIdempotencyKey().isBlank()) {
    	    throw new IllegalArgumentException(
    	            "Idempotency key is required");
    	}

        String key = request.getIdempotencyKey();

        var existingRecord = idempotencyService.findByKey(key);

        if (existingRecord.isPresent()) {
            var record = existingRecord.get();

            idempotencyService.validateExistingRequest(
                    record,
                    senderEmail,
                    request.getReceiverEmail(),
                    request.getAmount());

            if (record.getStatus() == IdempotencyStatus.COMPLETED) {
                return idempotencyService.getCompletedTransaction(record);
            }

            throw new IllegalStateException(
                    "This idempotency key cannot be processed right now");
        }

        Transaction existingTransaction = transactionRepository
                .findByIdempotencyKey(key)
                .orElse(null);

        if (existingTransaction != null) {
            boolean sameRequest =
                    existingTransaction.getSender().getEmail()
                            .equalsIgnoreCase(senderEmail)
                    && existingTransaction.getReceiver().getEmail()
                            .equalsIgnoreCase(request.getReceiverEmail())
                    && existingTransaction.getAmount()
                            .compareTo(request.getAmount()) == 0;

            if (!sameRequest) {
                throw new IllegalArgumentException(
                        "Idempotency key was already used for a different request");
            }

            return existingTransaction;
        }



	boolean claimed = idempotencyClaimService.tryClaimKey(
	        key,
	        senderEmail,
	        request.getReceiverEmail(),
	        request.getAmount());
	
	if (!claimed) {
	    var record = idempotencyService.findByKey(key)
	            .orElseThrow(() -> new IllegalStateException(
	                    "Idempotency key conflict; retry the request"));
	
	    idempotencyService.validateExistingRequest(
	            record,
	            senderEmail,
	            request.getReceiverEmail(),
	            request.getAmount());
	
	    if (record.getStatus() == IdempotencyStatus.COMPLETED) {
	        return idempotencyService.getCompletedTransaction(record);
	    }
	
	    throw new IllegalStateException(
	            "This idempotency key cannot be processed right now");
	}
	
	try {
	    Transaction transaction = walletTransferWorker.executeTransfer(
	            senderEmail, request);
	
	    idempotencyService.markCompleted(key, transaction);
	
	    return transaction;
	} catch (RuntimeException ex) {
	    try {
	        idempotencyService.markFailed(key);
	    } catch (RuntimeException statusException) {
	        ex.addSuppressed(statusException);
	    }

	    throw ex;
		}
    }

    public List<TransactionResponseDTO> getTransactionHistory(String email) {

        Wallet wallet = walletRepository.findByUserEmailForUpdate(email)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        Integer userId = wallet.getUser().getId();

        return transactionRepository
                .findBySenderIdOrReceiverId(userId, userId)
                .stream()
                .map(transaction -> {
                    TransactionResponseDTO dto = new TransactionResponseDTO();

                    dto.setId(transaction.getId());
                    dto.setSenderEmail(transaction.getSender().getEmail());
                    dto.setReceiverEmail(transaction.getReceiver().getEmail());
                    dto.setAmount(transaction.getAmount());
                    dto.setStatus(transaction.getStatus());
                    dto.setCreatedAt(transaction.getCreatedAt());

                    return dto;
                })
                .toList();
    }
}