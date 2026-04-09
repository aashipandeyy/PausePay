package com.financeautopilot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountResponse {

    private Long id;
    private String bankName;
    private String accountLabel;
    private String currency;
    private LocalDateTime createdAt;
}
