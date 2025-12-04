package com.devh.payment_sim.service.impl;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.devh.payment_sim.exception.PinNotSetException;
import com.devh.payment_sim.exception.ResourceNotFoundException;
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
    public void setPin(String walletUpiHandle, String rawPin) {
        Wallet wallet = walletRepository.findByUpiHandle(walletUpiHandle)
                .orElseThrow(()-> new ResourceNotFoundException("Wallet not found: " + walletUpiHandle));
        
        String hashedPin = BCrypt.hashpw(rawPin, BCrypt.gensalt());

        UPIPin upiPin = UPIPin.builder()
                .wallet(wallet)
                .pinHash(hashedPin)
                .build();

        upiPinRepository.save(upiPin);
    }

    @Override
    public boolean validatePin(String walletUpiHandle, String rawPin) {
        UPIPin upiPin = upiPinRepository.findByWallet_UpiHandle(walletUpiHandle)
                            .orElseThrow(()-> new PinNotSetException("UPI Pin not set"));
        
        return BCrypt.checkpw(rawPin, upiPin.getPinHash());
    }
    
}
