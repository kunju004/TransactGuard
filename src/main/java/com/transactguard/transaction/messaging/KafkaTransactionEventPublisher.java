package com.transactguard.transaction.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.kafka.enabled", havingValue = "true")
public class KafkaTransactionEventPublisher implements TransactionEventPublisher {

    private final KafkaTemplate<String, TransactionAuthorizedEvent> kafkaTemplate;
    private final String topic;

    public KafkaTransactionEventPublisher(
            KafkaTemplate<String, TransactionAuthorizedEvent> kafkaTemplate,
            @Value("${app.kafka.transaction-topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(TransactionAuthorizedEvent event) {
        kafkaTemplate.send(topic, event.transactionId().toString(), event);
    }
}
