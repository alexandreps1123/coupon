package com.desafio.coupon.application.exception;

public class InvalidExpirationDateException extends DomainException {
    public InvalidExpirationDateException(String message) {
        super(message);
    }
}
