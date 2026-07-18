package com.transactguard.transaction.messaging;

import java.util.UUID;

public record TransactionAuthorizedDomainEvent(UUID transactionId) {
}
