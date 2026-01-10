package com.example.mymeetverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class LaunchMeetingActivity extends AppCompatActivity {

    TextInputEditText meetingNameField, meetingDescriptionField, dateField, timeField;
    Button btnSelectParticipants, btnLaunchMeeting;
    
    Calendar calendar;
    ArrayList<String> selectedParticipants;
    DatabaseReference meetingsReference;
    DatabaseReference usersReference;
    
    String userEmail, userName;
    ArrayList<User> allUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_meeting);

        meetingNameField = findViewById(R.id.meetingNameField);
        meetingDescriptionField = findViewById(R.id.meetingDescriptionField);
        dateField = findViewById(R.id.dateField);
        timeField = findViewById(R.id.timeField);
        btnSelectParticipants = findViewById(R.id.btnSelectParticipants);
        btnLaunchMeeting = findViewById(R.id.btnLaunchMeeting);

        calendar = Calendar.getInstance();
        selectedParticipants = new ArrayList<>();
        allUsers = new ArrayList<>();

        // Get user info from intent
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("USER_EMAIL");
        userName = intent.getStringExtra("USER_NAME");

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
        meetingsReference = database.getReference("MeetingRequests");
        usersReference = database.getReference("Users");

        loadUsers();

        dateField.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    LaunchMeetingActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        dateField.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        timeField.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    LaunchMeetingActivity.this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        
                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        timeField.setText(timeFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false 
            );
            timePickerDialog.show();
        });

        btnSelectParticipants.setOnClickListener(v -> {
            showParticipantSelectionDialog();
        });

        btnLaunchMeeting.setOnClickListener(v -> {
            String meetingName = meetingNameField.getText().toString().trim();
            String description = meetingDescriptionField.getText().toString().trim();
            String date = dateField.getText().toString().trim();
            String time = timeField.getText().toString().trim();

            if (meetingName.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            saveMeetingRequest(meetingName, description, date, time);
        });
    }

    private void loadUsers() {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && !user.getEmail().equals(userEmail)) {
                        allUsers.add(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LaunchMeetingActivity.this, 
                    "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showParticipantSelectionDialog() {
        if (allUsers.isEmpty()) {
            Toast.makeText(this, "No users available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] userNames = new String[allUsers.size()];
        boolean[] checkedItems = new boolean[allUsers.size()];
        
        for (int i = 0; i < allUsers.size(); i++) {
            User user = allUsers.get(i);
            userNames[i] = user.getName() + " (" + user.getEmail() + ")";
            checkedItems[i] = selectedParticipants.contains(userNames[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Participants");
        
        builder.setMultiChoiceItems(userNames, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });

        builder.setPositiveButton("Done", (dialog, which) -> {
            selectedParticipants.clear();
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    selectedParticipants.add(userNames[i]);
                }
            }
            
            if (selectedParticipants.isEmpty()) {
                Toast.makeText(LaunchMeetingActivity.this, 
                    "No participants selected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LaunchMeetingActivity.this, 
                    selectedParticipants.size() + " participant(s) selected", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void saveMeetingRequest(String meetingName, String description, String date, String time) {
        String meetingId = meetingsReference.push().getKey();
        
        if (meetingId != null) {
            Meeting meeting = new Meeting(
                meetingId,
                meetingName,
                description,
                date,
                time,
                userEmail,
                userName,
                selectedParticipants,
                "pending"
            );

            meetingsReference.child(meetingId).setValue(meeting).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LaunchMeetingActivity.this, 
                        "Meeting request sent to admin for approval", 
                        Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(LaunchMeetingActivity.this, 
                        "Failed to send meeting request", 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
