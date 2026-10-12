package com.example.demo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.IdempotencyRecord;

public interface IdempotencyRecordRepository
        extends JpaRepository<IdempotencyRecord, Integer> {

    Optional<IdempotencyRecord> findByIdempotencyKey(
            String idempotencyKey);
}