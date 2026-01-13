package com.example.mymeetverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText emailField, passwordField;
    Button btnLogin;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        btnLogin = findViewById(R.id.btnLogin);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = database.getReference("Users");

        btnLogin.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            databaseReference.orderByChild("email").equalTo(email)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        try {
                                            if (snapshot.exists()) {
                                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                                    User user = userSnapshot.getValue(User.class);
                                                    if (user != null) {
                                                        // Validate user data
                                                        if (user.getEmail() == null || user.getEmail().isEmpty()) {
                                                            Toast.makeText(LoginActivity.this, "Error: Invalid user data", Toast.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                        
                                                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                                        Intent intent;
                                                        if (user.getRole() != null && user.getRole().equalsIgnoreCase("admin")) {
                                                            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                                            intent.putExtra("ADMIN_NAME", user.getName() != null ? user.getName() : "Admin");
                                                            intent.putExtra("ADMIN_EMAIL", user.getEmail());
                                                        } else {
                                                            intent = new Intent(LoginActivity.this, UserdashboardActivity.class);
                                                            intent.putExtra("USER_NAME", user.getName() != null ? user.getName() : "User");
                                                            intent.putExtra("USER_EMAIL", user.getEmail());
                                                            intent.putExtra("USER_ROLE", user.getRole() != null ? user.getRole() : "user");
                                                        }
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                        return;
                                                    }
                                                }
                                                Toast.makeText(LoginActivity.this, "Error: User data not found", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Error: User not found in database", Toast.LENGTH_LONG).show();
                                            }
                                        } catch (Exception e) {
                                            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: User authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }
}