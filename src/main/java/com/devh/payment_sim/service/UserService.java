package com.devh.payment_sim.service;

import java.util.Optional;

import com.devh.payment_sim.dto.UserRequest;
import com.devh.payment_sim.model.User;

public interface UserService {
    User registerUser(UserRequest user);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
}
