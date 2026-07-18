package com.transactguard.transaction.repository;

import com.transactguard.transaction.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    Optional<OutboxEvent> findFirstByAggregateIdAndEventTypeOrderByCreatedAtDesc(String aggregateId, String eventType);
}
