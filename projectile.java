package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import Tanks.levelSetup;
import java.util.concurrent.TimeUnit;
import Tanks.tank;
import Tanks.projectile;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.*;

/**
 * Represents a projectile in the Tanks game.
 */
public class projectile extends PApplet {

    private PApplet p;
    PVector position;
    PVector velocity;
    public PVector grav = new PVector(0, 45);
    float startX;
    int color;
    float startY;
    boolean complete = false;
    int[] scaledArray;
    tank attackingTank;
    destruction activeDestruction;
    public ArrayList<int[]> coordinates;

    /**
     * Constructor for the projectile class.
     *
     * @param p              The PApplet instance.
     * @param pos            The initial position of the projectile.
     * @param vel            The initial velocity of the projectile.
     * @param startX         The starting X coordinate.
     * @param startY         The starting Y coordinate.
     * @param color          The color of the projectile.
     * @param counter        A counter value (not used in this snippet).
     * @param attackingTank  The tank that fired this projectile.
     */
    public projectile(PApplet p, PVector pos, PVector vel, float startX, float startY, int color, int counter, tank attackingTank) {
        this.p = p;
        this.attackingTank = attackingTank;
        this.position = pos;
        this.velocity = vel;
        this.color = color;
    }

    /**
     * Updates the projectile's position and checks for collisions.
     *
     * @param deltaSeconds The time delta in seconds.
     * @param scaledArray  The current terrain height array.
     * @param currWind     The current wind vector.
     * @return The updated terrain height array.
     */
    public int[] tick(float deltaSeconds, int[] scaledArray, PVector currWind) {
        if (this.activeDestruction != null) {
            this.scaledArray = this.activeDestruction.tick(deltaSeconds, scaledArray);
        }
        if (this.complete) {
            return null;
        }
        this.velocity.add(PVector.mult(grav, deltaSeconds));
        // this.velocity.add(PVector.mult(currWind, deltaSeconds));
        this.position.add(PVector.mult(this.velocity, deltaSeconds));

        return this.scaledArray;
    }

    /**
     * Draws the projectile and any active destruction effect.
     */
    public void draw() {
        if (this.activeDestruction != null) {
            this.activeDestruction.draw();
            if (this.activeDestruction.finished) {
                this.activeDestruction = null;
            }
        }
        if (this.complete) {
            return;
        }
        p.pushMatrix();
        p.translate(startX, startY);
        p.noFill();
        p.stroke(this.color);
        p.strokeWeight(5);
        p.point(this.position.x, position.y);
        p.popMatrix();
    }

    /**
     * Checks for collisions with the terrain.
     *
     * @param colA The red component of the collision color.
     * @param colB The green component of the collision color.
     * @param colC The blue component of the collision color.
     */
    public void collisionDetection(int colA, int colB, int colC) {
        if (this.complete) {
            return;
        }
        if (this.position.x > 864 || this.position.y > 640 || this.position.x < 0) {
            this.complete = true;
            return;
        }
        int color = p.color(colA, colB, colC);
        if (p.get(Math.round(this.position.x), Math.round(this.position.y)) == color) {
            this.complete = true;
            this.activeDestruction = new destruction(p, 20, this.position);
            if (this.activeDestruction.getDestructed() != null) {
                this.coordinates = this.activeDestruction.getDestructed();
            }
        }
    }
}
