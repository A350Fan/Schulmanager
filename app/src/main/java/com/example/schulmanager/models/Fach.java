package com.example.schulmanager.models;

import androidx.annotation.NonNull; // Wird für @NonNull Annotationen verwendet
import java.io.Serializable;     // Ermöglicht das Serialisieren von Objekten für die Speicherung
import java.util.ArrayList;      // Für dynamische Listen von Noten
import java.util.List;           // Interface für Listen
import java.util.Locale;         // Für sprachspezifische Formatierungen (z.B. Komma statt Punkt bei Dezimalzahlen)

/**
 * Repräsentiert ein Schulfach mit seinen Eigenschaften wie Name, Halbjahr,
 * Abiturfachstatus und einer Liste der zugehörigen Noten.
 * Implementiert Serializable, um Objekte persistent speichern zu können.
 */
public class Fach implements Serializable {

    // --- Instanzvariablen ---
    private final long id;          // Eindeutige ID für das Fach, generiert beim Erstellen
    private String name;            // Name des Faches (z.B. "Mathematik", "Deutsch")
    private int halbjahr;           // Das Halbjahr, in dem das Fach belegt wird (z.B. 1, 2, 3, 4)
    private boolean isAbiturfach;   // Flag, ob das Fach ein Abiturfach ist
    private List<Note> noten;       // Liste der Noten, die zu diesem Fach gehören

    /**
     * Konstruktor zum Erstellen eines neuen Fach-Objekts.
     *
     * @param name Der Name des Faches.
     * @param halbjahr Das Halbjahr, in dem das Fach belegt wird.
     * @param isAbiturfach Gibt an, ob es sich um ein Abiturfach handelt.
     */
    public Fach(String name, int halbjahr, boolean isAbiturfach) {
        // Generiert eine eindeutige ID basierend auf dem aktuellen Zeitstempel
        this.id = System.currentTimeMillis();
        this.name = name;
        this.halbjahr = halbjahr;
        this.isAbiturfach = isAbiturfach;
        // Initialisiert die Notenliste, um NullPointerExceptions zu vermeiden
        this.noten = new ArrayList<>();
    }

    // --- Methoden zum Hinzufügen, Entfernen und Abrufen von Noten ---

    /**
     * Fügt eine Note zur Liste der Noten dieses Faches hinzu.
     *
     * @param note Die hinzuzufügende Note.
     */
    public void addNote(Note note) {
        // Stellt sicher, dass die Notenliste initialisiert ist, falls sie aus irgendeinem Grund null sein sollte
        if (this.noten == null) {
            this.noten = new ArrayList<>();
        }
        this.noten.add(note);
    }

    /**
     * Entfernt eine spezifische Note aus der Liste der Noten dieses Faches.
     *
     * @param note Die zu entfernende Note.
     */
    public void removeNote(Note note) {
        // Prüft, ob die Notenliste existiert, bevor versucht wird, eine Note zu entfernen
        if (this.noten != null) {
            this.noten.remove(note);
        }
    }

    /**
     * Gibt die Liste aller Noten zurück, die zu diesem Fach gehören.
     *
     * @return Eine Liste von Note-Objekten. Gibt eine leere Liste zurück, wenn keine Noten vorhanden sind oder die Liste nicht initialisiert war.
     */
    public List<Note> getNoten() {
        // Stellt sicher, dass die Liste niemals null zurückgibt, sondern eine leere Liste
        if (this.noten == null) {
            this.noten = new ArrayList<>();
        }
        return noten;
    }

    /**
     * Setzt die Liste der Noten für dieses Fach.
     * Nützlich, wenn Noten von außen geladen oder aktualisiert werden.
     *
     * @param noten Die neue Liste der Noten.
     */
    public void setNoten(List<Note> noten) {
        this.noten = noten;
    }

    /**
     * Berechnet den gewichteten Durchschnitt aller Noten des Faches.
     * Die Gewichtung ist: schriftlich 2x, mündlich 1x.
     * Noten vom Typ "sonstig" (oder unbekannte Typen) werden bei der Berechnung komplett ignoriert.
     * Nur Notenkategorien mit tatsächlich vorhandenen Noten tragen zu den Gesamtpunkten und der Gesamtgewichtung bei.
     *
     * @return Der ungerundete gewichtete Durchschnitt in Punkten (0.0-15.0).
     */
    public double getDurchschnitt() {
        // Wenn keine Noten vorhanden sind, ist der Durchschnitt 0.0
        if (noten == null || noten.isEmpty()) {
            return 0.0;
        }

        // Listen zum Sammeln der Noten für jede Kategorie
        List<Double> schriftlicheNoten = new ArrayList<>();
        List<Double> muendlicheNoten = new ArrayList<>();
        // Die Liste für sonstige Noten wird nicht mehr benötigt, da sie ignoriert werden

        // Iteriere durch alle Noten des Faches und ordne sie den Kategorien zu
        for (Note note : noten) {
            // Sicherstellen, dass der Punktwert zwischen 0 und 15 liegt
            double punktWert = Math.max(0.0, Math.min(15.0, note.getWert()));

            if ("schriftlich".equalsIgnoreCase(note.getTyp())) {
                schriftlicheNoten.add(punktWert);
            } else if ("muendlich".equalsIgnoreCase(note.getTyp())) {
                muendlicheNoten.add(punktWert);
            }
            // Noten mit anderen Typen (z.B. "sonstig") werden hier absichtlich ignoriert
        }

        double gesamtPunkteGewichtet = 0.0; // Summe der gewichteten Punkte
        double gesamtGewichtung = 0.0;       // Summe der Gewichtungen der berücksichtigten Noten

        // Prüfen, ob schriftliche Noten vorhanden sind und deren Durchschnitt mit Gewichtung addieren
        if (!schriftlicheNoten.isEmpty()) {
            double avgSchriftlich = calculateAverage(schriftlicheNoten);
            gesamtPunkteGewichtet += (avgSchriftlich * 1); // Schriftliche Noten 1x gewichten
            gesamtGewichtung += 1;
        }

        // Prüfen, ob mündliche Noten vorhanden sind und deren Durchschnitt mit Gewichtung addieren
        if (!muendlicheNoten.isEmpty()) {
            double avgMuendlich = calculateAverage(muendlicheNoten);
            gesamtPunkteGewichtet += (avgMuendlich * 1); // Mündliche Noten 1x gewichten
            gesamtGewichtung += 1;
        }

        // Falls keine der berücksichtigten Notenkategorien Noten enthält, ist der Durchschnitt 0.0
        if (gesamtGewichtung == 0) {
            return 0.0;
        } else {
            // Berechne den gewichteten Durchschnitt
            return gesamtPunkteGewichtet / gesamtGewichtung;
        }
    }

    /**
     * Hilfsmethode zur Berechnung des arithmetischen Durchschnitts einer Liste von Double-Werten.
     *
     * @param list Die Liste der Double-Werte.
     * @return Der Durchschnitt der Werte in der Liste, oder 0.0, wenn die Liste leer ist.
     */
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

    // --- Getter- und Setter-Methoden für die Instanzvariablen ---

    public long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHalbjahr() { return halbjahr; }
    public void setHalbjahr(int halbjahr) { this.halbjahr = halbjahr; }
    public boolean isAbiturfach() { return isAbiturfach; }
    public void setAbiturfach(boolean abitur) { this.isAbiturfach = abitur; }

    /**
     * Rundet einen double-Wert nach spezifischen Regeln für Notenpunkte (0-15 Punkte).
     * - Werte unter 1.0 werden auf 0 gerundet.
     * - Der Nachkommaanteil von 0.0 bis 0.49 wird abgerundet (z.B. 7.49 -> 7).
     * - Der Nachkommaanteil von 0.50 bis 0.99 wird aufgerundet (z.B. 7.51 -> 8).
     *
     * @param value Der zu rundende double-Wert (z.B. 7.49, 7.51).
     * @return Der gerundete Integer-Wert (0-15 Punkte).
     */
    private int roundToNearestNotePoint(double value) {
        if (value < 1.0) { // Punkte unter 1.0 werden zu 0 gerundet (entspricht "nicht gegeben" oder "ungenügend")
            return 0;
        }

        int intPart = (int) value;          // Ganzzahliger Teil der Punktzahl
        double fractionalPart = value - intPart; // Nachkommateil der Punktzahl

        if (fractionalPart >= 0.5) {
            return intPart + 1; // Aufrunden, wenn der Nachkommateil 0.5 oder größer ist
        } else {
            return intPart;     // Abrunden, wenn der Nachkommateil kleiner als 0.5 ist
        }
    }

    /**
     * Gibt den gerundeten Durchschnitt der Noten in Punkten (0-15) zurück.
     * Die Rundung erfolgt nach der spezifischen Regel aus {@link #roundToNearestNotePoint(double)}.
     * Dies ist der Wert, der typischerweise in der UI angezeigt wird.
     *
     * @return Der gerundete Durchschnittspunktwert.
     */
    public int getDurchschnittsPunkte() {
        double durchschnitt = getDurchschnitt(); // Hole den ungerundeten gewichteten Durchschnitt
        return roundToNearestNotePoint(durchschnitt); // Runde ihn nach den definierten Regeln
    }

    /**
     * Überschreibt die Standard toString()-Methode, um eine benutzerfreundliche String-Repräsentation des Fachs zu liefern.
     *
     * @return Eine formatierte Zeichenkette mit Fachname, Halbjahr und gerundetem Durchschnitt in Punkten.
     */
    @NonNull // Zeigt an, dass die Methode niemals null zurückgibt
    @Override
    public String toString() {
        // Formatiert den String für die Anzeige, z.B. "Mathematik (HJ 1) - Ø 8.3 Punkte"
        return String.format(Locale.GERMAN, "%s (HJ %d) - Ø %.1f Punkte",
                name, halbjahr, getDurchschnitt()); // Nutzt den ungerundeten Durchschnitt für die Anzeige mit einer Nachkommastelle
    }
}