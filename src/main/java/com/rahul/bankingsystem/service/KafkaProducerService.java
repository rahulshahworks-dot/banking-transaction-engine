package com.rahul.bankingsystem.service;

import com.rahul.bankingsystem.event.TransactionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_TRANSACTIONS = "transaction-events";

    /**
     * This listener is triggered strictly AFTER the database transaction commits.
     * If the DB commit fails or rolls back, this event will NOT be sent to Kafka.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionCompletedEvent(TransactionCompletedEvent event) {
        log.info("Transaction committed to DB. Emitting Kafka event for key: {}", event.getTransactionKey());
        kafkaTemplate.send(TOPIC_TRANSACTIONS, event.getTransactionKey().toString(), event);
    }
}
