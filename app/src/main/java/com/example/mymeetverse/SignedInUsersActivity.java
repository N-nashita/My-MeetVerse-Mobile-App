package com.example.mymeetverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.graphics.drawable.GradientDrawable;

import java.util.ArrayList;

public class SignedInUsersActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView menuIcon;
    RecyclerView usersRecyclerView;
    ArrayList<User> signedInUsers;
    UserAdapter adapter;
    DatabaseReference usersReference;
    
    String userRole;
    String userEmail;
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in_users);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuIcon = findViewById(R.id.menuIcon);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        signedInUsers = new ArrayList<>();

        // Get user info from intent
        Intent receivedIntent = getIntent();
        userRole = receivedIntent.getStringExtra("USER_ROLE");
        userEmail = receivedIntent.getStringExtra("USER_EMAIL");
        userName = receivedIntent.getStringExtra("USER_NAME");

        // Set appropriate menu based on role
        if (userRole != null && userRole.equalsIgnoreCase("admin")) {
            navigationView.inflateMenu(R.menu.admin_menu);
        } else {
            navigationView.inflateMenu(R.menu.user_menu);
        }

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                
                if (id == R.id.nav_home) {
                    // Navigate back to respective dashboard
                    Intent intent;
                    if (userRole != null && userRole.equalsIgnoreCase("admin")) {
                        intent = new Intent(SignedInUsersActivity.this, AdminDashboardActivity.class);
                    } else {
                        intent = new Intent(SignedInUsersActivity.this, UserdashboardActivity.class);
                        intent.putExtra("USER_EMAIL", userEmail);
                        intent.putExtra("USER_NAME", userName);
                        intent.putExtra("USER_ROLE", userRole);
                    }
                    startActivity(intent);
                    finish();
                } else if (id == R.id.nav_requests) {
                    Intent intent = new Intent(SignedInUsersActivity.this, MeetingRequestsActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_launch) {
                    Intent intent = new Intent(SignedInUsersActivity.this, LaunchMeetingActivity.class);
                    intent.putExtra("USER_EMAIL", userEmail);
                    intent.putExtra("USER_NAME", userName);
                    startActivity(intent);
                } else if (id == R.id.nav_settings) {
                    Toast.makeText(SignedInUsersActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_users) {
                    Toast.makeText(SignedInUsersActivity.this, "Already on Users page", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_history) {
                    Toast.makeText(SignedInUsersActivity.this, "History", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignedInUsersActivity.this, MeetingHistoryActivity.class);
                    intent.putExtra("USER_EMAIL", userEmail);
                    intent.putExtra("USER_NAME", userName);
                    intent.putExtra("USER_ROLE", userRole);
                    startActivity(intent);
                } else if (id == R.id.nav_logout) {
                    Toast.makeText(SignedInUsersActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignedInUsersActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        setupNavigationHeader();

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter();
        usersRecyclerView.setAdapter(adapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
        usersReference = database.getReference("Users");

        loadSignedInUsers();
    }

    private void setupNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderInitial = headerView.findViewById(R.id.tvHeaderInitial);
        TextView tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
        TextView tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);
        
        if (userRole != null && userRole.equalsIgnoreCase("admin")) {
            tvHeaderInitial.setText("A");
            tvHeaderName.setText(userName != null ? userName : "Admin User");
            tvHeaderEmail.setText(userEmail != null ? userEmail : "admin@mymeetverse.com");
            
            GradientDrawable background = (GradientDrawable) tvHeaderInitial.getBackground();
            background.setColor(Color.parseColor("#2C3E50"));
        } else {
            tvHeaderInitial.setText("U");
            tvHeaderName.setText(userName != null ? userName : "User");
            tvHeaderEmail.setText(userEmail != null ? userEmail : "user@email.com");
            
            GradientDrawable background = (GradientDrawable) tvHeaderInitial.getBackground();
            background.setColor(Color.parseColor("#7FB3D5"));
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void loadSignedInUsers() {
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                signedInUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        signedInUsers.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
                
                if (signedInUsers.isEmpty()) {
                    Toast.makeText(SignedInUsersActivity.this, 
                        "No users found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignedInUsersActivity.this, 
                    "Error loading users: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = signedInUsers.get(position);
            
            holder.tvUserName.setText(user.getName());
            holder.tvUserEmail.setText(user.getEmail());
            
            // Set initial and background color based on role
            String role = user.getRole() != null ? user.getRole().toLowerCase() : "user";
            if (role.equals("admin")) {
                holder.tvUserInitial.setText("A");
                // Dark color for admin
                GradientDrawable background = (GradientDrawable) holder.tvUserInitial.getBackground();
                background.setColor(Color.parseColor("#2C3E50"));
            } else {
                holder.tvUserInitial.setText("U");
                // Light color for user
                GradientDrawable background = (GradientDrawable) holder.tvUserInitial.getBackground();
                background.setColor(Color.parseColor("#7FB3D5"));
            }
        }

        @Override
        public int getItemCount() {
            return signedInUsers.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserInitial;
            TextView tvUserName;
            TextView tvUserEmail;

            UserViewHolder(View itemView) {
                super(itemView);
                tvUserInitial = itemView.findViewById(R.id.tvUserInitial);
                tvUserName = itemView.findViewById(R.id.tvUserName);
                tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            }
        }
    }
}
