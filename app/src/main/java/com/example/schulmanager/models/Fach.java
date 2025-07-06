package com.example.schulmanager.models;

import androidx.annotation.NonNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
     * Jede Note wird mit ihrer individuellen Gewichtung (note.getGewichtung())
     * in die Berechnung einbezogen. Noten mit einer Gewichtung von 0 werden ignoriert.
     *
     * @return Der ungerundete gewichtete Durchschnitt in Punkten (0.0-15.0).
     */
    public double getDurchschnitt() {
        // Wenn keine Noten vorhanden sind, ist der Durchschnitt 0.0
        if (noten == null || noten.isEmpty()) {
            return 0.0;
        }

        double summeGewichteterPunkte = 0.0; // Summe (Note * Gewichtung)
        double summeGewichtungen = 0.0;      // Summe aller Gewichtungen

        // Iteriere durch alle Noten des Faches
        for (Note note : noten) {
            // Sicherstellen, dass der Punktwert zwischen 0 und 15 liegt
            double punktWert = Math.max(0.0, Math.min(15.0, note.getWert()));
            double gewichtung = note.getGewichtung();

            // Nur Noten mit einer positiven Gewichtung berücksichtigen
            if (gewichtung > 0) {
                summeGewichteterPunkte += (punktWert * gewichtung);
                summeGewichtungen += gewichtung;
            }
        }

        // Falls keine Noten mit positiver Gewichtung gefunden wurden, ist der Durchschnitt 0.0
        if (summeGewichtungen == 0.0) {
            return 0.0;
        } else {
            // Berechne den gewichteten Durchschnitt
            return summeGewichteterPunkte / summeGewichtungen;
        }
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