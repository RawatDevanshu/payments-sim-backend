package com.devh.payment_sim.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devh.payment_sim.model.BankAccount;
import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.model.TransactionStatus;
import com.devh.payment_sim.model.TransactionType;
import com.devh.payment_sim.model.Wallet;
import com.devh.payment_sim.repository.BankAccountRepository;
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
    private final BankAccountRepository bankAccountRepository;
    private final UPIPinService upiPinService;

    @Override
    @Transactional
    public Transaction sendMoney(String fromUpi, String toUpi, BigDecimal amount, String upiPin, String remarks) {
        if (fromUpi.equals(toUpi)) {
            throw new IllegalArgumentException("Self-payments are not allowed");
        }

        Wallet sender = walletRepository.findByUpiHandle(fromUpi)
                .orElseThrow(()-> new RuntimeException("Sender wallet not found"));
        Wallet reciever = walletRepository.findByUpiHandle(toUpi)
                .orElseThrow(()-> new RuntimeException("Reciever wallet not found"));

        if(!upiPinService.validatePin(sender.getUpiHandle(), upiPin)){
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
                        .type(TransactionType.WALLET_TRANSFER)
                        .status(TransactionStatus.SUCCESS)
                        .remarks(remarks)
                        .build();
        
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction topUpFromBank(Long userId, String walletUpiHandle, Long bankAccountId, BigDecimal amount, String rawBankPin, String remarks){
        // 1. Fetch bank account
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                                    .orElseThrow(() -> new RuntimeException("Bank not found"));
    
        // 2. Verify ownership of bank account
        if(!bankAccount.getUser().getId().equals(userId)){
            throw new IllegalArgumentException("Bank account does not belong to user");
        }

        // 3. Fetch wallet
        Wallet recieverWallet = walletRepository.findByUpiHandle(walletUpiHandle)
                .orElseThrow(()-> new RuntimeException("Sender wallet not found"));
        
        // 4. Verify ownership of wallet
        if(!recieverWallet.getUser().getId().equals(userId)){
            throw new IllegalArgumentException("Wallet does not belong to user");
        }

        // 5. Verify PIN
        if(!BCrypt.checkpw(rawBankPin, bankAccount.getBankPinHash())){
            throw new IllegalArgumentException("Invalid UPI PIN");
        }

        // 6. Check Balance
        if(bankAccount.getBalance().compareTo(amount) < 0){
            throw new IllegalArgumentException("Insufficient bank balance");
        }

        // 7. Deduct from bank
        bankAccount.setBalance(bankAccount.getBalance().subtract(amount));
        bankAccountRepository.save(bankAccount);

        // 8. Credit to wallet
        recieverWallet.setBalance(recieverWallet.getBalance().add(amount));
        walletRepository.save(recieverWallet);

        // 9. Log transaction
        Transaction tx = Transaction.builder()
                    .fromBankAccount(bankAccount)
                    .toWallet(recieverWallet)
                    .amount(amount)
                    .type(TransactionType.BANK_TOPUP)
                    .status(TransactionStatus.SUCCESS)
                    .remarks(remarks)
                    .build();
        
        return transactionRepository.save(tx);
    }

    @Override
    @Transactional
    public Transaction withdrawFromWallet(Long userId, String walletUpiHandle, Long bankAccountId, BigDecimal amount, String rawWalletPin, String remarks){
        // 1. Fetch bank account
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                                    .orElseThrow(() -> new RuntimeException("Bank not found"));
    
        // 2. Verify ownership of bank account
        if(!bankAccount.getUser().getId().equals(userId)){
            throw new IllegalArgumentException("Bank account does not belong to user");
        }

        // 3. Fetch wallet
        Wallet senderWallet = walletRepository.findByUpiHandle(walletUpiHandle)
                .orElseThrow(()-> new RuntimeException("Sender wallet not found"));
        
        // 4. Verify ownership of wallet
        if(!senderWallet.getUser().getId().equals(userId)){
            throw new IllegalArgumentException("Wallet does not belong to user");
        }

        // 5. Verify PIN
        if(!upiPinService.validatePin(walletUpiHandle, rawWalletPin)){
            throw new IllegalArgumentException("Invalid UPI PIN");
        }

        // 6. Check Balance
        if(senderWallet.getBalance().compareTo(amount) < 0){
            throw new IllegalArgumentException("Insufficient bank balance");
        }

        // 7. Deduct from wallet
        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        walletRepository.save(senderWallet);

        // 8. Credit to bank
        bankAccount.setBalance(bankAccount.getBalance().add(amount));
        bankAccountRepository.save(bankAccount);

        // 9. Log transaction
        Transaction tx = Transaction.builder()
                    .fromWallet(senderWallet)
                    .toBankAccount(bankAccount)
                    .amount(amount)
                    .type(TransactionType.WALLET_WITHDRAW)
                    .status(TransactionStatus.SUCCESS)
                    .remarks(remarks)
                    .build();
        
        return transactionRepository.save(tx);
    }

    @Override
    public List<Transaction> getTransactionsByWalletUpi(String walletUpiHandle) {
        Wallet wallet = walletRepository.findByUpiHandle(walletUpiHandle)
                .orElseThrow(()->new RuntimeException("Wallet not found"));
        
        return transactionRepository.findByFromWalletOrToWallet(wallet, wallet);

    }
    
}
