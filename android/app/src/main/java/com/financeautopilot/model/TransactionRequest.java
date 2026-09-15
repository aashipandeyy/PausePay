package com.financeautopilot.model;

public class TransactionRequest {
    private final double amount;
    private final String type;
    private final String merchant;
    private final String rawSms;
    private final Long accountId;
    private final Long timestamp;

    public TransactionRequest(double amount, String type, String merchant, String rawSms,
                              Long accountId, Long timestamp) {
        this.amount = amount;
        this.type = type;
        this.merchant = merchant;
        this.rawSms = rawSms;
        this.accountId = accountId;
        this.timestamp = timestamp;
    }

    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getMerchant() { return merchant; }
    public String getRawSms() { return rawSms; }
    public Long getAccountId() { return accountId; }
    public Long getTimestamp() { return timestamp; }
}
