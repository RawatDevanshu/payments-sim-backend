package com.devh.payment_sim.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devh.payment_sim.config.JwtUtil;
import com.devh.payment_sim.core.ApiResponse;
import com.devh.payment_sim.dto.AuthRequest;
import com.devh.payment_sim.dto.AuthResponse;
import com.devh.payment_sim.dto.UserRequest;
import com.devh.payment_sim.dto.response.EntityToResponseMapper;
import com.devh.payment_sim.dto.response.UserResponse;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.security.CustomUserDetailsService;
import com.devh.payment_sim.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRequest request){
        
        log.info("===USER REGISTRATION INITIATED=== Email: {}", request.getEmail());
        User savedUser = userService.registerUser(request);
        log.info("===USER REGISTRATION COMPLETED=== UserId: {}, Email: {}", savedUser.getId(), savedUser.getEmail());

        UserResponse response = EntityToResponseMapper.toUserResponse(savedUser);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@RequestBody AuthRequest authRequest) {
        log.info("===USER LOGIN INITIATED=== Email: {}", authRequest.getEmail());
        
        // Check if user exists
        if (!userService.userExists(authRequest.getEmail())) {
            throw new AuthenticationException("User not found with this email") {};
        }
        log.debug("User found - Email: {}", authRequest.getEmail());
        
    
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authRequest.getEmail(), authRequest.getPassword()
        )
        );
        log.info("USER AUTHENTICATED - Email: {}", authRequest.getEmail());

        final UserDetails userDetails = 
                userDetailsService.loadUserByUsername(authRequest.getEmail());
        
        final String jwt = jwtUtil.generateToken(userDetails);

        ResponseCookie cookie = ResponseCookie.from("accessToken", jwt)
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofMinutes(15))
            .build();
        log.info("===USER LOGIN COMPLETED=== Email: {}", authRequest.getEmail());
        

        return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(ApiResponse.success("Login successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        UserResponse response = EntityToResponseMapper.toUserResponse(user);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", response));
    }
}