package com.devh.payment_sim.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.devh.payment_sim.dto.OpenAccountRequest;
import com.devh.payment_sim.exception.ConflictException;
import com.devh.payment_sim.exception.ResourceNotFoundException;
import com.devh.payment_sim.model.BankAccount;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.repository.BankAccountRepository;
import com.devh.payment_sim.repository.UserRepository;
import com.devh.payment_sim.service.BankAccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    @Override
    public BankAccount createAccount(OpenAccountRequest request) {
       
       boolean alreadyExists = bankAccountRepository.existsByAccountNumber(request.getAccountNumber());

       if(alreadyExists){
        log.warn("Account Number already exists - Account Number: {}", request.getAccountNumber());
        throw new ConflictException("Account Number already exists");
       }
        
       String hashedPin = BCrypt.hashpw(request.getBankPin(), BCrypt.gensalt());

       User user = userRepository.findById(request.getUserId())
            .orElseThrow(()-> {
                log.warn("User not found for bank account creation - userId: {}", request.getUserId());
                return new ResourceNotFoundException("User not found: "+ request.getUserId());
            });

        BankAccount account = BankAccount.builder()
                            .user(user)
                            .accountNumber(request.getAccountNumber())
                            .balance(BigDecimal.valueOf(10000))
                            .bankPinHash(hashedPin)
                            .build();

        BankAccount savedAccount = bankAccountRepository.save(account);
        log.info("Bank account created - AccountId: {}, UserId: {}, AccountNumber: {}", 
                 savedAccount.getId(), request.getUserId(), request.getAccountNumber());
        return savedAccount;
    }

    @Override
    public List<BankAccount> getBankAccountsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> {
                    log.warn("User not found while fetching accounts - userId: {}", userId);
                    return new ResourceNotFoundException("User not found: "+userId);
                });
        
        List<BankAccount> accounts = bankAccountRepository.findByUser(user);
        log.debug("Retrieved {} bank accounts for userId: {}", accounts.size(), userId);
        return accounts;
    }
    
    @Override
    public BankAccount getBankAccountByAccountNumber(String accountNumber) {
        return bankAccountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> {
                            log.warn("Bank account not found - accountNumber: {}", accountNumber);
                            return new ResourceNotFoundException("Bank Account not found: "+accountNumber);
                        });
    }
}
