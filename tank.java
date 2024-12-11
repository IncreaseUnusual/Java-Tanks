package Tanks;

import org.checkerframework.checker.units.qual.A;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import Tanks.levelSetup;
import Tanks.projectile;
import java.util.concurrent.TimeUnit;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.*;
import java.util.*;


public class tank extends PApplet{
    
    public  PVector pos;
    public int color = 0;
    private PApplet p;
    private int scaleFactor = 3;
    int tank_size = 100;
    char player;
    float power = 74;
    public float aimAngle;
    public int turret_length = -8;
    boolean falling = false;
    int health = 100;
    int fuel = 250;
    ArrayList<projectile> damagedBy;
    boolean damageDone;
    boolean isDead = false;
    int parachutes = 3;
    boolean deadAnimationComplete = false;
    int score = 0;
    destruction deadExplosion;
    boolean isFalling = false;
    int max_power = 130;

    /**
     * Constructor for tank class.
     * @param p The PApplet instance.
     * @param position The position of the tank.
     * @param color The color of the tank.
     * @param player The player ID of the tank.
     */
    public tank(PApplet p,PVector position, int color,char player){
        this.pos = position;
        this.color = color;
        this.p = p;
        this.player = player;
        this.aimAngle = PI/2f;
        this.damagedBy = new  ArrayList<projectile>();
    }

    /**
     * Draws the tank on the screen.
     */
    public void drawTank(){
        if (this.pos.y > 605){
            this.health = 0;
            isDead = true;
        }
        if (isDead){
            return;
        }
        if (this.pos.x > 864){
            this.pos.x = 864;
        }
        if (this.pos.x <= 0){
            this.pos.x = 0;
        }
        p.pushMatrix();
        p.translate(pos.x - 18 , pos.y + 17);
        // Draw body
        p.noStroke();
        p.fill(color);
        p.beginShape();
        p.rect(10 + 3, 10 - 4, 15, 5, 4);
        p.rect(10, 10, 22, 5, 4);
        p.endShape(CLOSE);
        p.popMatrix();
        //Turret
        p.pushMatrix();
        p.translate(pos.x + 1 , pos.y + 23 );
        p.stroke(0,0,0);
        p.strokeWeight(3);
        p.noFill();
        p.rotate(aimAngle);
        p.line(0,0,-8,0);
        p.popMatrix();
    }

    /**
     * Sets the height of the tank.
     * @param h The height value to set.
     */
    public void setHeight(int h){
        this.pos.y = h;
    }
    public float getX(){
        return this.pos.x;
    }

    /**
     * Moves the tank to the right.
     */
    public void moveRight(){
        this.fuel -= 1;
        if (this.fuel > 0){
            this.pos.x += 1;
        }
        else{
            this.fuel = 0;
        }
    }
    public void moveLeft(){
        this.fuel -= 1;
        if (this.fuel > 0){
            this.pos.x -= 1;
        }
        else{
            this.fuel = 0;;
        }
    }

    /**
     * Fires a projectile from the tank.
     * @param power The power of the projectile.
     * @param counter A counter value.
     * @return The projectile fired.
     */
    public projectile fireProj(float power,int counter){
        if (this.isDead){
            projectile proj = new projectile(p, null, pos, counter, power, counter, counter, null);
            proj.complete = true;
            return proj;
        }
        PVector currentAim = PVector.fromAngle(aimAngle);
        //Add Turret Direction and length)
        PVector projSpawn = PVector.add(pos,PVector.mult(currentAim,turret_length));

        projSpawn.y += 21;
        projSpawn.x += 1;
        this.power = power;

        PVector startVelocity = PVector.mult(currentAim,-this.power -50 );
        return new projectile(p,projSpawn,startVelocity,pos.x+1,pos.y+23,this.color,counter,this);
    }

    /**
     * Checks if the tank is dead and performs necessary actions.
     * @param attackingTank The tank that attacked.
     */
    public void checkDead(tank attackingTank){
        if (this.health == 0 && isDead == false){
            this.deadExplosion =
            new destruction(p, 15, new PVector(this.pos.x,this.pos.y + 28));
            isDead = true;
        }
    }


    public void aimTurret(float delta){
        this.aimAngle = constrain(aimAngle + delta, PI * 0.f, PI*0.9f + 0.4f) ;
    }
    public void increasePower(){
        this.power += 1;
        if (this.power > max_power){
            this.power = max_power;
        }

    }
    public void decreasePower(){
        this.power -= 1;
        if (this.power < 0){
            this.power = 0;
        }
    }

    /**
     * Decreases the health of the tank.
     * @param val The value by which health should be decreased.
     * @param p The projectile causing the damage.
     * @param type The type of damage.
     */
    public void decHealth(int val,projectile p,String type){
        if (!this.damagedBy.contains(p) && !isDead){
            this.damagedBy.add(p);
            this.health -= val;
            this.parachutes -= 1;
            this.max_power -= val;
            if (this.health < 0){
                this.health = 0;
            }
            if (this.parachutes < 0){
                this.parachutes = 0;
            }
            if (p.attackingTank.color == this.color){
                return;
            }
            p.attackingTank.score += val;
        }
    }
    public void fallDamage(int val){
        this.health -= val;
        if (this.health < 0){
            this.health = 0;
        }
    }

    /**
     * Calculates and returns the center position of the tank.
     * @return The center position of the tank.
     */

    public PVector getCenter() {
        // Calculate the center of the body rectangle
        float bodyCenterX = pos.x + 10 + (15 / 2); // 10 is the x offset, 15 is the width of the body
        float bodyCenterY = pos.y + (5 / 2); // 5 is the height of the body
    
        // Calculate the center of the turret rectangle
        float turretCenterX = pos.x + 1;
        float turretCenterY = pos.y + 23;
    
        // Calculate the overall center as the average of body and turret centers
        float centerX = (bodyCenterX + turretCenterX) / 2;
        float centerY = (bodyCenterY + turretCenterY) / 2;
    
        return new PVector(centerX, centerY);
    }

    public boolean repairHealth(){
        if (this.health == 100){
            return false;
        }
        if (this.score >= 20){
            this.health += 20;
            this.score -= 20;
            if (this.health > 100){
                this.health= 100;
            }
            return true;
        }
        return false;

    }

    /**
     * Adds fuel to the tank.
     * @return True if fuel was added, false otherwise.
     */
    public boolean addFuel(){
        if (this.score >= 10){
            this.fuel += 200;
            this.score -= 10;
            return true;
        }
        else{
            return false;
        }
    }
    
}