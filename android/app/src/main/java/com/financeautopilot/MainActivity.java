package com.financeautopilot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.financeautopilot.network.TokenManager;
import com.financeautopilot.notification.NudgeNotificationHelper;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST_CODE = 101;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 102;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenManager = new TokenManager(this);
        NudgeNotificationHelper.createChannel(this);
        requestSmsPermission();
        requestNotificationPermission();

        EditText tokenInput = findViewById(R.id.tokenInput);
        EditText accountIdInput = findViewById(R.id.accountIdInput);
        TextView statusText = findViewById(R.id.statusText);

        refreshStatus(statusText);

        ((Button) findViewById(R.id.saveButton)).setOnClickListener(v -> {
            String token = tokenInput.getText().toString().trim();
            String accountIdText = accountIdInput.getText().toString().trim();
            if (token.isEmpty() || accountIdText.isEmpty()) {
                Toast.makeText(this, "Enter the JWT token and account ID", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                tokenManager.saveToken(token);
                tokenManager.saveAccountId(Long.parseLong(accountIdText));
                refreshStatus(statusText);
                tokenInput.setText("");
                accountIdInput.setText("");
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Account ID must be a number", Toast.LENGTH_SHORT).show();
            }
        });

        ((Button) findViewById(R.id.disconnectButton)).setOnClickListener(v -> {
            tokenManager.clear();
            refreshStatus(statusText);
        });
    }

    private void refreshStatus(TextView statusText) {
        statusText.setText(tokenManager.isLoggedIn()
                ? "Connected — monitoring SMS transactions.\nAccount ID: " + tokenManager.getAccountId()
                : "Not connected");
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }
}
