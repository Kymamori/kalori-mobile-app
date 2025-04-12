package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class Register extends AppCompatActivity{
    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private TextView loginTextView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Fade-in animáció
        View rootView = findViewById(android.R.id.content);
        rootView.setAlpha(0f);
        rootView.animate().alpha(1f).setDuration(800);

        // FirebaseAuth inicializálása
        mAuth = FirebaseAuth.getInstance();

        // UI elemek
        emailEditText = findViewById(R.id.emailEditText);
        emailEditText.requestFocus();
        passwordEditText = findViewById(R.id.passwordEditText);
        //passwordEditText.requestFocus();
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);

        // Regisztráció gomb kattintása
        registerButton.setOnClickListener(v -> registerUser());

        // Login képernyőre navigálás
        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        });
    }

    // Felhasználó regisztrálása
    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Ellenőrizzük, hogy az email és a jelszó nem üres
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Register.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase regisztráció
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // A regisztráció sikeres
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        // Itt továbbléphetsz a bejelentkezett képernyőre
                        Intent intent = new Intent(Register.this, Login.class);
                        startActivity(intent);
                        finish();  // Bezárja a regisztrációs képernyőt

                    } else {
                        // A regisztráció nem sikerült
                        Toast.makeText(Register.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
