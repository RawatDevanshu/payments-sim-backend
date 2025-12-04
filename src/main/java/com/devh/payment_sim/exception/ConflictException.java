package com.devh.payment_sim.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message){
        super(message);
    }
}
