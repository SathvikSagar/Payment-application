package com.example.demo.repository;

import com.example.demo.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    Optional<Wallet> findByUserEmail(String email);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user.email = :email")
    Optional<Wallet> findByUserEmailForUpdate(
            @Param("email") String email);
    @Query("SELECT w.id FROM Wallet w WHERE w.user.email = :email")
    Optional<Integer> findWalletIdByUserEmail(
            @Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(
            @Param("id") Integer id);
}