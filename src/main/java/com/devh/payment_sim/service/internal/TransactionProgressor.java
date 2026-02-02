package com.devh.payment_sim.service.internal;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devh.payment_sim.model.BankAccount;
import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.model.TransactionStatus;
import com.devh.payment_sim.model.Wallet;
import com.devh.payment_sim.repository.BankAccountRepository;
import com.devh.payment_sim.repository.TransactionRepository;
import com.devh.payment_sim.repository.WalletRepository;
import com.devh.payment_sim.statemachine.TransactionStateMachine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProgressor {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    private final TransactionStateMachine stateMachine;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failTransaction(Transaction tx, String errorMessage) {
        log.error("[TXN:{}] Transaction failed at state: {} - Error: {}", tx.getTransactionId(), tx.getStatus(), errorMessage);
        tx.setStatus(TransactionStatus.FAILED);
        tx.setRemarks((tx.getRemarks() != null ? tx.getRemarks() + " | " : "") + "FAILED: " + errorMessage);
        transactionRepository.save(tx);
    }

    public void rollbackDebit(Wallet sender, BigDecimal amount) {
        log.warn("Rolling back wallet debit - reversing sender balance");
        sender.setBalance(sender.getBalance().add(amount));
        walletRepository.save(sender);
    }

    public void rollbackBankDebit(BankAccount bankAccount, BigDecimal amount) {
        log.warn("Rolling back bank debit - reversing sender balance");
        bankAccount.setBalance(bankAccount.getBalance().add(amount));
        bankAccountRepository.save(bankAccount);
    }

    public Transaction advance(Transaction tx) {
        tx.setStatus(stateMachine.next(tx));
        return transactionRepository.save(tx);
    }

}
