package com.example.mymeetverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignupActivity extends AppCompatActivity {

    TextInputEditText nameField, emailField, passwordField;
    Button btnSignup;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameField = findViewById(R.id.nameField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        btnSignup = findViewById(R.id.btnSignup);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
        database.getReference(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d("SignupActivity", "Firebase connected!");
                    Toast.makeText(SignupActivity.this, "Firebase connected!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("SignupActivity", "Firebase not connected!");
                    Toast.makeText(SignupActivity.this, "Firebase not connected!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SignupActivity", "Connection listener cancelled: " + error.getMessage());
            }
        });
        
        databaseReference = database.getReference("Users");
        
        Log.d("SignupActivity", "Firebase initialized");
        Log.d("SignupActivity", "Database URL: " + database.getReference().toString());

        btnSignup.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("SignupActivity", "Starting database query for email: " + email);

            databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("SignupActivity", "onDataChange called, exists: " + snapshot.exists());
                    if (snapshot.exists()) {
                        Toast.makeText(SignupActivity.this, "Email already registered", Toast.LENGTH_SHORT).show();
                    } else {
                        checkAndRegisterUser(name, email, password);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("SignupActivity", "Database error: " + error.getMessage());
                    Toast.makeText(SignupActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void checkAndRegisterUser(String name, String email, String password) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role;
                if (!snapshot.exists()) {
                    role = "Admin"; // First user - assign Admin role
                    Toast.makeText(SignupActivity.this, "You are the first user! Admin role assigned.", Toast.LENGTH_LONG).show();
                } else {
                    role = "User"; // Subsequent users - assign User role
                }

                // Generate unique user ID
                String userId = databaseReference.push().getKey();

                User user = new User(userId, name, email, password, role);

                // Save to database
                if (userId != null) {
                    databaseReference.child(userId).setValue(user).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Registration successful as " + role, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent); 
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignupActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}