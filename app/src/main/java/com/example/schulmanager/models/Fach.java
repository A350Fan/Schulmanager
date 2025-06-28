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

    // Getter und Setter - angepasst für Punkte
    public double getSchriftlich() { return schriftlich; }
    public void setSchriftlich(int punkte) {
        this.schriftlich = Math.max(0, Math.min(15, punkte));
    }

    public double getMuendlich() { return muendlich; }
    public void setMuendlich(int punkte) {
        this.muendlich = Math.max(0, Math.min(15, punkte));
    }

    public int getPunkte() {
        return Math.max(0, Math.min(15, (int)(15 - ((getDurchschnitt() - 1) * 3))));
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHalbjahr() { return halbjahr; }
    public void setHalbjahr(int halbjahr) { this.halbjahr = halbjahr; }
    public boolean isAbiturfach() { return isAbiturfach; }
    public void setAbiturfach(boolean abitur) { this.isAbiturfach = abitur; }

    public void setSchriftlich(double punkte) { this.schriftlich = punkte; }

    public void setMuendlich(double punkte) { this.muendlich = punkte; }
    // Weitere Getter/Setter...
}
