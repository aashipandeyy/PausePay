package com.financeautopilot.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SmsFilter {
    private static final Set<String> ALLOWED_SENDERS = new HashSet<>(Arrays.asList(
            "HDFCBK", "HDFCBN", "ICICIB", "ICICI", "SBIINB", "SBIPSG", "SBMSMS", "SBIUPI",
            "AXISBK", "UTIBOP", "KOTAKB", "KOTAK", "YESBNI", "YESBNK", "INDBNK", "INDUSL",
            "BOBTXN", "BOBSMS", "PNBSMS", "CNBBNK", "IDFCFB", "FEDBNK", "RBLBNK",
            "PAYTMB", "PYTMSM", "PAYTM", "GPAYBN", "GOOGPE", "PHPEBN", "PHONEPE",
            "AMZNPG", "AMAZON", "BHIMUPI", "HDFCCC", "ICICCC", "SBICARD", "AXISCC", "KOTAKCC"
    ));

    private static final Pattern SENSITIVE = Pattern.compile(
            "\\b(otp|one[\\s-]?time[\\s-]?(password|pin)|verification[\\s-]?code|login[\\s-]?(code|otp)|security[\\s-]?code|authentication[\\s-]?code|password[\\s-]?(reset|code)|two[\\s-]?factor|2fa|do[\\s-]?not[\\s-]?(share|disclose)|never[\\s-]?share)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PROMOTIONAL = Pattern.compile(
            "\\b(offer|pre[\\s-]?(approved|qualified)|congratulations|you[\\s-]?are[\\s-]?eligible|upgrade[\\s-]?your|apply[\\s-]?now|click[\\s-]?(here|to)|limited[\\s-]?(time|offer)|exclusive|subscribe)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TRANSACTION = Pattern.compile(
            "\\b(debited|credited|debit|credit|spent|withdrawn|withdrawal|transferred|received|payment|paid|purchase|transaction|txn|upi|neft|imps|rtgs|nach|auto[\\s-]?debit|cashback|emi)\\b",
            Pattern.CASE_INSENSITIVE);

    public static class FilterResult {
        public final boolean allowed;
        public final String reason;

        private FilterResult(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }

        public static FilterResult allow() { return new FilterResult(true, "passed"); }
        public static FilterResult reject(String reason) { return new FilterResult(false, reason); }
    }

    public static FilterResult evaluate(String senderId, String body) {
        if (senderId == null || body == null || body.trim().isEmpty()) {
            return FilterResult.reject("empty_input");
        }

        String sender = senderId.toUpperCase().trim();
        String message = body.toLowerCase().trim();

        if (!isAllowedSender(sender)) return FilterResult.reject("sender_not_financial");
        if (SENSITIVE.matcher(message).find()) return FilterResult.reject("sensitive_content");
        if (PROMOTIONAL.matcher(message).find()) return FilterResult.reject("promotional_content");
        if (!TRANSACTION.matcher(message).find()) return FilterResult.reject("no_transaction_keyword");
        return FilterResult.allow();
    }

    private static boolean isAllowedSender(String sender) {
        for (String keyword : ALLOWED_SENDERS) {
            if (sender.contains(keyword)) return true;
        }
        String stripped = sender.replaceAll("^[A-Z]{2}-", "");
        for (String keyword : ALLOWED_SENDERS) {
            if (stripped.contains(keyword)) return true;
        }
        return false;
    }
}
