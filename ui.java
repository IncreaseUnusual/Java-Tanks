package Tanks;

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.common.value.qual.ArrayLen;

import com.jogamp.opengl.math.VectorUtil;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import Tanks.projectile;
import Tanks.levelSetup;
import java.util.concurrent.TimeUnit;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import Tanks.tank;

import java.io.*;
import java.util.*;

/**
 * UI class for the Tanks game, responsible for displaying various UI components.
 */
public class ui extends PApplet {
    
    private PApplet p;
    private levelSetup level;
    PImage windImageRight;
    PImage windImageLeft;
    PImage fuel;
    ArrayList<Character> letters;
    PImage arrow;

    /**
     * Constructor for the ui class.
     * 
     * @param p     The PApplet instance.
     * @param level The levelSetup instance containing level data.
     */
    public ui(PApplet p, levelSetup level) {
        this.p = p;
        this.level = level;
        this.letters = this.level.players;
        Collections.sort(letters, new Comparator<Character>() {
            @Override
            public int compare(Character a, Character b) {
                return Character.compare(Character.toLowerCase(a), Character.toLowerCase(b));
            }
        });
    }

    /**
     * Draws the wind indicator on the screen.
     * 
     * @param wind The current wind value.
     */
    public void drawWind(int wind) {
        p.textAlign(RIGHT);
        p.textSize(15);
        this.windImageLeft.resize(50, 50);
        this.windImageRight.resize(50, 50);
        p.noStroke();
        p.fill(0, 0, 0);
        if (wind > 0) {
            p.image(this.windImageRight, 770, 7);
        } else if (wind < 0) {
            p.image(this.windImageLeft, 770, 7);
        }
        p.text(String.valueOf(abs(wind)), 845, 38);
        p.textAlign(LEFT);
    }

    /**
     * Draws the main UI components for the current turn.
     * 
     * @param turn The current player's turn index.
     */
    public void drawUi(int turn) {
        if (turn > this.level.tanks.size() - 1) {
            turn = this.level.tanks.size() - 1;
        }
        Character letterTurn = this.letters.get(turn);

        String message = "Player " + letterTurn + "'s turn";

        p.textAlign(RIGHT);
        p.textSize(15);
        p.noStroke();

        p.text(message, 110, 28);
        p.textAlign(LEFT);
        drawFuel(turn);
        drawScore();
        drawHealthBar(turn, 100);
    }

    /**
     * Draws the fuel indicator for the current player.
     * 
     * @param turn The current player's turn index.
     */
    public void drawFuel(int turn) {
        int fuel = this.level.tanks.get(turn).fuel;
        String fuelAMT = String.valueOf(fuel);
        this.fuel.resize(27, 27);
        p.image(this.fuel, 156, 10);
        p.textAlign(RIGHT);
        p.textSize(15);
        p.noStroke();

        p.text(fuelAMT, 220, 30);
        p.textAlign(LEFT);
    }

    /**
     * Draws the scores for all players.
     */
    public void drawScore() {
        // Define the position to start drawing scores
        int x = 850;
        int y = 80;
        int numOfPlayers = this.level.tanks.size();

        // Draw "Scores" text and underline
        p.textAlign(RIGHT);
        p.textSize(15);
        p.noStroke();
        p.fill(0);
        p.text("Scores", x - 24, y);
        p.stroke(0);
        p.line(x - 100, y + 4, x + 5, y + 4);
        p.line(x - 100, y - 15, x + 5, y - 15);  // Underline
        p.line(x - 100, y - 15, x - 100, y - 15 + numOfPlayers * 25);
        p.line(x + 5, y - 15, x + 5, y - 15 + numOfPlayers * 25);
        p.line(x - 100, y - 15 + numOfPlayers * 25, x + 5, y - 15 + numOfPlayers * 25);
        p.noStroke();

        // Draw player scores
        y += 20; // Move down for player scores
        x -= 1;
        for (int i = 0; i < this.level.tanks.size(); i++) {
            // Get the player's letter
            Character playerLetter = this.letters.get(i);
            
            // Get the player's score
            int playerScore = this.level.tanks.get(i).score;
            
            // Draw player's score
            String playerScoreText = "Player " + playerLetter + ": " + playerScore;
            p.fill(this.level.tanks.get(i).color);
            p.text(playerScoreText, x, y);
            
            // Move down for the next player's score
            y += 20;
        }
        p.textAlign(LEFT);
    }

    /**
     * Draws the health bar for the current player.
     * 
     * @param turn      The current player's turn index.
     * @param maxHealth The maximum health value.
     */
    public void drawHealthBar(int turn, int maxHealth) {
        int currentHealth = this.level.tanks.get(turn).health;
        int x = 320;
        int y = 30;
        int barWidth = 100; // Width of the health bar
        int barHeight = 20; // Height of the health bar
        int barX = x + 70; // X position of the health bar
        int barY = y - 15; // Y position of the health bar
        float healthPercentage = (float) currentHealth / maxHealth; // Calculate health percentage
    
        // Draw health text
        p.textAlign(LEFT);
        p.textSize(17);
        p.noStroke();
        p.fill(0);
        p.text("Health:", x, y);
    
        // Draw health bar background
        p.noStroke();
        p.fill(200); // Light gray
        p.rect(barX, barY, barWidth, barHeight);
    
        // Draw black border around health bar
        p.stroke(0); // Black color
        p.noFill();
        p.rect(barX, barY, barWidth, barHeight);
    
        // Calculate width of red health bar based on health percentage
        float redBarWidth = barWidth * healthPercentage;
        // Ensure red bar width doesn't exceed the width of the black border
        redBarWidth = p.constrain(redBarWidth, 0, barWidth);
    
        // Draw health bar with red filling
        p.fill(255, 0, 0); // Red color
        p.noStroke();
        p.rect(barX, barY, redBarWidth, barHeight);
    
        // Draw text showing max health
        p.fill(0);
        p.text(currentHealth, barX + barWidth + 10, y + 2);
        p.textSize(15);
        float power = this.level.tanks.get(turn).power;
        float displayPower = map(power, 40, this.level.tanks.get(turn).max_power, 50, 
        this.level.tanks.get(turn).max_power) - 30;
        if (displayPower > this.level.tanks.get(turn).max_power) {
            displayPower = this.level.tanks.get(turn).max_power / 2;
        }
        if (displayPower < 0) {
            displayPower = 0;
        }
        String displayPowerString = String.valueOf(Math.round(displayPower));        
        p.text("Power:  " + displayPowerString, x + 8, y + 24);
        drawArrow(turn);
    }
    
    /**
     * Draws an arrow above the current player's tank.
     * 
     * @param turn The current player's turn index.
     */
    public void drawArrow(int turn) {
        if (this.level.tanks.get(turn).isDead){
            return;
        }
        this.arrow.resize(35, 35);
        p.image(this.arrow, this.level.tanks.get(turn).pos.x - 15, 
        this.level.tanks.get(turn).pos.y - 30);
    }
    public void drawEnd() {
        // Define the position to start drawing scores
        int x = 500;
        int y = 100;
        int numOfPlayers = this.level.tanks.size();

        // Draw "Scores" text and underline
        p.textAlign(RIGHT);
        p.textSize(15);
        p.noStroke();
        p.fill(0);
        p.text("Scores", x - 24, y);
        p.stroke(0);
        p.line(x - 100, y + 4, x + 5, y + 4);
        p.line(x - 100, y - 15, x + 5, y - 15);  // Underline
        p.line(x - 100, y - 15, x - 100, y - 15 + numOfPlayers * 25);
        p.line(x + 5, y - 15, x + 5, y - 15 + numOfPlayers * 25);
        p.line(x - 100, y - 15 + numOfPlayers * 25, x + 5, y - 15 + numOfPlayers * 25);
        p.noStroke();

        // Draw player scores
        y += 20; // Move down for player scores
        x -= 1;
        for (int i = 0; i < this.level.tanks.size(); i++) {
            // Get the player's letter
            Character playerLetter = this.letters.get(i);
            
            // Get the player's score
            int playerScore = this.level.tanks.get(i).score;
            
            // Draw player's score
            String playerScoreText = "Player " + playerLetter + ": " + playerScore;
            p.fill(this.level.tanks.get(i).color);
            p.text(playerScoreText, x, y);
            
            // Move down for the next player's score
            y += 20;
        }
        p.textAlign(LEFT);
    }
}
