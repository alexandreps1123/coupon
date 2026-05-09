package com.desafio.coupon.application.service;

import com.desafio.coupon.adapter.persistence.repository.CouponRepository;
import com.desafio.coupon.application.dto.CouponDto;
import com.desafio.coupon.application.exception.CouponCodeAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CreateCouponServiceTest {

    @Autowired
    private CreateCouponService createCouponService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void shouldCreateCouponAndPersistNormalizedCode() {
        String rawCode = ("A" + UUID.randomUUID().toString().replace("-", "")).substring(0, 6);
        CouponDto response = createCouponService.execute(
            rawCode,
            "Coupon description",
            new BigDecimal("10.50"),
            LocalDateTime.now().plusDays(10),
            true
        );

        assertNotNull(response.id());
        assertEquals(rawCode.toUpperCase(), response.code());
        assertEquals("Coupon description", response.description());
        assertNotNull(couponRepository.findById(response.id()).orElseThrow());
    }

    @Test
    void shouldThrowConflictWhenCodeAlreadyExists() {
        String baseCode = ("B" + UUID.randomUUID().toString().replace("-", "")).substring(0, 6);
        createCouponService.execute(
            baseCode,
            "First coupon",
            new BigDecimal("10.50"),
            LocalDateTime.now().plusDays(10),
            false
        );

        assertThrows(
            CouponCodeAlreadyExistsException.class,
            () -> createCouponService.execute(
                baseCode,
                "Second coupon",
                new BigDecimal("20.00"),
                LocalDateTime.now().plusDays(10),
                true
            )
        );
    }

    @Test
    void shouldReturnSameCouponWhenCreateRequestIsIdempotent() {
        String baseCode = ("I" + UUID.randomUUID().toString().replace("-", "")).substring(0, 6);
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(10);

        CouponDto first = createCouponService.execute(
            baseCode,
            "Same coupon",
            new BigDecimal("10.50"),
            expirationDate,
            false
        );

        CouponDto second = createCouponService.execute(
            baseCode,
            "Same coupon",
            new BigDecimal("10.50"),
            expirationDate,
            false
        );

        assertEquals(first.id(), second.id());
        assertEquals(first.code(), second.code());
    }
}
