package com.desafio.coupon.application.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.desafio.coupon.adapter.persistence.entity.CouponEntity;
import com.desafio.coupon.adapter.persistence.repository.CouponRepository;
import com.desafio.coupon.application.dto.CouponDto;
import com.desafio.coupon.application.exception.CouponAlreadyDeletedException;

@SpringBootTest
@ActiveProfiles("test")
class CouponConcurrencyServiceTest {

    @Autowired
    private CreateCouponService createCouponService;

    @Autowired
    private DeleteCouponService deleteCouponService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void shouldAllowOnlyOneCreateWhenRequestsAreConcurrent() throws Exception {
        String code = ("C" + UUID.randomUUID().toString().replace("-", "")).substring(0, 6);
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(5);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        Callable<Object> task = () -> {
            await(start);
            try {
                return createCouponService.execute(
                    code,
                    "Concurrent create",
                    new BigDecimal("10.00"),
                    expirationDate,
                    false
                );
            } catch (Throwable t) {
                return t;
            }
        };

        Future<Object> f1 = pool.submit(task);
        Future<Object> f2 = pool.submit(task);
        start.countDown();

        List<Object> outcomes = List.of(f1.get(), f2.get());
        pool.shutdown();

        List<CouponDto> results = outcomes.stream()
            .filter(CouponDto.class::isInstance)
            .map(CouponDto.class::cast)
            .toList();
        List<Throwable> failures = outcomes.stream()
            .filter(Throwable.class::isInstance)
            .map(Throwable.class::cast)
            .toList();

        assertTrue(results.size() >= 1, "At least one concurrent create should succeed");
        assertTrue(failures.size() <= 1, "At most one concurrent create should fail");

        if (results.size() == 2) {
            assertTrue(results.get(0).id().equals(results.get(1).id()),
                "When both concurrent idempotent creates succeed, they must return the same coupon");
        }
    }

    @Test
    void shouldKeepSoftDeleteStateConsistentUnderConcurrentDelete() throws Exception {
        String code = ("D" + UUID.randomUUID().toString().replace("-", "")).substring(0, 6);
        CouponDto created = createCouponService.execute(
            code,
            "Concurrent delete",
            new BigDecimal("10.00"),
            LocalDateTime.now().plusDays(5),
            false
        );

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        Callable<Throwable> task = () -> {
            await(start);
            try {
                deleteCouponService.execute(created.code());
                return null;
            } catch (Throwable t) {
                return t;
            }
        };

        Future<Throwable> f1 = pool.submit(task);
        Future<Throwable> f2 = pool.submit(task);
        start.countDown();

        List<Throwable> outcomes = new ArrayList<>();
        outcomes.add(f1.get());
        outcomes.add(f2.get());
        pool.shutdown();

        CouponEntity saved = couponRepository.findByCode(created.code()).orElseThrow();
        assertTrue(saved.isDeleted(), "Coupon must be soft-deleted");
        assertTrue(saved.getDeletedAt() != null, "deletedAt must be filled");

        long knownFailures = outcomes.stream()
            .filter(t -> t != null)
            .map(this::rootCause)
            .filter(t -> t instanceof CouponAlreadyDeletedException)
            .count();

        assertTrue(knownFailures >= 0 && knownFailures <= 1,
            "Concurrent delete may produce at most one already-deleted conflict");
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
