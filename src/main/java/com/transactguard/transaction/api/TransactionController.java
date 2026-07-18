package com.transactguard.transaction.api;

import com.transactguard.transaction.application.TransactionAuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transactions", description = "Real-time payment authorization and risk decision APIs")
public class TransactionController {

    private final TransactionAuthorizationService authorizationService;

    public TransactionController(TransactionAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/transactions/authorize")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Authorize a tokenized payment",
            description = "Scores merchant and account risk signals, applies idempotency, updates account balance when approved, and emits an authorization event."
    )
    @ApiResponse(responseCode = "201", description = "Transaction decision created")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Idempotency key conflict",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public TransactionResponse authorize(@Valid @RequestBody AuthorizeTransactionRequest request) {
        return authorizationService.authorize(request);
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Get one transaction decision")
    @ApiResponse(responseCode = "200", description = "Transaction found")
    @ApiResponse(responseCode = "404", description = "Transaction not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public TransactionResponse getTransaction(
            @Parameter(description = "Transaction UUID returned by the authorization endpoint")
            @PathVariable UUID transactionId
    ) {
        return authorizationService.getTransaction(transactionId);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    @Operation(summary = "List recent transactions for an account")
    @ApiResponse(responseCode = "200", description = "Recent account transactions")
    @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<TransactionResponse> listAccountTransactions(
            @Parameter(description = "Account identifier", example = "acct_market_001")
            @PathVariable
            @Pattern(regexp = "[A-Za-z0-9_-]{3,64}", message = "invalid account id")
            String accountId
    ) {
        return authorizationService.listAccountTransactions(accountId);
    }
}
