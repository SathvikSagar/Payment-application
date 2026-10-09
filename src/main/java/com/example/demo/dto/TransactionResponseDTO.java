package com.example.demo.dto;

import com.example.demo.entity.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionResponseDTO {

    private Integer id;
    private String senderEmail;
    private String receiverEmail;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}