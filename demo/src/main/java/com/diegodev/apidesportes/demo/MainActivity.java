package com.diegodev.apidesportes.demo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.diegodev.apidesportes.jogos.ActivityEsporte;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ApiEsporteBrPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String TOKEN_TESTE = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3NLZXkiOiIxMjM0NTY3OCIsImlhdCI6MTc4MjQ3NjUxNH0.KWXLilJaDyqgj3e18jpZR1rDxxg5yJFtVZKrOOEBWB8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editToken = findViewById(R.id.editToken);
        Button btnAbrir = findViewById(R.id.btnAbrir);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String saved = prefs.getString(KEY_TOKEN, "");
        editToken.setText(!saved.isEmpty() ? saved : TOKEN_TESTE);

        btnAbrir.setOnClickListener(v -> {
            String token = editToken.getText().toString().trim();
            if (TextUtils.isEmpty(token)) {
                Toast.makeText(this, "Informe um token valido", Toast.LENGTH_LONG).show();
                return;
            }
            prefs.edit().putString(KEY_TOKEN, token).apply();
            startActivity(new Intent(this, ActivityEsporte.class));
        });
    }
}
