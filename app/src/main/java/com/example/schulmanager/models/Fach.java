// models/Fach.java
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
    private List<Note> noten;

    public Fach(String name, int halbjahr, boolean isAbiturfach) {
        this.id = System.currentTimeMillis();
        this.name = name;
        this.halbjahr = halbjahr;
        this.isAbiturfach = isAbiturfach;
        this.noten = new ArrayList<>(); // Sicherstellen, dass noten immer initialisiert ist
    }

    public void addNote(Note note) {
        if (this.noten == null) {
            this.noten = new ArrayList<>();
        }
        this.noten.add(note);
    }

    public void removeNote(Note note) {
        if (this.noten != null) {
            this.noten.remove(note);
        }
    }

    public List<Note> getNoten() {
        if (this.noten == null) {
            this.noten = new ArrayList<>(); // Sicherstellen, dass noten niemals null zurückgibt
        }
        return noten;
    }

    // NEU HINZUGEFÜGT: Setter für die Notenliste
    public void setNoten(List<Note> noten) {
        this.noten = noten;
    }

    /**
     * Berechnet den gewichteten Durchschnitt aller Noten des Fachs.
     * Die Gewichtung ist: schriftlich 2x, mündlich 1x, sonstig 1x.
     * Wenn eine Notenkategorie keine Noten enthält, wird sie mit 0 Punkten
     * in die gewichtete Gesamtberechnung einbezogen, aber ihre volle Gewichtung
     * fließt in den Teiler ein.
     *
     * @return Der ungerundete gewichtete Durchschnitt in Punkten (0.0-15.0).
     */
    public double getDurchschnitt() {
        // Sicherstellen, dass noten nicht null ist, bevor darauf zugegriffen wird
        if (noten == null || noten.isEmpty()) {
            return 0.0;
        }

        List<Double> schriftlicheNoten = new ArrayList<>();
        List<Double> muendlicheNoten = new ArrayList<>();
        List<Double> sonstigeNoten = new ArrayList<>();

        for (Note note : noten) {
            double punktWert = Math.max(0.0, Math.min(15.0, note.getWert())); // Sicherstellen, dass Werte 0-15 sind

            if ("schriftlich".equalsIgnoreCase(note.getTyp())) {
                schriftlicheNoten.add(punktWert);
            } else if ("muendlich".equalsIgnoreCase(note.getTyp())) {
                muendlicheNoten.add(punktWert);
            } else if ("sonstig".equalsIgnoreCase(note.getTyp())) {
                sonstigeNoten.add(punktWert);
            }
        }

        // Berechne den Durchschnitt für jeden Notentyp
        double avgSchriftlich = schriftlicheNoten.isEmpty() ? 0.0 : calculateAverage(schriftlicheNoten);
        double avgMuendlich = muendlicheNoten.isEmpty() ? 0.0 : calculateAverage(muendlicheNoten);
        double avgSonstig = sonstigeNoten.isEmpty() ? 0.0 : calculateAverage(sonstigeNoten);

        // KORRIGIERTE LOGIK: Feste Gewichtungen wie in den Kommentaren beschrieben
        final int GEWICHTUNG_SCHRIFTLICH = 2; // Soll 2x gewichtet werden
        final int GEWICHTUNG_MUENDLICH = 1;
        final int GEWICHTUNG_SONSTIG = 1;

        double gesamtPunkteGewichtet = 0.0;
        double gesamtGewichtung = 0.0;

        // Punkte und Gewichtungen immer hinzufügen, auch wenn die Liste leer ist.
        // Wenn eine Notenart keine Noten enthält, ist ihr avgX = 0.0, was korrekt ist.
        gesamtPunkteGewichtet += (avgSchriftlich * GEWICHTUNG_SCHRIFTLICH);
        gesamtGewichtung += GEWICHTUNG_SCHRIFTLICH;

        gesamtPunkteGewichtet += (avgMuendlich * GEWICHTUNG_MUENDLICH);
        gesamtGewichtung += GEWICHTUNG_MUENDLICH;

        gesamtPunkteGewichtet += (avgSonstig * GEWICHTUNG_SONSTIG);
        gesamtGewichtung += GEWICHTUNG_SONSTIG;


        // Falls die Gesamtgewichtung 0 ist (sollte bei festen Gewichtungen nicht passieren,
        // es sei denn, alle Gewichtungen sind 0, was hier nicht der Fall ist).
        if (gesamtGewichtung == 0) {
            return 0.0;
        } else {
            return gesamtPunkteGewichtet / gesamtGewichtung;
        }
    }

    // Hilfsmethode zur Berechnung des Durchschnitts einer Liste von Doubles
    private double calculateAverage(List<Double> list) {
        if (list.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (double d : list) {
            sum += d;
        }
        return sum / list.size();
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHalbjahr() { return halbjahr; }
    public void setHalbjahr(int halbjahr) { this.halbjahr = halbjahr; }
    public boolean isAbiturfach() { return isAbiturfach; }
    public void setAbiturfach(boolean abitur) { this.isAbiturfach = abitur; }

    /**
     * Rundet einen double-Wert nach spezifischen Regeln für Notenpunkte:
     * - Werte unter 1.0 werden auf 0 gerundet.
     * - Der Nachkommaanteil von 0.0 bis 0.49 wird abgerundet.
     * - Der Nachkommaanteil von 0.50 bis 0.99 wird aufgerundet.
     *
     * @param value Der zu rundende double-Wert (z.B. 7.49, 7.51).
     * @return Der gerundete Integer-Wert (0-15 Punkte).
     */
    private int roundToNearestNotePoint(double value) {
        if (value < 1.0) { // Punkte unter 1.0 werden zu 0 gerundet
            return 0;
        }

        int intPart = (int) value;
        double fractionalPart = value - intPart;

        if (fractionalPart >= 0.5) {
            return intPart + 1;
        } else {
            return intPart;
        }
    }

    /**
     * Gibt den gerundeten Durchschnitt der Noten in Punkten (0-15) zurück.
     * Die Rundung erfolgt nach der spezifischen Regel aus {@link #roundToNearestNotePoint(double)}.
     *
     * @return Der gerundete Durchschnittspunktwert.
     */
    public int getDurchschnittsPunkte() {
        double durchschnitt = getDurchschnitt();
        return roundToNearestNotePoint(durchschnitt);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.GERMAN, "%s (HJ %d) - Ø %.1f Punkte",
                name, halbjahr, getDurchschnitt());
    }

    // Die Methode getFormattedPunkte() wurde entfernt, da sie nicht mehr verwendet wird.
}