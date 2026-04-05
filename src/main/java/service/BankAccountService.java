package service;

import dto.request.CreateAccountRequest;
import dto.response.BankAccountResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface BankAccountService {

    BankAccountResponse createAccount(@Valid CreateAccountRequest createAccountRequest);

    List<BankAccountResponse> getMyAccounts();
}
