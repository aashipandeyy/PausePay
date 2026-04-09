package com.financeautopilot.serviceimpl;

import com.financeautopilot.dto.request.CreateAccountRequest;
import com.financeautopilot.dto.response.BankAccountResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.financeautopilot.model.BankAccount;
import com.financeautopilot.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.financeautopilot.repository.BankAccountRepository;
import com.financeautopilot.repository.UserRepository;
import com.financeautopilot.service.BankAccountService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private BankAccountRepository bankAccountRepository;
    private UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext() // like req.user.token in js
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public BankAccountResponse createAccount(@Valid CreateAccountRequest request) {
        User user = getCurrentUser();
        BankAccount account = BankAccount.builder()
                .user(user)
                .bankName(request.getBankName())
                .accountLabel(request.getAccountLabel())
                .currency(request.getCurrency())
                .build();
        BankAccount saved = bankAccountRepository.save(account);
        return toResponse(saved);
    }

    public List<BankAccountResponse> getMyAccounts() {
        User user = getCurrentUser();
        return bankAccountRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

    }

    private BankAccountResponse toResponse(BankAccount account) {
        return BankAccountResponse.builder()
                .id(account.getId())
                .bankName(account.getBankName())
                .accountLabel(account.getAccountLabel())
                .currency(account.getCurrency())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
