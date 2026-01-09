package com.example.mymeetverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MeetingRequestsActivity extends AppCompatActivity {

    ListView requestsListView;
    ArrayList<Meeting> meetingRequests;
    MeetingRequestAdapter adapter;
    DatabaseReference meetingsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_requests);

        requestsListView = findViewById(R.id.requestsListView);
        meetingRequests = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
        meetingsReference = database.getReference("MeetingRequests");

        adapter = new MeetingRequestAdapter();
        requestsListView.setAdapter(adapter);

        loadMeetingRequests();
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
        meetingsReference.child(meeting.getMeetingId()).child("status").setValue("approved")
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Meeting approved!", Toast.LENGTH_SHORT).show();
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
