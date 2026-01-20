package com.devh.payment_sim.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devh.payment_sim.core.ApiResponse;
import com.devh.payment_sim.dto.UPIPinRequest;
import com.devh.payment_sim.service.UPIPinService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
public class UPIPinController {
    private final UPIPinService upiPinService;
    
    @PostMapping("/set")
    public ResponseEntity<ApiResponse<String>> setUPIPin(@Valid @RequestBody UPIPinRequest request){
        log.info("===PIN SETUP INITIATED=== Upi Handle: {}", request.getWalletUpiHandle());
        upiPinService.setPin(request.getWalletUpiHandle(), request.getPin());
        log.info("===PIN SETUP COMPLETED===");
        
        return ResponseEntity.ok(ApiResponse.success("UPI PIN set successfully",null));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateUPIPin(@Valid @RequestBody UPIPinRequest request){
        log.info("===PIN VALIDATION INITIATED=== Upi Handle: {}", request.getWalletUpiHandle());
        boolean isValid = upiPinService.validatePin(request.getWalletUpiHandle(), request.getPin());
        log.info("===PIN VALIDATION COMPLETED=== result = isValid -> {}", isValid);

        return isValid 
                ? ResponseEntity.ok(ApiResponse.success("PIN is valid", null)) 
                : ResponseEntity.status(401).body(ApiResponse.success("Invalid PIN", null));
    }
}
