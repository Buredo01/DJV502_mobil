package com.example.massagebookingapp;

import android.Manifest;
import android.annotation.SuppressLint;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.massagebookingapp.model.Appointment;
import com.example.massagebookingapp.services.AlarmReceiver;
import com.example.massagebookingapp.services.NotificationHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Date;

import android.animation.ObjectAnimator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;


public class BookingActivity extends AppCompatActivity {
    private LinearLayout calendarContainer;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private NotificationHelper notificationHelper;

    private AlarmManager myAlarm;

    private final int[] hours = {8, 9, 10, 11, 12, 13, 14, 15, 16, 23};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        calendarContainer = findViewById(R.id.calendarContainer);

        // Engedély kérése
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101);
            }
        }

        notificationHelper = new NotificationHelper(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                requestExactAlarmPermission();

                Toast.makeText(this, "Engedélyezd a pontos riasztásokat a beállításokban!", Toast.LENGTH_LONG).show();
            }
        }
        myAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Aktuális dátum kiszámítása
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int today = calendar.get(Calendar.DAY_OF_WEEK);


        if (today == Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        } else if (today == Calendar.SATURDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 2);
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, Calendar.MONDAY - today);
        }

        // Formázás a megfelelő megjelenítéshez
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

        // Napok és időpontok lekérése
        String[] dayNames = {"Hétfő", "Kedd", "Szerda", "Csütörtök", "Péntek"};

        for (int i = 0; i < 5; i++) {
            TextView dayHeader = new TextView(this);

            // Megjelenítés
            dayHeader.setText(dayNames[i] + " - " + sdf.format(calendar.getTime()));
            dayHeader.setTextSize(18);
            dayHeader.setPadding(16, 16, 16, 8);
            calendarContainer.addView(dayHeader);

            for (int hour : hours) {
                Button timeSlot = new Button(this);
                Calendar slotCalendar = (Calendar) calendar.clone();
                slotCalendar.set(Calendar.HOUR_OF_DAY, hour);
                slotCalendar.set(Calendar.MINUTE, 0);
                slotCalendar.set(Calendar.SECOND, 0);
                slotCalendar.setTimeZone(TimeZone.getDefault());

                Timestamp slotTimestamp = new Timestamp(slotCalendar.getTime());

                timeSlot.setText(hour + ":00");
                timeSlot.setTag(slotTimestamp);

                if (slotCalendar.before(Calendar.getInstance())) {
                    timeSlot.setEnabled(false); // Disable the past time slots
                    timeSlot.setTextColor(ColorStateList.valueOf(getResources().getColor(android.R.color.white)));
                    timeSlot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.inaktiv_hatter)));
                } else {
                    // Foglalási állapot ellenőrzése és beállítása
                    checkIfSlotIsBooked(slotTimestamp, timeSlot);
                }

                timeSlot.setOnClickListener(this::onTimeSlotClick);

                calendarContainer.addView(timeSlot);
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Értesítések engedélyezve!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Értesítések letiltva!", Toast.LENGTH_SHORT).show();
            }
        }
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


    public void onTimeSlotClick(View view) {
        Button button = (Button) view;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1.0f, 1.2f, 1.0f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.start();
        scaleY.start();

        Timestamp slotTimestamp = (Timestamp) button.getTag(); // Timestamp helyesen lekérése

        new AlertDialog.Builder(this)
                .setTitle("Foglalás megerősítése")
                .setMessage("Biztosan lefoglalja ezt az időpontot?")
                .setPositiveButton("Igen", (dialog, which) -> bookAppointment(slotTimestamp, button))
                .setNegativeButton("Mégse", null)
                .show();
    }

    @SuppressLint("SetTextI18n")
    private void checkIfSlotIsBooked(Timestamp slotTimestamp, Button button) {
        Timestamp roundedSlotTimestamp = roundToNearestHour(slotTimestamp); // Round the slot time

        db.collection("appointments")
                .whereEqualTo("appointmentTimestamp", roundedSlotTimestamp) // Query by rounded Timestamp
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // If there is a document that matches the Timestamp
                        button.setText("Foglalt");
                        button.setEnabled(false);
                        button.setTextColor(ColorStateList.valueOf(getResources().getColor(android.R.color.white)));
                        button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
                        Log.d("BookingActivity", "Adott és kapott timestamp egyezik: " + roundedSlotTimestamp);
                    } else {
                        button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_dark)));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingActivity.this, "Hiba történt az időpont ellenőrzésekor.", Toast.LENGTH_SHORT).show();
                    Log.e("BookingActivity", "Error checking slot availability", e);
                });
    }



    private void bookAppointment(Timestamp slotTimestamp, Button button) {
        String userId = mAuth.getCurrentUser().getUid();
        String appointmentId = db.collection("appointments").document().getId();
        Timestamp createdAt = new Timestamp(new Date());

        Appointment appointment = new Appointment(appointmentId, userId, roundToNearestHour(slotTimestamp), createdAt);

        db.collection("appointments").document(appointmentId).set(appointment)
                .addOnSuccessListener(aVoid -> {
                    button.setEnabled(false);
                    button.setText("Foglalt");

                    // Színváltó animáció zöldről pirosra
                    int colorFrom = getResources().getColor(android.R.color.holo_green_dark);
                    int colorTo = getResources().getColor(android.R.color.holo_red_dark);
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                    colorAnimation.setDuration(500);
                    colorAnimation.addUpdateListener(animator ->
                            button.setBackgroundTintList(ColorStateList.valueOf((int) animator.getAnimatedValue()))
                    );
                    colorAnimation.start();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getDefault());
                    String localTime = sdf.format(slotTimestamp.toDate());

                    new AlertDialog.Builder(this)
                            .setTitle("Sikeres foglalás")
                            .setMessage("Időpont: " + localTime)
                            .setNeutralButton("Emlékeztető állítás", (dialog, which) -> setAlarm(slotTimestamp, 30))
                            .setPositiveButton("Ok", null)
                            .show();

                    notificationHelper.showNotification("Sikeres foglalás!\nIdőpont: " + localTime);
                    refreshBookingStatus();
                })
                .addOnFailureListener(e -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Hiba")
                            .setMessage("A foglalás nem sikerült.")
                            .setPositiveButton("Ok", null)
                            .show();
                });
    }



    private void refreshBookingStatus() {
        for (int i = 0; i < calendarContainer.getChildCount(); i++) {
            View view = calendarContainer.getChildAt(i);
            if (view instanceof Button) {
                Button button = (Button) view;
                Timestamp slotTimestamp = (Timestamp) button.getTag();
                if(button.getBackgroundTintList() != ColorStateList.valueOf(getResources().getColor(R.color.inaktiv_hatter))){
                    checkIfSlotIsBooked(slotTimestamp, button);
                }
            }
        }
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Pontosan ütemezett ébresztés szükséges")
                        .setMessage("A foglalási emlékeztetőhöz pontosan ütemezett riasztások engedélyezése szükséges.")
                        .setPositiveButton("Beállítások", (dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("Mégse", null)
                        .show();
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public void setAlarm(Timestamp slotTimestamp, long minutesBefore) {
        // Az időpont előtti emlékeztető beállítása
        long reminderTime = slotTimestamp.toDate().getTime() - minutesBefore * 60 * 1000;

        Intent intent  = new Intent(this, AlarmReceiver.class);
        int requestCode = (int) (slotTimestamp.toDate().getTime() / 1000);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        myAlarm.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
        );

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
        String reminderTimeFormatted = sdf.format(reminderTime);

        Toast.makeText(this, "Emlékeztető beállítva erre az időpontra: " + reminderTimeFormatted, Toast.LENGTH_LONG).show();

    }

    public void backtoMain(View view) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}