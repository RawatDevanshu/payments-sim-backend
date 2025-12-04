package com.devh.payment_sim.exception;

public class PinNotSetException extends RuntimeException {
    public PinNotSetException(String message){
        super(message);
    }
}
