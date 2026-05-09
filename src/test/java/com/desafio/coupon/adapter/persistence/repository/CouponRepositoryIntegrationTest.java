package com.desafio.coupon.adapter.persistence.repository;

import com.desafio.coupon.adapter.persistence.entity.CouponEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CouponRepositoryIntegrationTest {

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void shouldSaveAndFindById() {
        CouponEntity coupon = new CouponEntity(
            "REPO01",
            "Repository save/findById",
            new BigDecimal("15.00"),
            LocalDateTime.now().plusDays(10),
            true
        );

        CouponEntity saved = couponRepository.save(coupon);
        Optional<CouponEntity> found = couponRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("REPO01", found.orElseThrow().getCode());
    }

    @Test
    @Sql(scripts = "/sql/coupons-seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldFindByCode() {
        Optional<CouponEntity> found = couponRepository.findByCode("ACT001");

        assertTrue(found.isPresent());
        assertEquals("Active coupon seed", found.orElseThrow().getDescription());
    }

    @Test
    @Sql(scripts = "/sql/coupons-seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldCheckIfCodeExists() {
        assertTrue(couponRepository.existsByCode("ACT001"));
        assertFalse(couponRepository.existsByCode("MISS01"));
    }

    @Test
    @Sql(scripts = "/sql/coupons-seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldReturnOnlyActiveCoupons() {
        List<CouponEntity> activeCoupons = couponRepository.findAllActive();

        assertEquals(1, activeCoupons.size());
        assertEquals("ACT001", activeCoupons.getFirst().getCode());
        assertFalse(activeCoupons.getFirst().isDeleted());
    }

    @Test
    @Sql(scripts = "/sql/coupons-seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldReturnAllCoupons() {
        List<CouponEntity> coupons = couponRepository.findAll();

        assertEquals(2, coupons.size());
        assertTrue(coupons.stream().anyMatch(coupon -> coupon.getCode().equals("ACT001")));
        assertTrue(coupons.stream().anyMatch(coupon -> coupon.getCode().equals("DEL001")));
    }

    @Test
    @Sql(scripts = "/sql/coupons-seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldReturnEmptyWhenCodeIsNotFound() {
        Optional<CouponEntity> found = couponRepository.findByCode("NONE00");

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenCodeDoesNotExist() {
        assertFalse(couponRepository.existsByCode("EMPTY1"));
    }

    @Test
    void shouldReturnEmptyWhenIdDoesNotExist() {
        Optional<CouponEntity> found = couponRepository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }
}
