package com.desafio.coupon.adapter.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Error response")
public record ErrorResponse(
    
    @Schema(description = "HTTP status code", example = "400")
    int status,
    
    @Schema(description = "Error message", example = "Invalid coupon code")
    String message,
    
    @Schema(description = "Detailed error messages (for validation errors)")
    List<String> errors,
    
    @Schema(description = "Timestamp of the error", example = "2024-01-01T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp
) {
    
    public ErrorResponse(int status, String message, Clock clock) {
        this(status, message, null, LocalDateTime.now(clock));
    }
    
    public ErrorResponse(int status, String message, List<String> errors, Clock clock) {
        this(status, message, errors, LocalDateTime.now(clock));
    }
}
