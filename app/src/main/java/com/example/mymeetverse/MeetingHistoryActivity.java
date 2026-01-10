package com.example.mymeetverse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MeetingHistoryActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView menuIcon;
    RecyclerView historyRecyclerView;
    
    ArrayList<Meeting> meetingHistory;
    HistoryAdapter adapter;
    DatabaseReference historyReference;
    
    String userEmail, userName, userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_history);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuIcon = findViewById(R.id.menuIcon);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);

        Intent receivedIntent = getIntent();
        userEmail = receivedIntent.getStringExtra("USER_EMAIL");
        userName = receivedIntent.getStringExtra("USER_NAME");
        userRole = receivedIntent.getStringExtra("USER_ROLE");

        // Set appropriate menu based on role
        if (userRole != null && userRole.equalsIgnoreCase("admin")) {
            navigationView.inflateMenu(R.menu.admin_menu);
        } else {
            navigationView.inflateMenu(R.menu.user_menu);
        }

        meetingHistory = new ArrayList<>();
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter();
        historyRecyclerView.setAdapter(adapter);
        
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
        historyReference = database.getReference("MeetingHistory");
        
        loadMeetingHistory();

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                
                if (id == R.id.nav_home) {
                    Intent intent;
                    if (userRole != null && userRole.equalsIgnoreCase("admin")) {
                        intent = new Intent(MeetingHistoryActivity.this, AdminDashboardActivity.class);
                    } else {
                        intent = new Intent(MeetingHistoryActivity.this, UserdashboardActivity.class);
                        intent.putExtra("USER_EMAIL", userEmail);
                        intent.putExtra("USER_NAME", userName);
                        intent.putExtra("USER_ROLE", userRole);
                    }
                    startActivity(intent);
                    finish();
                } else if (id == R.id.nav_requests) {
                    Intent intent = new Intent(MeetingHistoryActivity.this, MeetingRequestsActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_launch) {
                    Intent intent = new Intent(MeetingHistoryActivity.this, LaunchMeetingActivity.class);
                    intent.putExtra("USER_EMAIL", userEmail);
                    intent.putExtra("USER_NAME", userName);
                    startActivity(intent);
                } else if (id == R.id.nav_users) {
                    Intent intent = new Intent(MeetingHistoryActivity.this, SignedInUsersActivity.class);
                    intent.putExtra("USER_ROLE", userRole);
                    intent.putExtra("USER_EMAIL", userEmail);
                    intent.putExtra("USER_NAME", userName);
                    startActivity(intent);
                } else if (id == R.id.nav_history) {
                    Toast.makeText(MeetingHistoryActivity.this, "Already on History page", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_settings) {
                    Toast.makeText(MeetingHistoryActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_logout) {
                    Toast.makeText(MeetingHistoryActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MeetingHistoryActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        
        setupNavigationHeader();
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
    
    private void loadMeetingHistory() {
        historyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                meetingHistory.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Meeting meeting = dataSnapshot.getValue(Meeting.class);
                    if (meeting != null) {
                        meetingHistory.add(meeting);
                    }
                }
                adapter.notifyDataSetChanged();
                
                if (meetingHistory.isEmpty()) {
                    Toast.makeText(MeetingHistoryActivity.this, 
                        "No meeting history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MeetingHistoryActivity.this, 
                    "Error loading history: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history_meeting, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            Meeting meeting = meetingHistory.get(position);
            
            holder.tvHistoryMeetingName.setText(meeting.getMeetingName());
            holder.tvHistoryDescription.setText(meeting.getDescription());
            holder.tvHistoryDateTime.setText(meeting.getDate() + " at " + meeting.getTime());
            holder.tvHistoryOrganizer.setText("Organized by: " + meeting.getRequestedByName());
            holder.tvHistoryStatus.setText("Status: Completed");
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            holder.tvHistoryTimestamp.setText("Completed: " + sdf.format(meeting.getTimestamp()));
        }

        @Override
        public int getItemCount() {
            return meetingHistory.size();
        }

        class HistoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvHistoryMeetingName;
            TextView tvHistoryDescription;
            TextView tvHistoryDateTime;
            TextView tvHistoryOrganizer;
            TextView tvHistoryStatus;
            TextView tvHistoryTimestamp;

            HistoryViewHolder(View itemView) {
                super(itemView);
                tvHistoryMeetingName = itemView.findViewById(R.id.tvHistoryMeetingName);
                tvHistoryDescription = itemView.findViewById(R.id.tvHistoryDescription);
                tvHistoryDateTime = itemView.findViewById(R.id.tvHistoryDateTime);
                tvHistoryOrganizer = itemView.findViewById(R.id.tvHistoryOrganizer);
                tvHistoryStatus = itemView.findViewById(R.id.tvHistoryStatus);
                tvHistoryTimestamp = itemView.findViewById(R.id.tvHistoryTimestamp);
            }
        }
    }
}
