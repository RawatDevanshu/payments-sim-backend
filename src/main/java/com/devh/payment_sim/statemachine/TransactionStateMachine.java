package com.devh.payment_sim.statemachine;

import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.model.TransactionStatus;

import static com.devh.payment_sim.model.TransactionStatus.*;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TransactionStateMachine {
    
    public TransactionStatus next(Transaction tx){
        TransactionStatus current = tx.getStatus();

        switch(current){

            case CREATED:
                log.info("[TX:{}] CREATED -> VALIDATING", tx.getId());
                return VALIDATING;

            case VALIDATING:
                log.info("[TX:{}] VALIDATING -> PROCESSING", tx.getId());
                return PROCESSING;

            case PROCESSING:
                log.info("[TX:{}] PROCESSING -> DEBIT_PENDING", tx.getId());
                return DEBIT_PENDING;

            case DEBIT_PENDING:
                log.info("[TX:{}] DEBIT_PENDING -> CREDIT_PENDING", tx.getId());
                return CREDIT_PENDING;
            
            case CREDIT_PENDING:
                log.info("[TX:{}] CREDIT_PENDING -> COMPLETED", tx.getId());
                return COMPLETED;
            
            // Terminal States
            case COMPLETED, FAILED, CANCELLED:
                return current;

            default:
                log.error("[TX:{}] Unknown State: {}", tx.getId(), tx.getStatus());
                return FAILED;

        }
    }
}
