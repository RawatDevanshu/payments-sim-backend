package com.devh.payment_sim.service.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.devh.payment_sim.exception.ConflictException;
import com.devh.payment_sim.exception.ResourceNotFoundException;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.model.Wallet;
import com.devh.payment_sim.repository.UserRepository;
import com.devh.payment_sim.repository.WalletRepository;
import com.devh.payment_sim.service.WalletService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Override
    public Wallet createWallet(Long userId, String upiHandle) {
        User user = userRepository.findById(userId)
                        .orElseThrow(()-> new ResourceNotFoundException("User not found: "+userId));

        if(walletRepository.existsByUpiHandle(upiHandle)){
            throw new ConflictException("Upi handle already in use");
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .upiHandle(upiHandle)
                .balance(BigDecimal.ZERO)
                .isActive(true)
                .build();
        
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWalletByUpi(String upiHandle) {
        return walletRepository.findByUpiHandle(upiHandle)
                .orElseThrow(()->new ResourceNotFoundException("Wallet not found: "+upiHandle));
    }
    
}
