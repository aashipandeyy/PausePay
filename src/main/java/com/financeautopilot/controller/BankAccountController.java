package com.financeautopilot.controller;

import com.financeautopilot.dto.request.CreateAccountRequest;
import com.financeautopilot.dto.response.BankAccountResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.financeautopilot.service.BankAccountService;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<BankAccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.ok(bankAccountService.createAccount(request));
    }

    @GetMapping
    public ResponseEntity<List<BankAccountResponse>> getMyAccounts() {
        return ResponseEntity.ok(bankAccountService.getMyAccounts());
    }
}
