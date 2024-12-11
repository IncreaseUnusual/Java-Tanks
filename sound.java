package Tanks;

import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import java.net.URL;

public class sound {

    Clip clip;
    URL soundURL[] = new URL[30];
    FloatControl volumeControl;

    public sound() {
        // Load the resource and check if it is found
        URL resource = getClass().getResource("/Tanks/duckSong.wav");
        soundURL[0] = resource;
        URL resource2 = getClass().getResource("/Tanks/boom.wav");
        soundURL[1] = resource2;
        URL resource3 = getClass().getResource("/Tanks/powerUp.wav");
        soundURL[2] = resource3;
        URL resource4 = getClass().getResource("/Tanks/error.wav");
        soundURL[3] = resource4;
    }

    public void setFile(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
            // Get the volume control after opening the clip
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (i == 1){
                decreaseVolume(10);
            }
            if (i == 3){
                volumeControl.setValue(volumeControl.getValue()+ 3);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    public void play() {
        if (clip != null) {
            clip.start();
            decreaseVolume(15);
        } else {
            System.out.println("Clip is null, cannot play sound.");
        }
    }

    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            System.out.println("Clip is null, cannot loop sound.");
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        } else {
            System.out.println("Clip is null, cannot stop sound.");
        }
    }

    public void decreaseVolume(float amount) {
        if (volumeControl != null) {
            // Reduce the volume by the specified amount (in decibels)
            float currentVolume = volumeControl.getValue();
            float newVolume = currentVolume - amount;
            // Ensure the new volume is within the allowed range
            if (newVolume < volumeControl.getMinimum()) {
                newVolume = volumeControl.getMinimum();
            }
            volumeControl.setValue(newVolume);
        }
    }
}
