package com.desafio.coupon.application.exception;

public class CouponNotFoundException extends DomainException {
    
    public CouponNotFoundException(String message) {
        super(message);
    }
}
