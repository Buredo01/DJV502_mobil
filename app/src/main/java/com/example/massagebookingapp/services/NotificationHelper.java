package com.example.massagebookingapp.services;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.massagebookingapp.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "appointment_channel";

    private NotificationManager mManager;
    private Context context;

    public NotificationHelper(Context context){
        this.context=context;
        this.mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Időpont Értesítések",
                    NotificationManager.IMPORTANCE_HIGH  // Fontosabb szintre állítva
            );
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.RED);
            channel.setDescription("Értesítések az időpontokról");

            this.mManager.createNotificationChannel(channel);
        }
    }

    public void showNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Foglalás visszaigazolás")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // Magas prioritás, hogy mindig megjelenjen
                .setDefaults(Notification.DEFAULT_ALL)  // Hang és rezgés beállítása
                .setAutoCancel(true);

        // Ellenőrizzük az engedélyt
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;  // Ha nincs engedély, ne küldjük az értesítést
        }

        this.mManager.notify(1, builder.build());
    }

    public void showDeleteNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Törlés visszaigazolás")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // Magas prioritás, hogy mindig megjelenjen
                .setDefaults(Notification.DEFAULT_ALL)  // Hang és rezgés beállítása
                .setAutoCancel(true);

        // Ellenőrizzük az engedélyt
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;  // Ha nincs engedély, ne küldjük az értesítést
        }

        this.mManager.notify(1, builder.build());
    }

    public void showReminderNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Emlékeztető időpontról")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // Magas prioritás, hogy mindig megjelenjen
                .setDefaults(Notification.DEFAULT_ALL)  // Hang és rezgés beállítása
                .setAutoCancel(true);

        // Ellenőrizzük az engedélyt
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;  // Ha nincs engedély, ne küldjük az értesítést
        }

        this.mManager.notify(1, builder.build());
    }
}

