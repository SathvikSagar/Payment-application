package com.example.demo.repository;

import com.example.demo.entity.Transaction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository
        extends JpaRepository<Transaction, Integer> {
	List<Transaction> findBySenderIdOrReceiverId(
	        Integer senderId,
	        Integer receiverId
	);
}