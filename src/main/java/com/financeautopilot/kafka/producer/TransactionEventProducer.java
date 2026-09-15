package com.financeautopilot.kafka.producer;

import com.financeautopilot.kafka.event.TransactionCategorizedEvent;
import com.financeautopilot.kafka.event.TransactionSavedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.transaction-saved:transaction.saved}")
    private String transactionSavedTopic;

    @Value("${kafka.topic.transaction-categorized:transaction.categorized}")
    private String transactionCategorizedTopic;

    public void publishTransactionSaved(TransactionSavedEvent event) {
        String key = String.valueOf(event.getTransactionId());
        kafkaTemplate.send(transactionSavedTopic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish transaction.saved for transaction {}", event.getTransactionId(), ex);
                    } else {
                        log.debug("Published transaction.saved for transaction {}", event.getTransactionId());
                    }
                });
    }

    public void publishTransactionCategorized(TransactionCategorizedEvent event) {
        String key = String.valueOf(event.getTransactionId());
        kafkaTemplate.send(transactionCategorizedTopic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish transaction.categorized for transaction {}", event.getTransactionId(), ex);
                    } else {
                        log.debug("Published transaction.categorized for transaction {}", event.getTransactionId());
                    }
                });
    }
}