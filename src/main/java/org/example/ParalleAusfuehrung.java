package org.example;

import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class ParalleAusfuehrung extends Task {
    public int nrOfThreads;
    public double reproduktion;
    public double selektion;
    public double movement;
    public Integer[][] spielfeld;
    private Boolean fiveSpecies;
    public ExecutorService executor;

    public ParalleAusfuehrung(Integer[][] spielfeld, Boolean fiveSpecies, int nrOfThreads, double selektion, double reproduktion, double movement, ExecutorService executor){
        this.spielfeld = spielfeld;
        this.fiveSpecies = fiveSpecies;
        this.reproduktion = reproduktion;
        this.selektion = selektion;
        this.movement = movement;
        this.nrOfThreads = nrOfThreads;
        this.executor = executor;
    }

    @Override
    protected String call() {
        updateMessage("Start");
        starteBerechnung();
        updateMessage("End");
        return "succesfull";
    }

    private void starteBerechnung() {
        //spielfeld[1].length - 1 -> weil der rand.nextInt(spielfeld[1].length) exklusiv spielfeld[1].length liefert
        int rest_pixel = (spielfeld[1].length - 1) % nrOfThreads;
        int bereiche_groesse = (spielfeld[1].length - 1) / nrOfThreads;
        int[] bereiche = new int[nrOfThreads + 1];
        int[] kritischeRegionen = new int[nrOfThreads + 1];
        ReentrantLock[] locks = new ReentrantLock[nrOfThreads + 1];
        bereiche[0] = 0;
        kritischeRegionen[0] = 0;
        locks[0] = new ReentrantLock();


        for (int i = 1; i <= nrOfThreads; i++) {
            if (i == 1) {
                bereiche[i] = bereiche_groesse + rest_pixel;
            }
            else {
                bereiche[i] = bereiche[i - 1] + bereiche_groesse;
            }

            kritischeRegionen[i] = bereiche[i];
            locks[i] = new ReentrantLock();
        }

        List<Future<String>> list = new ArrayList<>();
        for(int i=0; i< nrOfThreads; i++){
            Future<String> future = executor.submit(new Interaktion(spielfeld, movement, selektion, reproduktion, fiveSpecies, bereiche[i], bereiche[i + 1], kritischeRegionen, locks));
            list.add(future);
        }
        for(Future<String> fut : list){
            try {
                fut.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
