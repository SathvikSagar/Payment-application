package com.example.demo.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {

	@NotBlank
    private String name;
	@NotBlank
	@Email
    private String email;
	@NotBlank
	@Column(unique = true)
	@Pattern(regexp = "^[0-9]{10}$")
    private String phoneNumber;
    @NotBlank
    @Size(min=8)
    private String password;
}
