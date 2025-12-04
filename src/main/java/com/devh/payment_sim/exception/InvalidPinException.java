package com.devh.payment_sim.exception;

public class InvalidPinException extends RuntimeException {
    public InvalidPinException(String message){
        super(message);
    }
}
