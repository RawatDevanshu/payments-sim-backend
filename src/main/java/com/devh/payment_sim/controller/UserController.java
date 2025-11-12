package com.devh.payment_sim.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devh.payment_sim.core.ApiResponse;
import com.devh.payment_sim.dto.UserRequest;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<User>> registerUser(@RequestBody UserRequest request){
        User savedUser = userService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", savedUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id){
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success("User fetched successfully", user)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("User not found")));
    }
}
