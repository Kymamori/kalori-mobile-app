package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.MealAdapter;
import com.example.myapplication.models.Meal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MealsOver200 extends AppCompatActivity implements MealAdapter.OnMealClickListener {

    private RecyclerView recyclerView;
    private MealAdapter mealAdapter;
    private List<Meal> meals = new ArrayList<>();
    private List<String> docIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meals_over_200);

        recyclerView = findViewById(R.id.mealsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        mealAdapter = new MealAdapter(meals, docIds, this, true); // Az Activity mint OnMealClickListener
        recyclerView.setAdapter(mealAdapter);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("meals")
                    .whereGreaterThan("calories", 200)
                    .orderBy("calories", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        meals.clear();
                        docIds.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Meal meal = doc.toObject(Meal.class);
                            meals.add(meal);
                            docIds.add(doc.getId());
                        }
                        mealAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        System.err.println("Hiba történt: " + e.getMessage());
                    });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logoutMenuItem) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return true;
        } else if (id == R.id.homeMenuItem) {
            Intent intent = new Intent(MealsOver200.this, HomePage.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMealClick(int position) {
        Meal clickedMeal = meals.get(position);
        System.out.println("Clicked meal: " + clickedMeal.getMealName());
    }
}
