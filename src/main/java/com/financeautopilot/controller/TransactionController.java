package com.financeautopilot.controller;

import com.financeautopilot.dto.request.SmsTransactionRequest;
import com.financeautopilot.dto.response.NudgeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.financeautopilot.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<NudgeResponse> saveResponse(
            @Valid @RequestBody SmsTransactionRequest request) {
        return ResponseEntity.ok(transactionService.saveFromSms(request));
    }
}
