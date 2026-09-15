package com.financeautopilot.network;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class TokenManager {
    private static final String PREF_NAME = "pausepay_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_ACCOUNT = "account_id";
    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            prefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize secure token storage", e);
        }
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
