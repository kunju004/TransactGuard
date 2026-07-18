package com.transactguard.transaction.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "A tokenized payment authorization request.")
public record AuthorizeTransactionRequest(
        @Schema(example = "acct_market_001", description = "Platform account to debit when the transaction is approved.")
        @NotBlank
        @Size(max = 64)
        @Pattern(regexp = "[A-Za-z0-9_-]{3,64}", message = "must contain only letters, numbers, underscores, or hyphens")
        String accountId,

        @Schema(example = "tok_urbanRoast0001", description = "Tokenized card reference. Raw card numbers are intentionally rejected.")
        @NotBlank
        @Size(max = 128)
        @Pattern(regexp = "tok_[A-Za-z0-9_-]{8,120}", message = "must be a tokenized card value and must not be a PAN")
        String cardToken,

        @Schema(example = "merchant_urban_roast")
        @NotBlank
        @Size(max = 64)
        @Pattern(regexp = "[A-Za-z0-9_-]{3,64}", message = "must contain only letters, numbers, underscores, or hyphens")
        String merchantId,

        @Schema(example = "5812", description = "ISO 18245 merchant category code.")
        @NotBlank
        @Pattern(regexp = "\\d{4}", message = "must be a four digit merchant category code")
        String merchantCategoryCode,

        @Schema(example = "US", description = "Two-letter merchant country code.")
        @NotBlank
        @Pattern(regexp = "[A-Z]{2}", message = "must be an ISO 3166-1 alpha-2 country code")
        String merchantCountry,

        @Schema(example = "USD", description = "Three-letter transaction currency.")
        @NotBlank
        @Pattern(regexp = "[A-Z]{3}", message = "must be an ISO 4217 currency code")
        String currency,

        @Schema(example = "42.25")
        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 12, fraction = 2)
        BigDecimal amount,

        @Schema(example = "idem-demo-0001", description = "Client-generated replay protection key.")
        @NotBlank
        @Size(min = 8, max = 80)
        @Pattern(regexp = "[A-Za-z0-9._:-]{8,80}", message = "contains unsupported characters")
        String idempotencyKey
) {
}
