package com.shoestore.backend.exceptation;

public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}
