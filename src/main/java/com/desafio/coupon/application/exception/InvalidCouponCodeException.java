package com.desafio.coupon.application.exception;

public class InvalidCouponCodeException extends DomainException {
    public InvalidCouponCodeException(String message) {
        super(message);
    }
}
