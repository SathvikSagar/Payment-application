package com.example.demo.service;

import com.example.demo.dto.DepositRequestDTO;
import com.example.demo.dto.TransactionResponseDTO;
import com.example.demo.dto.TransferRequestDTO;
import com.example.demo.dto.WalletResponseDTO;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.TransactionStatus;
import com.example.demo.entity.User;
import com.example.demo.entity.Wallet;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    public WalletService(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }
    
    public Wallet createWallet(User user) {
        Wallet wallet = new Wallet();

        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUser(user);

        return walletRepository.save(wallet);
    }
    public WalletResponseDTO getWalletBalance(String email) {

        Wallet wallet = walletRepository.findByUserEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Wallet not found"));

        WalletResponseDTO response = new WalletResponseDTO();
        response.setBalance(wallet.getBalance());

        return response;
    }
    @Transactional
    public WalletResponseDTO deposit(String email, DepositRequestDTO request) {

        Wallet wallet = walletRepository.findByUserEmail(email)
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
    @Transactional
    public void transfer(String senderEmail, TransferRequestDTO request) {

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero");
        }

        if (senderEmail.equalsIgnoreCase(request.getReceiverEmail())) {
            throw new IllegalArgumentException(
                    "Cannot transfer money to yourself");
        }

        Wallet sender = walletRepository.findByUserEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

        Wallet receiver = walletRepository.findByUserEmail(request.getReceiverEmail())
                .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

        BigDecimal amount = request.getAmount();

        if (sender.getBalance().compareTo(amount) < 0) {
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

        transactionRepository.save(transaction);
    }
    public List<TransactionResponseDTO> getTransactionHistory(String email) {

        Wallet wallet = walletRepository.findByUserEmail(email)
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