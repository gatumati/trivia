package com.example.onlinetrivia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.widget.TextView;
import android.os.Handler;

public class NameActivity extends Activity {
    private boolean doubleBackToExitPressedOnce = false;
    private String nickname;
    private long lastBackPressedTime = 0;
    private Toast exitToast;

    // SharedPreferences constants
    private static final String USER_PREFERENCES = "user_data";
    private static final String NICKNAME_KEY = "nickname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_nickname);

        EditText nicknameEditText = findViewById(R.id.nicknameEditText);
        Button okButton = findViewById(R.id.okButton);

        // Retrieve nickname from SharedPreferences only
        SharedPreferences sharedPreferences = getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        nickname = sharedPreferences.getString(NICKNAME_KEY, "");
        nicknameEditText.setText(nickname);

        okButton.setOnClickListener(view -> handleOkButtonClick(nicknameEditText));

        nicknameEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                okButton.performClick();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            if (exitToast != null) {
                exitToast.cancel();
            }
            finishAffinity();
            System.exit(0);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        createAndShowToast("Press again to exit.");

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    private void handleOkButtonClick(EditText nicknameEditText) {
        nickname = nicknameEditText.getText().toString();

        if (!nickname.trim().isEmpty()) {
            // Save nickname to SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(NICKNAME_KEY, nickname);
            editor.apply();

            Intent intent = new Intent();
            intent.putExtra("new_nickname", nickname);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            // Handle empty nickname scenario, like showing a Toast or a message
        }
    }

    private void createAndShowToast(String message) {
        try {
            if (exitToast != null) {
                exitToast.cancel();
            }

            exitToast = new Toast(NameActivity.this);
            View view = LayoutInflater.from(NameActivity.this).inflate(R.layout.custom_transparent_toast, null);
            TextView toastText = view.findViewById(R.id.toastText);

            if (toastText == null) {
                throw new RuntimeException("toastText is null. Check if ID is correct in the layout.");
            }

            toastText.setText(message);
            exitToast.setView(view);
            exitToast.setDuration(Toast.LENGTH_SHORT);
            exitToast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
