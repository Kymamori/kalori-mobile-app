package com.example.myapplication.alarm;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document("USER_ID")
                .collection("meals")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    Toast.makeText(context, "Meals synced!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error syncing meals: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

