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

@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
public class UPIPinController {
    private final UPIPinService upiPinService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<String>> setUPIPin(@Valid @RequestBody UPIPinRequest request){
        upiPinService.setPin(request.getWalletUpiHandle(), request.getPin());
        return ResponseEntity.ok(ApiResponse.success("UPI PIN set successfully",null));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateUPIPin(@Valid @RequestBody UPIPinRequest request){
        boolean isValid = upiPinService.validatePin(request.getWalletUpiHandle(), request.getPin());
        return isValid 
                ? ResponseEntity.ok(ApiResponse.success("PIN is valid", null)) 
                : ResponseEntity.status(401).body(ApiResponse.success("Invalid PIN", null));
    }
}
