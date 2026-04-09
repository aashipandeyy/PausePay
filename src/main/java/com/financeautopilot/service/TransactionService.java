package com.financeautopilot.service;

import com.financeautopilot.dto.request.SmsTransactionRequest;
import com.financeautopilot.dto.response.NudgeResponse;
import jakarta.validation.Valid;

public interface TransactionService {
    NudgeResponse saveFromSms(@Valid SmsTransactionRequest request);
}
