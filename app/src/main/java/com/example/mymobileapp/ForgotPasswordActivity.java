package com.example.mymobileapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etResetEmail;
    private MaterialButton btnSendResetLink;
    private ImageView ivBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        etResetEmail = findViewById(R.id.etResetEmail);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(v -> finish());

        btnSendResetLink.setOnClickListener(v -> {
            String email = etResetEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etResetEmail.setError("Email is required");
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Error: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
