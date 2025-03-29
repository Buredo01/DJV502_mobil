package com.example.massagebookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.massagebookingapp.R;


import com.example.massagebookingapp.services.NetworkUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final int SECRET_KEY = 666;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView userTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Internetkapcsolat ellenőrzése
        if (!NetworkUtils.isInternetAvailable(this)) {
            NetworkUtils.showNoInternetDialog(this);
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userTextView = findViewById(R.id.userTextView);

        findViewById(R.id.menuButton).setOnClickListener(this::showMenu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            loadUserData(currentUser.getUid());
        }
    }

    // Dropdown Menü megjelenítése
    public void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_items, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
        popupMenu.show();
    }

    // Menü elem kiválasztása
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.menu_profile){
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }else if(item.getItemId() == R.id.menu_appointments){
            startActivity(new Intent(this, AppointmentsActivity.class));
            return true;
        }else if(item.getItemId() == R.id.menu_booking){
            startActivity(new Intent(this, BookingActivity.class));
            return true;
        }else {
            return false;
        }
    }

    private void loadUserData(String userId) {
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String username = document.getString("username");
                    userTextView.setText("Kedves, " + username + "!");
                }
            }
        });
    }

    public void toBooking(View view){
        startActivity(new Intent(this, BookingActivity.class));
    }
}