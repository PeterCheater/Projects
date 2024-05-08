package com.petercheater.dice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Button roll, stats, logout;
    SeekBar bet_bar;
    TextView player_name, bet_amount, result;
    ImageView img1, img2;
    SQLiteDatabase db;
    MediaPlayer mp;
    Random random; // randomizer
    Player player;
    final int[] dice = {R.drawable.dice1, R.drawable.dice2, R.drawable.dice3, R.drawable.dice4, R.drawable.dice5, R.drawable.dice6}; // array to store dice images

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bindings
        result = findViewById(R.id.tv_result);
        player_name = findViewById(R.id.tv_playerName);
        bet_amount = findViewById(R.id.tv_betAmount);
        bet_bar = findViewById(R.id.sb_bet);
        img1 = findViewById(R.id.ivVar1);
        img2 = findViewById(R.id.ivVar2);
        roll = findViewById(R.id.btn_roll);
        stats = findViewById(R.id.btn_stats);
        logout = findViewById(R.id.btn_logout);

        db = openOrCreateDatabase("DiceGame", MODE_PRIVATE, null);
        mp = MediaPlayer.create(this, R.raw.dice_roll);

        Intent i = getIntent(); // Retrieve the Intent that started this activity

        // Retrieve the player's information from the Intent extras
        final String username = i.getStringExtra("username");
        int chips = i.getIntExtra("chips", 100);
        int wins = i.getIntExtra("wins", 0);
        int losses = i.getIntExtra("losses", 0);

        player = new Player(username, chips, wins, losses); // create a new instance with the values
        random = new Random();
        firstRun();

        roll.setOnClickListener(v -> {
            if (player.getChips() == 0) {
                Toast.makeText(this, String.format("You are out of chips. %s !", player.getName()), Toast.LENGTH_LONG).show();
            } else {
                roll.setText(R.string.rolling);
                roll.setEnabled(false);
                mp.start();
                animate();
                new Handler().postDelayed(() -> {
                    // This code will run after 3000 milliseconds
                    check(random.nextInt(6), random.nextInt(6));
                    roll.setText(R.string.roll_dice);
                    roll.setEnabled(true);
                }, 3000);
            }
        });

        stats.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatsActivity.class);
            intent.putExtra("chips", player.getChips());
            intent.putExtra("wins", player.getWins());
            intent.putExtra("losses", player.getLosses());
            intent.putExtra("betsAverage", player.AverageBetAmount());
            statsLauncher.launch(intent); // Start StatsActivity for result
        });

        logout.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        finish(); // user confirmed, return to the login form
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // User canceled, do nothing
                    })
                    .show();
        });

        bet_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int thumbImageId;

                if (progress <= 4) thumbImageId = R.drawable.chip_1;
                else if (progress <= 9) thumbImageId = R.drawable.chip_5;
                else if (progress <= 24) thumbImageId = R.drawable.chip_10;
                else if (progress <= 99) thumbImageId = R.drawable.chip_25;
                else thumbImageId = R.drawable.chip_100;

                seekBar.setThumb(ContextCompat.getDrawable(getApplicationContext(), thumbImageId)); // Set the thumb image
                bet_amount.setText(String.format("Bet amount: %d/%d%s", progress, player.getChips(), (progress == player.getChips()) ? "\nall in !" : ""));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void firstRun() {
        player_name.setText(player.getName()); // display the player name in the TextView
        bet_amount.setText(String.format("Bet amount: %d/%d", bet_bar.getProgress(), player.getChips()));
        bet_bar.setMax(player.getChips()); // update the maximum bet allowed according to the chips amount left
    }

    private void check(final int num1, final int num2) {
        img1.setImageResource(dice[num1]); // set the player's dice image to the corresponding value
        img2.setImageResource(dice[num2]); // set the CPU's dice image to the corresponding value
        final int betAmount = bet_bar.getProgress(); // read the bet amount from the slider
        player.addBetAmount(betAmount); // save the bet amount into the ArrayList
        if (num1 > num2) {
            player.won(betAmount); // add 1 win for this player
            displayResult("Winner: " + player.getName());
        } else if (num2 > num1) {
            player.lost(betAmount); // add 1 loss for this player
            displayResult("Winner: CPU");
        } else displayResult("Draw !");
        updateBetBar();
        updatePlayerRecord();
    }

    private void updateBetBar() {
        bet_bar.setMax(player.getChips()); // update the maximum bet allowed according to the chips amount left
        if (player.getChips() >= 2)
            bet_bar.setProgress(player.getChips() / 2); // set the slider to half the amount of chips left
        if (player.getChips() == 0)
            bet_amount.setText(R.string.bet_amount_null); // Bet amount: 0/0
    }

    private void animate() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            int count = 0;

            @Override
            public void run() {
                if (count < 20) {
                    // Change the images
                    img1.setImageResource(dice[random.nextInt(6)]);
                    img2.setImageResource(dice[random.nextInt(6)]);

                    count++;
                    handler.postDelayed(this, 125); // Schedule the next image change after the delay
                }
            }
        });
    }

    private void displayResult(final String msg) {
        result.setText(String.format("%s", msg));
        Toast.makeText(this, result.getText().toString(), Toast.LENGTH_LONG).show();
    }

    private void updatePlayerRecord() {

        // Use a SQLiteStatement for executing parameterized SQL queries to prevent SQL injection
        SQLiteStatement statement = db.compileStatement("UPDATE Player SET Chips = ?, Wins = ?, Losses = ? WHERE Name = ?");

        // Bind the parameters to the statement
        statement.bindLong(1, player.getChips());
        statement.bindLong(2, player.getWins());
        statement.bindLong(3, player.getLosses());
        statement.bindString(4, player.getName());

        statement.executeUpdateDelete(); // Execute the update statement
        statement.close();
    }

    private final ActivityResultLauncher<Intent> statsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getBooleanExtra("resetStats", false)) {
                        player.reset();
                        updatePlayerRecord();
                        firstRun();
                    }
                }
            }
    );
}
