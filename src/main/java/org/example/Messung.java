package org.example;

import java.util.ArrayList;

public class Messung {
    private long startTime;
    private long endTime;
    public double timeInMs;
    public double timeInS;

    public void zeitBerechnen(int generation){
        timeInMs = (endTime - startTime) / 1_000_000;
        timeInS = timeInMs / 1000;
        System.out.printf("Erzeugte Generationen: %s\nDauer: %sms (%s Sekunden)\n", generation, timeInMs, timeInS);
    }

    // Getters and Setters
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public double getTimeInMs() {
        return this.timeInMs;
    }

    public double getTimeInS() {
        return this.timeInS;
    }
}
