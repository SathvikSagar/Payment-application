package com.example.demo.service;
import com.example.demo.config.*;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.exception.EmailAlreadyExistException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.WalletService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletService walletService;
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            WalletService walletService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.walletService = walletService;
    }
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
        
    }
    public LoginResponseDTO login(LoginRequestDTO request) {

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
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);

        return response;
        
    }
    @Transactional
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
        walletService.createWallet(savedUser);
        
        responseDTO.setId(savedUser.getId());
        responseDTO.setName(savedUser.getName());
        responseDTO.setEmail(savedUser.getEmail());
        responseDTO.setPhoneNumber(savedUser.getPhoneNumber());
        responseDTO.setRole(savedUser.getRole());
        
        return responseDTO;
        
        
        
    }
    
    
}