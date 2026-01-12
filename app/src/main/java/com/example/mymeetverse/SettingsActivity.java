package com.example.mymeetverse;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView menuIcon;
    Button btnChangeAdmin;
    Button btnEditUsers;
    Button btnRemoveAdmin;
    
    String adminEmail, adminName;
    DatabaseReference usersReference;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuIcon = findViewById(R.id.menuIcon);
        btnChangeAdmin = findViewById(R.id.btnChangeAdmin);
        btnEditUsers = findViewById(R.id.btnEditUsers);
        btnRemoveAdmin = findViewById(R.id.btnRemoveAdmin);
        
        Intent receivedIntent = getIntent();
        adminEmail = receivedIntent.getStringExtra("ADMIN_EMAIL");
        adminName = receivedIntent.getStringExtra("ADMIN_NAME");
        
        if (adminEmail == null) adminEmail = "admin@gmail.com";
        if (adminName == null) adminName = "Admin";
        
        FirebaseDatabase database = FirebaseDatabase.getInstance(
            "https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
        usersReference = database.getReference("Users");
        
        setupNavigationHeader();
        setupNavigationMenu();
        
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        
        btnChangeAdmin.setOnClickListener(v -> showChangeAdminDialog());
        btnEditUsers.setOnClickListener(v -> showEditUsersDialog());
        btnRemoveAdmin.setOnClickListener(v -> showRemoveAdminDialog());
    }
    
    private void setupNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderInitial = headerView.findViewById(R.id.tvHeaderInitial);
        TextView tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
        TextView tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);
        
        tvHeaderInitial.setText("A");
        tvHeaderName.setText(adminName);
        tvHeaderEmail.setText(adminEmail);
        
        GradientDrawable background = (GradientDrawable) tvHeaderInitial.getBackground();
        background.setColor(Color.parseColor("#2C3E50"));
    }
    
    private void setupNavigationMenu() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(SettingsActivity.this, AdminDashboardActivity.class);
                    intent.putExtra("ADMIN_EMAIL", adminEmail);
                    intent.putExtra("ADMIN_NAME", adminName);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.nav_requests) {
                    Intent intent = new Intent(SettingsActivity.this, MeetingRequestsActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_settings) {
                    Toast.makeText(SettingsActivity.this, "Already on Settings", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_users) {
                    Intent intent = new Intent(SettingsActivity.this, SignedInUsersActivity.class);
                    intent.putExtra("USER_ROLE", "admin");
                    intent.putExtra("USER_NAME", adminName);
                    intent.putExtra("USER_EMAIL", adminEmail);
                    startActivity(intent);
                } else if (id == R.id.nav_history) {
                    Intent intent = new Intent(SettingsActivity.this, MeetingHistoryActivity.class);
                    intent.putExtra("USER_ROLE", "admin");
                    intent.putExtra("USER_NAME", adminName);
                    intent.putExtra("USER_EMAIL", adminEmail);
                    startActivity(intent);
                } else if (id == R.id.nav_logout) {
                    Toast.makeText(SettingsActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    private void showChangeAdminDialog() {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<User> users = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
                
                if (users.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "No users available", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                View dialogView = LayoutInflater.from(SettingsActivity.this)
                    .inflate(R.layout.dialog_user_selection, null);
                builder.setView(dialogView);
                
                TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
                RecyclerView usersRecyclerView = dialogView.findViewById(R.id.dialogUsersRecyclerView);
                Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
                
                dialogTitle.setText("Select New Admin");
                
                AlertDialog dialog = builder.create();
                
                usersRecyclerView.setLayoutManager(new LinearLayoutManager(SettingsActivity.this));
                UserSelectionAdapter adapter = new UserSelectionAdapter(users, new UserSelectionAdapter.OnUserClickListener() {
                    @Override
                    public void onUserClick(User user) {
                        changeUserToAdmin(user, dialog);
                    }
                });
                usersRecyclerView.setAdapter(adapter);
                
                btnCancel.setOnClickListener(v -> dialog.dismiss());
                
                dialog.show();
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, 
                    "Error loading users: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void changeUserToAdmin(User user, AlertDialog dialog) {
        new AlertDialog.Builder(this)
            .setTitle("Change Admin")
            .setMessage("Make " + user.getName() + " the new admin?")
            .setPositiveButton("Yes", (d, which) -> {
                usersReference.child(user.getUserId()).child("role").setValue("Admin")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SettingsActivity.this, 
                            user.getName() + " is now an admin!", 
                            Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SettingsActivity.this, 
                            "Failed to update admin: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showEditUsersDialog() {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<User> users = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getRole() != null && user.getRole().equalsIgnoreCase("user")) {
                        users.add(user);
                    }
                }
                
                if (users.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "No users available", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                View dialogView = LayoutInflater.from(SettingsActivity.this)
                    .inflate(R.layout.dialog_user_selection, null);
                builder.setView(dialogView);
                
                TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
                RecyclerView usersRecyclerView = dialogView.findViewById(R.id.dialogUsersRecyclerView);
                Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
                
                dialogTitle.setText("Edit Users");
                
                AlertDialog dialog = builder.create();
                
                usersRecyclerView.setLayoutManager(new LinearLayoutManager(SettingsActivity.this));
                UserEditAdapter adapter = new UserEditAdapter(users, new UserEditAdapter.OnUserActionListener() {
                    @Override
                    public void onDeleteUser(User user) {
                        deleteUser(user);
                    }
                });
                usersRecyclerView.setAdapter(adapter);
                
                btnCancel.setOnClickListener(v -> dialog.dismiss());
                
                dialog.show();
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, 
                    "Error loading users: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void deleteUser(User user) {
        new AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete " + user.getName() + "?")
            .setPositiveButton("Delete", (d, which) -> {
                usersReference.child(user.getUserId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SettingsActivity.this, 
                            user.getName() + " has been deleted", 
                            Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SettingsActivity.this, 
                            "Failed to delete user: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showRemoveAdminDialog() {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<User> adminUsers = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getRole() != null && user.getRole().equalsIgnoreCase("admin")) {
                        adminUsers.add(user);
                    }
                }
                
                if (adminUsers.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "No admin users found", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (adminUsers.size() == 1) {
                    Toast.makeText(SettingsActivity.this, 
                        "Cannot remove the last admin. There must be at least one admin.", 
                        Toast.LENGTH_LONG).show();
                    return;
                }
                
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                View dialogView = LayoutInflater.from(SettingsActivity.this)
                    .inflate(R.layout.dialog_user_selection, null);
                builder.setView(dialogView);
                
                TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
                RecyclerView usersRecyclerView = dialogView.findViewById(R.id.dialogUsersRecyclerView);
                Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
                
                dialogTitle.setText("Remove Admin Privileges");
                
                AlertDialog dialog = builder.create();
                
                usersRecyclerView.setLayoutManager(new LinearLayoutManager(SettingsActivity.this));
                final int totalAdmins = adminUsers.size();
                UserSelectionAdapter adapter = new UserSelectionAdapter(adminUsers, new UserSelectionAdapter.OnUserClickListener() {
                    @Override
                    public void onUserClick(User user) {
                        removeAdminPrivileges(user, dialog, totalAdmins);
                    }
                });
                usersRecyclerView.setAdapter(adapter);
                
                btnCancel.setOnClickListener(v -> dialog.dismiss());
                
                dialog.show();
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, 
                    "Error loading users: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void removeAdminPrivileges(User user, AlertDialog dialog, int totalAdmins) {
        if (totalAdmins <= 1) {
            Toast.makeText(this, 
                "Cannot remove the last admin. There must be at least one admin.", 
                Toast.LENGTH_LONG).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Remove Admin Privileges")
            .setMessage("Remove admin privileges from " + user.getName() + "?")
            .setPositiveButton("Yes", (d, which) -> {
                usersReference.child(user.getUserId()).child("role").setValue("User")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SettingsActivity.this, 
                            user.getName() + " is now a regular user", 
                            Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SettingsActivity.this, 
                            "Failed to update user: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    static class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.ViewHolder> {
        
        interface OnUserClickListener {
            void onUserClick(User user);
        }
        
        ArrayList<User> users;
        OnUserClickListener listener;
        
        UserSelectionAdapter(ArrayList<User> users, OnUserClickListener listener) {
            this.users = users;
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_selectable, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = users.get(position);
            holder.tvUserName.setText(user.getName());
            holder.tvUserEmail.setText(user.getEmail());
            
            String role = user.getRole() != null ? user.getRole().toLowerCase() : "user";
            if (role.equals("admin")) {
                holder.tvUserInitial.setText("A");
                GradientDrawable background = (GradientDrawable) holder.tvUserInitial.getBackground();
                background.setColor(Color.parseColor("#141f64"));
            } else {
                holder.tvUserInitial.setText("U");
                GradientDrawable background = (GradientDrawable) holder.tvUserInitial.getBackground();
                background.setColor(Color.parseColor("#7FB3D5"));
            }
            
            holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
        
        @Override
        public int getItemCount() {
            return users.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserInitial;
            TextView tvUserName;
            TextView tvUserEmail;
            
            ViewHolder(View view) {
                super(view);
                tvUserInitial = view.findViewById(R.id.tvUserInitial);
                tvUserName = view.findViewById(R.id.tvUserName);
                tvUserEmail = view.findViewById(R.id.tvUserEmail);
            }
        }
    }
    
    static class UserEditAdapter extends RecyclerView.Adapter<UserEditAdapter.ViewHolder> {
        
        interface OnUserActionListener {
            void onDeleteUser(User user);
        }
        
        ArrayList<User> users;
        OnUserActionListener listener;
        
        UserEditAdapter(ArrayList<User> users, OnUserActionListener listener) {
            this.users = users;
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_editable, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = users.get(position);
            holder.tvUserName.setText(user.getName());
            holder.tvUserEmail.setText(user.getEmail());
            
            String role = user.getRole() != null ? user.getRole().toLowerCase() : "user";
            if (role.equals("admin")) {
                holder.tvUserInitial.setText("A");
                GradientDrawable background = (GradientDrawable) holder.tvUserInitial.getBackground();
                background.setColor(Color.parseColor("#141f64"));
            } else {
                holder.tvUserInitial.setText("U");
                GradientDrawable background = (GradientDrawable) holder.tvUserInitial.getBackground();
                background.setColor(Color.parseColor("#7FB3D5"));
            }
            
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteUser(user));
        }
        
        @Override
        public int getItemCount() {
            return users.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserInitial;
            TextView tvUserName;
            TextView tvUserEmail;
            Button btnDelete;
            
            ViewHolder(View view) {
                super(view);
                tvUserInitial = view.findViewById(R.id.tvUserInitial);
                tvUserName = view.findViewById(R.id.tvUserName);
                tvUserEmail = view.findViewById(R.id.tvUserEmail);
                btnDelete = view.findViewById(R.id.btnDelete);
            }
        }
    }
}
