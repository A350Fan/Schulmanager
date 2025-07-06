// models/Note.java

package com.example.schulmanager.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public class Note implements Serializable {
    private double wert; // Der Punktwert der Note (0-15)
    private String typ; // z.B. "schriftlich", "muendlich", "sonstig"
    private long datum; // Optional: Zeitstempel der Notenerfassung
    private double gewichtung; // NEU: Gewichtung der Note (Standard: 1.0)

    // Hilfsmethode zur Validierung des Punktwerts
    private double validateWert(double value) {
        return Math.max(0.0, Math.min(15.0, value)); // Sicherstellen, dass der Wert zwischen 0 und 15 liegt
    }

    // Konstruktor (alt, setzt Gewichtung auf Standard 1.0)
    public Note(double wert, String typ) {
        this.wert = validateWert(wert); // Validierung hier anwenden
        this.typ = typ;
        this.datum = System.currentTimeMillis(); // Aktuelles Datum setzen
        this.gewichtung = 1.0; // Standardgewichtung
    }

    // Optional: Konstruktor mit Datum (alt, setzt Gewichtung auf Standard 1.0)
    public Note(double wert, String typ, long datum) {
        this.wert = validateWert(wert); // Validierung hier anwenden
        this.typ = typ;
        this.datum = datum;
        this.gewichtung = 1.0; // Standardgewichtung
    }

    // NEU: Konstruktor mit Gewichtung
    public Note(double wert, String typ, double gewichtung) {
        this.wert = validateWert(wert);
        this.typ = typ;
        this.datum = System.currentTimeMillis();
        // Hier könntest du auch eine Validierung für die Gewichtung hinzufügen, z.B. gewichtung > 0
        this.gewichtung = Math.max(0.0, gewichtung); // Gewichtung muss mindestens 0 sein
    }

    // NEU: Konstruktor mit Datum und Gewichtung
    public Note(double wert, String typ, long datum, double gewichtung) {
        this.wert = validateWert(wert);
        this.typ = typ;
        this.datum = datum;
        // Hier könntest du auch eine Validierung für die Gewichtung hinzufügen
        this.gewichtung = Math.max(0.0, gewichtung);
    }

    // Getter für gewichtung
    public double getGewichtung() {
        return gewichtung;
    }

    // Setter für gewichtung (falls du Noten bearbeiten möchtest)
    public void setGewichtung(double gewichtung) {
        this.gewichtung = Math.max(0.0, gewichtung);
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

    // Die drei sind für evtles Notenbearbeiten (not (yet) implemented!)
    // Wenn du sie später implementierst, denke daran, die Validierung auch hier anzuwenden.

    /**
     * public void setWert(double wert) {
     * this.wert = validateWert(wert); // Auch hier validieren, falls diese Methode aktiviert wird
     * }
     * <p>
     * public void setTyp(String typ) {
     * this.typ = typ;
     * }
     * <p>
     * public void setDatum(long datum) {
     * this.datum = datum;
     * }
     */

    @NonNull
    @Override
    public String toString() {
        // Optional: Die Gewichtung auch im toString anzeigen
        return String.format(Locale.GERMAN, "%.1f (%s) [x%.1f]", wert, typ, gewichtung);
    }
}