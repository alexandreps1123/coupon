package com.desafio.coupon.application.exception;

public class CouponAlreadyDeletedException extends DomainException {
    
    public CouponAlreadyDeletedException(String message) {
        super(message);
    }
}
