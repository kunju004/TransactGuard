package com.transactguard.transaction.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "accounts",
        indexes = {
                @Index(name = "idx_accounts_account_id", columnList = "account_id", unique = true)
        }
)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false, unique = true, length = 64)
    private String accountId;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Column(nullable = false)
    private boolean active;

    @Version
    private long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Account() {
    }

    public Account(String accountId, String currency, BigDecimal availableBalance, boolean active) {
        this.accountId = Objects.requireNonNull(accountId, "accountId");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.availableBalance = Objects.requireNonNull(availableBalance, "availableBalance");
        this.active = active;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public boolean isActive() {
        return active;
    }

    public boolean canCover(BigDecimal amount) {
        return availableBalance.compareTo(amount) >= 0;
    }

    public void debit(BigDecimal amount) {
        if (!canCover(amount)) {
            throw new IllegalStateException("Account balance cannot cover debit");
        }
        availableBalance = availableBalance.subtract(amount);
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
