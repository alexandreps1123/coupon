package com.desafio.coupon.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponDto(
    UUID id,
    String code,
    String description,
    BigDecimal discountValue,
    LocalDateTime expirationDate,
    boolean published,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime deletedAt
) {
}
