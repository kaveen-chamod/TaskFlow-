package com.example.mymobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etAddTask;
    private MaterialButton btnSubmitTask;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        etAddTask = findViewById(R.id.etAddTask);
        btnSubmitTask = findViewById(R.id.btnSubmitTask);
        ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(v -> finish());

        btnSubmitTask.setOnClickListener(v -> {
            String taskName = etAddTask.getText().toString().trim();
            if (!taskName.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("task_name", taskName);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
