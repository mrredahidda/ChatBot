package com.example.chatbot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.onurkaganaldemir.ktoastlib.KToast;

public class ForgotPasswordScreen extends AppCompatActivity {
    private static final long RATE_LIMIT_TIME_THRESHOLD = 60 * 1000; // 1 minute
    private static final String PREF_KEY_LAST_RESET_REQUEST_TIME = "last_reset_request_time";
    private static final String PREF_KEY_EMAIL_PREFIX = "email_";

    Button forgotPasswordButton;
    EditText forgotPasswordEmail;
    FirebaseAuth mAuth;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_screen);

        backButton = findViewById(R.id.backButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        forgotPasswordEmail = findViewById(R.id.forgotPasswordEmail);
        mAuth = FirebaseAuth.getInstance();

        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = forgotPasswordEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    KToast.errorToast(ForgotPasswordScreen.this, "Please enter your email address to reset your password.", Gravity.BOTTOM, 500);
                    return;
                }

                if(!isNetworkAvailable()){
                    KToast.errorToast(ForgotPasswordScreen.this, "Please check you internet connection, and try again.", Gravity.BOTTOM, 500);
                    return;
                } else {

                    if (isRateLimited(email)) {
                        KToast.warningToast(ForgotPasswordScreen.this, "Password reset rate limit exceeded for this email address. Please try again later.", Gravity.BOTTOM, 500);
                        return;
                    }

                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        updateLastResetRequestTime(email);
                                        KToast.successToast(ForgotPasswordScreen.this, "Password reset email sent. Please check your inbox to reset your password.", Gravity.BOTTOM, 500);
                                    } else {
                                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                            KToast.errorToast(ForgotPasswordScreen.this, "The email address does not exist. Please check the email address and try again.", Gravity.BOTTOM, 500);
                                        } else {
                                            KToast.errorToast(ForgotPasswordScreen.this, "Failed to send password reset email. Please try again later.", Gravity.BOTTOM, 500);
                                        }
                                    }
                                }
                            });
                }

                forgotPasswordEmail.clearFocus();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ForgotPasswordScreen.this, LoginScreen.class);
                startActivity(it);
                finish();
            }
        });
    }

    private boolean isRateLimited(String email) {
        SharedPreferences prefs = getSharedPreferences("password_reset_prefs", MODE_PRIVATE);
        long lastResetRequestTime = prefs.getLong(getEmailKey(email), 0);
        long currentTime = System.currentTimeMillis();

        return (currentTime - lastResetRequestTime) < RATE_LIMIT_TIME_THRESHOLD;
    }

    private void updateLastResetRequestTime(String email) {
        SharedPreferences prefs = getSharedPreferences("password_reset_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(getEmailKey(email), System.currentTimeMillis());
        editor.apply();
    }

    private String getEmailKey(String email) {
        return PREF_KEY_EMAIL_PREFIX + email.replace(".", "_");
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}