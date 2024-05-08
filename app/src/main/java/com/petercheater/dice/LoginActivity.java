package com.petercheater.dice;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText username, password;
    Button login, signup;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // bindings
        username = findViewById(R.id.et_username);
        password = findViewById(R.id.et_password);
        login = findViewById(R.id.btn_login);
        signup = findViewById(R.id.btn_signUp);

        db = openOrCreateDatabase("DiceGame", MODE_PRIVATE, null);
        // db.execSQL("DROP TABLE Player");
        db.execSQL("CREATE TABLE IF NOT EXISTS Player (Id INTEGER PRIMARY KEY AUTOINCREMENT, Name TEXT, Password TEXT, Chips INTEGER, Wins INTEGER, Losses INTEGER)");

        login.setOnClickListener(v -> {
            final String usernameStr = username.getText().toString();
            final String passwordStr = password.getText().toString();

            if (usernameStr.isEmpty() || passwordStr.isEmpty()) {
                Toast.makeText(this, "Please provide both fields!", Toast.LENGTH_SHORT).show();
            } else {
                try (final Cursor c = db.rawQuery("SELECT * FROM Player WHERE Name = '" + usernameStr + "' AND Password = '" + passwordStr + "'", null)) {
                    if (c.getCount() > 0) {
                        c.moveToFirst(); // Move the cursor to the first row
                        final String uName = c.getString(1);
                        final int chips = Integer.parseInt(c.getString(3));
                        final int wins = Integer.parseInt(c.getString(4));
                        final int losses = Integer.parseInt(c.getString(5));
                        startGame(uName, chips, wins, losses); // Start the game after log-in
                    } else {
                        // User not found in the database or incorrect username/password
                        Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        signup.setOnClickListener(v -> {
            final String usernameStr = username.getText().toString();
            final String passwordStr = password.getText().toString();

            if (usernameStr.isEmpty() || passwordStr.isEmpty()) {
                Toast.makeText(this, "Please provide both fields!", Toast.LENGTH_SHORT).show();
            } else {
                if (usernameStr.length() < 3) {
                    Toast.makeText(this, "Username should be at least 3 characters long !", Toast.LENGTH_SHORT).show();
                }
                else if (usernameStr.length() > 15) {
                    Toast.makeText(this, "Username cannot exceed 15 characters !", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (!playerExists(usernameStr, passwordStr)) {
                        db.execSQL("INSERT INTO Player (Name, Password, Chips, Wins, Losses) VALUES ('" + usernameStr + "', '" + passwordStr + "', 0, 0, 0)");
                        Toast.makeText(this, String.format("Welcome, %s.", usernameStr), Toast.LENGTH_LONG).show();
                        startGame(usernameStr, 0, 0, 0); // Start the game after sign-up
                    } else {
                        Toast.makeText(this, "User already exists, did you mean to log in ?", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private boolean playerExists(final String username, final String secret) {
        try (final Cursor c = db.rawQuery("SELECT * FROM Player WHERE Name = '" + username + "' AND Password = '" + secret + "'", null)) {
            return c.moveToFirst(); // if the cursor has at least one row, the player exists in the database
        }
    }

    private void startGame(final String username, final int chips, final int wins, final int losses) {
        final Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("username", username);
        i.putExtra("chips", chips);
        i.putExtra("wins", wins);
        i.putExtra("losses", losses);
        startActivity(i); // launch MainActivity
        this.username.setText("");
        this.password.setText("");
    }
}