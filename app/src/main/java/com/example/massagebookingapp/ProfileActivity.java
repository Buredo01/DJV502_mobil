package com.example.massagebookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView usernameTextView, emailTextView, phoneTextView;
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        profileImageView = findViewById(R.id.profileImageView);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
        }

        findViewById(R.id.editProfileButton).setOnClickListener(v -> showEditProfileDialog());
    }

    private void loadUserData(String userId) {
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    usernameTextView.setText(document.getString("username"));
                    emailTextView.setText(document.getString("email"));
                    phoneTextView.setText(document.getString("phone"));
                }
            }
        });
    }

    private void showEditProfileDialog() {
        EditText usernameEditText = new EditText(this);
        usernameEditText.setText(usernameTextView.getText().toString());
        usernameEditText.setHint("Új felhasználónév");

        EditText phoneEditText = new EditText(this);
        phoneEditText.setText(phoneTextView.getText().toString());
        phoneEditText.setHint("Új telefonszám");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(usernameEditText);
        layout.addView(phoneEditText);

        new AlertDialog.Builder(this)
                .setTitle("Adatok módosítása")
                .setView(layout)
                .setPositiveButton("Mentés", (dialog, which) -> {
                    String newUsername = usernameEditText.getText().toString();
                    String newPhone = phoneEditText.getText().toString();
                    updateUserData(newUsername, newPhone);
                })
                .setNegativeButton("Mégse", null)
                .show();
    }

    private void updateUserData(String newUsername, String newPhone) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            if (!TextUtils.isEmpty(newUsername) && !newUsername.equals(usernameTextView.getText().toString())) {
                updateFirestoreData(userId, "username", newUsername);
            }
            if (!TextUtils.isEmpty(newPhone) && !newPhone.equals(phoneTextView.getText().toString())) {
                updateFirestoreData(userId, "phone", newPhone);
            }
        }
    }

    private void updateFirestoreData(String userId, String field, String newValue) {
        db.collection("users").document(userId)
                .update(field, newValue)
                .addOnSuccessListener(aVoid -> {
                    loadUserData(userId);
                    Toast.makeText(this, "Adatok sikeresen frissítve!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt Firestore frissítéskor.", Toast.LENGTH_SHORT).show();
                });
    }

    public void onLogoutClick(View view) {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
