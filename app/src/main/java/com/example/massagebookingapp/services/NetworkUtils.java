package com.example.massagebookingapp.services;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkUtils {

    // Ellenőrzi, hogy van-e internetkapcsolat
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void showInternetDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Internet elérhetőség")
                .setMessage("Az alkalmazás internetkapcsolatot használ!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Internet hozzáférés engedélyezve!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public static void showNoInternetDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Internet elérhetőség")
                .setMessage("Az alkalmazás internetkapcsolatot igényel. Kérlek, csatlakozz az internethez.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Internet hozzáférés engedélyezve!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
