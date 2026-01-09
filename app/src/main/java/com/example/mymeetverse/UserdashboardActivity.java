package com.example.mymeetverse;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class UserdashboardActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView menuIcon;
    
    String userEmail, userName, userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdashboard);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuIcon = findViewById(R.id.menuIcon);

        // Get user info from intent
        Intent receivedIntent = getIntent();
        userEmail = receivedIntent.getStringExtra("USER_EMAIL");
        userName = receivedIntent.getStringExtra("USER_NAME");
        userRole = receivedIntent.getStringExtra("USER_ROLE");

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                
                if (id == R.id.nav_home) {
                    Toast.makeText(UserdashboardActivity.this, "Home", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_launch) {
                    Intent intent = new Intent(UserdashboardActivity.this, LaunchMeetingActivity.class);
                    intent.putExtra("USER_EMAIL", userEmail);
                    intent.putExtra("USER_NAME", userName);
                    startActivity(intent);
                } else if (id == R.id.nav_users) {
                    Toast.makeText(UserdashboardActivity.this, "Users", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_history) {
                    Toast.makeText(UserdashboardActivity.this, "History", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_logout) {
                    Toast.makeText(UserdashboardActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
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
}
