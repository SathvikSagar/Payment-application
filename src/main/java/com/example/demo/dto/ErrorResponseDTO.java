package com.example.demo.dto;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponseDTO {

    private int status;
    private String message;
    private Map<String, String> errors;
}