package com.financeautopilot.kafka.consumer;

import com.financeautopilot.kafka.event.TransactionCategorizedEvent;
import com.financeautopilot.kafka.event.TransactionSavedEvent;
import com.financeautopilot.kafka.producer.TransactionEventProducer;
import com.financeautopilot.model.Transaction;
import com.financeautopilot.model.enums.Category;
import com.financeautopilot.repository.TransactionRepository;
import com.financeautopilot.serviceimpl.CategorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CategorizationConsumer {

    private final TransactionRepository transactionRepository;
    private final CategorizationService categorizationService;
    private final TransactionEventProducer eventProducer;

    @KafkaListener(topics = "transaction.saved", groupId = "finance-autopilot-categorization")
    public void consume(TransactionSavedEvent event, Acknowledgment acknowledgment) {
        try {
            Transaction transaction = transactionRepository.findById(event.getTransactionId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Transaction not found: " + event.getTransactionId()));

            String category = categorizationService.categorizeSync(event.getMerchantName());
            transaction.setCategory(Category.valueOf(category));
            transaction.setCategorizedAt(java.time.LocalDateTime.now());
            transactionRepository.save(transaction);

            eventProducer.publishTransactionCategorized(
                    TransactionCategorizedEvent.builder()
                            .transactionId(transaction.getId())
                            .userId(event.getUserId())
                            .category(category)
                            .build());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to categorize transaction {}", event.getTransactionId(), e);
            throw e;
        }
    }
}