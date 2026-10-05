package com.example.demo.exception;

import java.util.Map;

import java.util.stream.Collectors;
import com.example.demo.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
@RestControllerAdvice

public class GlobalExceptionHandler {
	@ExceptionHandler(EmailAlreadyExistException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponseDTO handleEmailAlreadyExist(
	        EmailAlreadyExistException ex) {

	    ErrorResponseDTO response = new ErrorResponseDTO();

	    response.setStatus(409);
	    response.setMessage(ex.getMessage());
	    response.setErrors(null);

	    return response;
	}
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponseDTO handleNotValidException(
	        MethodArgumentNotValidException ex) {

	    Map<String, String> errors =
	            ex.getBindingResult()
	                    .getFieldErrors()
	                    .stream()
	                    .map(fieldError -> Map.entry(
	                            fieldError.getField(),
	                            fieldError.getDefaultMessage()
	                    ))
	                    .collect(Collectors.toMap(
	                            Map.Entry::getKey,
	                            Map.Entry::getValue,
	                            (first, second) -> first
	                    ));

	    ErrorResponseDTO response = new ErrorResponseDTO();

	    response.setStatus(400);
	    response.setMessage("Validation failed");
	    response.setErrors(errors);

	    return response;
	}
	

}
