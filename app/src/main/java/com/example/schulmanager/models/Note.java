// models/Note.java
package com.example.schulmanager.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public class Note implements Serializable {
    private double wert; // Der Punktwert der Note (0-15)
    private String typ;  // z.B. "schriftlich", "muendlich", "sonstig"
    private long datum;  // Optional: Zeitstempel der Notenerfassung

    // Hilfsmethode zur Validierung des Punktwerts
    private double validateWert(double value) {
        return Math.max(0.0, Math.min(15.0, value)); // Sicherstellen, dass der Wert zwischen 0 und 15 liegt
    }

    // Konstruktor
    public Note(double wert, String typ) {
        this.wert = validateWert(wert); // Validierung hier anwenden
        this.typ = typ;
        this.datum = System.currentTimeMillis(); // Aktuelles Datum setzen
    }

    // Optional: Konstruktor mit Datum
    public Note(double wert, String typ, long datum) {
        this.wert = validateWert(wert); // Validierung hier anwenden
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

    // Die drei sind für evtles Notenbearbeiten (not (yet) implemented!)
    // Wenn du sie später implementierst, denke daran, die Validierung auch hier anzuwenden.
    /**
     public void setWert(double wert) {
     this.wert = validateWert(wert); // Auch hier validieren, falls diese Methode aktiviert wird
     }

     public void setTyp(String typ) {
     this.typ = typ;
     }

     public void setDatum(long datum) {
     this.datum = datum;
     }
     */

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.GERMAN, "%.1f (%s)", wert, typ);
    }
}