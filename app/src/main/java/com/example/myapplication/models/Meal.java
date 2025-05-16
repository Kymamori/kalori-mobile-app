package com.example.myapplication.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Meal {
    private String mealName;
    private int calories;
    private Timestamp timestamp;

    public Meal() {}

    public Meal(String mealName, int calories, Timestamp timestamp) {
        this.mealName = mealName;
        this.calories = calories;
        this.timestamp = timestamp;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}

