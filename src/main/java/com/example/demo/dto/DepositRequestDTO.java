package com.example.demo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepositRequestDTO {

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal amount;
}