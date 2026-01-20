package com.devh.payment_sim.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devh.payment_sim.core.ApiResponse;
import com.devh.payment_sim.dto.response.EntityToResponseMapper;
import com.devh.payment_sim.dto.response.UserResponse;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id){
        log.info("===FETCH USER INITIATED=== UserId: {}", id);
        User fetchedUser = userService.getUserById(id);
        log.info("===FETCH USER COMPLETED=== EmailId: {}", fetchedUser.getEmail());
        
        UserResponse response = EntityToResponseMapper.toUserResponse(fetchedUser);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
    }
}
