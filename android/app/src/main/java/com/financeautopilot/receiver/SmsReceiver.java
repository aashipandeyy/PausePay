package com.financeautopilot.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.financeautopilot.filter.SmsFilter;
import com.financeautopilot.model.NudgeResponse;
import com.financeautopilot.model.TransactionRequest;
import com.financeautopilot.network.ApiClient;
import com.financeautopilot.network.TokenManager;
import com.financeautopilot.notification.NudgeNotificationHelper;
import com.financeautopilot.parser.SmsParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        TokenManager tokenManager = new TokenManager(context);
        if (!tokenManager.isLoggedIn()) {
            Log.d(TAG, "user is not connected");
            return;
        }

        String format = bundle.getString("format");
        for (Object pdu : pdus) {
            SmsMessage sms = format == null
                    ? SmsMessage.createFromPdu((byte[]) pdu)
                    : SmsMessage.createFromPdu((byte[]) pdu, format);
            if (sms == null) continue;

            String sender = sms.getOriginatingAddress();
            String body = sms.getMessageBody();
            SmsFilter.FilterResult filter = SmsFilter.evaluate(sender, body);
            if (!filter.allowed) {
                Log.d(TAG, "discarded: " + filter.reason);
                continue;
            }

            SmsParser.ParsedTransaction parsed = SmsParser.parse(body);
            if (!parsed.valid) {
                Log.d(TAG, "parser could not extract transaction");
                continue;
            }

            sendToBackend(context, parsed, body, sms.getTimestampMillis(), tokenManager);
        }
    }

    private void sendToBackend(Context context, SmsParser.ParsedTransaction parsed,
                               String rawSms, long timestamp, TokenManager tokenManager) {
        TransactionRequest request = new TransactionRequest(
                parsed.amount,
                parsed.type,
                parsed.merchant,
                rawSms,
                tokenManager.getAccountId(),
                timestamp);

        ApiClient.getInstance()
                .sendTransaction("Bearer " + tokenManager.getToken(), request)
                .enqueue(new Callback<NudgeResponse>() {
                    @Override
                    public void onResponse(Call<NudgeResponse> call, Response<NudgeResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Log.e(TAG, "backend returned HTTP " + response.code());
                            return;
                        }

                        NudgeResponse nudge = response.body();
                        if (nudge.isHasNudge()) {
                            NudgeNotificationHelper.show(
                                    context,
                                    titleFor(nudge.getNudgeType()),
                                    nudge.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<NudgeResponse> call, Throwable t) {
                        Log.e(TAG, "network error: " + t.getMessage());
                    }
                });
    }

    private String titleFor(String type) {
        if (type == null) return "Finance Alert";
        switch (type) {
            case "OVERSPEND": return "Budget Exceeded";
            case "BUDGET_WARNING": return "Budget Warning";
            case "FREQUENCY_ALERT": return "Spending Frequency Alert";
            case "DAILY_LIMIT": return "Daily Spend Alert";
            case "MONTHLY_AWARENESS": return "Monthly Spend Update";
            default: return "Finance Update";
        }
    }
}
