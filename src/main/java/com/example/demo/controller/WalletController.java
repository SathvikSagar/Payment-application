package com.example.demo.controller;
import java.util.List;
import com.example.demo.dto.DepositRequestDTO;
import com.example.demo.dto.TransactionResponseDTO;
import com.example.demo.dto.TransferRequestDTO;
import com.example.demo.dto.WalletResponseDTO;
import com.example.demo.entity.Transaction;
import com.example.demo.service.WalletService;

import jakarta.validation.Valid;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<WalletResponseDTO> getWalletBalance(
            Authentication authentication) {

        String email = authentication.getName();

        WalletResponseDTO response =
                walletService.getWalletBalance(email);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/deposit")
    public ResponseEntity<WalletResponseDTO> deposit(
            @Valid @RequestBody DepositRequestDTO request,
            Authentication authentication) {

        String email = authentication.getName();

        WalletResponseDTO response =
                walletService.deposit(email, request);

        return ResponseEntity.ok(response);
    }

	@PostMapping("/transfer")
	public ResponseEntity<TransactionResponseDTO> transfer(
	        @Valid @RequestBody TransferRequestDTO request,
	        Authentication authentication) {
	
	    String senderEmail = authentication.getName();
	
	    Transaction transaction =
	            walletService.transfer(senderEmail, request);
	
	    TransactionResponseDTO response = new TransactionResponseDTO();
	    response.setId(transaction.getId());
	    response.setSenderEmail(transaction.getSender().getEmail());
	    response.setReceiverEmail(transaction.getReceiver().getEmail());
	    response.setAmount(transaction.getAmount());
	    response.setStatus(transaction.getStatus());
	    response.setCreatedAt(transaction.getCreatedAt());
	
	    return ResponseEntity.ok(response);
	}

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionHistory(
            Authentication authentication) {

        String email = authentication.getName();

        List<TransactionResponseDTO> transactions =
                walletService.getTransactionHistory(email);

        return ResponseEntity.ok(transactions);
    }
}