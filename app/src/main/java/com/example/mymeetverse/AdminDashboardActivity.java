package com.example.mymeetverse;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView menuIcon;
    RecyclerView meetingsRecyclerView;
    
    String adminEmail, adminName;
    ArrayList<Meeting> approvedMeetings;
    MeetingCardAdapter adapter;
    DatabaseReference approvedMeetingsReference;
    Handler handler;
    Runnable countdownRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admindashboard);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuIcon = findViewById(R.id.menuIcon);
        meetingsRecyclerView = findViewById(R.id.meetingsRecyclerView);

        // Get admin info from intent login
        Intent receivedIntent = getIntent();
        adminEmail = receivedIntent.getStringExtra("ADMIN_EMAIL");
        adminName = receivedIntent.getStringExtra("ADMIN_NAME");
        
        // Set default if not provided
        if (adminEmail == null) adminEmail = "admin@gmail.com";
        if (adminName == null) adminName = "Admin";

        approvedMeetings = new ArrayList<>();
        meetingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MeetingCardAdapter();
        meetingsRecyclerView.setAdapter(adapter);
        
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
        approvedMeetingsReference = database.getReference("ApprovedMeetings");
        
        handler = new Handler();
        
        setupNavigationHeader();
        loadApprovedMeetings();
        startCountdownUpdates();

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                
                if (id == R.id.nav_home) {
                    Toast.makeText(AdminDashboardActivity.this, "Home", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_requests) {
                    Intent intent = new Intent(AdminDashboardActivity.this, MeetingRequestsActivity.class);
                    intent.putExtra("ADMIN_EMAIL", adminEmail);
                    intent.putExtra("ADMIN_NAME", adminName);
                    startActivity(intent);
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(AdminDashboardActivity.this, SettingsActivity.class);
                    intent.putExtra("ADMIN_EMAIL", adminEmail);
                    intent.putExtra("ADMIN_NAME", adminName);
                    startActivity(intent);
                } else if (id == R.id.nav_users) {
                    Intent intent = new Intent(AdminDashboardActivity.this, SignedInUsersActivity.class);
                    intent.putExtra("USER_ROLE", "admin");
                    intent.putExtra("USER_NAME", adminName);
                    intent.putExtra("USER_EMAIL", adminEmail);
                    startActivity(intent);
                } else if (id == R.id.nav_history) {
                    Intent intent = new Intent(AdminDashboardActivity.this, MeetingHistoryActivity.class);
                    intent.putExtra("USER_ROLE", "admin");
                    intent.putExtra("USER_NAME", adminName);
                    intent.putExtra("USER_EMAIL", adminEmail);
                    startActivity(intent);
                } else if (id == R.id.nav_logout) {
                    Toast.makeText(AdminDashboardActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
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
    
    private void setupNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderInitial = headerView.findViewById(R.id.tvHeaderInitial);
        TextView tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
        TextView tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);
        
        tvHeaderInitial.setText("A");
        tvHeaderName.setText(adminName);
        tvHeaderEmail.setText(adminEmail);
        
        // Set admin color
        GradientDrawable background = (GradientDrawable) tvHeaderInitial.getBackground();
        background.setColor(Color.parseColor("#2C3E50"));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
        }
    }
    
    private void loadApprovedMeetings() {
        Toast.makeText(this, "Loading meetings...", Toast.LENGTH_SHORT).show();
        
        approvedMeetingsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                approvedMeetings.clear();
                
                Toast.makeText(AdminDashboardActivity.this, 
                    "Found " + snapshot.getChildrenCount() + " meetings", 
                    Toast.LENGTH_SHORT).show();
                
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Meeting meeting = dataSnapshot.getValue(Meeting.class);
                    if (meeting != null) {
                        if (isMeetingExpired(meeting)) {  // Check if meeting has expired
                            moveToHistory(meeting);
                        } else {
                            approvedMeetings.add(meeting);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                
                if (approvedMeetings.isEmpty()) {
                    Toast.makeText(AdminDashboardActivity.this, 
                        "No approved meetings yet. Approve a meeting request first!", 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AdminDashboardActivity.this, 
                        "Loaded " + approvedMeetings.size() + " meeting(s)", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, 
                    "Error loading meetings: " + error.getMessage(), 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void startCountdownUpdates() {
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                handler.postDelayed(this, 1000); // Update every second
            }
        };
        handler.post(countdownRunnable);
    }
    
    private String calculateCountdown(String dateStr, String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            Date meetingDate = sdf.parse(dateStr + " " + timeStr);
            
            if (meetingDate != null) {
                long diff = meetingDate.getTime() - System.currentTimeMillis();
                
                if (diff <= 0) {
                    return "Meeting Started";
                }
                
                long days = diff / (1000 * 60 * 60 * 24);
                long hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
                long minutes = (diff % (1000 * 60 * 60)) / (1000 * 60);
                long seconds = (diff % (1000 * 60)) / 1000;
                
                return String.format(Locale.getDefault(), "%02dd %02dh %02dm %02ds", 
                    days, hours, minutes, seconds);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Invalid Date";
    }
    
    private boolean isMeetingExpired(Meeting meeting) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            Date meetingDate = sdf.parse(meeting.getDate() + " " + meeting.getTime());
            
            if (meetingDate != null) {
                return System.currentTimeMillis() > meetingDate.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void moveToHistory(Meeting meeting) {
        DatabaseReference historyRef = FirebaseDatabase.getInstance(
            "https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("MeetingHistory");
        
        meeting.setTimestamp(System.currentTimeMillis());
        historyRef.child(meeting.getMeetingId()).setValue(meeting)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    approvedMeetingsReference.child(meeting.getMeetingId()).removeValue();
                }
            });
    }
    
    class MeetingCardAdapter extends RecyclerView.Adapter<MeetingCardAdapter.MeetingViewHolder> {

        @NonNull
        @Override
        public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_meeting_card, parent, false);
            return new MeetingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
            Meeting meeting = approvedMeetings.get(position);
            
            holder.tvCardMeetingName.setText(meeting.getMeetingName());
            holder.tvCardDescription.setText(meeting.getDescription());
            holder.tvCardDateTime.setText(meeting.getDate() + " at " + meeting.getTime());
            holder.tvCardOrganizer.setText("Organized by: " + meeting.getRequestedByName());
            
            // Display participants
            if (meeting.getParticipants() != null && !meeting.getParticipants().isEmpty()) {
                StringBuilder participantsText = new StringBuilder("Participants: ");
                for (int i = 0; i < meeting.getParticipants().size(); i++) {
                    participantsText.append(meeting.getParticipants().get(i));
                    if (i < meeting.getParticipants().size() - 1) {
                        participantsText.append(", ");
                    }
                }
                holder.tvCardParticipants.setText(participantsText.toString());
            } else {
                holder.tvCardParticipants.setText("Participants: None");
            }
            
            holder.tvCountdown.setText(calculateCountdown(meeting.getDate(), meeting.getTime()));
            
            // Admin can see all links
            holder.linkContainer.setVisibility(View.VISIBLE);
            if (meeting.getMeetingLink() != null && !meeting.getMeetingLink().isEmpty()) {
                holder.tvMeetingLink.setText(meeting.getMeetingLink());
            } else {
                holder.tvMeetingLink.setText("Link not generated");
            }

            holder.btnCopyLink.setOnClickListener(v -> {
                if (meeting.getMeetingLink() != null && !meeting.getMeetingLink().isEmpty()) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Meeting Link", meeting.getMeetingLink());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(AdminDashboardActivity.this, "Link copied!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "No link available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return approvedMeetings.size();
        }

        class MeetingViewHolder extends RecyclerView.ViewHolder {
            TextView tvCardMeetingName;
            TextView tvCardDescription;
            TextView tvCardDateTime;
            TextView tvCardOrganizer;
            TextView tvCardParticipants;
            TextView tvCountdown;
            TextView tvMeetingLink;
            Button btnCopyLink;
            LinearLayout linkContainer;

            MeetingViewHolder(View itemView) {
                super(itemView);
                tvCardMeetingName = itemView.findViewById(R.id.tvCardMeetingName);
                tvCardDescription = itemView.findViewById(R.id.tvCardDescription);
                tvCardDateTime = itemView.findViewById(R.id.tvCardDateTime);
                tvCardOrganizer = itemView.findViewById(R.id.tvCardOrganizer);
                tvCardParticipants = itemView.findViewById(R.id.tvCardParticipants);
                tvCountdown = itemView.findViewById(R.id.tvCountdown);
                tvMeetingLink = itemView.findViewById(R.id.tvMeetingLink);
                btnCopyLink = itemView.findViewById(R.id.btnCopyLink);
                linkContainer = itemView.findViewById(R.id.linkContainer);
            }
        }
    }
}
