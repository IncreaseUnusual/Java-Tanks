package Tanks;

import org.checkerframework.checker.units.qual.A;

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


public class levelSetup{
    String resourcePath;
    String configPath;
    JSONObject json;
    int level = 1; 
    String layout;
    PApplet p;
    String backgroundData;
    String foregroundColour;
    String parachute;
    String treeImage;
    char[][] terrain; 
    int[] arrayOfHeights;
    int[] scaledArray;
    int[] treePositions;
    int[] scaledTrees;
    int treeCount;
    int[] scaledPlayers;
    public ArrayList<tank> tanks;
    public ArrayList<Character> players;
    private JSONObject playerObjects;  
    public boolean hasTrees = false;
    public String windRight;
    String fuel;
    public String windLeft;
    int[] scores;
    boolean initial;
    String arrow;
    String duck;
    


    // Attributes and fields...

    /**
     * Loads the configuration data for a specific game level.
     *
     * @param p          The PApplet instance for drawing and processing.
     * @param configPath The file path to the configuration file for the level.
     * @param level      The level number to load.
     * @param initial    Indicates whether it's the initial setup.
     * @param scores     An array containing scores for tanks.
     */

    public void loadData(PApplet p,String configPath,int level,boolean initial,int[] scores){

        resourcePath =  "src/main/resources/Tanks/";
        json  = p.loadJSONObject(configPath);
        this.initial = initial;
        this.scores = scores;
        hasTrees = false;
        this.playerObjects = json.getJSONObject("player_colours");
        JSONArray levelArray  = json.getJSONArray("levels");
        JSONObject levelData = levelArray.getJSONObject(level - 1);
        //Should this be declared here?
        this.level = level;
        this.arrow = resourcePath + "arrow.png";
        this.layout = resourcePath + levelData.getString("layout");
        this.backgroundData = resourcePath + levelData.getString("background");
        this.foregroundColour = levelData.getString("foreground-colour");
        this.parachute = resourcePath + "parachute.png";
        this.duck = resourcePath + "duckSong.mp3";
        //If Exists
        this.treeImage = resourcePath + levelData.getString("trees");
        this.windRight = resourcePath + "wind.png";
        this.windLeft = resourcePath + "wind-1.png";
        System.out.println(this.windLeft);
        this.tanks = new ArrayList<tank>();
        this.players = new ArrayList<Character>();
        this.p = p;
        this.fuel = resourcePath + "fuel.png";
    }


    /**
     * Reads the terrain layout for the current level from a text file.
     *
     * @param p The PApplet instance for drawing and processing.
     */
    public void readLevel(PApplet p){
        String file_path = "level" + Integer.toString(level) + ".txt";
        try{
            Scanner scan = new Scanner(new File(file_path));
            ArrayList<String> lines = new ArrayList<>();
            int maxLength = 0;
            
            int row  = 0;
            while (scan.hasNextLine()){
                String line = scan.nextLine();
                lines.add(line);
                //Get max possible lines. (Width)
                maxLength = Math.max(maxLength,line.length());
                row++;
            }
            scan.close();

            //File less than 20 char
            if (row < 20){
                int diff = 20 - lines.size() + 1;
                for (int i = 0; i < diff; i++){
                    row++;
                    lines.add("");
                }
            }

            this.terrain  = new char[row][maxLength];
            //Iterating through each row
            for (int i = 0; i < lines.size(); i++){
                String line = lines.get(i);
                for (int j = 0; j < line.length(); j++){
                    this.terrain[i][j] = line.charAt(j);
                }
            }
            //Represents some Line.
            int[] arrayOfHeights = new int[this.terrain[0].length];
            int[] arrayOfPlayers = new int[this.terrain[0].length];

            for (int i = 0; i < this.terrain.length; i++){
                for (int j = 0; j < this.terrain[i].length; j++){
                    //Each Line
                    if (this.terrain[i][j] == 'X'){
                        int height = this.terrain.length - i;
                        arrayOfHeights[j] = height - 1;
                    }
                    else if (this.terrain[i][j] != 'X' && this.terrain[i][j] != 'T' && Character.isLetter(this.terrain[i][j])){
                        //Check the measurements
                        int c = this.getPlayerColor(this.terrain[i][j]);
                        tank newTank = new tank(p,new PVector(j * 32 - 17 , this.terrain.length - i + 160  ),c,this.terrain[i][j]);
                        this.tanks.add(newTank);
                        this.players.add(this.terrain[i][j]);
                    }
            }
            this.arrayOfHeights = arrayOfHeights;
            this.scaledPlayers = arrayOfPlayers;

            this.scaleArray();
            this.setTreePosition(p);
            sortTanksByPlayer();
            if (!this.initial){
                for (int k = 0; k < this.tanks.size(); k ++){
                    this.tanks.get(k).score = scores[k];
                }
            }
            //Scale the array
        }
    }

        catch (FileNotFoundException e){
            System.out.println("file not foun bruh");

        }

    }

    /**
     * Scales the terrain and player arrays to match the screen size.
     */

    public void scaleArray(){
        int[] scaledArray = new int[this.arrayOfHeights.length * 32 ];
        int[] scaledArrayPlayers = new int[this.arrayOfHeights.length * 32 ];
        for (int i = 0 , j = 0; i < scaledArray.length; i++){
            //Need to scale each height by 32;
            scaledArray[i] = arrayOfHeights[j] * 32;
            //Not using
            scaledArrayPlayers[i] = arrayOfHeights[j] * 32;
            //Next Value.
            if (i % 32 == 0 && i != 0){
                if (!(j >= 27)){
                    j++;
                }
            }
        }
        this.scaledPlayers = scaledArrayPlayers;
        this.scaledArray = scaledArray;


        movingAverage();
    }

    public void nextLevel(){
        this.level += 1;
    }


    /**
     * Sets the positions of trees on the terrain.
     *
     * @param p The PApplet instance for drawing and processing.
     */
    public void setTreePosition(PApplet p){
        
        int[] treePositions = new int[this.terrain[0].length];
        for (int i = 0; i < this.terrain.length; i++){
            for (int j = 0; j < this.terrain[i].length; j++){
                //Each Line
                if (this.terrain[i][j] == 'T'){
                    int height = this.terrain.length - i;
                    treePositions[j] = 1;
                    this.hasTrees = true;
                }
                else{
                    if (treePositions[j] != 1){
                        treePositions[j] = 0;
                    }
                    
                }
            }
        }

        int[] scaledTrees = new int[864];

        int j = 0;

        for (int k = 0; k < treePositions.length; k++){
            if (j > scaledTrees.length){
                break;
            }
            int left = 0;
            int right = 0;
            if (j - 15 < 0 ){
                left = 0;
            }
            else{
                left = j - 15;
            }
            if (j + 15 > scaledTrees.length ){
                right = scaledTrees.length ;
            }
            else{
                right = j + 15;
            }
            int val = (int)Math.round(Math.floor(Math.random() *(right - left + 1) + left));

            if (treePositions[k] == 1){
                if (val >= scaledTrees.length){
                    val = scaledTrees.length - 1 ;
                }
                else if (val < 0){
                    val = 1;
                }
                scaledTrees[val] = 1;
                j += 3;
            }
            j += 32;
        }

        for (int i = 0; i < scaledTrees.length; i++){
            if (scaledTrees[i] == 1){
                scaledTrees[i] = scaledArray[i];
                this.treeCount += 1;
            }
        }
        this.scaledTrees = scaledTrees;


    }

    
    public String getLayout(){
        return this.layout;
    }
    /**
     * Performs damage detection based on active projectiles and tank positions.
     *
     * @param activeProjectiles An array of active projectiles in the level.
     * @param currentLevel      The current levelSetup instance.
     * @param deltaSeconds      Time elapsed since the last frame.
     */
    public void damageDetection(projectile[] activeProjectiles, levelSetup currentLevel, float deltaSeconds){
        for (int k = 0 ; k < activeProjectiles.length; k++){
            if (activeProjectiles[k] != null){
                activeProjectiles[k].draw();
                for (int j = 0;j <currentLevel.tanks.size(); j ++){

                    PVector center = currentLevel.tanks.get(j).getCenter();
                    int x = Math.round(currentLevel.tanks.get(j).pos.x+2);
                    int y = Math.round(currentLevel.tanks.get(j).pos.y+26);        
                    int col = currentLevel.tanks.get(j).color;
                    if ((activeProjectiles[k].activeDestruction != null)){
                        PVector int_projectile_cent = new PVector(activeProjectiles[k].activeDestruction.pos.x,
                        activeProjectiles[k].activeDestruction.pos.y);
                        int distance = Math.round(PApplet.dist(center.x, center.y,
                        int_projectile_cent.x, int_projectile_cent.y));
                        if (p.get(x,y) != col){
                            //Damage reduction only done once.
                            int damage = Math.abs(60 - distance);
                            currentLevel.tanks.get(j).decHealth(damage,activeProjectiles[k],"damage");
                            currentLevel.tanks.get(j).checkDead(activeProjectiles[k].attackingTank);
                        }
                    }
                    if (currentLevel.tanks.get(j).deadAnimationComplete == false && 
                    currentLevel.tanks.get(j).isDead == true && currentLevel.tanks.get(j).deadExplosion != null){
                        currentLevel.tanks.get(j).deadExplosion.tick(deltaSeconds,currentLevel.scaledArray);
                        currentLevel.tanks.get(j).deadExplosion.draw();
                        if (currentLevel.tanks.get(j).deadExplosion.finished){
                            currentLevel.tanks.get(j).deadAnimationComplete = true;
                        }
                    }
                }
            }
        }

        //Check if any projectiles have exceed bounds/exploded
        for (int k = 0 ; k < activeProjectiles.length; k++){
            if (activeProjectiles[k] != null){
                if (activeProjectiles[k].position == null){
                    continue;
                }
                if(activeProjectiles[k].position.x > 864 || activeProjectiles[k].position.y > 640 || 
                activeProjectiles[k].position.x < 0){
                    activeProjectiles[k] = null;
                }
            }
        }
    }
    public void sortTanksByPlayer() {
        // Define a custom comparator for tank objects
        Comparator<tank> tankComparator = new Comparator<tank>() {
            @Override
            public int compare(tank t1, tank t2) {
                // Compare based on the "player" attribute
                return Character.compare(t1.player, t2.player);
            }
        };

        // Sort the tanks ArrayList using the comparator
        Collections.sort(this.tanks, tankComparator);
    }

    /**
     * Applies a moving average filter to smooth terrain.
     */
    public void movingAverage(){
        for (int k = 0; k < 2; k++){
            int[] newArray = new int[this.scaledArray.length];
            for (int i = 0; i < newArray.length; i ++){
                //Get Next 32 Values
                int sum = 0;
                if (newArray.length - 32 >= i){
                    for (int j = i; j < (i + 32); j ++){
                        sum += this.scaledArray[j];
                    }
                    newArray[i] = sum / 32;
                }
                else{
                    newArray[i] = this.scaledArray[i];
                }
            }
            this.scaledArray = newArray;
        }
    }
    
    public String getBackgroundData(){
        return this.backgroundData;
    }
    public String getForegroundColour(){
        return this.foregroundColour;
    }
    public int[] getScaledTrees(){
        return this.scaledTrees;
    }

    public String getTreeImage(){
        return this.treeImage;
    }
    

    private int getPlayerColor(char c){
        String value = String.valueOf(c);

        String[] colorVal = this.playerObjects.getString(value).split(",");

        return p.color(Integer.valueOf(colorVal[0]),Integer.valueOf(colorVal[1]),Integer.valueOf(colorVal[2]));
    }

    public String getParachute(){
        return this.parachute;
    }
    
    

}
