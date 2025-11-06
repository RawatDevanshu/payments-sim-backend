package com.devh.payment_sim.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.devh.payment_sim.model.User;
import com.devh.payment_sim.repository.UserRepository;
import com.devh.payment_sim.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User registerUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
}
