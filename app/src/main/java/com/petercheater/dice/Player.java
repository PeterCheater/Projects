package com.petercheater.dice;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Player {
    private final String name;
    private int chips, wins, losses;
    private final ArrayList<Integer> bets = new ArrayList<>(); // ArrayList to store bet amounts

    public Player(final String name, final int chips, final int wins, final int losses) {
        this.name = name;
        this.chips = chips;
        this.wins = wins;
        this.losses = losses;
    }

    public void addBetAmount(final int bet) {
        bets.add(bet);
    }

    public double AverageBetAmount() {
        if (bets.isEmpty()) return 0.0; // Return 0 if no bets made yet

        int total = 0;
        for (final int bet : bets) {
            total += bet;
        }

        return (double) total / bets.size();
    }


    public String getName() {
        return name;
    }

    public int getChips() {
        return chips;
    }

    public void setChips(final int chips) {
        this.chips = chips;
    }

    public int getWins() {
        return wins;
    }

    public void won(final int betAmount) {
        setChips(getChips() + (betAmount * 2));
        this.wins++;
    }

    public void lost(final int betAmount) {
        setChips(getChips() - betAmount); // subtract chips on loss
        this.losses++;
    }

    public int getLosses() {
        return losses;
    }

    public void reset() {
        this.chips = 100;
        this.wins = 0;
        this.losses = 0;
        bets.clear();
    }
}
