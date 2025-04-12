package com.example.myapplication;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        EdgeToEdge.enable(this);
        setContentView(R.layout.homepage);

        // Fade-in animáció
        View rootView = findViewById(android.R.id.content);
        rootView.setAlpha(0f);
        rootView.animate().alpha(1f).setDuration(800);
    }
}
