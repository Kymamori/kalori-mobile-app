package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.MealAdapter;
import com.example.myapplication.alarm.AlarmManagerHelper;
import com.example.myapplication.models.Meal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity implements MealAdapter.OnMealClickListener {

    private RecyclerView recyclerView;
    private List<Meal> meals = new ArrayList<>();
    private MealAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private List<String> docIds = new ArrayList<>(); // To store document IDs for updates
    private boolean animateFirstMeal = true; // Flag to animate only the first meal on login

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        // Create the notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "meal_channel",
                    "Meal Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        recyclerView = findViewById(R.id.mealsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MealAdapter(meals, docIds, this, true);  // Pass the listener to the adapter
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            fetchMeals();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        // Floating button for adding meals
        FloatingActionButton addMealFab = findViewById(R.id.addMealFab);
        addMealFab.setOnClickListener(v -> showAddMealDialog());
    }

    @Override
    protected void onStart() {
        super.onStart();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            fetchMeals();
        } else {
            // Ha a felhasználó nincs bejelentkezve, akkor ne próbáljunk meg adatokat lekérni
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        // Android 13+ (API 33) permission handling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Ellenőrizzük, hogy a POST_NOTIFICATIONS permission engedélyezve van-e
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Ha nincs engedélyezve, kérjük meg a felhasználót
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                // Ha már van engedély, folytathatjuk a műveleteket (pl. értesítéseket küldhetünk)
                Toast.makeText(this, "Notification permission already granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Engedély megadva, most már küldhetünk értesítést
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                // Engedély elutasítva
                Toast.makeText(this, "Notification permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Fetch meals from Firestore
    private void fetchMeals() {
        db.collection("users")
                .document(user.getUid())
                .collection("meals")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    meals.clear();
                    docIds.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Meal meal = doc.toObject(Meal.class);
                        meals.add(meal);
                        docIds.add(doc.getId()); // Add document ID for updates
                    }
                    adapter.notifyDataSetChanged();

                    // Apply animation only to the first meal if it's a fresh login or new meal
                    if (animateFirstMeal && !meals.isEmpty()) {
                        adapter.setAnimateAll(true);  // Animate the first meal
                        animateFirstMeal = false;  // Prevent future animations for all meals
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading meals: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Show the dialog to add a new meal
    private void showAddMealDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_meal, null);
        EditText mealNameEditText = dialogView.findViewById(R.id.mealNameEditText);
        EditText caloriesEditText = dialogView.findViewById(R.id.caloriesEditText);

        new AlertDialog.Builder(this)
                .setTitle("Add Meal")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String mealName = mealNameEditText.getText().toString().trim();
                    String caloriesStr = caloriesEditText.getText().toString().trim();

                    if (mealName.isEmpty() || caloriesStr.isEmpty()) {
                        Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int calories;
                    try {
                        calories = Integer.parseInt(caloriesStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Calories must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser == null) {
                        Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Meal meal = new Meal(mealName, calories, Timestamp.now());
                    db.collection("users")
                            .document(user.getUid())
                            .collection("meals")
                            .add(meal)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Meal saved!", Toast.LENGTH_SHORT).show();

                                // Send notification after saving the meal
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "meal_channel")
                                        .setSmallIcon(R.drawable.ic_meal)
                                        .setContentTitle("Új étkezés hozzáadva")
                                        .setContentText("Étkezés: " + mealName)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                notificationManager.notify(1, builder.build());

                                fetchMeals(); // Refresh the meals list after saving
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error saving meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void deleteMeal(String docId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("users")
                .document(user.getUid())
                .collection("meals")
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Meal deleted!", Toast.LENGTH_SHORT).show();
                    fetchMeals(); // Refresh the meals list after deletion
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Show dialog to update meal
    @Override
    public void onMealClick(int position) {
        Meal meal = meals.get(position);
        String docId = docIds.get(position);

        // Infláljuk a dialógust
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_meal, null);
        EditText mealNameEditText = dialogView.findViewById(R.id.mealNameEditText);
        EditText caloriesEditText = dialogView.findViewById(R.id.caloriesEditText);

        // Pre-fill the fields with existing data
        mealNameEditText.setText(meal.getMealName());
        caloriesEditText.setText(String.valueOf(meal.getCalories()));

        // AlertDialog létrehozása a frissítéshez
        new AlertDialog.Builder(this)
                .setTitle("Update Meal")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String updatedMealName = mealNameEditText.getText().toString().trim();
                    String updatedCaloriesStr = caloriesEditText.getText().toString().trim();

                    if (updatedMealName.isEmpty() || updatedCaloriesStr.isEmpty()) {
                        Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int updatedCalories;
                    try {
                        updatedCalories = Integer.parseInt(updatedCaloriesStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Calories must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser == null) {
                        Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update meal in Firestore
                    db.collection("users")
                            .document(user.getUid())
                            .collection("meals")
                            .document(docId)
                            .update("mealName", updatedMealName, "calories", updatedCalories)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Meal updated!", Toast.LENGTH_SHORT).show();
                                fetchMeals(); // Refresh the meals list after updating
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error updating meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Delete", (dialog, which) -> {
                    // Handle delete
                    deleteMeal(docId);
                })
                .create()
                .show();
    }

    // Create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    // Handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logoutMenuItem) {
            AlarmManagerHelper.cancelAlarm(this);
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return true;
        } else if (item.getItemId() == R.id.over200MealsMenuItem) {
            // Indítjuk a 200 kalória feletti ételeket listázó activityt
            Intent intent = new Intent(this, MealsOver200.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStop() {
        super.onStop();

        // Cancel the AlarmManager when the activity is stopped (or when no longer needed)
        AlarmManagerHelper.cancelAlarm(this);
    }
}
