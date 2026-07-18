package com.transactguard.transaction.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingTransactionEventPublisher implements TransactionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingTransactionEventPublisher.class);

    @Override
    public void publish(TransactionAuthorizedEvent event) {
        log.info("Kafka disabled; transaction event captured locally event={}", event);
    }
}
