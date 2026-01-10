package com.example.mymeetverse;

import java.util.ArrayList;

public class Meeting {
    private String meetingId;
    private String meetingName;
    private String description;
    private String date;
    private String time;
    private String requestedBy;
    private String requestedByName;
    private ArrayList<String> participants;
    private String status;
    private long timestamp;
    private String meetingLink;

    public Meeting() {}

    public Meeting(String meetingId, String meetingName, String description, String date, String time, 
                   String requestedBy, String requestedByName, ArrayList<String> participants, String status) {
        this.meetingId = meetingId;
        this.meetingName = meetingName;
        this.description = description;
        this.date = date;
        this.time = time;
        this.requestedBy = requestedBy;
        this.requestedByName = requestedByName;
        this.participants = participants;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getMeetingName() {
        return meetingName;
    }

    public void setMeetingName(String meetingName) {
        this.meetingName = meetingName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public ArrayList<String> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<String> participants) {
        this.participants = participants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }
}
