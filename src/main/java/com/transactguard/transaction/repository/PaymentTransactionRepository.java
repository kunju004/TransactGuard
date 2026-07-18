package com.transactguard.transaction.repository;

import com.transactguard.transaction.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByIdempotencyKeyHash(String idempotencyKeyHash);

    List<PaymentTransaction> findTop25ByAccountIdOrderByCreatedAtDesc(String accountId);
}
