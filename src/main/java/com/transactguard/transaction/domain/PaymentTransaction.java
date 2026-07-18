package com.transactguard.transaction.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "payment_transactions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_transactions_idempotency", columnNames = "idempotency_key_hash")
        },
        indexes = {
                @Index(name = "idx_transactions_account_created", columnList = "account_id,created_at"),
                @Index(name = "idx_transactions_status", columnList = "status")
        }
)
public class PaymentTransaction {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false, length = 64)
    private String accountId;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @Column(name = "merchant_category_code", nullable = false, length = 4)
    private String merchantCategoryCode;

    @Column(name = "merchant_country", nullable = false, length = 2)
    private String merchantCountry;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RiskDecision riskDecision;

    @Column(nullable = false)
    private int riskScore;

    @Column(nullable = false, length = 512)
    private String riskReasons;

    @Column(name = "idempotency_key_hash", nullable = false, length = 64)
    private String idempotencyKeyHash;

    @Column(name = "request_fingerprint", nullable = false, length = 64)
    private String requestFingerprint;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PaymentTransaction() {
    }

    public PaymentTransaction(
            String accountId,
            String merchantId,
            String merchantCategoryCode,
            String merchantCountry,
            String currency,
            BigDecimal amount,
            TransactionStatus status,
            RiskDecision riskDecision,
            int riskScore,
            String riskReasons,
            String idempotencyKeyHash,
            String requestFingerprint
    ) {
        this.id = UUID.randomUUID();
        this.accountId = Objects.requireNonNull(accountId, "accountId");
        this.merchantId = Objects.requireNonNull(merchantId, "merchantId");
        this.merchantCategoryCode = Objects.requireNonNull(merchantCategoryCode, "merchantCategoryCode");
        this.merchantCountry = Objects.requireNonNull(merchantCountry, "merchantCountry");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.amount = Objects.requireNonNull(amount, "amount");
        this.status = Objects.requireNonNull(status, "status");
        this.riskDecision = Objects.requireNonNull(riskDecision, "riskDecision");
        this.riskScore = riskScore;
        this.riskReasons = Objects.requireNonNull(riskReasons, "riskReasons");
        this.idempotencyKeyHash = Objects.requireNonNull(idempotencyKeyHash, "idempotencyKeyHash");
        this.requestFingerprint = Objects.requireNonNull(requestFingerprint, "requestFingerprint");
    }

    public UUID getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public String getMerchantCountry() {
        return merchantCountry;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public RiskDecision getRiskDecision() {
        return riskDecision;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getRiskReasons() {
        return riskReasons;
    }

    public String getIdempotencyKeyHash() {
        return idempotencyKeyHash;
    }

    public String getRequestFingerprint() {
        return requestFingerprint;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
