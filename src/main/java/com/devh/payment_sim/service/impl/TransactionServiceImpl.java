package com.devh.payment_sim.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devh.payment_sim.exception.InsufficientFundsException;
import com.devh.payment_sim.exception.InvalidPinException;
import com.devh.payment_sim.exception.ResourceNotFoundException;
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
import com.devh.payment_sim.service.internal.TransactionProgressor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionProgressor progressor;

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UPIPinService upiPinService;

    private BigDecimal validateAmount(BigDecimal amount){
        
        // Transfer amount preprocessing
        // Normalize & validate amount defensively
        if (amount == null || amount.signum() <= 0) {
            log.warn("Invalid amount for transfer - amount: {}", amount);
            throw new IllegalArgumentException("Amount must be positive");
        }
        // Ensure at most 2 decimals; reject >2
        if (amount.scale() > 2) {
            log.warn("Invalid decimal scale for amount - amount: {}", amount);
            throw new IllegalArgumentException("Amount cannot have more than 2 decimal places");
        }
        // Normalize to 2-decimal fixed scale for arithmetic consistency
        amount = amount.setScale(2, RoundingMode.HALF_UP);

        return amount;
    }

    @Override
    @Transactional
    public Transaction sendMoney(String fromUpi, String toUpi, BigDecimal amount, String upiPin, String remarks) {
        log.info("Initiating wallet transfer - From: {}, To: {}, Amount: {}", fromUpi, toUpi, amount);
        
       amount = validateAmount(amount);

        if (fromUpi.equals(toUpi)) {
            log.warn("Self-payment attempt - upiHandle: {}", fromUpi);
            throw new IllegalArgumentException("Self-payments are not allowed");
        }

        Wallet sender = walletRepository.findByUpiHandle(fromUpi)
                .orElseThrow(()-> {
                    log.warn("Sender wallet not found - upiHandle: {}", fromUpi);
                    return new ResourceNotFoundException("Sender's wallet not found: " + fromUpi);
                });
        Wallet receiver = walletRepository.findByUpiHandle(toUpi)
                .orElseThrow(()-> {
                    log.warn("Receiver wallet not found - upiHandle: {}", toUpi);
                    return new ResourceNotFoundException("Receiver's wallet not found: " + toUpi);
                });

        Transaction tx = Transaction.builder()
                        .transactionId(UUID.randomUUID().toString())
                        .fromWallet(sender)
                        .toWallet(receiver)
                        .amount(amount)
                        .type(TransactionType.WALLET_TRANSFER)
                        .status(TransactionStatus.CREATED)
                        .remarks(remarks)
                        .build();

        tx = progressor.createInitialTransaction(tx);

        // VALIDATING
        tx = progressor.advance(tx);

        if(!upiPinService.validatePin(sender.getUpiHandle(), upiPin)){
            log.warn("Invalid PIN for transfer - upiHandle: {}", fromUpi);
            progressor.failTransaction(tx.getTransactionId(), tx.getStatus().name(), "Invalid UPI PIN");
            throw new InvalidPinException("Invalid UPI PIN");
        }

        if(sender.getBalance().compareTo(amount) < 0){
            log.warn("Insufficient wallet balance for transfer - upiHandle: {}, Required: {}, Available: {}", 
                     fromUpi, amount, sender.getBalance());
            progressor.failTransaction(tx.getTransactionId(), tx.getStatus().name(), "Insufficient wallet balance");
            throw new InsufficientFundsException("Insufficient wallet balance");
        }

        // PROCESSING
        tx = progressor.advance(tx);

        // DEBIT PENDING
        tx = progressor.advance(tx);

        sender.setBalance(sender.getBalance().subtract(amount));
        walletRepository.save(sender);

        try{
            // CREDIT PENDING
            tx = progressor.advance(tx);
            receiver.setBalance(receiver.getBalance().add(amount));
            walletRepository.save(receiver);
        } catch (Exception ex) {
            log.error("[TXN:{}] Credit operation failed, rolling back debit", tx.getTransactionId());
            progressor.failTransaction(tx.getTransactionId(), tx.getStatus().name(), "Credit operation failed: " + ex.getMessage());
            throw ex;
        }

        // COMPLETED
        tx = progressor.advance(tx);

        log.info("Wallet transfer completed - TxnId: {}, Amount: {}", tx.getTransactionId(), amount);
        return tx;
    }

    @Override
    @Transactional
    public Transaction topUpFromBank(Long userId, String walletUpiHandle, String bankAccountNumber, BigDecimal amount, String rawBankPin, String remarks){
        log.info("Initiating bank top-up - UserId: {}, Wallet: {}, BankAccount: {}, Amount: {}", 
                 userId, walletUpiHandle, bankAccountNumber, amount);
        
        amount = validateAmount(amount);

        // 1. Fetch bank account
        BankAccount bankAccount = bankAccountRepository.findByAccountNumber(bankAccountNumber)
                                    .orElseThrow(() -> {
                                        log.warn("Bank account not found - accountNumber: {}", bankAccountNumber);
                                        return new ResourceNotFoundException("Bank account not found: " + bankAccountNumber);
                                    });
    
        // 2. Verify ownership of bank account
        if(!bankAccount.getUser().getId().equals(userId)){
            log.warn("Bank account ownership mismatch - accountNumber: {}, userId: {}", bankAccountNumber, userId);
            throw new IllegalArgumentException("Bank account does not belong to user");
        }

        // 3. Fetch wallet
        Wallet receiverWallet = walletRepository.findByUpiHandle(walletUpiHandle)
                .orElseThrow(()-> {
                    log.warn("Receiver wallet not found - upiHandle: {}", walletUpiHandle);
                    return new ResourceNotFoundException("Receiver's wallet not found: " + walletUpiHandle);
                });
        
        // 4. Verify ownership of wallet
        if(!receiverWallet.getUser().getId().equals(userId)){
            log.warn("Wallet ownership mismatch - upiHandle: {}, userId: {}", walletUpiHandle, userId);
            throw new IllegalArgumentException("Wallet does not belong to user");
        }

        // CREATING
        Transaction tx = Transaction.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .fromBankAccount(bankAccount)
                    .toWallet(receiverWallet)
                    .amount(amount)
                    .type(TransactionType.BANK_TOPUP)
                    .status(TransactionStatus.CREATED)
                    .remarks(remarks)
                    .build();

        tx = progressor.createInitialTransaction(tx);

        // VALIDATING
        tx = progressor.advance(tx);

        // 5. Verify PIN
        if(!BCrypt.checkpw(rawBankPin, bankAccount.getBankPinHash())){
            log.warn("Invalid bank PIN for top-up - accountNumber: {}", bankAccountNumber);
            progressor.failTransaction(tx.getTransactionId(), tx.getStatus().name(), "Invalid Bank PIN");
            throw new InvalidPinException("Invalid Bank PIN");
        }

        // 6. Check Balance
        if(bankAccount.getBalance().compareTo(amount) < 0){
            log.warn("Insufficient bank balance for top-up - accountNumber: {}, Required: {}, Available: {}", 
                     bankAccountNumber, amount, bankAccount.getBalance());
            progressor.failTransaction(tx.getTransactionId(), tx.getStatus().name(), "Insufficient bank balance");
            throw new InsufficientFundsException("Insufficient bank balance");
        }

        // PROCESSING
        tx = progressor.advance(tx);

        // DEBIT_PENDING
        tx = progressor.advance(tx);

        // 7. Deduct from bank
        bankAccount.setBalance(bankAccount.getBalance().subtract(amount));
        bankAccountRepository.save(bankAccount);

        try{
        // CREDIT_PENDING
        tx = progressor.advance(tx);

        // 8. Credit to wallet
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));
        walletRepository.save(receiverWallet);
        } catch (Exception ex) {
            log.error("[TXN:{}] Credit operation failed, rolling back debit", tx.getTransactionId());
            progressor.failTransaction(tx.getTransactionId(), tx.getStatus().name(), "Credit operation failed: " + ex.getMessage());
            throw ex;
        }

        // COMPLETED
        tx = progressor.advance(tx);

        log.info("Bank top-up completed - TxnId: {}, Amount: {}", tx.getTransactionId(), amount);
        return tx;
    }

    @Override
    @Transactional
    public Transaction withdrawFromWallet(Long userId, String walletUpiHandle, String bankAccountNumber, BigDecimal amount, String rawWalletPin, String remarks){
        log.info("Initiating wallet withdrawal - UserId: {}, Wallet: {}, BankAccount: {}, Amount: {}", 
                 userId, walletUpiHandle, bankAccountNumber, amount);
        
        amount = validateAmount(amount);

        // 1. Fetch bank account
        BankAccount bankAccount = bankAccountRepository.findByAccountNumber(bankAccountNumber)
                                    .orElseThrow(() -> {
                                        log.warn("Bank account not found - accountNumber: {}", bankAccountNumber);
                                        return new ResourceNotFoundException("Bank account not found: " + bankAccountNumber);
                                    });
    
        // 2. Verify ownership of bank account
        if(!bankAccount.getUser().getId().equals(userId)){
            log.warn("Bank account ownership mismatch - accountNumber: {}, userId: {}", bankAccountNumber, userId);
            throw new IllegalArgumentException("Bank account does not belong to user");
        }

        // 3. Fetch wallet
        Wallet senderWallet = walletRepository.findByUpiHandle(walletUpiHandle)
                .orElseThrow(()-> {
                    log.warn("Sender wallet not found - upiHandle: {}", walletUpiHandle);
                    return new ResourceNotFoundException("Sender's wallet not found: " + walletUpiHandle);
                });
        
        // 4. Verify ownership of wallet
        if(!senderWallet.getUser().getId().equals(userId)){
            log.warn("Wallet ownership mismatch - upiHandle: {}, userId: {}", walletUpiHandle, userId);
            throw new IllegalArgumentException("Wallet does not belong to user");
        }

        Transaction tx = Transaction.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .fromWallet(senderWallet)
                    .toBankAccount(bankAccount)
                    .amount(amount)
                    .type(TransactionType.WALLET_WITHDRAW)
                    .status(TransactionStatus.CREATED)
                    .remarks(remarks)
                    .build();

        tx = progressor.createInitialTransaction(tx);

        // VALIDATING
        tx = progressor.advance(tx);

        // 5. Verify PIN
        if(!upiPinService.validatePin(walletUpiHandle, rawWalletPin)){
            log.warn("Invalid PIN for withdrawal - upiHandle: {}", walletUpiHandle);
            progressor.failTransaction(tx.getTransactionId(), tx.getStatus().name(), "Invalid UPI PIN");
            throw new InvalidPinException("Invalid UPI PIN");
        }

        // 6. Check Balance
        if(senderWallet.getBalance().compareTo(amount) < 0){
            log.warn("Insufficient wallet balance for withdrawal - upiHandle: {}, Required: {}, Available: {}", 
                     walletUpiHandle, amount, senderWallet.getBalance());
            progressor.failTransaction(tx.getTransactionId(), tx.getStatus().name(), "Insufficient wallet balance");
            throw new InsufficientFundsException("Insufficient wallet balance");
        }

        // PROCESSING
        tx = progressor.advance(tx);

        // DEBIT_PENDING
        tx = progressor.advance(tx);

        // 7. Deduct from wallet
        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        walletRepository.save(senderWallet);

        try{
        // CREDIT_PENDING
        tx = progressor.advance(tx);

        // 8. Credit to bank
        bankAccount.setBalance(bankAccount.getBalance().add(amount));
        bankAccountRepository.save(bankAccount);
        } catch (Exception ex) {
            log.error("[TXN:{}] Credit operation failed, rolling back debit", tx.getTransactionId());
            progressor.failTransaction(tx.getTransactionId(), tx.getStatus().name(), "Credit operation failed: " + ex.getMessage());
            throw ex;
        }

        // COMPLETED
        tx = progressor.advance(tx);
        
        log.info("Wallet withdrawal completed - TxnId: {}, Amount: {}", tx.getTransactionId(), amount);
        return tx;
    }

    @Override
    public Page<Transaction> getTransactionsByWalletUpi(String walletUpiHandle, Pageable pageable) {
        Wallet wallet = walletRepository.findByUpiHandle(walletUpiHandle)
                .orElseThrow(()->new ResourceNotFoundException("Wallet not found: "+walletUpiHandle));

        
        // Enforce a default sort if none provided
            Pageable effectivePageable = pageable.isPaged() && pageable.getSort().isSorted()
                    ? pageable
                    : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                            Sort.by(Sort.Order.desc("timestamp"), Sort.Order.desc("id")));
        
        Page<Transaction> allTransactions = transactionRepository.findByFromWalletOrToWallet(wallet, wallet, effectivePageable);
        return allTransactions;

    }
    
}

