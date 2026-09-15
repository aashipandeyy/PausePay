package com.financeautopilot.network;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "pausepay_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_ACCOUNT = "account_id";
    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveAccountId(long accountId) {
        prefs.edit().putLong(KEY_ACCOUNT, accountId).apply();
    }

    public long getAccountId() {
        return prefs.getLong(KEY_ACCOUNT, -1);
    }

    public boolean isLoggedIn() {
        return getToken() != null && getAccountId() != -1;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
