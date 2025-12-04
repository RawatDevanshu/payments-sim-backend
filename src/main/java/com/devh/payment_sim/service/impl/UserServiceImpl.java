package com.devh.payment_sim.service.impl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.devh.payment_sim.dto.UserRequest;
import com.devh.payment_sim.exception.ResourceNotFoundException;
import com.devh.payment_sim.model.Role;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.repository.UserRepository;
import com.devh.payment_sim.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserRequest request) {
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .name(request.getName())
                .role(Role.ROLE_USER)
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(hashedPassword)
                .build();

        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
    
}
