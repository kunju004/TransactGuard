package com.transactguard.transaction.messaging;

public interface TransactionEventPublisher {

    void publish(TransactionAuthorizedEvent event);
}
