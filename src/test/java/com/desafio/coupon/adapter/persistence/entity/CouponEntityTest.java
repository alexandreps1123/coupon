package com.desafio.coupon.adapter.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CouponEntityTest {

    @Test
    void shouldCreateEntityUsingBasicConstructor() {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(2);
        CouponEntity entity = new CouponEntity(
            "ABC123",
            "Coupon",
            new BigDecimal("10.00"),
            expirationDate,
            true
        );

        assertEquals("ABC123", entity.getCode());
        assertEquals("Coupon", entity.getDescription());
        assertEquals(new BigDecimal("10.00"), entity.getDiscountValue());
        assertEquals(expirationDate, entity.getExpirationDate());
        assertEquals(true, entity.isPublished());
        assertFalse(entity.isDeleted());
        assertNull(entity.getDeletedAt());
    }

    @Test
    void shouldCreateEntityUsingFullConstructor() {
        UUID id = UUID.randomUUID();
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(2);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime deletedAt = LocalDateTime.now().plusHours(1);

        CouponEntity entity = new CouponEntity(
            id,
            "XYZ999",
            "Deleted coupon",
            new BigDecimal("20.00"),
            expirationDate,
            false,
            true,
            createdAt,
            deletedAt
        );

        assertEquals(id, entity.getId());
        assertEquals("XYZ999", entity.getCode());
        assertEquals("Deleted coupon", entity.getDescription());
        assertEquals(new BigDecimal("20.00"), entity.getDiscountValue());
        assertEquals(expirationDate, entity.getExpirationDate());
        assertFalse(entity.isPublished());
        assertEquals(true, entity.isDeleted());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(deletedAt, entity.getDeletedAt());
    }
}
