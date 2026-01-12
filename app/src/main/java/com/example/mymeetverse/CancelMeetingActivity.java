package com.example.mymeetverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CancelMeetingActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView menuIcon;
    ListView meetingsListView;
    TextView tvEmptyMessage;
    
    String userEmail, userName, userRole;
    ArrayList<Meeting> userMeetings;
    CancelMeetingAdapter adapter;
    DatabaseReference approvedMeetingsReference;
    DatabaseReference meetingRequestsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_meeting);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuIcon = findViewById(R.id.menuIcon);
        meetingsListView = findViewById(R.id.meetingsListView);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        // Get user info from intent
        Intent receivedIntent = getIntent();
        userEmail = receivedIntent.getStringExtra("USER_EMAIL");
        userName = receivedIntent.getStringExtra("USER_NAME");
        userRole = receivedIntent.getStringExtra("USER_ROLE");

        userMeetings = new ArrayList<>();
        
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
        approvedMeetingsReference = database.getReference("ApprovedMeetings");
        meetingRequestsReference = database.getReference("MeetingRequests");

        adapter = new CancelMeetingAdapter();
        meetingsListView.setAdapter(adapter);

        setupNavigationHeader();
        setupNavigationMenu();
        loadUserMeetings();

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void setupNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderInitial = headerView.findViewById(R.id.tvHeaderInitial);
        TextView tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
        TextView tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);
        
        tvHeaderInitial.setText("U");
        tvHeaderName.setText(userName != null ? userName : "User");
        tvHeaderEmail.setText(userEmail != null ? userEmail : "user@email.com");
        
        GradientDrawable background = (GradientDrawable) tvHeaderInitial.getBackground();
        background.setColor(Color.parseColor("#7FB3D5"));
    }

    private void setupNavigationMenu() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(CancelMeetingActivity.this, UserdashboardActivity.class);
                    intent.putExtra("USER_EMAIL", userEmail);
                    intent.putExtra("USER_NAME", userName);
                    intent.putExtra("USER_ROLE", userRole);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.nav_launch) {
                    Intent intent = new Intent(CancelMeetingActivity.this, LaunchMeetingActivity.class);
                    intent.putExtra("USER_EMAIL", userEmail);
                    intent.putExtra("USER_NAME", userName);
                    intent.putExtra("USER_ROLE", userRole);
                    startActivity(intent);
                } else if (id == R.id.nav_users) {
                    Intent intent = new Intent(CancelMeetingActivity.this, SignedInUsersActivity.class);
                    intent.putExtra("USER_ROLE", userRole);
                    intent.putExtra("USER_EMAIL", userEmail);
                    intent.putExtra("USER_NAME", userName);
                    startActivity(intent);
                } else if (id == R.id.nav_history) {
                    Intent intent = new Intent(CancelMeetingActivity.this, MeetingHistoryActivity.class);
                    intent.putExtra("USER_EMAIL", userEmail);
                    intent.putExtra("USER_NAME", userName);
                    intent.putExtra("USER_ROLE", userRole);
                    startActivity(intent);
                } else if (id == R.id.nav_cancel_meeting) {
                    Toast.makeText(CancelMeetingActivity.this, "Already on Cancel Meeting page", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_logout) {
                    Toast.makeText(CancelMeetingActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CancelMeetingActivity.this, LoginActivity.class);
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

    private void loadUserMeetings() {
        approvedMeetingsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userMeetings.clear();
                
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Meeting meeting = dataSnapshot.getValue(Meeting.class);
                    if (meeting != null) {
                        // Only show meetings launched by this user
                        if (meeting.getRequestedBy() != null && 
                            meeting.getRequestedBy().equalsIgnoreCase(userEmail)) {
                            userMeetings.add(meeting);
                        }
                    }
                }
                
                adapter.notifyDataSetChanged();
                
                if (userMeetings.isEmpty()) {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    meetingsListView.setVisibility(View.GONE);
                } else {
                    tvEmptyMessage.setVisibility(View.GONE);
                    meetingsListView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CancelMeetingActivity.this, 
                    "Error loading meetings: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelMeeting(Meeting meeting) {
        // Remove from ApprovedMeetings
        approvedMeetingsReference.child(meeting.getMeetingId()).removeValue()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Also remove from MeetingRequests if exists
                    meetingRequestsReference.child(meeting.getMeetingId()).removeValue()
                        .addOnCompleteListener(task2 -> {
                            Toast.makeText(CancelMeetingActivity.this, 
                                "Meeting cancelled successfully", 
                                Toast.LENGTH_LONG).show();
                        });
                } else {
                    Toast.makeText(CancelMeetingActivity.this, 
                        "Failed to cancel meeting", 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    class CancelMeetingAdapter extends ArrayAdapter<Meeting> {
        CancelMeetingAdapter() {
            super(CancelMeetingActivity.this, R.layout.item_cancel_meeting, userMeetings);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_cancel_meeting, parent, false);
            }

            Meeting meeting = userMeetings.get(position);

            TextView tvMeetingName = convertView.findViewById(R.id.tvMeetingName);
            TextView tvDescription = convertView.findViewById(R.id.tvDescription);
            TextView tvDateTime = convertView.findViewById(R.id.tvDateTime);
            TextView tvStatus = convertView.findViewById(R.id.tvStatus);
            Button btnCancelMeeting = convertView.findViewById(R.id.btnCancelMeeting);

            tvMeetingName.setText(meeting.getMeetingName());
            tvDescription.setText(meeting.getDescription());
            tvDateTime.setText(meeting.getDate() + " at " + meeting.getTime());
            
            String status = meeting.getStatus();
            if (status != null) {
                if (status.equals("approved")) {
                    tvStatus.setText("Status: Approved");
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                } else if (status.equals("pending")) {
                    tvStatus.setText("Status: Pending");
                    tvStatus.setTextColor(Color.parseColor("#FF9800"));
                } else {
                    tvStatus.setText("Status: " + status);
                    tvStatus.setTextColor(Color.parseColor("#CCCCCC"));
                }
            }

            btnCancelMeeting.setOnClickListener(v -> {
                new AlertDialog.Builder(CancelMeetingActivity.this)
                    .setTitle("Cancel Meeting")
                    .setMessage("Are you sure you want to cancel \"" + meeting.getMeetingName() + "\"?\n\nThis action cannot be undone and all participants will lose access to this meeting.")
                    .setPositiveButton("Yes, Cancel Meeting", (dialog, which) -> cancelMeeting(meeting))
                    .setNegativeButton("No, Keep It", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            });

            return convertView;
        }
    }
}
