package com.devh.payment_sim.service.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.model.TransactionStatus;
import com.devh.payment_sim.model.Wallet;
import com.devh.payment_sim.repository.TransactionRepository;
import com.devh.payment_sim.repository.WalletRepository;
import com.devh.payment_sim.service.TransactionService;
import com.devh.payment_sim.service.UPIPinService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UPIPinService upiPinService;

    @Override
    public Transaction sendMoney(String fromUpi, String toUpi, BigDecimal amount, String upiPin, String remarks) {
        Wallet sender = walletRepository.findByUpiHandle(fromUpi)
                .orElseThrow(()-> new RuntimeException("Sender wallet not found"));
        Wallet reciever = walletRepository.findByUpiHandle(toUpi)
                .orElseThrow(()-> new RuntimeException("Reciever wallet not found"));

        if(!upiPinService.validatePin(sender.getId(), upiPin)){
            throw new RuntimeException("Invalid UPI PIN");
        }

        if(sender.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient Balance");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        reciever.setBalance(reciever.getBalance().add(amount));

        walletRepository.save(sender);
        walletRepository.save(reciever);

        Transaction transaction = Transaction.builder()
                        .fromWallet(sender)
                        .toWallet(reciever)
                        .amount(amount)
                        .status(TransactionStatus.SUCCESS)
                        .remarks(remarks)
                        .build();
        
        return transactionRepository.save(transaction);
    }
    
}
