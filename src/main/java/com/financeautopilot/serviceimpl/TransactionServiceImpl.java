package com.financeautopilot.serviceimpl;

import com.financeautopilot.dto.request.SmsTransactionRequest;
import com.financeautopilot.dto.response.NudgeResponse;
import com.financeautopilot.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.financeautopilot.model.BankAccount;
import com.financeautopilot.model.Transaction;
import com.financeautopilot.model.User;
import com.financeautopilot.model.enums.Category;
import com.financeautopilot.model.enums.TransactionType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.financeautopilot.repository.BankAccountRepository;
import com.financeautopilot.repository.TransactionRepository;
import com.financeautopilot.repository.UserRepository;
import com.financeautopilot.service.TransactionService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final CategorizationService categorizationService;
    private final BehaviorEngineService behaviorEngineService;

    public NudgeResponse saveFromSms(SmsTransactionRequest request) {

        // get current logged in user
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // find the bank account
        BankAccount account = bankAccountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // convert timestamp to LocalDate
        LocalDate transactionDate = request.getTimestamp() != null
                ? Instant.ofEpochMilli(request.getTimestamp())
                .atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.now();

        // build and save transaction
        Transaction transaction = Transaction.builder()
                .bankAccount(account)
                .amount(request.getAmount())
                .type(TransactionType.valueOf(request.getType().toUpperCase()))
                .merchantName(request.getMerchant() != null
                        ? request.getMerchant() : "UNKNOWN")
                .description(request.getRawSms())
                .date(transactionDate)
                .category(Category.UNCATEGORIZED)
                .isAnomaly(false)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction saved: {} ₹{}", saved.getMerchantName(), saved.getAmount());

        // categorize in background (async, doesn't block response)
        CompletableFuture<String> categorizationFuture =
                categorizationService.categorize(saved.getMerchantName());

        categorizationFuture.thenAccept(category -> {
            saved.setCategory(Category.valueOf(category));
            transactionRepository.save(saved);
            log.info("Transaction {} categorized as {}", saved.getId(), category);
        });

        // run behavior engine synchronously and return nudge
        NudgeResponse nudge = behaviorEngineService.evaluate(saved, user.getId());
        nudge.setTransaction(toResponse(saved));

        return nudge;
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