package com.financeautopilot.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsParser {
    public static class ParsedTransaction {
        public double amount;
        public String merchant;
        public String type;
        public boolean valid;
    }

    public static ParsedTransaction parse(String sms) {
        ParsedTransaction result = new ParsedTransaction();
        if (sms == null || sms.trim().isEmpty()) return result;

        result.amount = extractAmount(sms);
        if (result.amount <= 0) return result;
        result.type = detectType(sms.toLowerCase());
        result.merchant = extractMerchant(sms);
        result.valid = true;
        return result;
    }

    private static double extractAmount(String sms) {
        String[] patterns = {
                "(?:Rs\\.?|INR|₹)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
                "([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(?:Rs\\.?|INR|₹)"
        };

        for (String patternText : patterns) {
            Matcher matcher = Pattern.compile(patternText, Pattern.CASE_INSENSITIVE).matcher(sms);
            if (matcher.find()) {
                try {
                    double amount = Double.parseDouble(matcher.group(1).replace(",", ""));
                    if (amount > 0) return amount;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }

    private static String detectType(String sms) {
        if (containsAny(sms, "debited", "debit", "paid", "spent", "withdrawn", "withdrawal", "purchase")) {
            return "DEBIT";
        }
        if (containsAny(sms, "credited", "credit", "received", "added", "refund")) {
            return "CREDIT";
        }
        return "DEBIT";
    }

    private static String extractMerchant(String sms) {
        Pattern at = Pattern.compile("(?:at|@)\\s+([A-Za-z][A-Za-z0-9\\s]{1,25}?)(?:\\s+(?:via|on|using|ref|upi)|[.\\n]|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = at.matcher(sms);
        if (matcher.find()) return cleanMerchant(matcher.group(1));

        Pattern to = Pattern.compile("(?:to|towards)\\s+([A-Za-z][A-Za-z0-9\\s]{1,25}?)(?:\\s+(?:via|on|ref)|[.\\n]|$)", Pattern.CASE_INSENSITIVE);
        matcher = to.matcher(sms);
        if (matcher.find()) return cleanMerchant(matcher.group(1));

        Pattern vpa = Pattern.compile("([a-zA-Z0-9][a-zA-Z0-9.]{1,20})@[a-zA-Z]{2,}");
        matcher = vpa.matcher(sms);
        if (matcher.find()) return cleanMerchant(matcher.group(1));
        return "UNKNOWN";
    }

    private static String cleanMerchant(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "UNKNOWN";
        String cleaned = raw.trim().replaceAll("(?i)\\b(pvt|ltd|india|payment|pay|app|online|digital)\\b", "")
                .replaceAll("[.,;:]+$", "").trim();
        if (cleaned.length() < 2) return "UNKNOWN";
        return cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1).toLowerCase();
    }

    private static boolean containsAny(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }
}
