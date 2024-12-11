package Tanks;

import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Represents a destruction effect in the Tanks game.
 */
public class destruction extends PApplet {

    private PApplet p;
    PVector pos;
    float radius;
    float radius_red = 30f;
    int color_red;
    int color_yellow;
    int color_orange;
    float radius_orange = 15f;
    float radius_yellow = 6f;
    final float time_duration = 0.3f;
    float time_elapsed;
    boolean finished = false;
    boolean executed = false;
    int[] scaledArray;
    ArrayList<int[]> coordinates;
    boolean damageDone = false;

    /**
     * Constructor for the destruction class.
     *
     * @param p      The PApplet instance.
     * @param radius The radius of the destruction effect.
     * @param pos    The position of the destruction effect.
     */
    public destruction(PApplet p, float radius, PVector pos) {
        this.p = p;
        this.radius = radius;
        this.pos = pos;
        this.color_orange = p.color(255, 165, 0);
        this.color_yellow = p.color(255, 255, 0);
        this.color_red = p.color(255, 0, 0);
        this.coordinates = new ArrayList<>();
    }

    /**
     * Gets the hitbox of the destruction effect.
     *
     * @return The hitbox coordinates.
     */
    public int[] getHitbox() {
        if (!damageDone) {
            int[] hitBox = new int[4];
            hitBox[0] = Math.round(this.pos.x - this.radius);
            hitBox[1] = Math.round(this.pos.x + this.radius);
            hitBox[2] = Math.round(this.pos.y - this.radius);
            hitBox[3] = Math.round(this.pos.y + this.radius);
            damageDone = true;
            return hitBox;
        }
        return new int[0];
    }

    /**
     * Updates the destruction effect over time.
     *
     * @param deltaSeconds The time delta in seconds.
     * @param scaledArray  The current terrain height array.
     * @return The updated terrain height array.
     */
    public int[] tick(float deltaSeconds, int[] scaledArray) {
        time_elapsed += deltaSeconds;
        this.scaledArray = scaledArray;

        if (time_elapsed > time_duration) {
            this.finished = true;
        }

        return this.scaledArray;
    }

    /**
     * Draws the destruction effect.
     */
    public void draw() {
        float drawEllapsed = constrain(time_elapsed / time_duration, 0f, 1f);
        float radius_scale = sin(drawEllapsed * PI) * 1.18f;

        if (drawEllapsed >= 0.15 && !executed) {
            destructionUpdate();
        }
        // Draw Red
        p.noStroke();
        p.fill(this.color_red);
        p.ellipseMode(CENTER);
        p.ellipse(pos.x, pos.y, radius_red * radius_scale, radius_red * radius_scale);
        // Draw Orange
        p.noStroke();
        p.fill(this.color_orange);
        p.ellipseMode(CENTER);
        p.ellipse(pos.x, pos.y, radius_orange * radius_scale, radius_orange * radius_scale);
        // Draw Yellow
        p.noStroke();
        p.fill(this.color_yellow);
        p.ellipseMode(CENTER);
        p.ellipse(pos.x, pos.y, radius_yellow * radius_scale, radius_yellow * radius_scale);
    }

    /**
     * Performs the update to the destruction effect.
     */
    public void destructionUpdate() {
        // Carve Terrain
        int leftX = Math.max(0, Math.round(this.pos.x - radius_red));
        int rightX = Math.min(864 - 1, Math.round(this.pos.x + radius_red));
        int yHeight = Math.max(0, Math.round(this.pos.y - radius_red));
        int yHeightMin = Math.min(640 - 1, Math.round(this.pos.y + radius_red));
        int height = (yHeight - yHeight) / 2;

        HashSet<Integer> seenXValues = new HashSet<>();
        int counter = 0;

        for (int theta = 180; theta <= 360; theta++) {
            double x = pos.x + radius_red * Math.cos(Math.toRadians(theta));
            int calc = Math.round(pos.y) +
                    (int) Math.round(Math.sqrt(Math.pow(radius_red, 2) - Math.pow(((int) Math.round(x) - pos.x), 2))) - 32;

            int[] new_val = new int[]{(int) Math.round(x), calc};
            if (!seenXValues.contains((int) Math.round(x))) {
                this.coordinates.add(new_val);
                int x_rounded = (int) Math.round(x);
                seenXValues.add(x_rounded);
                counter += 1;
            }
        }
        executed = true;
    }

    /**
     * Gets the coordinates affected by the destruction effect.
     *
     * @return The list of affected coordinates.
     */
    public ArrayList<int[]> getDestructed() {
        return this.coordinates;
    }
}
