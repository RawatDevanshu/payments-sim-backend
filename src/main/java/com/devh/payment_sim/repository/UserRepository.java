package com.devh.payment_sim.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devh.payment_sim.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
}
