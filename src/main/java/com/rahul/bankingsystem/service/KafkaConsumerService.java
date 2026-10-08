package com.rahul.bankingsystem.service;

import com.rahul.bankingsystem.event.TransactionCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

    /**
     * Simulates downstream audit logging and notifications.
     */
    @KafkaListener(topics = "transaction-events", groupId = "banking-group")
    public void consumeTransactionEvent(TransactionCompletedEvent event) {
        log.info("======================================================");
        log.info("KAFKA CONSUMER: Received Transaction Completed Event!");
        log.info("Key: {}", event.getTransactionKey());
        log.info("Source: {}, Target: {}", event.getSourceAccountId(), event.getTargetAccountId());
        log.info("Amount: {} {}", event.getAmount(), event.getCurrency());
        log.info("Status: {}", event.getStatus());
        log.info("Simulating Async Audit Log and Email Notifications...");
        log.info("======================================================");
    }
}