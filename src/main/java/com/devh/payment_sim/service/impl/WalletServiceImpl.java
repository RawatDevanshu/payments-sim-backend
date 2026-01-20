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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Override
    public Wallet createWallet(Long userId, String upiHandle) {
        User user = userRepository.findById(userId)
                        .orElseThrow(()-> {
                            log.warn("User not found for wallet creation - userId: {}", userId);
                            return new ResourceNotFoundException("User not found: "+userId);
                        });

        if(walletRepository.existsByUpiHandle(upiHandle)){
            log.warn("UPI handle already in use - upiHandle: {}", upiHandle);
            throw new ConflictException("Upi handle already in use");
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .upiHandle(upiHandle)
                .balance(BigDecimal.ZERO)
                .isActive(true)
                .build();
        
        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created successfully - WalletId: {}, UPI: {}, UserId: {}", 
                 savedWallet.getId(), upiHandle, userId);
        return savedWallet;
    }

    @Override
    public Wallet getWalletByUpi(String upiHandle) {
        return walletRepository.findByUpiHandle(upiHandle)
                .orElseThrow(()->  {
                    log.warn("Wallet not found - upiHandle: {}", upiHandle);
                    return new ResourceNotFoundException("Wallet not found: "+upiHandle);
                });
    }
    
}
