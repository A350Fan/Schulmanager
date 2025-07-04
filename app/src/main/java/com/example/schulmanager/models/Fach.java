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
        this.noten = new ArrayList<>();
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
            this.noten = new ArrayList<>();
        }
        return noten;
    }

    /**
     * Berechnet den gewichteten Durchschnitt aller Noten des Fachs.
     * Die Gewichtung ist: schriftlich 2x, mündlich 1x, sonstig 1x.
     * Wenn eine Notenkategorie keine Noten enthält, wird sie mit 0 Punkten
     * in die gewichtete Gesamtberechnung einbezogen.
     *
     * @return Der ungerundete gewichtete Durchschnitt in Punkten (0.0-15.0).
     */
    public double getDurchschnitt() {
        if (noten == null || noten.isEmpty()) {
            return 0.0;
        }

        List<Double> schriftlicheNoten = new ArrayList<>();
        List<Double> muendlicheNoten = new ArrayList<>();
        List<Double> sonstigeNoten = new ArrayList<>();

        for (Note note : noten) {
            double punktWert = Math.max(0.0, Math.min(15.0, note.getWert()));

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

        // Gewichtung anwenden: schriftlich 2x, mündlich 1x, sonstig 1x
        // Die Gesamtgewichtung ist 2 + 1 + 1 = 4.
        // Falls eine Kategorie keine Noten hat, geht ihr Durchschnitt als 0.0 in die Summe ein.
        double gesamtPunkteGewichtet = (avgSchriftlich * 2) + (avgMuendlich * 1) + (avgSonstig * 1);
        double gesamtGewichtung = 2 + 1 + 1;

        // Der Gesamtdurchschnitt ist die gewichtete Summe geteilt durch die Summe der Gewichtungen.
        return gesamtPunkteGewichtet / gesamtGewichtung;
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
        if (value < 1.0) {
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