package com.desafio.coupon.adapter.rest.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for CouponController.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CouponControllerIntegrationTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    void shouldCreateCoupon() throws Exception {
        HttpResponse<JsonNode> response = postCoupon(
            "TST001",
            "Test coupon",
            new BigDecimal("10.50"),
            LocalDateTime.now().plusDays(30),
            false
        );

        assertEquals(HttpStatus.CREATED.value(), response.statusCode());
        JsonNode body = response.body();
        assertEquals("TST001", body.get("code").asText());
        assertEquals("Test coupon", body.get("description").asText());
        assertEquals(0, new BigDecimal("10.50").compareTo(body.get("discountValue").decimalValue()));
        assertFalse(body.get("published").asBoolean());
        assertFalse(body.get("deleted").asBoolean());
    }

    @Test
    void shouldCreatePublishedCoupon() throws Exception {
        HttpResponse<JsonNode> response = postCoupon(
            "PUB001",
            "Published coupon",
            new BigDecimal("15.00"),
            LocalDateTime.now().plusDays(30),
            true
        );

        assertEquals(HttpStatus.CREATED.value(), response.statusCode());
        assertTrue(response.body().get("published").asBoolean());
    }

    @Test
    void shouldNormalizeCodeWhenCreating() throws Exception {
        HttpResponse<JsonNode> response = postCoupon(
            "no-rm01",
            "Test coupon",
            new BigDecimal("10.50"),
            LocalDateTime.now().plusDays(30),
            false
        );

        assertEquals(HttpStatus.CREATED.value(), response.statusCode());
        assertEquals("NORM01", response.body().get("code").asText());
    }

    @Test
    void shouldReturnConflictWhenCouponCodeAlreadyExists() throws Exception {
        postCoupon(
            "du-p001",
            "First coupon",
            new BigDecimal("10.50"),
            LocalDateTime.now().plusDays(30),
            false
        );

        HttpResponse<JsonNode> response = postCoupon(
            "DUP001",
            "Second coupon",
            new BigDecimal("20.00"),
            LocalDateTime.now().plusDays(30),
            true
        );

        assertEquals(HttpStatus.CONFLICT.value(), response.statusCode());
        assertTrue(response.body().get("message").asText().contains("already exists"));
    }

    @Test
    void shouldReturnBadRequestWhenCodeIsInvalid() throws Exception {
        HttpResponse<JsonNode> response = postCoupon(
            "SHORT",
            "Test coupon",
            new BigDecimal("10.50"),
            LocalDateTime.now().plusDays(30),
            false
        );

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
        assertTrue(response.body().get("message").asText().contains("6 alphanumeric characters"));
    }

    @Test
    void shouldReturnBadRequestWhenDiscountValueIsTooLow() throws Exception {
        HttpResponse<JsonNode> response = postCoupon(
            "LOW001",
            "Test coupon",
            new BigDecimal("0.49"),
            LocalDateTime.now().plusDays(30),
            false
        );

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
        assertTrue(response.body().get("message").asText().contains("at least 0.5"));
    }

    @Test
    void shouldReturnBadRequestWhenExpirationDateIsInPast() throws Exception {
        HttpResponse<JsonNode> response = postCoupon(
            "PAST01",
            "Test coupon",
            new BigDecimal("10.50"),
            LocalDateTime.now().minusDays(1),
            false
        );

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
        assertTrue(response.body().get("message").asText().contains("past"));
    }

    @Test
    void shouldReturnBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        HttpResponse<JsonNode> response = postJson("""
            {
              "description": "",
              "discountValue": null,
              "expirationDate": null
            }
            """);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
        assertEquals("Validation failed", response.body().get("message").asText());
        assertTrue(response.body().get("errors").size() >= 3);
    }

    @Test
    void shouldGetCouponByCode() throws Exception {
        HttpResponse<JsonNode> createResponse = postCoupon(
            "GET001",
            "Get test coupon",
            new BigDecimal("20.00"),
            LocalDateTime.now().plusDays(30),
            false
        );

        JsonNode createdCoupon = createResponse.body();
        HttpResponse<JsonNode> getResponse = get("/api/coupons/" + createdCoupon.get("code").asText());

        assertEquals(HttpStatus.OK.value(), getResponse.statusCode());
        assertEquals(createdCoupon.get("id").asText(), getResponse.body().get("id").asText());
        assertEquals("GET001", getResponse.body().get("code").asText());
    }

    @Test
    void shouldReturnNotFoundWhenCouponDoesNotExist() throws Exception {
        HttpResponse<JsonNode> response = get("/api/coupons/XXXXXX");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode());
        assertTrue(response.body().get("message").asText().contains("not found"));
    }

    @Test
    void shouldListOnlyActiveCouponsByDefault() throws Exception {
        HttpResponse<JsonNode> firstCreateResponse = postCoupon(
            "LSA001",
            "Active list coupon",
            new BigDecimal("15.00"),
            LocalDateTime.now().plusDays(30),
            false
        );
        HttpResponse<JsonNode> secondCreateResponse = postCoupon(
            "LSD001",
            "Deleted list coupon",
            new BigDecimal("15.00"),
            LocalDateTime.now().plusDays(30),
            false
        );

        delete("/api/coupons/" + secondCreateResponse.body().get("code").asText());
        HttpResponse<JsonNode> listResponse = get("/api/coupons");

        assertEquals(HttpStatus.OK.value(), listResponse.statusCode());
        assertTrue(containsCouponCode(listResponse.body(), firstCreateResponse.body().get("code").asText()));
        assertFalse(containsCouponCode(listResponse.body(), secondCreateResponse.body().get("code").asText()));
    }

    @Test
    void shouldListDeletedCouponsWhenRequested() throws Exception {
        HttpResponse<JsonNode> createResponse = postCoupon(
            "LIA001",
            "Include deleted coupon",
            new BigDecimal("15.00"),
            LocalDateTime.now().plusDays(30),
            false
        );

        delete("/api/coupons/" + createResponse.body().get("code").asText());
        HttpResponse<JsonNode> listResponse = get("/api/coupons?includeDeleted=true");

        assertEquals(HttpStatus.OK.value(), listResponse.statusCode());
        assertTrue(containsCouponCode(listResponse.body(), createResponse.body().get("code").asText()));
    }

    @Test
    void shouldDeleteCoupon() throws Exception {
        HttpResponse<JsonNode> createResponse = postCoupon(
            "DEL001",
            "Delete test coupon",
            new BigDecimal("15.00"),
            LocalDateTime.now().plusDays(30),
            false
        );

        String couponCode = createResponse.body().get("code").asText();
        HttpResponse<JsonNode> deleteResponse = delete("/api/coupons/" + couponCode);
        HttpResponse<JsonNode> getResponse = get("/api/coupons/" + couponCode);

        assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse.statusCode());
        assertEquals(HttpStatus.OK.value(), getResponse.statusCode());
        assertTrue(getResponse.body().get("deleted").asBoolean());
    }

    @Test
    void shouldReturnConflictWhenDeletingAlreadyDeletedCoupon() throws Exception {
        HttpResponse<JsonNode> createResponse = postCoupon(
            "DUP001",
            "Duplicate delete test",
            new BigDecimal("15.00"),
            LocalDateTime.now().plusDays(30),
            false
        );

        String couponCode = createResponse.body().get("code").asText();
        delete("/api/coupons/" + couponCode);
        HttpResponse<JsonNode> deleteResponse = delete("/api/coupons/" + couponCode);

        assertEquals(HttpStatus.CONFLICT.value(), deleteResponse.statusCode());
        assertTrue(deleteResponse.body().get("message").asText().contains("already deleted"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownCoupon() throws Exception {
        HttpResponse<JsonNode> response = delete("/api/coupons/XXXXXX");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode());
        assertTrue(response.body().get("message").asText().contains("not found"));
    }

    @Test
    void shouldHandleConcurrentCouponCreation() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            CompletableFuture.runAsync(() -> {
                try {
                    HttpResponse<JsonNode> response = postCoupon("CON001", "Concurrent coupon 1", new BigDecimal("10.00"), LocalDateTime.now().plusDays(30), false);
                    assertEquals(HttpStatus.CREATED.value(), response.statusCode());
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, executor),
            CompletableFuture.runAsync(() -> {
                try {
                    HttpResponse<JsonNode> response = postCoupon("CON002", "Concurrent coupon 2", new BigDecimal("10.00"), LocalDateTime.now().plusDays(30), false);
                    assertEquals(HttpStatus.CREATED.value(), response.statusCode());
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, executor),
            CompletableFuture.runAsync(() -> {
                try {
                    HttpResponse<JsonNode> response = postCoupon("CON003", "Concurrent coupon 3", new BigDecimal("10.00"), LocalDateTime.now().plusDays(30), false);
                    assertEquals(HttpStatus.CREATED.value(), response.statusCode());
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, executor)
        );
        allFutures.join();
        executor.shutdown();
    }

    @Test
    void shouldReturnResponseWithCorrectContract() throws Exception {
        HttpResponse<JsonNode> response = postCoupon(
            "CTR001",
            "Contract test coupon",
            new BigDecimal("10.50"),
            LocalDateTime.now().plusDays(30),
            true
        );

        assertEquals(HttpStatus.CREATED.value(), response.statusCode());
        JsonNode body = response.body();
        assertTrue(body.has("id"));
        assertTrue(body.has("code"));
        assertTrue(body.has("description"));
        assertTrue(body.has("discountValue"));
        assertTrue(body.has("expirationDate"));
        assertTrue(body.has("published"));
        assertTrue(body.has("deleted"));
        assertTrue(body.has("deletedAt"));
        assertEquals("CTR001", body.get("code").asText());
        assertEquals("Contract test coupon", body.get("description").asText());
        assertEquals(0, new BigDecimal("10.50").compareTo(body.get("discountValue").decimalValue()));
        assertTrue(body.get("published").asBoolean());
        assertFalse(body.get("deleted").asBoolean());
        assertTrue(body.get("deletedAt").isNull());
    }

    private HttpResponse<JsonNode> postCoupon(String code, String description, BigDecimal discountValue,
                                              LocalDateTime expirationDate, boolean published)
            throws IOException, InterruptedException {
        String json = """
            {
              "code": "%s",
              "description": "%s",
              "discountValue": %s,
              "expirationDate": "%s",
              "published": %s
            }
            """.formatted(
                code,
                description,
                discountValue,
                expirationDate.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                published
            );

        return postJson(json);
    }

    private HttpResponse<JsonNode> postJson(String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri("/api/coupons"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        return send(request);
    }

    private HttpResponse<JsonNode> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri(path)).GET().build();
        return send(request);
    }

    private HttpResponse<JsonNode> delete(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri(path)).DELETE().build();
        return send(request);
    }

    private HttpResponse<JsonNode> send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return new JsonHttpResponse(response, parseBody(response.body()));
    }

    private JsonNode parseBody(String body) throws IOException {
        if (body == null || body.isBlank()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(body);
    }

    private URI uri(String path) {
        return URI.create("http://localhost:%d%s".formatted(port, path));
    }

    private boolean containsCouponCode(JsonNode coupons, String code) {
        for (JsonNode coupon : coupons) {
            if (code.equals(coupon.get("code").asText())) {
                return true;
            }
        }
        return false;
    }

    private record JsonHttpResponse(HttpResponse<String> delegate, JsonNode body) implements HttpResponse<JsonNode> {

        @Override
        public int statusCode() {
            return delegate.statusCode();
        }

        @Override
        public HttpRequest request() {
            return delegate.request();
        }

        @Override
        public Optional<HttpResponse<JsonNode>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return delegate.headers();
        }

        @Override
        public JsonNode body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return delegate.sslSession();
        }

        @Override
        public URI uri() {
            return delegate.uri();
        }

        @Override
        public HttpClient.Version version() {
            return delegate.version();
        }
    }
}
