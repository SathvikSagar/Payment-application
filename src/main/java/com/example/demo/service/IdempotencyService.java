
package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.IdempotencyRecord;
import com.example.demo.entity.IdempotencyStatus;
import com.example.demo.entity.Transaction;
import com.example.demo.repository.IdempotencyRecordRepository;

@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;

    public IdempotencyService(IdempotencyRecordRepository repository) {
        this.repository = repository;
    }


	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void claimKey(
	        String key,
	        String senderEmail,
	        String receiverEmail,
	        BigDecimal amount) {
	
	    IdempotencyRecord record = new IdempotencyRecord();
	    record.setIdempotencyKey(key);
	    record.setSenderEmail(senderEmail);
	    record.setReceiverEmail(receiverEmail);
	    record.setAmount(amount);
	    record.setStatus(IdempotencyStatus.PROCESSING);
	    record.setCreatedAt(LocalDateTime.now());
	
	    repository.saveAndFlush(record);
	}

	

	@Transactional(readOnly = true)
	public Optional<IdempotencyRecord> findByKey(String key) {
	    return repository.findByIdempotencyKey(key);
	}
	public void validateExistingRequest(
	        IdempotencyRecord record,
	        String senderEmail,
	        String receiverEmail,
	        BigDecimal amount) {

	    if (!isSameRequest(record, senderEmail, receiverEmail, amount)) {
	        throw new IllegalArgumentException(
	                "Idempotency key was already used for a different request");
	    }

	    if (record.getStatus() == IdempotencyStatus.PROCESSING) {
	        throw new IllegalStateException(
	                "A request with this idempotency key is already processing");
	    }

	    if (record.getStatus() == IdempotencyStatus.FAILED) {
	        throw new IllegalStateException(
	                "This idempotency key belongs to a failed request");
	    }
	}

	public boolean isSameRequest(
	        IdempotencyRecord record,
	        String senderEmail,
	        String receiverEmail,
	        BigDecimal amount) {
	
	    return record.getSenderEmail().equalsIgnoreCase(senderEmail)
	            && record.getReceiverEmail().equalsIgnoreCase(receiverEmail)
	            && record.getAmount().compareTo(amount) == 0;
	}

	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markCompleted(String key, Transaction transaction) {
	    IdempotencyRecord record = repository.findByIdempotencyKey(key)
	            .orElseThrow(() ->
	                    new RuntimeException("Idempotency record not found"));
	
	    record.setStatus(IdempotencyStatus.COMPLETED);
	    record.setTransaction(transaction);
	
	    repository.save(record);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markFailed(String key) {
	    IdempotencyRecord record = repository.findByIdempotencyKey(key)
	            .orElseThrow(() ->
	                    new RuntimeException("Idempotency record not found"));
	
	    record.setStatus(IdempotencyStatus.FAILED);
	
	    repository.save(record);
	}

	public Transaction getCompletedTransaction(IdempotencyRecord record) {
	    if (record.getStatus() != IdempotencyStatus.COMPLETED) {
	        throw new IllegalStateException(
	                "Idempotency record is not completed");
	    }
	
	    if (record.getTransaction() == null) {
	        throw new IllegalStateException(
	                "Completed idempotency record has no transaction");
	    }
	
	    return record.getTransaction();
	}

	



}
