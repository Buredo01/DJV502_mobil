package com.example.massagebookingapp;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.massagebookingapp.services.NetworkUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final int SECRET_KEY = 666;
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Internetkapcsolat ellenőrzése
        if (!NetworkUtils.isInternetAvailable(this)) {
            showNoInternetDialog();
        } else {
            NetworkUtils.showInternetDialog(this); // Ha van internet, tájékoztató dialógus
        }

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        mAuth = FirebaseAuth.getInstance();
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Nincs internetkapcsolat")
                .setMessage("Az alkalmazás internetkapcsolatot igényel. Kérjük, kapcsolja be az internetet.")
                .setPositiveButton("Beállítások", (dialog, which) -> {
                    // Nyisd meg az internet beállításokat
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Kilépés", (dialog, which) -> {
                    // Bezárhatjuk az alkalmazást, ha nem engedélyezi az internetet
                    finish();
                })
                .show();
    }

    public void loginUser(View view) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please enter email and password", Snackbar.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Hiba a bejelentkezésnél.", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    public void registUser(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("SECRET_KEY", 666);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
        startActivity(intent, options.toBundle());
    }
}