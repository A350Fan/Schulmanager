package com.example.schulmanager.models;

import java.io.Serializable;

public class Fach implements Serializable {
    private long id;
    private String name;
    private int halbjahr;
    private boolean isAbiturfach;
    private double schriftlich;
    private double muendlich;

    public Fach(String name, int halbjahr, boolean isAbiturfach) {
        this.id = System.currentTimeMillis();
        this.name = name;
        this.halbjahr = halbjahr;
        this.isAbiturfach = isAbiturfach;
        this.schriftlich = 0.0;
        this.muendlich = 0.0;
    }

    // Getter und Setter
    public double getDurchschnitt() {
        return (schriftlich + muendlich) / 2;
    }

    public int getPunkte() {
        return Math.max(0, Math.min(15, (int)(15 - ((getDurchschnitt() - 1) * 3))));
    }

    // Weitere Getter/Setter...
}
