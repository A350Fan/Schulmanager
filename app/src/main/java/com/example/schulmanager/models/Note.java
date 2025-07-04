// models/Note.java
package com.example.schulmanager.models;

import java.io.Serializable;

public class Note implements Serializable {
    private double wert; // Der Punktwert der Note (0-15)
    private String typ;  // z.B. "schriftlich", "muendlich", "sonstig"
    private long datum;  // Optional: Zeitstempel der Notenerfassung

    // Konstruktor
    public Note(double wert, String typ) {
        this.wert = wert;
        this.typ = typ;
        this.datum = System.currentTimeMillis(); // Aktuelles Datum setzen
    }

    // Optional: Konstruktor mit Datum
    public Note(double wert, String typ, long datum) {
        this.wert = wert;
        this.typ = typ;
        this.datum = datum;
    }

    // Getter
    public double getWert() {
        return wert;
    }

    public String getTyp() {
        return typ;
    }

    public long getDatum() {
        return datum;
    }

    // Setter (falls Noten bearbeitet werden sollen)
    public void setWert(double wert) {
        this.wert = wert;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public void setDatum(long datum) {
        this.datum = datum;
    }

    @Override
    public String toString() {
        return String.format("%.1f (%s)", wert, typ);
    }
}