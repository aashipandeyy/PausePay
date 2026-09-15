package com.financeautopilot.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSavedEvent {
    private Long transactionId;
    private Long userId;
    private String merchantName;
}