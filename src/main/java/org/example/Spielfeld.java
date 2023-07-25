package org.example;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Spielfeld {

    private Integer[][] spielfeld;
    private final Random rand = new Random();

    public Integer[][] getSpielfeld() {
        return spielfeld;
    }

    public int getSpielfeldWidth() {
        return this.spielfeld[0].length;
    }

    public int getSpielfeldHight() {
        return this.spielfeld[1].length;
    }

    public void initialiseFields(int width, int height, Boolean fiveSpecies) {
        this.spielfeld = new Integer[width][height];
        for (int x = 0; x < width; x++) {
           for (int y = 0; y < height; y++) {
               if(!fiveSpecies) {
                   spielfeld[x][y] = rand.nextInt(4);
               } else {
                   spielfeld[x][y] = rand.nextInt(6);
               }
           }
        }
    }

    public void initialiseFieldsStatic(int width, int height, Boolean fiveSpecies) {
        this.spielfeld = new Integer[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(!fiveSpecies) {
                    spielfeld[x][y] = x % 4;
                } else {
                    spielfeld[x][y] = x % 6;
                }
            }
        }
    }
}
