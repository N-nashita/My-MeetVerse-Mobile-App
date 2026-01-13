# MeetVerse Android App

A comprehensive meeting management Android application built with Java and Firebase, designed to streamline meeting scheduling, approval workflows, and participant management.

## Features

### User Roles

- **Admin**: Full control over meeting approvals, user management, and system settings
- **User**: Can launch meeting requests, view approved meetings, and manage their scheduled meetings

### Core Functionality

#### For Admins

- **Meeting Request Management**: Review and approve/reject meeting requests
- **User Management**:
  - View all registered users
  - Promote users to admin role
  - Remove admin privileges (with protection to keep at least one admin)
  - Delete regular users
- **Dashboard**: View all approved meetings with live countdown timers
- **Settings**: Configure admin roles and manage system users
- **Meeting History**: Track all past meetings with their status (completed/rejected)

#### For Users

- **Launch Meetings**:
  - Create meeting requests with details (name, description, date, time)
  - Add multiple participants via email
  - Submit for admin approval
- **Dashboard**: View approved meetings where they are participants
- **Meeting Access**: Get meeting links for approved meetings
- **Cancel Meetings**: Request cancellation of scheduled meetings
- **Meeting History**: View past meeting records

### Security Features

- **Firebase Authentication**: Secure password management without storing passwords in database
- **Role-Based Access Control**: Different permissions for admins and users
- **First User Admin**: The first registered user automatically becomes an admin
- **Admin Protection**: System prevents deletion of the last admin

### Smart Features

- **Auto-Expiry**: Meetings automatically move to history after their scheduled time
- **Live Countdown**: Real-time countdown timer for upcoming meetings
- **Meeting Links**: Automatic generation of unique meeting links upon approval
- **Empty States**: Clear messages when no meetings or history exists
- **Participant Management**: Only meeting participants can see meeting links

## Technology Stack

- **Language**: Java
- **Database**: Firebase Realtime Database
- **Authentication**: Firebase Authentication
- **Architecture**: Android SDK with Activity-based navigation

## Firebase Structure

```
MyMeetVerse
├── Users/
│   ├── {userId}/
│   │   ├── name
│   │   ├── email
│   │   ├── role (Admin/User)
│   │   └── userId
├── MeetingRequests/
│   ├── {meetingId}/
│   │   ├── meetingName
│   │   ├── description
│   │   ├── date
│   │   ├── time
│   │   ├── requestedBy
│   │   ├── requestedByName
│   │   ├── participants (array)
│   │   └── status (pending/approved/rejected)
├── ApprovedMeetings/
│   └── {meetingId}/
│       ├── [same as MeetingRequests]
│       └── meetingLink
└── MeetingHistory/
    └── {meetingId}/
        ├── [same as ApprovedMeetings]
        └── timestamp
```

## Setup Instructions

### Prerequisites

- Android Studio (Latest version recommended)
- Android SDK (API Level 21+)
- Firebase account
- Java Development Kit (JDK) 8 or higher

### Installation

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd MyMeetVerse
   ```

2. **Firebase Configuration**

   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Add an Android app to your Firebase project
   - Download `google-services.json` and place it in `app/` directory
   - Enable Firebase Realtime Database:
     - Go to Realtime Database in Firebase Console
     - Create database in `asia-southeast1` region
     - Start in test mode (configure rules later)
   - Enable Firebase Authentication:
     - Go to Authentication → Sign-in method
     - Enable Email/Password authentication

3. **Update Firebase URL**

   - The app uses: `https://my-meetverse-app-default-rtdb.asia-southeast1.firebasedatabase.app/`
   - Update this URL in all Activity files if your Firebase URL is different

4. **Build the Project**

   - Open project in Android Studio
   - Sync Gradle files
   - Build → Make Project

5. **Run the App**
   - Connect an Android device or start an emulator
   - Click Run button in Android Studio

### First Time Setup

1. **Launch the app** and navigate to the signup page
2. **Register the first user** - this user will automatically become the admin
3. **Additional users** can register and will have regular user roles
4. **Admins can promote** other users to admin from Settings

## Usage Guide

### For Admins

1. **Approving Meetings**:

   - Navigate to Meeting Requests from the menu
   - Review pending requests
   - Click Approve to generate meeting link or Reject to decline

2. **Managing Users**:

   - Go to Settings
   - Use "Edit Users" to delete regular users
   - Use "Change Admin" to promote users to admin
   - Use "Remove Admin" to demote admins (keeps at least one admin)

3. **Viewing Meetings**:
   - Dashboard shows all approved meetings
   - History shows all past/rejected meetings

### For Users

1. **Launching a Meeting**:

   - Click Launch Meeting from menu
   - Fill in meeting details
   - Add participant emails (comma-separated)
   - Submit for approval

2. **Viewing Meetings**:

   - Dashboard shows meetings where you're a participant
   - Click to copy meeting link when approved

3. **Canceling Meetings**:
   - Go to Cancel Meeting
   - Select your meeting
   - Request cancellation

## App Structure

```
app/src/main/java/com/example/mymeetverse/
├── AdminDashboardActivity.java      # Admin home screen
├── UserdashboardActivity.java       # User home screen
├── LoginActivity.java               # User login
├── SignupActivity.java              # User registration
├── LaunchMeetingActivity.java       # Create meeting requests
├── MeetingRequestsActivity.java     # Admin approval interface
├── MeetingHistoryActivity.java      # Past meetings view
├── CancelMeetingActivity.java       # Meeting cancellation
├── SettingsActivity.java            # Admin settings & user management
├── SignedInUsersActivity.java       # View all users
├── Meeting.java                     # Meeting data model
└── User.java                        # User data model
```

## Key Features Implementation

### Auto-Expiry System

- Background checks compare current time with meeting time
- Expired meetings automatically transfer to history
- Countdown timers update in real-time

### Role-Based Navigation

- Dynamic menu based on user role
- Admin-only features hidden from regular users
- Seamless role promotion updates

### Participant System

- Email-based participant identification
- Only participants see meeting links
- Meeting link visibility control

## Security Considerations

- Passwords managed by Firebase Authentication
- No plain-text passwords in database
- Role-based access control
- Protected admin deletion
- Secure meeting link generation

## Contributing

When contributing to this project:

1. Follow existing code style
2. Test all features thoroughly
3. Update documentation for new features
4. Ensure Firebase rules are properly configured

## License

This project is developed as an educational application for meeting management.

## Support

For issues or questions, please open an issue in the repository.

---

**Built with ❤️ using Android Studio and Firebase**
