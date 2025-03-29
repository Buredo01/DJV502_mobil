package com.example.massagebookingapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.massagebookingapp.services.AlarmReceiver;
import com.example.massagebookingapp.services.NotificationHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AppointmentsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LinearLayout appointmentsContainer;

    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointments);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        appointmentsContainer = findViewById(R.id.appointmentsContainer);

        // Engedély kérése
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101);
            }
        }

        notificationHelper = new NotificationHelper(this);

        // Felhasználó foglalásainak lekérése
        fetchUserAppointments();
        fetchNextAppointment();
        fetchUserAppointmentsCount();
    }

    private void fetchUserAppointments() {
        String userId = mAuth.getCurrentUser().getUid();

        // Get current date at midnight (start of today)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();

        db.collection("appointments")
                .whereEqualTo("userId", userId)
                .orderBy("appointmentTimestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String appointmentId = document.getString("appointmentId");
                            Timestamp appointmentTimestamp = document.getTimestamp("appointmentTimestamp");

                            if (appointmentTimestamp != null) {
                                Date appointmentDate = appointmentTimestamp.toDate();
                                if (appointmentDate.after(today)) {
                                    TextView appointmentTextView = new TextView(this);
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                    appointmentTextView.setText("Időpont: " + sdf.format(appointmentDate));
                                    appointmentTextView.setTextSize(16);
                                    appointmentTextView.setPadding(16, 16, 16, 8);

                                    Button cancelButton = new Button(this);
                                    cancelButton.setText("Lemondás");
                                    cancelButton.setTextColor(ColorStateList.valueOf(getResources().getColor(android.R.color.white)));
                                    cancelButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
                                    cancelButton.setOnClickListener(v -> showCancelConfirmationDialog(appointmentId));

                                    appointmentsContainer.addView(appointmentTextView);
                                    appointmentsContainer.addView(cancelButton);
                                }
                            }
                        }
                    } else {
                        TextView noAppontment = new TextView(this);
                        noAppontment.setText("Nincsenek foglalásaid.");
                        appointmentsContainer.addView(noAppontment);
                        Toast.makeText(AppointmentsActivity.this, "Nincsenek foglalásaid.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AppointmentsActivity.this, "Hiba történt a foglalások lekérésekor.", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUserAppointmentsCount() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("appointments")
                .whereEqualTo("userId", userId)
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(countSnapshot -> {
                    long appointmentCount = countSnapshot.getCount();
                    TextView appointmentCountTextView = findViewById(R.id.appointmentCountTextView);
                    if (appointmentCountTextView != null) {
                        appointmentCountTextView.setText("Összes foglalásod: " + appointmentCount);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AppointmentsActivity.this, "Hiba történt a foglalások lekérésekor.", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchNextAppointment() {
        String userId = mAuth.getCurrentUser().getUid();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        TextView nextAppointmentTextView = findViewById(R.id.nextAppointmentTextView);
        nextAppointmentTextView.setText("Következő időpont: ");

        db.collection("appointments")
                .whereEqualTo("userId", userId) // Szűrés az aktuális felhasználóra
                .whereGreaterThanOrEqualTo("appointmentTimestamp", today) // Szűrés a mai nap vagy későbbi időpontokra
                .orderBy("appointmentTimestamp") // Rendezés a dátum alapján
                .limit(1) // Csak az első (legközelebbi) találatot kérjük le
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Timestamp appointmentTimestamp = document.getTimestamp("appointmentTimestamp");
                        if (appointmentTimestamp != null) {
                            Date nextAppointment = appointmentTimestamp.toDate();
                            nextAppointmentTextView.setText("Következő időpont: " + sdf.format(nextAppointment));
                        }
                    } else {
                        nextAppointmentTextView.setText("Nincs következő foglalás.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AppointmentsActivity.this, "Hiba történt a foglalások lekérésekor.", Toast.LENGTH_SHORT).show();
                });
    }


    private Timestamp roundToNearestHour(Timestamp timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp.toDate());

        // Round to nearest hour
        int minute = calendar.get(Calendar.MINUTE);
        if (minute >= 30) {
            calendar.add(Calendar.HOUR_OF_DAY, 1); // Round up
        }
        calendar.set(Calendar.MINUTE, 0); // Set minutes to 0
        calendar.set(Calendar.SECOND, 0); // Set seconds to 0
        calendar.set(Calendar.MILLISECOND, 0); // Set milliseconds to 0

        return new Timestamp(calendar.getTime());
    }



    private void showCancelConfirmationDialog(String appointmentId) {
        new AlertDialog.Builder(this)
                .setTitle("Foglalás lemondása")
                .setMessage("Biztosan le akarod mondani ezt a foglalást?")
                .setPositiveButton("Igen", (dialog, which) -> cancelAppointment(appointmentId))
                .setNegativeButton("Mégse", null)
                .show();
    }

    private void cancelAppointment(String appointmentId) {
        db.collection("appointments").document(appointmentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AppointmentsActivity.this, "Foglalás sikeresen lemondva.", Toast.LENGTH_SHORT).show();

                    // A foglalások listájának és a számlálónak a frissítése
                    refreshAppointmentsList();  // Újra lekérjük a foglalásokat
                    fetchUserAppointmentsCount(); // Frissítjük a foglalások számát
                    fetchNextAppointment(); // Frissítjük a következő időpontot

                    notificationHelper.showDeleteNotification("Foglalás sikeresen lemondva.");
                    cancelAlarm(appointmentId);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AppointmentsActivity.this, "Hiba történt a foglalás törlésekor.", Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelAlarm(String appointmentId) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, appointmentId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }


    private void refreshAppointmentsList() {
        appointmentsContainer.removeAllViews(); // Előző foglalások eltávolítása
        fetchUserAppointments(); // Újra lekérjük és megjelenítjük a foglalásokat
    }

    public void backtoMain(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}