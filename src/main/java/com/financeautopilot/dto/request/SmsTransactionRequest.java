package com.financeautopilot.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsTransactionRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    // debit or credit
    @NotBlank(message = "Type is required")
    private String type;

    // merchant name extracted by android - could be "UNKNOWN"
    private String merchant;

    // raw SMS text - useful for debugging and re-parsing later
    @NotBlank(message = "Raw SMS is required")
    private String rawSms;

    // account this transaction belongs to
    @NotNull(message = "Account ID is required")
    private Long accountId;

    // timestamp from the sms (android sends this as epoch ms)
    private Long timestamp;
}
