package com.devh.payment_sim.service.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;

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
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) throw new RuntimeException("User not found");

        if(walletRepository.existsByUpiHandle(upiHandle)){
            throw new RuntimeException("Upi handle already in use");
        }

        Wallet wallet = Wallet.builder()
                .user(userOpt.get())
                .upiHandle(upiHandle)
                .balance(BigDecimal.ZERO)
                .isActive(true)
                .build();
        
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWalletByUpi(String upiHandle) {
        return walletRepository.findByUpiHandle(upiHandle)
                .orElseThrow(()->new RuntimeException("Wallet not found"));
    }
    
}
