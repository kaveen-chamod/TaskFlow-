package com.example.mymobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvSignup, tvForgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            Log.d("Firebase", "Firebase Connected");
        }

        // Initialize Views
        etEmail = findViewById(R.id.etUsername); // Reusing ID from layout (serving as email)
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        
        // Add progress bar if it exists in layout, or I should add it
        // Re-reading activity_login.xml, I didn't add one. I'll stick to basic logic for now.

        // Main Navigation to Todo screen (with login logic)
        btnLogin.setOnClickListener(v -> loginUser());

        // Footer Navigation to Signup screen
        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Navigation to Forgot Password screen
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        // Basic Firebase Login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, TodoActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed: " + 
                                (task.getException() != null ? task.getException().getMessage() : ""), 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(LoginActivity.this, TodoActivity.class));
            finish();
        }
    }
}
