package com.example.demo.exception;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import com.example.demo.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
@RestControllerAdvice

public class GlobalExceptionHandler {
	private static final Logger logger =
	        LoggerFactory.getLogger(GlobalExceptionHandler.class);
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

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponseDTO handleIllegalArgument(
	        IllegalArgumentException ex) {
	
	    ErrorResponseDTO response = new ErrorResponseDTO();
	
	    response.setStatus(400);
	    response.setMessage(ex.getMessage());
	    response.setErrors(null);
	
	    return response;
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponseDTO handleUnexpectedException(Exception ex) {
	
	    // Log the actual exception for debugging
	    // while returning a safe message to the client.
	    
		logger.error("Unexpected server error", ex);
	    ErrorResponseDTO response = new ErrorResponseDTO();
	    response.setStatus(500);
	    response.setMessage("An unexpected error occurred. Please try again later.");
	    response.setErrors(null);
	
	    return response;
	}


	

}
