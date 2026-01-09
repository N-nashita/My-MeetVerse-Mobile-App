package com.example.mymeetverse;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class LaunchMeetingActivity extends AppCompatActivity {

    TextInputEditText meetingNameField, meetingDescriptionField, dateField, timeField;
    Button btnSelectParticipants, btnLaunchMeeting;
    ListView participantsListView;
    
    Calendar calendar;
    ArrayList<String> selectedParticipants;
    ArrayAdapter<String> participantsAdapter;
    DatabaseReference meetingsReference;
    
    String userEmail, userName;

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
        participantsListView = findViewById(R.id.participantsListView);

        calendar = Calendar.getInstance();
        selectedParticipants = new ArrayList<>();
        participantsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedParticipants);
        participantsListView.setAdapter(participantsAdapter);

        // Get user info from intent
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("USER_EMAIL");
        userName = intent.getStringExtra("USER_NAME");

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
        meetingsReference = database.getReference("MeetingRequests");

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
            Toast.makeText(this, "Select participants feature coming soon", Toast.LENGTH_SHORT).show();
            selectedParticipants.add("Sample User");
            participantsAdapter.notifyDataSetChanged();
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
