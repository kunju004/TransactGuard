package com.transactguard.transaction.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Standard API error response.")
public record ErrorResponse(
        Instant timestamp,
        @Schema(example = "400")
        int status,
        @Schema(example = "Bad Request")
        String error,
        @Schema(example = "Request validation failed")
        String message,
        @Schema(example = "demo-request-001")
        String correlationId,
        Map<String, String> fieldErrors
) {
}
