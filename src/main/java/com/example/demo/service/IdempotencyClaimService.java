
package com.example.demo.service;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyClaimService {

    private final IdempotencyService idempotencyService;

    public IdempotencyClaimService(
            IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    public boolean tryClaimKey(
            String key,
            String senderEmail,
            String receiverEmail,
            BigDecimal amount) {

        try {
            idempotencyService.claimKey(
                    key,
                    senderEmail,
                    receiverEmail,
                    amount);
            return true;
        } catch (DataIntegrityViolationException ex) {
            if (isIdempotencyKeyDuplicate(ex)) {
                return false;
            }
            throw ex;
        }
    }

    private boolean isIdempotencyKeyDuplicate(
            DataIntegrityViolationException ex) {

        Throwable cause = ex;

        while (cause != null) {
            String message = cause.getMessage();

            if (message != null
                    && message.contains("UKixtia2gi4nwc73k5jwss4vflk")) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
}
