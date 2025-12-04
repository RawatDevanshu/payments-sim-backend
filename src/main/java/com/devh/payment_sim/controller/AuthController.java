package com.devh.payment_sim.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devh.payment_sim.config.JwtUtil;
import com.devh.payment_sim.core.ApiResponse;
import com.devh.payment_sim.dto.AuthRequest;
import com.devh.payment_sim.dto.AuthResponse;
import com.devh.payment_sim.dto.UserRequest;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.security.CustomUserDetailsService;
import com.devh.payment_sim.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@Valid @RequestBody UserRequest request){
        User savedUser = userService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authRequest.getEmail(), authRequest.getPassword()
        )
        );

        final UserDetails userDetails = 
                userDetailsService.loadUserByUsername(authRequest.getEmail());
        
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }
    
}
