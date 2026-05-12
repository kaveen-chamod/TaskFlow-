package com.example.mymobileapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserInfoActivity extends AppCompatActivity {

    private TextView tvUsernameLabel, tvEmailLabel;
    private ShapeableImageView ivProfile;
    private static final String PREFS_NAME = "UserProfilePrefs";
    private static final String KEY_PROFILE_URI = "profile_uri";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    // Register the photo picker activity result launcher
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    // Grant persistable URI permission if needed (optional for Glide but good for local storage)
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    
                    // Load image into view using Glide
                    loadProfileImage(uri);
                    
                    // Persist the URI in SharedPreferences
                    saveImageUri(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            // Redirect to login if not authenticated
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ImageView ivBack = findViewById(R.id.ivBack);
        ivProfile = findViewById(R.id.ivProfile);
        tvUsernameLabel = findViewById(R.id.tvUsernameLabel);
        tvEmailLabel = findViewById(R.id.tvEmailLabel);
        MaterialButton btnEditInfo = findViewById(R.id.btnEditInfo);
        MaterialButton btnSignOut = findViewById(R.id.btnSignOut);

        ivBack.setOnClickListener(v -> finish());

        // Trigger photo picker on image click
        ivProfile.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        btnEditInfo.setOnClickListener(v -> showEditDialog());
        btnSignOut.setOnClickListener(v -> showSignOutDialog());

        // Load saved image on start
        loadSavedProfileImage();
        
        // Load User Data from Firebase
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Display email from Auth immediately
            tvEmailLabel.setText("Email: " + user.getEmail());

            // Fetch Username from Firestore
            db.collection("users").document(currentUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null && !username.isEmpty()) {
                                tvUsernameLabel.setText("Username : " + username);
                            } else {
                                tvUsernameLabel.setText("Username : User");
                            }
                        } else {
                            tvUsernameLabel.setText("Username : User");
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvUsernameLabel.setText("Username : User");
                        Toast.makeText(UserInfoActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadProfileImage(Uri uri) {
        Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(ivProfile);
    }

    private void saveImageUri(Uri uri) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PROFILE_URI, uri.toString()).apply();
    }

    private void loadSavedProfileImage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uriString = prefs.getString(KEY_PROFILE_URI, null);
        if (uriString != null) {
            loadProfileImage(Uri.parse(uriString));
        }
    }

    private void showEditDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_info);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etEmail = dialog.findViewById(R.id.etEditEmail);
        EditText etUsername = dialog.findViewById(R.id.etEditUsername);
        MaterialButton btnOk = dialog.findViewById(R.id.btnOk);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);

        // Pre-fill existing data
        String currentUsername = tvUsernameLabel.getText().toString().replace("Username : ", "");
        String currentEmail = tvEmailLabel.getText().toString().replace("Email: ", "");
        etUsername.setText(currentUsername);
        etEmail.setText(currentEmail);

        btnOk.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(UserInfoActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update Firestore
            Map<String, Object> updates = new HashMap<>();
            updates.put("username", newUsername);
            updates.put("email", newEmail);

            db.collection("users").document(currentUserId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(UserInfoActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        tvUsernameLabel.setText("Username : " + newUsername);
                        tvEmailLabel.setText("Email: " + newEmail);
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UserInfoActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showSignOutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sign_out);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        MaterialButton btnOk = dialog.findViewById(R.id.btnSignOutOk);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnSignOutCancel);

        btnOk.setOnClickListener(v -> {
            mAuth.signOut();
            dialog.dismiss();
            Intent intent = new Intent(UserInfoActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
