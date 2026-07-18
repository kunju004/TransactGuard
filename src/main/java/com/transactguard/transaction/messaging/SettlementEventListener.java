package com.transactguard.transaction.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.kafka.enabled", havingValue = "true")
public class SettlementEventListener {

    private static final Logger log = LoggerFactory.getLogger(SettlementEventListener.class);

    @KafkaListener(topics = "${app.kafka.settlement-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleSettlementMessage(String payload) {
        log.info("Received settlement message payload={}", payload);
    }
}
