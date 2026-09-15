package com.financeautopilot.scheduler;

import com.financeautopilot.model.Transaction;
import com.financeautopilot.model.enums.Category;
import com.financeautopilot.repository.TransactionRepository;
import com.financeautopilot.repository.UserRepository;
import com.financeautopilot.serviceimpl.CategorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class NightlyCategorizationJob {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategorizationService categorizationService;

    // Recovery job for transactions that remain uncategorized after the Kafka pipeline.
    @Scheduled(cron = "0 0 2 * * *")
    public void categorizeUncategorized() {
        log.info("Nightly categorization job started");

        userRepository.findAll().forEach(user -> {
            List<Transaction> uncategorized = transactionRepository
                    .findByCategoryAndBankAccountUserId(Category.UNCATEGORIZED.name(), user.getId());

            log.info("Found {} uncategorized transactions for user {}", uncategorized.size(), user.getId());

            uncategorized.forEach(transaction -> {
                String category = categorizationService.categorizeSync(transaction.getMerchantName());
                transaction.setCategory(Category.valueOf(category));
                transaction.setCategorizedAt(java.time.LocalDateTime.now());
                transactionRepository.save(transaction);
            });
        });

        log.info("Nightly categorization job completed");
    }
}
