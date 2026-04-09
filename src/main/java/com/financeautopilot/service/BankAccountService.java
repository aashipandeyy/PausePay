package com.financeautopilot.service;

import com.financeautopilot.dto.request.CreateAccountRequest;
import com.financeautopilot.dto.response.BankAccountResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface BankAccountService {

    BankAccountResponse createAccount(@Valid CreateAccountRequest createAccountRequest);

    List<BankAccountResponse> getMyAccounts();
}
