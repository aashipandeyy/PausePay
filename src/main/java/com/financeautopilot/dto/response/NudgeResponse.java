package com.financeautopilot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NudgeResponse {
    // whether there's a nudge to show
    private Boolean hasNudge;

    // the message to show e.g. "you've spent 3200 on food this month"
    private String message;

    // what type of nudge: BUDGET_WARNING, FREQUENCY_ALERT, DAILY_LIMIT, OVERSPEND
    private String nudgeType;

    // saved transaction
    private TransactionResponse transaction;
}
