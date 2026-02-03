package com.devh.payment_sim.service.internal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.model.TransactionStatus;
import com.devh.payment_sim.repository.TransactionRepository;
import com.devh.payment_sim.statemachine.TransactionStateMachine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProgressor {

    private final TransactionRepository transactionRepository;

    private final TransactionStateMachine stateMachine;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction createInitialTransaction(Transaction tx) {
        return transactionRepository.save(tx);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failTransaction(String transactionId, String failureState, String errorMessage) {
        log.error("[TXN:{}] Transaction failed at state: {} - Error: {}", transactionId, failureState, errorMessage);

        Transaction tx = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalStateException("Transaction not found in failTransaction()"));
                
        tx.setStatus(TransactionStatus.FAILED);
        tx.setRemarks((tx.getRemarks() != null ? tx.getRemarks() + " | " : "") + errorMessage);
        transactionRepository.save(tx);
    }

    public Transaction advance(Transaction tx) {
        tx.setStatus(stateMachine.next(tx));
        return transactionRepository.save(tx);
    }

}
