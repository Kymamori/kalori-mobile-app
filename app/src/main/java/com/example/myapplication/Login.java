package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Fade-in animáció
        View rootView = findViewById(android.R.id.content);
        rootView.setAlpha(0f);
        rootView.animate().alpha(1f).setDuration(800);

        // Firebase Authentication inicializálása
        mAuth = FirebaseAuth.getInstance();

        // UI elemek
        emailEditText = findViewById(R.id.emailEditText);
        emailEditText.requestFocus();
        passwordEditText = findViewById(R.id.passwordEditText);
        //passwordEditText.requestFocus();
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        // Bejelentkezés gomb kattintásának kezelése
        loginButton.setOnClickListener(v -> loginUser());

        // Regisztrációs képernyőre navigálás
        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(emailEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Ellenőrizzük, hogy az email és a jelszó nem üres
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Login.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bejelentkezés Firebase Authentication-nel
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // A bejelentkezés sikeres
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();

                        // Itt átirányíthatod a felhasználót a főoldalra vagy más képernyőre
                        Intent intent = new Intent(Login.this, HomePage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // A bejelentkezés nem sikerült
                        Toast.makeText(Login.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
