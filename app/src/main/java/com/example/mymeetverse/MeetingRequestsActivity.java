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
import java.util.UUID;

public class MeetingRequestsActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView menuIcon;
    ListView requestsListView;
    ArrayList<Meeting> meetingRequests;
    MeetingRequestAdapter adapter;
    DatabaseReference meetingsReference;
    DatabaseReference approvedMeetingsReference;
    
    String adminEmail, adminName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_requests);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuIcon = findViewById(R.id.menuIcon);
        requestsListView = findViewById(R.id.requestsListView);
        meetingRequests = new ArrayList<>();

        // Get admin info from intent
        Intent receivedIntent = getIntent();
        adminEmail = receivedIntent.getStringExtra("ADMIN_EMAIL");
        adminName = receivedIntent.getStringExtra("ADMIN_NAME");
        
        if (adminEmail == null) adminEmail = "admin@gmail.com";
        if (adminName == null) adminName = "Admin";

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
        meetingsReference = database.getReference("MeetingRequests");
        approvedMeetingsReference = database.getReference("ApprovedMeetings");

        adapter = new MeetingRequestAdapter();
        requestsListView.setAdapter(adapter);

        setupNavigationHeader();
        setupNavigationMenu();
        loadMeetingRequests();
        
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
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
                    Intent intent = new Intent(MeetingRequestsActivity.this, AdminDashboardActivity.class);
                    intent.putExtra("ADMIN_EMAIL", adminEmail);
                    intent.putExtra("ADMIN_NAME", adminName);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.nav_requests) {
                    Toast.makeText(MeetingRequestsActivity.this, "Already on Requests page", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(MeetingRequestsActivity.this, SettingsActivity.class);
                    intent.putExtra("ADMIN_EMAIL", adminEmail);
                    intent.putExtra("ADMIN_NAME", adminName);
                    startActivity(intent);
                } else if (id == R.id.nav_users) {
                    Intent intent = new Intent(MeetingRequestsActivity.this, SignedInUsersActivity.class);
                    intent.putExtra("USER_ROLE", "admin");
                    intent.putExtra("USER_NAME", adminName);
                    intent.putExtra("USER_EMAIL", adminEmail);
                    startActivity(intent);
                } else if (id == R.id.nav_history) {
                    Intent intent = new Intent(MeetingRequestsActivity.this, MeetingHistoryActivity.class);
                    intent.putExtra("USER_ROLE", "admin");
                    intent.putExtra("USER_NAME", adminName);
                    intent.putExtra("USER_EMAIL", adminEmail);
                    startActivity(intent);
                } else if (id == R.id.nav_logout) {
                    Toast.makeText(MeetingRequestsActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
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

    private void loadMeetingRequests() {
        meetingsReference.orderByChild("status").equalTo("pending")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    meetingRequests.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Meeting meeting = dataSnapshot.getValue(Meeting.class);
                        if (meeting != null) {
                            meetingRequests.add(meeting);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (meetingRequests.isEmpty()) {
                        Toast.makeText(MeetingRequestsActivity.this, 
                            "No pending requests", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MeetingRequestsActivity.this, 
                        "Error loading requests: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void approveMeeting(Meeting meeting) {
        // Generate unique meeting link
        String uniqueLink = "https://meet.mymeetverse.com/" + UUID.randomUUID().toString().substring(0, 8);
        meeting.setMeetingLink(uniqueLink);
        meeting.setStatus("approved");
        
        // Save to ApprovedMeetings
        approvedMeetingsReference.child(meeting.getMeetingId()).setValue(meeting)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Update status in MeetingRequests
                    meetingsReference.child(meeting.getMeetingId()).child("status").setValue("approved")
                        .addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                Toast.makeText(this, "Meeting approved and link generated!", Toast.LENGTH_LONG).show();
                            }
                        });
                } else {
                    Toast.makeText(this, "Failed to approve meeting", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void rejectMeeting(Meeting meeting) {
        meetingsReference.child(meeting.getMeetingId()).child("status").setValue("rejected")
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Meeting rejected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to reject meeting", Toast.LENGTH_SHORT).show();
                }
            });
    }

    class MeetingRequestAdapter extends ArrayAdapter<Meeting> {
        MeetingRequestAdapter() {
            super(MeetingRequestsActivity.this, R.layout.item_meeting_request, meetingRequests);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_meeting_request, parent, false);
            }

            Meeting meeting = meetingRequests.get(position);

            TextView tvMeetingName = convertView.findViewById(R.id.tvMeetingName);
            TextView tvRequestedBy = convertView.findViewById(R.id.tvRequestedBy);
            TextView tvDateTime = convertView.findViewById(R.id.tvDateTime);
            TextView tvDescription = convertView.findViewById(R.id.tvDescription);
            Button btnApprove = convertView.findViewById(R.id.btnApprove);
            Button btnReject = convertView.findViewById(R.id.btnReject);

            tvMeetingName.setText(meeting.getMeetingName());
            tvRequestedBy.setText("Requested by: " + meeting.getRequestedByName());
            tvDateTime.setText(meeting.getDate() + " at " + meeting.getTime());
            tvDescription.setText(meeting.getDescription());

            btnApprove.setOnClickListener(v -> {
                new AlertDialog.Builder(MeetingRequestsActivity.this)
                    .setTitle("Approve Meeting")
                    .setMessage("Approve meeting: " + meeting.getMeetingName() + "?")
                    .setPositiveButton("Approve", (dialog, which) -> approveMeeting(meeting))
                    .setNegativeButton("Cancel", null)
                    .show();
            });

            btnReject.setOnClickListener(v -> {
                new AlertDialog.Builder(MeetingRequestsActivity.this)
                    .setTitle("Reject Meeting")
                    .setMessage("Reject meeting: " + meeting.getMeetingName() + "?")
                    .setPositiveButton("Reject", (dialog, which) -> rejectMeeting(meeting))
                    .setNegativeButton("Cancel", null)
                    .show();
            });

            return convertView;
        }
    }
}
