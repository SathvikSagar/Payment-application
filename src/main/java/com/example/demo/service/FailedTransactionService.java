
package com.example.demo.service;

import com.example.demo.entity.Transaction;
import com.example.demo.entity.TransactionStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class FailedTransactionService {

    private final TransactionRepository transactionRepository;

    public FailedTransactionService(
            TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedTransaction(
            User sender,
            User receiver,
            BigDecimal amount) {

        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
    }
}
