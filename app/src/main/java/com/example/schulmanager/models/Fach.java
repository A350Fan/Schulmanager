// models/Fach.java
package com.example.schulmanager.models;

import androidx.annotation.NonNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Repräsentiert ein Schulfach mit seinen Eigenschaften und den zugehörigen Noten.
 * Ein Fach hat einen Namen, gehört zu einem Halbjahr, kann ein Abiturfach sein
 * und verwaltet eine Liste von Noten. Es kann den Notendurchschnitt berechnen.
 */
public class Fach implements Serializable {
    private final long id;           // Eine eindeutige ID für das Fach (Zeitstempel der Erstellung).
    private String name;             // Der Name des Fachs (z.B. "Mathematik").
    private int halbjahr;            // Das Halbjahr, zu dem das Fach gehört (z.B. 1, 2, 3, 4).
    private boolean isAbiturfach;    // Flag, ob das Fach ein Abiturfach ist.
    private List<Note> noten;        // Eine Liste der Noten, die in diesem Fach erfasst wurden.

    /**
     * Konstruktor für ein neues Fach.
     * Initialisiert das Fach mit einem Namen, Halbjahr und Abiturfach-Status.
     * Weist eine eindeutige ID zu und initialisiert die Notenliste als leere ArrayList.
     *
     * @param name Der Name des Fachs.
     * @param halbjahr Das Halbjahr des Fachs.
     * @param isAbiturfach True, wenn es ein Abiturfach ist, sonst false.
     */
    public Fach(String name, int halbjahr, boolean isAbiturfach) {
        // Generiert eine eindeutige ID basierend auf dem aktuellen Zeitstempel in Millisekunden.
        this.id = System.currentTimeMillis();
        this.name = name;             // Setzt den Namen des Fachs.
        this.halbjahr = halbjahr;     // Setzt das Halbjahr.
        this.isAbiturfach = isAbiturfach; // Setzt den Abiturfach-Status.
        this.noten = new ArrayList<>(); // Initialisiert die Notenliste, um NullPointerExceptions zu vermeiden.
    }

    /**
     * Fügt eine neue Note zur Notenliste des Fachs hinzu.
     * Stellt sicher, dass die Notenliste initialisiert ist, bevor eine Note hinzugefügt wird.
     *
     * @param note Die hinzuzufügende Note.
     */
    public void addNote(Note note) {
        // Defensive Programmierung: Sollte durch den Konstruktor und getNoten() bereits initialisiert sein,
        // aber schadet nicht als zusätzliche Absicherung.
        if (this.noten == null) {
            this.noten = new ArrayList<>(); // Initialisiert die Liste, falls sie unerwartet null ist.
        }
        this.noten.add(note); // Fügt die Note zur Liste hinzu.
    }

    /**
     * Entfernt eine spezifische Note aus der Notenliste des Fachs.
     *
     * @param note Die zu entfernende Note.
     */
    public void removeNote(Note note) {
        // Prüft, ob die Notenliste existiert, bevor versucht wird, eine Note zu entfernen.
        if (this.noten != null) {
            this.noten.remove(note); // Entfernt die Note aus der Liste.
        }
    }

    /**
     * Gibt die Liste aller Noten dieses Fachs zurück.
     * Stellt sicher, dass die zurückgegebene Liste niemals null ist.
     *
     * @return Eine Liste von Note-Objekten (kann leer sein, aber niemals null).
     */
    public List<Note> getNoten() {
        // Stellt sicher, dass die Notenliste niemals null ist, bevor sie zurückgegeben wird.
        if (this.noten == null) {
            this.noten = new ArrayList<>(); // Initialisiert eine leere Liste, falls null.
        }
        return noten; // Gibt die Notenliste zurück.
    }

    /**
     * Setzt die Notenliste des Fachs auf eine neue Liste.
     * Dies ist nützlich für die Deserialisierung (z.B. mit Gson) oder wenn die Liste komplett ausgetauscht werden soll.
     *
     * @param noten Die neue Liste von Noten.
     */
    public void setNoten(List<Note> noten) {
        this.noten = noten; // Setzt die interne Notenliste auf die übergebene Liste.
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
        // Prüft, ob die Notenliste null oder leer ist. In diesem Fall ist der Durchschnitt 0.0.
        if (noten == null || noten.isEmpty()) {
            return 0.0;
        }

        // Listen zum Sammeln der Punktwerte für jeden Notentyp.
        List<Double> schriftlicheNoten = new ArrayList<>();
        List<Double> muendlicheNoten = new ArrayList<>();
        List<Double> sonstigeNoten = new ArrayList<>();

        // Iteriert über alle Noten des Fachs.
        for (Note note : noten) {
            // Holt den Punktwert der Note und stellt sicher, dass er im Bereich 0.0-15.0 liegt.
            // Die Validierung erfolgt bereits in der Note-Klasse, dies ist eine zusätzliche Absicherung.
            double punktWert = Math.max(0.0, Math.min(15.0, note.getWert()));

            // Ordnet die Note dem entsprechenden Typ zu.
            // Hier wird das NoteTyp-Enum verwendet, was robuster ist als String-Vergleiche.
            if (note.getNoteTypEnum() == NoteTyp.SCHRIFTLICH) {
                schriftlicheNoten.add(punktWert);
            } else if (note.getNoteTypEnum() == NoteTyp.MUENDLICH) {
                muendlicheNoten.add(punktWert);
            } else if (note.getNoteTypEnum() == NoteTyp.SONSTIG) {
                sonstigeNoten.add(punktWert);
            }
        }

        // Berechnet den Durchschnitt für jeden Notentyp.
        // Wenn eine Liste leer ist, wird der Durchschnitt 0.0 angenommen.
        double avgSchriftlich = schriftlicheNoten.isEmpty() ? 0.0 : calculateAverage(schriftlicheNoten);
        double avgMuendlich = muendlicheNoten.isEmpty() ? 0.0 : calculateAverage(muendlicheNoten);
        double avgSonstig = sonstigeNoten.isEmpty() ? 0.0 : calculateAverage(sonstigeNoten);

        // Feste Gewichtungen für die Notentypen.
        final int GEWICHTUNG_SCHRIFTLICH = 2; // Schriftliche Noten werden doppelt gewichtet.
        final int GEWICHTUNG_MUENDLICH = 1;   // Mündliche Noten werden einfach gewichtet.
        final int GEWICHTUNG_SONSTIG = 1;     // Sonstige Noten werden einfach gewichtet.

        double gesamtPunkteGewichtet = 0.0; // Summe der gewichteten Punkte.
        double gesamtGewichtung = 0.0;     // Summe der verwendeten Gewichtungen.

        // Addiert die gewichteten Punkte und die Gewichtungen zu den Gesamtwerten.
        // Die Gewichtungen fließen immer ein, auch wenn der Durchschnitt einer Kategorie 0.0 ist.
        gesamtPunkteGewichtet += (avgSchriftlich * GEWICHTUNG_SCHRIFTLICH);
        gesamtGewichtung += GEWICHTUNG_SCHRIFTLICH;

        gesamtPunkteGewichtet += (avgMuendlich * GEWICHTUNG_MUENDLICH);
        gesamtGewichtung += GEWICHTUNG_MUENDLICH;

        gesamtPunkteGewichtet += (avgSonstig * GEWICHTUNG_SONSTIG);
        gesamtGewichtung += GEWICHTUNG_SONSTIG;

        // Vermeidet Division durch Null, falls aus irgendeinem Grund die Gesamtgewichtung 0 sein sollte.
        // Dies sollte bei den aktuellen festen Gewichtungen nicht vorkommen.
        if (gesamtGewichtung == 0) {
            return 0.0;
        } else {
            // Berechnet den gewichteten Gesamtdurchschnitt.
            return gesamtPunkteGewichtet / gesamtGewichtung;
        }
    }

    /**
     * Hilfsmethode zur Berechnung des arithmetischen Durchschnitts einer Liste von Double-Werten.
     * @param list Die Liste der Double-Werte.
     * @return Der Durchschnitt der Werte in der Liste, oder 0.0, wenn die Liste leer ist.
     */
    private double calculateAverage(List<Double> list) {
        if (list.isEmpty()) {
            return 0.0; // Gibt 0.0 zurück, wenn die Liste leer ist, um Division durch Null zu vermeiden.
        }
        double sum = 0; // Initialisiert die Summe.
        // Addiert alle Werte in der Liste zur Summe.
        for (double d : list) {
            sum += d;
        }
        return sum / list.size(); // Berechnet den Durchschnitt.
    }

    // --- Getter und Setter für die Fach-Eigenschaften ---

    /**
     * Gibt die eindeutige ID des Fachs zurück.
     * @return Die ID des Fachs.
     */
    public long getId() { return id; }

    /**
     * Gibt den Namen des Fachs zurück.
     * @return Der Name des Fachs.
     */
    public String getName() { return name; }

    /**
     * Setzt den Namen des Fachs.
     * @param name Der neue Name des Fachs.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Gibt das Halbjahr zurück, zu dem das Fach gehört.
     * @return Die Halbjahresnummer.
     */
    public int getHalbjahr() { return halbjahr; }

    /**
     * Setzt das Halbjahr des Fachs.
     * @param halbjahr Die neue Halbjahresnummer.
     */
    public void setHalbjahr(int halbjahr) { this.halbjahr = halbjahr; }

    /**
     * Prüft, ob das Fach ein Abiturfach ist.
     * @return True, wenn es ein Abiturfach ist, sonst false.
     */
    public boolean isAbiturfach() { return isAbiturfach; }

    /**
     * Setzt den Abiturfach-Status des Fachs.
     * @param abitur True, wenn es ein Abiturfach sein soll, sonst false.
     */
    public void setAbiturfach(boolean abitur) { this.isAbiturfach = abitur; }

    /**
     * Rundet einen double-Wert nach spezifischen Regeln für Notenpunkte (0-15 Punkte):
     * - Werte unter 1.0 werden auf 0 gerundet (entspricht 0 Punkten).
     * - Der Nachkommaanteil von 0.0 bis 0.49 wird abgerundet.
     * - Der Nachkommaanteil von 0.50 bis 0.99 wird aufgerundet.
     *
     * @param value Der zu rundende double-Wert (z.B. 7.49, 7.51).
     * @return Der gerundete Integer-Wert (0-15 Punkte).
     */
    private int roundToNearestNotePoint(double value) {
        // Punkte unter 1.0 werden zu 0 gerundet, da 0 Punkte das Minimum sind.
        if (value < 1.0) {
            return 0;
        }

        int intPart = (int) value;       // Der Ganzzahlanteil des Wertes (z.B. 7 bei 7.49).
        double fractionalPart = value - intPart; // Der Nachkommaanteil (z.B. 0.49 bei 7.49).

        // Wenn der Nachkommaanteil 0.5 oder größer ist, wird aufgerundet.
        if (fractionalPart >= 0.5) {
            return intPart + 1; // Rundet auf die nächste Ganzzahl auf.
        } else {
            return intPart;     // Rundet auf die aktuelle Ganzzahl ab.
        }
    }

    /**
     * Gibt den gerundeten Durchschnitt der Noten in Punkten (0-15) zurück.
     * Die Rundung erfolgt nach der spezifischen Regel aus {@link #roundToNearestNotePoint(double)}.
     *
     * @return Der gerundete Durchschnittspunktwert als Integer.
     */
    public int getDurchschnittsPunkte() {
        double durchschnitt = getDurchschnitt(); // Holt den ungerundeten Durchschnitt.
        return roundToNearestNotePoint(durchschnitt); // Rundet den Durchschnitt und gibt ihn zurück.
    }

    /**
     * Gibt eine String-Repräsentation des Fachs zurück.
     * Nützlich für Debugging und einfache Anzeige in Listen.
     * @return Eine formatierte Zeichenkette des Fachs (z.B. "Mathematik (HJ 1) - Ø 10.5 Punkte").
     */
    @NonNull
    @Override
    public String toString() {
        // Formatiert den Fachnamen, das Halbjahr und den Durchschnitt (auf eine Nachkommastelle).
        return String.format(Locale.GERMAN, "%s (HJ %d) - Ø %.1f Punkte",
                name, halbjahr, getDurchschnitt());
    }
}