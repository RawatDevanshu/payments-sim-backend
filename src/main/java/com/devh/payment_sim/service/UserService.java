package com.devh.payment_sim.service;

import com.devh.payment_sim.dto.UserRequest;
import com.devh.payment_sim.model.User;

public interface UserService {
    User registerUser(UserRequest user);
    User getUserById(Long id);
    User getUserByEmail(String email);
}
