package com.financeautopilot.serviceimpl;

import com.financeautopilot.dto.request.SmsTransactionRequest;
import com.financeautopilot.dto.response.NudgeResponse;
import com.financeautopilot.dto.response.TransactionResponse;
import com.financeautopilot.kafka.event.TransactionSavedEvent;
import com.financeautopilot.kafka.producer.TransactionEventProducer;
import com.financeautopilot.model.BankAccount;
import com.financeautopilot.model.Transaction;
import com.financeautopilot.model.User;
import com.financeautopilot.model.enums.Category;
import com.financeautopilot.model.enums.TransactionType;
import com.financeautopilot.repository.BankAccountRepository;
import com.financeautopilot.repository.TransactionRepository;
import com.financeautopilot.repository.UserRepository;
import com.financeautopilot.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final TransactionEventProducer eventProducer;

    @Override
    public NudgeResponse saveFromSms(SmsTransactionRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        LocalDate transactionDate = request.getTimestamp() != null
                ? Instant.ofEpochMilli(request.getTimestamp())
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.now();

        Transaction transaction = Transaction.builder()
                .bankAccount(account)
                .amount(request.getAmount())
                .type(TransactionType.valueOf(request.getType().toUpperCase()))
                .merchantName(request.getMerchant() != null ? request.getMerchant() : "UNKNOWN")
                .description(request.getRawSms())
                .date(transactionDate)
                .category(Category.UNCATEGORIZED)
                .isAnomaly(false)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction saved: {} ₹{}", saved.getMerchantName(), saved.getAmount());

        eventProducer.publishTransactionSaved(
                TransactionSavedEvent.builder()
                        .transactionId(saved.getId())
                        .userId(user.getId())
                        .merchantName(saved.getMerchantName())
                        .build());

        // Categorization and behavior evaluation now happen asynchronously through Kafka.
        // The HTTP response therefore cannot contain the eventual Kafka-generated nudge.
        return NudgeResponse.builder()
                .hasNudge(false)
                .transaction(toResponse(saved))
                .build();
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType().name())
                .merchant(t.getMerchantName())
                .category(t.getCategory().name())
                .isAnomaly(t.getIsAnomaly())
                .anomalyReason(t.getAnomalyReason())
                .date(t.getDate())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
