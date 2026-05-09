package com.desafio.coupon.application.exception;

public class InvalidCouponDescriptionException extends DomainException {
    public InvalidCouponDescriptionException(String message) {
        super(message);
    }
}
