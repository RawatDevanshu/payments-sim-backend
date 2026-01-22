package com.devh.payment_sim.service.impl;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.devh.payment_sim.exception.ConflictException;
import com.devh.payment_sim.exception.PinNotSetException;
import com.devh.payment_sim.exception.ResourceNotFoundException;
import com.devh.payment_sim.model.UPIPin;
import com.devh.payment_sim.model.Wallet;
import com.devh.payment_sim.repository.UPIPinRepository;
import com.devh.payment_sim.repository.WalletRepository;
import com.devh.payment_sim.service.UPIPinService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UPIPinServiceImpl implements UPIPinService {
    private final UPIPinRepository upiPinRepository;
    private final WalletRepository walletRepository;

    @Override
    public void setPin(String walletUpiHandle, String rawPin) {
        boolean pinAlreadySet = upiPinRepository.existsByWallet_UpiHandle(walletUpiHandle);

        if(pinAlreadySet){
            log.warn("Pin is already set for this upi handle - Wallet Upi Handle: {}", walletUpiHandle);
            throw new ConflictException("Pin already set for this Upi Handle");
        }

        Wallet wallet = walletRepository.findByUpiHandle(walletUpiHandle)
                .orElseThrow(()-> {
                    log.warn("Wallet not found while setting PIN - upiHandle: {}", walletUpiHandle);
                    return new ResourceNotFoundException("Wallet not found: " + walletUpiHandle);
                });
        
        String hashedPin = BCrypt.hashpw(rawPin, BCrypt.gensalt());

        UPIPin upiPin = UPIPin.builder()
                .wallet(wallet)
                .pinHash(hashedPin)
                .build();

        upiPinRepository.save(upiPin);
        log.info("UPI PIN set successfully - upiHandle: {}", walletUpiHandle);
    }

    @Override
    public boolean validatePin(String walletUpiHandle, String rawPin) {
        UPIPin upiPin = upiPinRepository.findByWallet_UpiHandle(walletUpiHandle)
                            .orElseThrow(()-> {
                                log.warn("PIN not set for wallet - upiHandle: {}", walletUpiHandle);
                                return new PinNotSetException("UPI Pin not set");
                            });
        
        boolean isValid = BCrypt.checkpw(rawPin, upiPin.getPinHash());
        
        if (!isValid) {
            log.warn("Invalid PIN attempt - upiHandle: {}", walletUpiHandle);
        }
        
        return isValid;
    }
    
}
