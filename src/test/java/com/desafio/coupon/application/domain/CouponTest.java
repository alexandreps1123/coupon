package com.desafio.coupon.application.domain;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.desafio.coupon.application.exception.InvalidCouponCodeException;
import com.desafio.coupon.application.exception.InvalidCouponDescriptionException;
import com.desafio.coupon.application.exception.InvalidDiscountValueException;
import com.desafio.coupon.application.exception.InvalidExpirationDateException;
import com.desafio.coupon.application.exception.CouponAlreadyDeletedException;

class CouponTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2026-01-01T10:00:00Z"),
        ZoneId.of("America/Sao_Paulo")
    );

    private static LocalDateTime now() {
        return LocalDateTime.now(FIXED_CLOCK);
    }

    @Test
    void shouldCreateCouponWithNormalizedCode() {
        Coupon coupon = Coupon.create(
            "ab-c123",
            "Coupon description",
            new BigDecimal("10.50"),
            now().plusDays(1),
                false,
                FIXED_CLOCK
            );

        assertEquals("ABC123", coupon.getCode());
    }

    @Test
    void shouldThrowWhenCodeIsNull() {
        assertThrows(
            InvalidCouponCodeException.class,
            () -> Coupon.create(
                null,
                "Coupon description",
                new BigDecimal("10.50"),
                now().plusDays(1),
                false,
                FIXED_CLOCK
            )
        );
    }

    @Test
    void shouldThrowWhenCodeIsBlank() {
        assertThrows(
            InvalidCouponCodeException.class,
            () -> Coupon.create(
                "   ",
                "Coupon description",
                new BigDecimal("10.50"),
                now().plusDays(1),
                false,
                FIXED_CLOCK
            )
        );
    }

    @Test
    void shouldThrowWhenCodeLengthIsInvalidAfterNormalization() {
        assertThrows(
            InvalidCouponCodeException.class,
            () -> Coupon.create(
                "A-B1",
                "Coupon description",
                new BigDecimal("10.50"),
                now().plusDays(1),
                false,
                FIXED_CLOCK
            )
        );
    }

    @Test
    void shouldThrowWhenDescriptionIsBlank() {
        assertThrows(
            InvalidCouponDescriptionException.class,
            () -> Coupon.create(
                "ABC123",
                "   ",
                new BigDecimal("10.50"),
                now().plusDays(1),
                false,
                FIXED_CLOCK
            )
        );
    }

    @Test
    void shouldThrowWhenDescriptionIsNull() {
        assertThrows(
            InvalidCouponDescriptionException.class,
            () -> Coupon.create(
                "ABC123",
                null,
                new BigDecimal("10.50"),
                now().plusDays(1),
                false,
                FIXED_CLOCK
            )
        );
    }

    @Test
    void shouldThrowWhenDescriptionExceedsLimit() {
        assertThrows(
            InvalidCouponDescriptionException.class,
            () -> Coupon.create(
                "ABC123",
                "a".repeat(501),
                new BigDecimal("10.50"),
                now().plusDays(1),
                false,
                FIXED_CLOCK
            )
        );
    }

    @Test
    void shouldThrowWhenDiscountValueIsNull() {
        assertThrows(
            InvalidDiscountValueException.class,
            () -> Coupon.create(
                "ABC123",
                "Coupon description",
                null,
                now().plusDays(1),
                false,
                FIXED_CLOCK
            )
        );
    }

    @Test
    void shouldThrowWhenDiscountValueIsBelowMinimum() {
        assertThrows(
            InvalidDiscountValueException.class,
            () -> Coupon.create(
                "ABC123",
                "Coupon description",
                new BigDecimal("0.49"),
                now().plusDays(1),
                false,
                FIXED_CLOCK
            )
        );
    }

    @Test
    void shouldThrowWhenExpirationDateIsNull() {
        assertThrows(
            InvalidExpirationDateException.class,
            () -> Coupon.create(
                "ABC123",
                "Coupon description",
                new BigDecimal("10.50"),
                null,
                false,
                FIXED_CLOCK
            )
        );
    }

    @Test
    void shouldThrowWhenExpirationDateIsInPast() {
        assertThrows(
            InvalidExpirationDateException.class,
            () -> Coupon.create(
                "ABC123",
                "Coupon description",
                new BigDecimal("10.50"),
                now().minusSeconds(1),
                false,
                FIXED_CLOCK
            )
        );
    }

    @Test
    void shouldThrowWhenClockIsNull() {
        assertThrows(
            InvalidExpirationDateException.class,
            () -> Coupon.create(
                "ABC123",
                "Coupon description",
                new BigDecimal("10.50"),
                now().plusDays(1),
                false,
                null
            )
        );
    }

    @Test
    void shouldAcceptExpirationDateEqualToNow() {
        Coupon coupon = Coupon.create(
            "ABC123",
            "Coupon description",
            new BigDecimal("10.50"),
            now(),
            true,
            FIXED_CLOCK
        );

        assertEquals("ABC123", coupon.getCode());
        assertTrue(coupon.isPublished());
    }

    @Test
    void shouldCreateUnpublishedCoupon() {
        Coupon coupon = Coupon.create(
            "ABC123",
            "Coupon description",
            new BigDecimal("10.50"),
            now().plusDays(2),
            false,
            FIXED_CLOCK
        );

        assertFalse(coupon.isPublished());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Coupon left = Coupon.create(
            "ABC123",
            "Coupon description",
            new BigDecimal("10.50"),
            now().plusDays(2),
            true,
            FIXED_CLOCK
        );
        Coupon right = Coupon.builder()
            .code("ABC123")
            .description("Coupon description")
            .discountValue(new BigDecimal("10.50"))
            .expirationDate(now().plusDays(2))
            .published(true)
            .build();
        Coupon different = Coupon.builder()
            .code("XYZ999")
            .description("Different coupon")
            .discountValue(new BigDecimal("11.00"))
            .expirationDate(now().plusDays(3))
            .published(false)
            .build();

        assertEquals(left, left);
        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertFalse(left.equals(null));
        assertFalse(left.equals("not-coupon"));
        assertFalse(left.equals(different));
    }

    @Test
    void shouldBuildCouponWithBuilder() {
        Coupon coupon = Coupon.builder()
            .code("ABC123")
            .description("Builder coupon")
            .discountValue(new BigDecimal("20.00"))
            .expirationDate(now().plusDays(5))
            .published(true)
            .deleted(false)
            .deletedAt(null)
            .build();

        assertEquals("ABC123", coupon.getCode());
        assertEquals("Builder coupon", coupon.getDescription());
        assertEquals(new BigDecimal("20.00"), coupon.getDiscountValue());
        assertEquals(now().plusDays(5), coupon.getExpirationDate());
        assertTrue(coupon.isPublished());
        assertFalse(coupon.isDeleted());
        assertEquals(null, coupon.getDeletedAt());
    }

    @Test
    void shouldDeleteCouponInDomain() {
        Coupon coupon = Coupon.create(
            "ABC123",
            "Coupon description",
            new BigDecimal("10.50"),
            now().plusDays(2),
            true,
            FIXED_CLOCK
        );

        Coupon deletedCoupon = coupon.delete(FIXED_CLOCK);

        assertTrue(deletedCoupon.isDeleted());
        assertNotNull(deletedCoupon.getDeletedAt());
    }

    @Test
    void shouldThrowWhenDeletingAlreadyDeletedCouponInDomain() {
        Coupon deletedCoupon = Coupon.builder()
            .code("ABC123")
            .description("Coupon description")
            .discountValue(new BigDecimal("10.50"))
            .expirationDate(now().plusDays(2))
            .published(true)
            .deleted(true)
            .deletedAt(now())
            .build();

        assertThrows(CouponAlreadyDeletedException.class, () -> deletedCoupon.delete(FIXED_CLOCK));
    }

}
