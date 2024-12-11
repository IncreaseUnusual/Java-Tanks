package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import java.io.File;
import java.util.*;
import Tanks.levelSetup;
import Tanks.projectile;
import Tanks.tank;
import Tanks.destruction;
import Tanks.ui;
import Tanks.sound;

/**
 * Main application class for the Tanks game.
 */
public class App extends PApplet {

    public static final int CELLSIZE = 32;
    public static final int CELLHEIGHT = 32;
    public static final int CELLAVG = 32;
    public static final int TOPBAR = 0;
    public static int WIDTH = 864;
    public static int HEIGHT = 640;
    public static final int BOARD_WIDTH = WIDTH / CELLSIZE;
    public static final int BOARD_HEIGHT = 20;
    private PImage bg;
    private PImage terrainPixels;
    private PImage treeImage;
    private static final int pixel_scale = 32;
    boolean gameEnded = false;

    public static final int INITIAL_PARACHUTES = 1;
    File duck;
    public static final int FPS = 30;
    PVector wind;
    public static Random random = new Random();
    public String configPath;
    public PVector grav = new PVector(0, 10);
    public String resourcePath;
    sound sound = new sound();
    boolean ended = false;
    public int level = 1;
    public levelSetup currentLevel;
    public PImage[] pixelArray;
    private float previousMillis = millis();
    public projectile currProjectile;
    public projectile[] activeProjectiles;
    private PImage parachute;
    int projectile_counter = 0;
    public HashSet<Integer> keysPressed = new HashSet<Integer>();
    private ArrayList<tank> tanks;
    Thread game;
    boolean starting = true;
    public int currentTurn = 0;
    public tank currentPlayer;
    public ui currentUi;
    public float deltaSeconds;
    public List<String> clr_items;

    /**
     * Constructor for the App class.
     */
    public App() {
        this.configPath = "config.json";
    }

    /**
     * Sets up the initial settings for the application window.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Initializes the game setup, loads resources, and starts the music.
     */
    @Override
    public void setup() {
        playMusic(0);
        size(WIDTH, HEIGHT);
        float speed = random(-35, 35);
        this.wind = new PVector(speed, 0);
        this.activeProjectiles = new projectile[10000];
        setupLevel(true);
    }

    /**
     * Handles key press events.
     * @param event the key event
     */
    @Override
    public void keyPressed(KeyEvent event) {
        if (key == CODED) {
            this.keysPressed.add(keyCode);
        } else if (key == ' ') {
            this.currentTurn += 1;
            if (this.currentTurn == this.currentLevel.tanks.size()) {
                this.currentTurn = 0;
            }
            setWind();
            this.currProjectile = this.currentPlayer.fireProj(currentPlayer.power, this.currentTurn);
            if (this.projectile_counter < 10000) {
                this.activeProjectiles[projectile_counter] = this.currProjectile;
                playSE(1);
            }
            this.projectile_counter += 1;
        }
        if (key == 'r') {
            if (ended){
                this.level = 1;
                ended = false;
                for (int k = 0; k < this.currentLevel.tanks.size(); k++) {
                    this.currentLevel.tanks.get(k).isDead = false;
                    this.currentLevel.tanks.get(k).score = 0;
                    this.currentLevel.tanks.get(k).health = 100;
                }
                return;
            }
            boolean val = this.currentLevel.tanks.get(this.currentTurn).repairHealth();
            if (val) {
                playSE(2);
            } else {
                playSE(3);
            }
        }
        if (key == 'f') {
            boolean val = this.currentLevel.tanks.get(this.currentTurn).addFuel();
            if (val) {
                playSE(2);
            } else {
                playSE(3);
            }
        }
        if (key == 's') {
            this.keysPressed.add(1111);
        }
        if (key == 'w') {
            this.keysPressed.add(2222);
        }
    }

    /**
     * Handles key release events.
     */
    @Override
    public void keyReleased() {
        if (key == CODED) {
            this.keysPressed.remove(keyCode);
        }
        if (key == 's') {
            this.keysPressed.remove(1111);
        }
        if (key == 'w') {
            this.keysPressed.remove(2222);
        }
    }

    /**
     * Handles mouse press events.
     * @param e the mouse event
     */
    @Override
    public void mousePressed(MouseEvent e) {
    }

    /**
     * Handles mouse release events.
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Main draw loop for the game.
     */
    @Override
    public void draw() {
        if (ended){
            System.out.println("here");
            this.currentUi.drawEnd();
            return;
        }
        int counter = 1;
        for (int k = 0; k < this.currentLevel.tanks.size(); k++) {
            if (this.currentLevel.tanks.get(k).isDead) {
                counter += 1;
            }
        }
        if (counter >= this.currentLevel.tanks.size()) {
            newLevel();
            this.projectile_counter = 0;
        } else if (counter == this.currentLevel.tanks.size()) {
            System.out.println("FINISHED");
            exit();
        }
        if (this.currentTurn == this.currentLevel.tanks.size()) {
            this.currentTurn -= 1;
        }
        if (this.currentTurn > this.currentLevel.tanks.size()) {
            this.currentTurn = 0;
        }
        if (this.currentLevel.tanks.get(this.currentTurn).fuel == 0) {
            this.currentTurn += 1;
            if (this.currentTurn > this.currentLevel.tanks.size()) {
                this.currentTurn = 0;
            }
        }
        if (this.currentTurn >= this.currentLevel.tanks.size()) {
            this.currentTurn = 0;
        }
        if (this.currentLevel.tanks.get(this.currentTurn).isDead) {
            if (this.currentTurn == this.currentLevel.tanks.size()) {
                this.currentTurn = 0;
            } else {
                this.currentTurn += 1;
            }
        }

        float timeMillis = millis();
        float delta = timeMillis - previousMillis;
        this.deltaSeconds = delta / 1000f;
        this.previousMillis = timeMillis;
        if (this.currentTurn > this.currentLevel.tanks.size() - 1) {
            this.currentPlayer = currentLevel.tanks.get(this.currentLevel.tanks.size() - 1);
        } else {
            this.currentPlayer = currentLevel.tanks.get(this.currentTurn);
        }
        background(bg);
        generatePixelArray();
        drawTerrain();
        if (currentLevel.hasTrees) {
            generateTrees();
        }
        generateTank(this.currentTurn, false);
        generateDestruction();
        generatePixelArray();

        for (int k = 0; k < currentLevel.tanks.size(); k++) {
            if (this.currentLevel.tanks.get(k).isDead) {
                continue;
            }
            currentLevel.tanks.get(k).drawTank();
        }

        if (this.keysPressed.contains(UP)) {
            this.currentPlayer.aimTurret(0.95f * this.deltaSeconds + 0.05f);
        }
        if (this.keysPressed.contains(2222)) {
            this.currentPlayer.increasePower();
        }
        if (this.keysPressed.contains(1111)) {
            this.currentPlayer.decreasePower();
        }
        if (this.keysPressed.contains(DOWN)) {
            this.currentPlayer.aimTurret(-0.95f * this.deltaSeconds - 0.05f);
        }
        if (this.keysPressed.contains(LEFT)) {
            this.currentPlayer.moveLeft();
        }
        if (this.keysPressed.contains(RIGHT)) {
            this.currentPlayer.moveRight();
        }

        if (ended){
            this.currentUi.drawEnd();
        }
        else{
            this.currentLevel.damageDetection(this.activeProjectiles, this.currentLevel, this.deltaSeconds);
            this.currentUi.drawWind(Math.round(this.wind.x));
            this.currentUi.drawUi(this.currentTurn);
        }
    }

    /**
     * Generates the pixel array for the terrain.
     */
    public void generatePixelArray() {
        PImage[] pixelArray = new PImage[currentLevel.scaledArray.length];
        for (int i = 0; i < pixelArray.length; i++) {
            int val = currentLevel.scaledArray[i];
            if (val < 0) {
                val = 0;
                this.currentLevel.scaledArray[i] = 0;
                continue;
            }
            PImage pixel = new PImage(1, val);
            pixel.loadPixels();
            for (int x = 0; x < 1; x++) {
                for (int y = 0; y < val; y++) {
                    pixel.pixels[x + y] = color(Integer.valueOf(clr_items.get(0)), Integer.valueOf(clr_items.get(1)), Integer.valueOf(clr_items.get(2)));
                }
            }
            pixel.updatePixels();
            pixelArray[i] = pixel;
        }
        this.pixelArray = pixelArray;
    }

    /**
     * Draws the terrain on the screen.
     */
    public void drawTerrain() {
        pushMatrix();
        scale(1, -1);
        for (int i = 0; i < this.pixelArray.length; i++) {
            if (this.pixelArray[i] != null) {
                image(this.pixelArray[i], i, 640 * -1);
            }
        }
        popMatrix();
    }

    /**
     * Generates trees on the terrain.
     */
    public void generateTrees() {
        treeImage.resize(35, 35);
        int length = currentLevel.getScaledTrees().length;
        for (int k = 0; k < length; k++) {
            if (currentLevel.getScaledTrees()[k] >= 1) {
                int val = 640 - currentLevel.scaledArray[k] - 32;
                if (val >= 630) {
                    continue;
                }
                image(this.treeImage, k - 18, 640 - currentLevel.scaledArray[k] - 32);
            }
        }
    }

    /**
     * Generates tanks on the terrain.
     * @param turn the current turn
     * @param initial flag indicating if it's the initial setup
     */

    public void generateTank(int turn,boolean initial){
        
        for (int k = 0; k < currentLevel.tanks.size();k++){
            int func = Math.round(currentLevel.tanks.get(k).pos.x);
            if (func < 0){
                func = 0 ;
            }
            int height = 640-  currentLevel.scaledArray[func] - 32;
            //Check Dead:
            currentLevel.tanks.get(k).isFalling = true; //line 363
            if ((k  == currentTurn || starting == true) || !currentLevel.tanks.get(k).isFalling){
                if (func > 864){
                    func = 864;
                }
                else if(func <= 0){
                    func = 0;
                }
                currentLevel.tanks.get(k).setHeight(640-  currentLevel.scaledArray[func] - 32);
            }
            else{
                boolean groundExists = false;
                
                if (func > 864){
                    func = 864;
                }
                else if(func <= 0){
                    func = 0;
                }
                if (height <= currentLevel.tanks.get(k).pos.y){
                    currentLevel.tanks.get(k).isFalling = false;
                    groundExists = true;
                }
                if (!groundExists){
                    if (currentLevel.tanks.get(k).parachutes == 0 ){
                        currentLevel.tanks.get(k).pos.y += 2;
                        this.currentLevel.tanks.get(k).decHealth(Math.round(currentLevel.tanks.get(k).pos.y - height)
                        ,this.activeProjectiles[k],"fallDmg");
                    }
                    else{
                        if (!currentLevel.tanks.get(k).isDead){
                            image(this.parachute,currentLevel.tanks.get(k).pos.x - 29, currentLevel.tanks.get(k).pos.y- 36);
                        }
    
                        currentLevel.tanks.get(k).pos.y += 1;

                    }

                }

            }

        }
        this.starting = false;

        return;
    }
    /**
     * Updates the wind speed with a random change.
     */
    public void setWind() {
        float diff = random(-5, 5);
        this.wind.x += diff * 1.8;
    }

    /**
     * Advances to the next level, resets turn and projectile data,
     * and increments the level counter. Ends the game after level 3.
     */
    public void newLevel() {
        this.currentTurn = 0;
        this.level += 1;
        if (this.level > 3) {
            ended = true;
            this.level = 3;  
            System.out.println("ended");
        }
        else{
            this.activeProjectiles = new projectile[100];
            setupLevel(false);
        }

    }

    /**
     * Processes the destruction caused by active projectiles.
     * Updates the terrain based on projectile impact.
     */
    public void generateDestruction() {
        for (int k = 0; k < this.activeProjectiles.length; k++) {
            if (this.activeProjectiles[k] != null) {
                this.activeProjectiles[k].tick(deltaSeconds, currentLevel.scaledArray, this.wind);
                this.activeProjectiles[k].collisionDetection(
                    Integer.valueOf(clr_items.get(0)),
                    Integer.valueOf(clr_items.get(1)),
                    Integer.valueOf(clr_items.get(2))
                );
                if (this.activeProjectiles[k].activeDestruction != null) {
                    if (!this.activeProjectiles[k].activeDestruction.finished) {
                        ArrayList<int[]> coordinates = this.activeProjectiles[k].activeDestruction.getDestructed();
                        if (coordinates != null) {
                            for (int j = 0; j < coordinates.size(); j++) {
                                int val = coordinates.get(j)[0];
                                if (val > 864) {
                                    val = 864;
                                }
                                if (val < 0) {
                                    val = 0;
                                }
                                int terrainHeightatX = this.currentLevel.scaledArray[val];
                                int newHeight = 640 - coordinates.get(j)[1] - 30;
                                if (newHeight > terrainHeightatX) {
                                    continue;
                                }
                                this.currentLevel.scaledArray[val] = newHeight;
                            }
                            coordinates = null;
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets up the level, initializes tanks and UI components,
     * and loads images and level data.
     * @param initial Indicates if it's the initial setup of the level.
     */
    public void setupLevel(boolean initial) {
        if (initial) {
            this.currentLevel = new levelSetup();
            currentLevel.loadData(this, configPath, this.level, initial, new int[0]);
        } else {
            int[] arrayOfScores = new int[this.currentLevel.tanks.size()];
            for (int k = 0; k < this.currentLevel.tanks.size(); k++) {
                arrayOfScores[k] = this.currentLevel.tanks.get(k).score;
            }
            currentLevel.loadData(this, configPath, this.level, initial, arrayOfScores);
        }
        frameRate(FPS);
        currentLevel.readLevel(this);
        this.clr_items = Arrays.asList(currentLevel.getForegroundColour().split("\\s*,\\s*"));
        bg = loadImage(currentLevel.getBackgroundData());
        this.parachute = loadImage(currentLevel.getParachute());
        if (currentLevel.hasTrees) {
            this.treeImage = loadImage(currentLevel.getTreeImage());
        }
        if (initial) {
            this.tanks = currentLevel.tanks;
        }
        generatePixelArray();
        generateTank(0, initial);
        if (initial) {
            this.currentUi = new ui(this, this.currentLevel);
        }
        currentUi.windImageRight = loadImage(this.currentLevel.windRight);
        currentUi.windImageLeft = loadImage(this.currentLevel.windLeft);
        currentUi.fuel = loadImage(this.currentLevel.fuel);
        currentUi.arrow = loadImage(this.currentLevel.arrow);
    }

    /**
     * Plays background music.
     * @param i Index of the music file to play.
     */
    public void playMusic(int i) {
        sound.setFile(i);
        sound.play();
        sound.loop();
    }

    /**
     * Stops the currently playing background music.
     */
    public void stopMusic() {
        sound.stop();
    }

    /**
     * Plays a sound effect.
     * @param i Index of the sound effect file to play.
     */
    public void playSE(int i) {
        sound.setFile(i);
        sound.play();
    }

    /**
     * The main method to start the Tanks game application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        PApplet.main("Tanks.App");
    }

}

