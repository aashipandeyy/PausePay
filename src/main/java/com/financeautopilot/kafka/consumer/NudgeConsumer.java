package com.financeautopilot.kafka.consumer;

import com.financeautopilot.dto.response.NudgeResponse;
import com.financeautopilot.kafka.event.TransactionCategorizedEvent;
import com.financeautopilot.model.Transaction;
import com.financeautopilot.repository.TransactionRepository;
import com.financeautopilot.serviceimpl.BehaviorEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NudgeConsumer {

    private final TransactionRepository transactionRepository;
    private final BehaviorEngineService behaviorEngineService;

    @KafkaListener(topics = "transaction.categorized", groupId = "finance-autopilot-nudges")
    public void consume(TransactionCategorizedEvent event, Acknowledgment acknowledgment) {
        try {
            Transaction transaction = transactionRepository.findById(event.getTransactionId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Transaction not found: " + event.getTransactionId()));

            NudgeResponse nudge = behaviorEngineService.evaluate(transaction, event.getUserId());

            if (Boolean.TRUE.equals(nudge.getHasNudge())) {
                log.info("Nudge triggered for transaction {}: {}", transaction.getId(), nudge.getMessage());
                // The current Android flow uses the HTTP response/local notification path.
                // This asynchronous consumer cannot update an HTTP response that has already returned.
                // A future push channel such as FCM can consume this result independently.
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process nudge for transaction {}", event.getTransactionId(), e);
            throw e;
        }
    }
}