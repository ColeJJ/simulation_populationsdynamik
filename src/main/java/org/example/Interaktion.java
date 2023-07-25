package org.example;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

public class Interaktion implements Callable<String> {

    private int bereichAnfang;
    private int bereichEnde;
    private double probReproduction;
    private double probSelection;
    private double probMovement;
    private Integer[][] spielfeld;
    private Boolean fiveSpecies;
    private int[] kritischeRegionen;
    private ReentrantLock[] locks;

    public Interaktion(Integer[][] spielfeld, double probMovement, double probSelection, double probReproduction, Boolean fiveSpecies, int bereichAnfang, int bereichEnde, int[] kritischeRegionen, ReentrantLock[] locks){
        this.spielfeld = spielfeld;
        this.probMovement = probMovement;
        this.probSelection = probSelection;
        this.probReproduction = probReproduction;
        this.fiveSpecies = fiveSpecies;
        this.bereichAnfang = bereichAnfang;
        this.bereichEnde = bereichEnde;
        this.locks = locks;
        this.kritischeRegionen = kritischeRegionen;
    }

    @Override
    public String call() throws Exception {
        run();
        return Thread.currentThread().getName();
    }

    public void run() {
        int x;
        int y;
        Random rand = new Random();
        boolean isAtomarArea = false;
        int lockIndex = 0;

        for(int i = 0; i<(bereichEnde - bereichAnfang) * spielfeld[1].length; i++) {
            x = rand.nextInt(bereichEnde - bereichAnfang) + bereichAnfang;
            y = rand.nextInt(spielfeld[1].length);

            int[] nachbarPixel = getRandomNachbarPixel(x,y);

            int nachbarX = nachbarPixel[0];
            int nachbarY = nachbarPixel[1];

            if(kritischeRegionen.length == 2) {
                isAtomarArea = false;
            } else {
                for (int a = 0; a < kritischeRegionen.length; a++) {
                    if (y == kritischeRegionen[a] || nachbarY == kritischeRegionen[a]) {
                        lockIndex = a;
                        isAtomarArea = true;
                    }
                }
            }

            if (isAtomarArea) {
                // check ob bereits gelocked -> warten
                while(!locks[lockIndex].tryLock()) {}
                // lock -> action -> unlock + notify
                // locks[lockIndex].lock();
                try {
                    this.interaction(x, y, nachbarX, nachbarY);
                } finally {
                    isAtomarArea = false;
                    locks[lockIndex].unlock();
                }
            } else {
                this.interaction(x, y, nachbarX, nachbarY);
            } 
        }
    }

    private void interaction(int x, int y, int nachbarX, int nachbarY) {
        double randomNum = ThreadLocalRandom.current().nextDouble();
        if (randomNum < (probReproduction / (probReproduction + probSelection + probMovement))) {
            reproduction(x, y, nachbarX, nachbarY);
        } else if (randomNum < (probReproduction + probSelection) / (probReproduction + probSelection + probMovement)) {
            if(!fiveSpecies) {
                selection(x, y, nachbarX, nachbarY);
            } else {
                selectionFiveSpecies(x, y, nachbarX, nachbarY);
            }
        } else {
            movement(x, y, nachbarX, nachbarY);
        }
    }

    private void reproduction(int x, int y, int nachbarX, int nachbarY) {
        if(spielfeld[x][y] != 0 && spielfeld[nachbarX][nachbarY] == 0) {
            spielfeld[nachbarX][nachbarY] = spielfeld[x][y];
        }
        if(spielfeld[nachbarX][nachbarY] != 0 && spielfeld[x][y] == 0) {
            spielfeld[x][y] = spielfeld[nachbarX][nachbarY];
        }
    }

    private void selection(int x, int y, int nachbarX, int nachbarY) {
        if(spielfeld[nachbarX][nachbarY] != 0 && spielfeld[x][y] != 0) {
            //RandomPixel -> Blau, NachbarPixel -> Gelb
            if(spielfeld[x][y] == 1 && spielfeld[nachbarX][nachbarY] == 3) {
                spielfeld[x][y] = 0;
            }
            //RandomPixel -> Blau, NachbarPixel -> Rot
            if(spielfeld[x][y] == 1 && spielfeld[nachbarX][nachbarY] == 2) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
            //RandomPixel -> Gelb, NachbarPixel -> Blau
            if(spielfeld[x][y] == 3 && spielfeld[nachbarX][nachbarY] == 1) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
            //RandomPixel -> Gelb, NachbarPixel -> Rot
            if(spielfeld[x][y] == 3 && spielfeld[nachbarX][nachbarY] == 2) {
                spielfeld[x][y] = 0;
            }
            //RandomPixel -> Rot, NachbarPixel -> Blau
            if(spielfeld[x][y] == 2 && spielfeld[nachbarX][nachbarY] == 1) {
                spielfeld[x][y] = 0;
            }
            //RandomPixel -> Rot, NachbarPixel -> Gelb
            if(spielfeld[x][y] == 2 && spielfeld[nachbarX][nachbarY] == 3) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
        }
    }

    public void selectionFiveSpecies(int x, int y, int nachbarX, int nachbarY) {
        if(spielfeld[nachbarX][nachbarY] != 0 && spielfeld[x][y] != 0) {
            // Gelb schlägt Blau
            if(spielfeld[x][y] == 3 && spielfeld[nachbarX][nachbarY] == 1) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
            if(spielfeld[x][y] == 1 && spielfeld[nachbarX][nachbarY] == 3) {
                spielfeld[x][y] = 0;
            }

            // Gelb schlägt Orange
            if(spielfeld[x][y] == 3 && spielfeld[nachbarX][nachbarY] == 4) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
            if(spielfeld[x][y] == 4 && spielfeld[nachbarX][nachbarY] == 3) {
                spielfeld[x][y] = 0;
            }

            // Blau schlägt Orange
            if(spielfeld[x][y] == 1 && spielfeld[nachbarX][nachbarY] == 4) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
            if(spielfeld[x][y] == 4 && spielfeld[nachbarX][nachbarY] == 1) {
                spielfeld[x][y] = 0;
            }

            // Blau schlägt Grün
            if(spielfeld[x][y] == 1 && spielfeld[nachbarX][nachbarY] == 5) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
            if(spielfeld[x][y] == 5 && spielfeld[nachbarX][nachbarY] == 1) {
                spielfeld[x][y] = 0;
            }

            // Orange schlägt Grün
            if(spielfeld[x][y] == 4 && spielfeld[nachbarX][nachbarY] == 5) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
            if(spielfeld[x][y] == 5 && spielfeld[nachbarX][nachbarY] == 4) {
                spielfeld[x][y] = 0;
            }

            // Orange schlägt Rot
            if(spielfeld[x][y] == 4 && spielfeld[nachbarX][nachbarY] == 2) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
            if(spielfeld[x][y] == 2 && spielfeld[nachbarX][nachbarY] == 4) {
                spielfeld[x][y] = 0;
            }

            // Grün schlägt Rot
            if(spielfeld[x][y] == 5 && spielfeld[nachbarX][nachbarY] == 2) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
            if(spielfeld[x][y] == 2 && spielfeld[nachbarX][nachbarY] == 5) {
                spielfeld[x][y] = 0;
            }

            // Grün schlägt Gelb
            if(spielfeld[x][y] == 5 && spielfeld[nachbarX][nachbarY] == 3) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
            if(spielfeld[x][y] == 3 && spielfeld[nachbarX][nachbarY] == 5) {
                spielfeld[x][y] = 0;
            }

            // Rot schlägt Gelb
            if(spielfeld[x][y] == 2 && spielfeld[nachbarX][nachbarY] == 3) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
            if(spielfeld[x][y] == 3 && spielfeld[nachbarX][nachbarY] == 2) {
                spielfeld[x][y] = 0;
            }

            // Rot schlägt Blau
            if(spielfeld[x][y] == 2 && spielfeld[nachbarX][nachbarY] == 1) {
                spielfeld[nachbarX][nachbarY] = 0;
            }
            if(spielfeld[x][y] == 1 && spielfeld[nachbarX][nachbarY] == 2) {
                spielfeld[x][y] = 0;
            }
        }
    }

    private void movement(int x, int y, int nachbarX, int nachbarY) {
        int randomPixelZustand = spielfeld[x][y];
        int nachbarPixelZustand = spielfeld[nachbarX][nachbarY];
        spielfeld[x][y] = nachbarPixelZustand;
        spielfeld[nachbarX][nachbarY] = randomPixelZustand;
    }

    public int[] getRandomNachbarPixel(int x, int y) {
        Random random = new Random();
        int nachbarIndex = random.nextInt(4);
        int nachbarX = x;
        int nachbarY = y;
        int[] nachbarPixel = new int[2];
        switch (nachbarIndex) {
            case 0: // Links
                nachbarX = x != 0 ? x - 1 : spielfeld[0].length - 1;
                break;
            case 1: // Rechts
                nachbarX = x != spielfeld[0].length - 1 ? x + 1 : 0;
                break;
            case 2: // Oben
                nachbarY = y != 0 ? y - 1 : spielfeld[1].length - 1;
                break;
            case 3: // Unten
                nachbarY = y != spielfeld[1].length - 1 ? y + 1 : 0;
                break;
        }
        nachbarPixel[0] = nachbarX;
        nachbarPixel[1] = nachbarY;
        return nachbarPixel;
    }
}
