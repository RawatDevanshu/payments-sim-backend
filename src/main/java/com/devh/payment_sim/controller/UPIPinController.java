package com.devh.payment_sim.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devh.payment_sim.dto.UPIPinRequest;
import com.devh.payment_sim.service.UPIPinService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
public class UPIPinController {
    private final UPIPinService upiPinService;
    
    @PostMapping
    public ResponseEntity<String> setUPIPin(@RequestBody UPIPinRequest request){
        upiPinService.setPin(request.getWalletUpiHandle(), request.getPin());
        return ResponseEntity.ok("UPI PIN set successfully");
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateUPIPin(@RequestBody UPIPinRequest request){
        boolean isValid = upiPinService.validatePin(request.getWalletUpiHandle(), request.getPin());
        return isValid 
                ? ResponseEntity.ok("PIN is valid") 
                : ResponseEntity.status(401).body("Invalid PIN");
    }
}
