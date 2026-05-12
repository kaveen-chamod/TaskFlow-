package com.example.mymobileapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TodoAdapter.OnTodoClickListener {

    private EditText etTask;
    private RecyclerView rvTodo;
    private TodoAdapter adapter;
    private List<TodoItem> todoList;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ImageView ivMenu;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;
    private ListenerRegistration taskListener;

    private final ActivityResultLauncher<Intent> addTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String newTaskName = result.getData().getStringExtra("task_name");
                    if (newTaskName != null && !newTaskName.isEmpty()) {
                        saveTaskToFirestore(newTaskName);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);
        ivMenu = findViewById(R.id.ivMenu);
        etTask = findViewById(R.id.etTask);
        rvTodo = findViewById(R.id.rvTodo);

        navView.setNavigationItemSelectedListener(this);

        ivMenu.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        todoList = new ArrayList<>();
        adapter = new TodoAdapter(todoList, this);
        rvTodo.setLayoutManager(new LinearLayoutManager(this));
        rvTodo.setAdapter(adapter);

        etTask.setOnClickListener(v -> {
            Intent intent = new Intent(TodoActivity.this, AddTaskActivity.class);
            addTaskLauncher.launch(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupRealtimeListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (taskListener != null) {
            taskListener.remove();
            taskListener = null;
        }
    }

    private void setupRealtimeListener() {
        if (currentUserId == null) return;

        taskListener = db.collection("tasks")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(TodoActivity.this, "Listen failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        todoList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            TodoItem item = doc.toObject(TodoItem.class);
                            if (item != null) {
                                item.setDocumentId(doc.getId());
                                todoList.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void saveTaskToFirestore(String taskName) {
        Map<String, Object> task = new HashMap<>();
        task.put("title", taskName);
        task.put("userId", currentUserId);
        task.put("isCompleted", false);
        task.put("timestamp", FieldValue.serverTimestamp());

        db.collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(TodoActivity.this, "Task added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TodoActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditClick(TodoItem item, int position) {
        showEditTaskDialog(item, position);
    }

    @Override
    public void onDeleteClick(int position) {
        if (position < 0 || position >= todoList.size()) {
            return;
        }

        TodoItem item = todoList.get(position);
        db.collection("tasks").document(item.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    todoList.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onToggleCompletion(TodoItem item, int position) {
        boolean newStatus = !item.getIsCompleted();
        item.setIsCompleted(newStatus);
        if (position >= 0 && position < todoList.size()) {
            adapter.notifyItemChanged(position);
        }
        db.collection("tasks").document(item.getDocumentId())
                .update("isCompleted", newStatus);
    }

    private void showEditTaskDialog(TodoItem item, int position) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_task);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etEditTaskName = dialog.findViewById(R.id.etEditTaskName);
        MaterialButton btnUpdateTask = dialog.findViewById(R.id.btnUpdateTask);
        MaterialButton btnCancelTask = dialog.findViewById(R.id.btnCancelTask);

        etEditTaskName.setText(item.getTitle());

        btnUpdateTask.setOnClickListener(v -> {
            String newName = etEditTaskName.getText().toString().trim();
            if (!newName.isEmpty()) {
                db.collection("tasks").document(item.getDocumentId())
                        .update("title", newName)
                        .addOnSuccessListener(aVoid -> {
                            item.setTitle(newName);
                            if (position >= 0 && position < todoList.size()) {
                                adapter.notifyItemChanged(position);
                            }
                            dialog.dismiss();
                        });
            }
        });

        btnCancelTask.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_developer) {
            Intent intent = new Intent(this, DeveloperInfoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_user) {
            Intent intent = new Intent(this, UserInfoActivity.class);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}