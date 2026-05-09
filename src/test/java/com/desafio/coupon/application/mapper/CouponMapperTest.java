package com.desafio.coupon.application.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.desafio.coupon.adapter.persistence.entity.CouponEntity;
import com.desafio.coupon.application.dto.CouponDto;
import com.desafio.coupon.application.domain.Coupon;

class CouponMapperTest {
    private final CouponPersistenceMapper mapper = new CouponPersistenceMapper();

    @Test
    void shouldMapCouponToCouponEntity() {
        LocalDateTime expirationDate = LocalDateTime.parse("2026-06-15T12:00:00");
        LocalDateTime deletedAt = LocalDateTime.parse("2026-06-10T10:30:00");
        Coupon coupon = Coupon.builder()
            .code("ABC123")
            .description("Mapped coupon")
            .discountValue(new BigDecimal("10.50"))
            .expirationDate(expirationDate)
            .published(true)
            .deleted(true)
            .deletedAt(deletedAt)
            .build();

        CouponEntity entity = mapper.toEntity(coupon);

        assertNotNull(entity);
        assertEquals("ABC123", entity.getCode());
        assertEquals("Mapped coupon", entity.getDescription());
        assertEquals(new BigDecimal("10.50"), entity.getDiscountValue());
        assertEquals(expirationDate, entity.getExpirationDate());
        assertTrue(entity.isPublished());
        assertTrue(entity.isDeleted());
        assertEquals(deletedAt, entity.getDeletedAt());
        assertNull(entity.getId());
    }

    @Test
    void shouldMapCouponEntityToCouponDto() {
        UUID id = UUID.randomUUID();
        LocalDateTime expirationDate = LocalDateTime.parse("2026-07-20T09:00:00");
        LocalDateTime createdAt = LocalDateTime.parse("2026-05-01T08:00:00");
        LocalDateTime deletedAt = LocalDateTime.parse("2026-05-03T09:30:00");
        CouponEntity entity = new CouponEntity(
            id,
            "XYZ999",
            "Entity coupon",
            new BigDecimal("12.00"),
            expirationDate,
            false,
            true,
            createdAt,
            deletedAt
        );

        CouponDto coupon = mapper.toDto(entity);

        assertNotNull(coupon);
        assertEquals(id, coupon.id());
        assertEquals("XYZ999", coupon.code());
        assertEquals("Entity coupon", coupon.description());
        assertEquals(new BigDecimal("12.00"), coupon.discountValue());
        assertEquals(expirationDate, coupon.expirationDate());
        assertFalse(coupon.published());
        assertTrue(coupon.deleted());
        assertEquals(createdAt, coupon.createdAt());
        assertEquals(deletedAt, coupon.deletedAt());
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        assertNull(mapper.toEntity(null));
        assertNull(mapper.toDto(null));
        assertNull(mapper.toDomain(null));
    }

    @Test
    void shouldMapCouponEntityToDomain() {
        LocalDateTime expirationDate = LocalDateTime.parse("2026-07-20T09:00:00");
        LocalDateTime deletedAt = LocalDateTime.parse("2026-07-10T12:15:00");
        CouponEntity entity = new CouponEntity(
            UUID.randomUUID(),
            "XYZ999",
            "Entity coupon",
            new BigDecimal("12.00"),
            expirationDate,
            false,
            true,
            LocalDateTime.parse("2026-05-01T08:00:00"),
            deletedAt
        );

        Coupon coupon = mapper.toDomain(entity);

        assertNotNull(coupon);
        assertEquals("XYZ999", coupon.getCode());
        assertEquals("Entity coupon", coupon.getDescription());
        assertEquals(new BigDecimal("12.00"), coupon.getDiscountValue());
        assertEquals(expirationDate, coupon.getExpirationDate());
        assertFalse(coupon.isPublished());
        assertTrue(coupon.isDeleted());
        assertEquals(deletedAt, coupon.getDeletedAt());
    }
}
