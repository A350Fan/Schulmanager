// models/Fach.java (Angepasst)
package com.example.schulmanager.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Fach implements Serializable {
    private final long id;
    private String name;
    private int halbjahr;
    private boolean isAbiturfach;
    // Alte Felder entfernen:
    // private double schriftlich;
    // private double muendlich;

    // NEU: Listen für Noten
    private List<Note> noten;


    public Fach(String name, int halbjahr, boolean isAbiturfach) {
        this.id = System.currentTimeMillis();
        this.name = name;
        this.halbjahr = halbjahr;
        this.isAbiturfach = isAbiturfach;
        this.noten = new ArrayList<>(); // NEU: Initialisiere die Notenliste
    }

    // Getter und Setter

    // NEU: Methode zum Hinzufügen einer Note
    public void addNote(Note note) {
        if (this.noten == null) { // Falls deserialisiert und Liste null ist
            this.noten = new ArrayList<>();
        }
        this.noten.add(note);
    }

    // NEU: Methode zum Entfernen einer Note
    public void removeNote(Note note) {
        if (this.noten != null) {
            this.noten.remove(note);
        }
    }

    // NEU: Getter für die Notenliste
    public List<Note> getNoten() {
        if (this.noten == null) { // Sicherstellen, dass die Liste nie null ist
            this.noten = new ArrayList<>();
        }
        return noten;
    }

    // NEU: Berechnet den gewichteten Durchschnitt aller Noten
    // Annahme: Schriftliche Noten zählen 2x, mündliche 1x
    // Oder: Wir können es auch einfacher halten und alle gleich gewichten,
    // es sei denn, du hast spezifische Gewichtungen für verschiedene Notentypen.
    // Für den Anfang machen wir es einfach und gewichten schriftlich und mündlich
    // wie bisher (oder alle Noten gleich, je nach dem wie wir das im Dialog erfassen)
    public double getDurchschnitt() {
        if (noten == null || noten.isEmpty()) {
            return 0.0;
        }

        //double gesamtPunkte = 0.0; dead code
        int anzahlSchriftlich = 0;
        int anzahlMuendlich = 0;
        double summeSchriftlich = 0.0;
        double summeMuendlich = 0.0;

        for (Note note : noten) {
            // Punkte validieren (obwohl schon beim Hinzufügen passieren sollte)
            double punktWert = Math.max(0.0, Math.min(15.0, note.getWert()));

            if ("schriftlich".equalsIgnoreCase(note.getTyp())) {
                summeSchriftlich += punktWert;
                anzahlSchriftlich++;
            } else if ("muendlich".equalsIgnoreCase(note.getTyp())) {
                summeMuendlich += punktWert;
                anzahlMuendlich++;
            }
            // Falls es andere Notentypen gibt, müssen wir deren Gewichtung festlegen
        }

        // Beispiel für Gewichtung (schriftlich 2x, mündlich 1x)
        double gewichteterDurchschnitt = 0.0;
        if (anzahlSchriftlich > 0 && anzahlMuendlich > 0) {
            gewichteterDurchschnitt = ( (summeSchriftlich / anzahlSchriftlich) * 2 + (summeMuendlich / anzahlMuendlich) ) / 3;
        } else if (anzahlSchriftlich > 0) {
            gewichteterDurchschnitt = summeSchriftlich / anzahlSchriftlich;
        } else if (anzahlMuendlich > 0) {
            gewichteterDurchschnitt = summeMuendlich / anzahlMuendlich;
        }
        // Wenn keine schriftlichen oder mündlichen Noten, aber andere Typen,
        // müsste man hier eine Logik für "sonstige" Noten einfügen
        // Für den Moment gehen wir davon aus, dass wir schriftlich/mündlich unterscheiden

        return gewichteterDurchschnitt;
    }


    // Wichtige Änderung: getPunkte() sollte jetzt den Durchschnitt der Noten zurückgeben
    // und nicht mehr versuchen, von einer Note in Punkte umzurechnen, da die Noten ja schon Punkte sind.
    // Ich werde stattdessen getDurchschnitt() verwenden, welches Punkte zurückgibt
    @Deprecated // Diese Methode ist jetzt nicht mehr ganz korrekt im Kontext der Notenlisten
    public int getPunkte() {
        // Dies war die Umrechnung von einer 1-6 Skala in 0-15 Punkte.
        // Da getDurchschnitt() jetzt den Durchschnitt der 0-15 Punkte liefert,
        // sollte diese Methode eigentlich nicht mehr benötigt werden oder anders benannt werden.
        // Für die Kompatibilität mit dem Adapter, lassen wir es vorerst auf getDurchschnittsPunkte() verweisen
        return getDurchschnittsPunkte();
    }


    public long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHalbjahr() { return halbjahr; }
    public void setHalbjahr(int halbjahr) { this.halbjahr = halbjahr; }
    public boolean isAbiturfach() { return isAbiturfach; }
    public void setAbiturfach(boolean abitur) { this.isAbiturfach = abitur; }

    // Diese Setter für einzelne Schriftlich/Mündlich-Punkte sind jetzt überflüssig
    // und sollten entfernt oder angepasst werden, da wir jetzt Listen haben.
    // public void setSchriftlich(double punkte) { this.schriftlich = punkte; }
    // public void setMuendlich(double punkte) { this.muendlich = punkte; }

    // NEU: Dies ist die Methode, die den Durchschnitt der Noten in Punkten (0-15) zurückgibt
    public int getDurchschnittsPunkte() {
        // Hier rufen wir die neue getDurchschnitt() Methode auf, die den gewichteten
        // Durchschnitt der Noten (Punkte 0-15) berechnet.
        return (int) Math.round(getDurchschnitt());
    }

    // toString() aktualisieren (optional, aber gut für Debugging)
    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.GERMAN, "%s (HJ %d) - Ø %.1f Punkte",
                name, halbjahr, getDurchschnitt()); // Zeigt den genauen Durchschnitt an
    }
//    public String getFormattedPunkte() {
//        // Zeigt den gerundeten Durchschnitt als String
//        return String.valueOf(getDurchschnittsPunkte());
//    } dead method

    // --- Anpassung der Konstruktoren, falls Noten direkt übergeben werden sollen ---
    // (Für unser Dialog-Flow brauchen wir das im Moment nicht direkt,
    // da Noten später hinzugefügt werden)
}