package com.example.demo.exception;

import com.example.demo.dto.ErrorResponseDTO;
import com.example.demo.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void unexpectedExceptionReturnsSafeErrorResponse() {
        Exception exception =
                new RuntimeException("Database password leaked");

        ErrorResponseDTO response =
                handler.handleUnexpectedException(exception);

        assertEquals(500, response.getStatus());
        assertEquals(
                "An unexpected error occurred. Please try again later.",
                response.getMessage()
        );
        assertNull(response.getErrors());
    }
}