package com.example.demo.service;
import com.example.demo.config.*;
import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.exception.EmailAlreadyExistException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
    }
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
        
    }
    public UserResponseDTO login(LoginRequestDTO request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException(
                    "Invalid email or password");
        }
        return null;
    }
    public UserResponseDTO register(RegisterRequestDTO request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistException("Email already registered");
        }
        
        UserResponseDTO responseDTO = new UserResponseDTO();
        

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole("USER");

        User savedUser = userRepository.save(user);
        
        responseDTO.setId(savedUser.getId());
        responseDTO.setName(savedUser.getName());
        responseDTO.setEmail(savedUser.getEmail());
        responseDTO.setPhoneNumber(savedUser.getPhoneNumber());
        responseDTO.setRole(savedUser.getRole());
        
        return responseDTO;
        
        
        
    }
    
    
}