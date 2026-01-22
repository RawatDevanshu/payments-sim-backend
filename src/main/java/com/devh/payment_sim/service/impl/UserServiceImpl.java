package com.devh.payment_sim.service.impl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.devh.payment_sim.dto.UserRequest;
import com.devh.payment_sim.exception.ConflictException;
import com.devh.payment_sim.exception.ResourceNotFoundException;
import com.devh.payment_sim.model.Role;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.repository.UserRepository;
import com.devh.payment_sim.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserRequest request) {
        boolean emailAlreadyExists = userRepository.existsByEmail(request.getEmail());
        boolean phoneAlreadyExists = userRepository.existsByPhone(request.getPhone());

        if(emailAlreadyExists && phoneAlreadyExists){
        log.warn("Email and Phone Number already in use - Email: {}, Phone Number: {}", request.getEmail(), request.getPhone());
        throw new ConflictException("User with this email and phone number already registered");
       }

       if(emailAlreadyExists){
        log.warn("Email already in use - Email: {}", request.getEmail());
        throw new ConflictException("User with this email already registered");
       }

       if(phoneAlreadyExists){
        log.warn("Phone Number already in use - Phone Number: {}", request.getPhone());
        throw new ConflictException("User with this phone number already registered");
       }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .name(request.getName())
                .role(Role.ROLE_USER)
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(hashedPassword)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully - UserId: {}, Email: {}", savedUser.getId(), request.getEmail());
        return savedUser;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found - userId: {}", id);
                    return new ResourceNotFoundException("User not found: " + id);
                });
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found - email: {}", email);
                    return new ResourceNotFoundException("User not found: " + email);
                });
    }

    @Override
    public boolean userExists(String email) {
        boolean exists = userRepository.existsByEmail(email);
        log.debug("User existence check - email: {}, exists: {}", email, exists);
        return exists;
    }
    
}
