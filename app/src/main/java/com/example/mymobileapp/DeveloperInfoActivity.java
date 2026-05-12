package com.example.mymobileapp;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class DeveloperInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_info);

        ImageView ivBack = findViewById(R.id.ivBack);
        MaterialButton btnExit = findViewById(R.id.btnExit);

        ivBack.setOnClickListener(v -> finish());
        btnExit.setOnClickListener(v -> finish());
    }
}
