package com.devh.payment_sim.service.impl;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.devh.payment_sim.model.UPIPin;
import com.devh.payment_sim.model.Wallet;
import com.devh.payment_sim.repository.UPIPinRepository;
import com.devh.payment_sim.repository.WalletRepository;
import com.devh.payment_sim.service.UPIPinService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UPIPinServiceImpl implements UPIPinService {
    private final UPIPinRepository upiPinRepository;
    private final WalletRepository walletRepository;

    @Override
    public void setPin(Long walletId, String rawPin) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(()-> new RuntimeException("Wallet not found"));
        
        String hashedPin = BCrypt.hashpw(rawPin, BCrypt.gensalt());

        UPIPin upiPin = UPIPin.builder()
                .wallet(wallet)
                .pinHash(hashedPin)
                .build();

        upiPinRepository.save(upiPin);
    }

    @Override
    public boolean validatePin(Long walletId, String rawPin) {
        UPIPin upiPin = upiPinRepository.findByWalletId(walletId)
                            .orElseThrow(()-> new RuntimeException("UPI Pin not set"));
        
        return BCrypt.checkpw(rawPin, upiPin.getPinHash());
    }
    
}
