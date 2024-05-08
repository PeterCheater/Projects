package com.petercheater.dice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class StatsActivity extends AppCompatActivity {
    TextView stats;
    Button goBack, resetStats;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        stats = findViewById(R.id.tv_stats);
        goBack = findViewById(R.id.btn_goBack);
        resetStats = findViewById(R.id.btn_reset);

        final Intent intent = getIntent();
        final int chips = intent.getIntExtra("chips", 0);
        final int wins = intent.getIntExtra("wins", 0);
        final int losses = intent.getIntExtra("losses", 0);
        final double BetsAverage = intent.getDoubleExtra("betsAverage", 0.0);
        final double ratio = (double) wins / ((losses == 0)? 1 : losses);
        final String ratioDescription = String.format(ratio >= 1 ? "(very good)" : ratio > 0.8 ? "(good)" : ratio >= 0.7 ? "(not bad)" : ratio >= 0.6 ? "(above average)" : ratio == 0.5 ? "(average)" : ratio >= 0.4 ? "(below average)" : ratio >= 0.2 ? "(poor)" : "");
        stats.setText(String.format("- Chips left: %d\n- Wins: %d\n- Losses: %d\n- W/L ratio: %.2f %s\n- Average bet: %.2f", chips, wins, losses, ratio, ratioDescription, BetsAverage));

        resetStats.setEnabled(chips != 100 || wins != 0 || losses != 0);

        goBack.setOnClickListener(v -> finish());

        resetStats.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to reset your stats?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        resetPlayerStats(); // user confirmed, reset player's stats
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // User canceled, do nothing
                    })
                    .show();
        });
    }

    private void resetPlayerStats() {
        Intent i = new Intent();
        i.putExtra("resetStats", true);
        setResult(Activity.RESULT_OK, i);
        finish(); // Finish the StatsActivity and return to MainActivity
    }
}
