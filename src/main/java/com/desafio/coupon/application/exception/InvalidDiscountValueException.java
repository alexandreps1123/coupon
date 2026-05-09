package com.desafio.coupon.application.exception;

public class InvalidDiscountValueException extends DomainException {
    public InvalidDiscountValueException(String message) {
        super(message);
    }
}
