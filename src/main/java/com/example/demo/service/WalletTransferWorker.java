
package com.example.demo.service;

import com.example.demo.dto.TransferRequestDTO;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.TransactionStatus;
import com.example.demo.entity.Wallet;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletTransferWorker {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final FailedTransactionService failedTransactionService;

    public WalletTransferWorker(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            FailedTransactionService failedTransactionService) {

        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.failedTransactionService = failedTransactionService;
    }


	@Transactional
	public Transaction executeTransfer(
	        String senderEmail,
	        TransferRequestDTO request) {
	
	    Integer senderId = walletRepository
	            .findWalletIdByUserEmail(senderEmail)
	            .orElseThrow(() ->
	                    new RuntimeException("Sender wallet not found"));
	
	    Integer receiverId = walletRepository
	            .findWalletIdByUserEmail(request.getReceiverEmail())
	            .orElseThrow(() ->
	                    new RuntimeException("Receiver wallet not found"));
	
	    Wallet firstWallet;
	    Wallet secondWallet;
	
	    if (senderId < receiverId) {
	        firstWallet = walletRepository.findByIdForUpdate(senderId)
	                .orElseThrow(() ->
	                        new RuntimeException("Sender wallet not found"));
	
	        secondWallet = walletRepository.findByIdForUpdate(receiverId)
	                .orElseThrow(() ->
	                        new RuntimeException("Receiver wallet not found"));
	    } else {
	        firstWallet = walletRepository.findByIdForUpdate(receiverId)
	                .orElseThrow(() ->
	                        new RuntimeException("Receiver wallet not found"));
	
	        secondWallet = walletRepository.findByIdForUpdate(senderId)
	                .orElseThrow(() ->
	                        new RuntimeException("Sender wallet not found"));
	    }
	
	    Wallet sender = senderId.equals(firstWallet.getId())
	            ? firstWallet : secondWallet;
	
	    Wallet receiver = receiverId.equals(firstWallet.getId())
	            ? firstWallet : secondWallet;
	
	    BigDecimal amount = request.getAmount();
	
	    if (sender.getBalance().compareTo(amount) < 0) {
	        failedTransactionService.recordFailedTransaction(
	                sender.getUser(),
	                receiver.getUser(),
	                amount);
	
	        throw new IllegalArgumentException("Insufficient balance");
	    }
	
	    sender.setBalance(sender.getBalance().subtract(amount));
	    receiver.setBalance(receiver.getBalance().add(amount));
	
	    walletRepository.save(sender);
	    walletRepository.save(receiver);
	
	    Transaction transaction = new Transaction();
	    transaction.setSender(sender.getUser());
	    transaction.setReceiver(receiver.getUser());
	    transaction.setAmount(amount);
	    transaction.setStatus(TransactionStatus.SUCCESS);
	    transaction.setCreatedAt(LocalDateTime.now());
	    transaction.setIdempotencyKey(request.getIdempotencyKey());
	
	    return transactionRepository.saveAndFlush(transaction);
	}

}
