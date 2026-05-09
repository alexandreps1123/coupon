package com.desafio.coupon.application.exception;

public class CouponCodeAlreadyExistsException extends DomainException {

    public CouponCodeAlreadyExistsException(String message) {
        super(message);
    }

    public CouponCodeAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
