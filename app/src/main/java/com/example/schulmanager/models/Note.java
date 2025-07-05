// models/Note.java (Aktualisiert für NoteTyp Enum)
package com.example.schulmanager.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

/**
 * Repräsentiert eine einzelne Note, die einem Schulfach zugeordnet ist.
 * Eine Note besteht aus einem Punktwert, einem Notentyp (schriftlich, mündlich, sonstig)
 * und einem Zeitstempel ihrer Erfassung. Sie ist Serializable, um gespeichert und geladen werden zu können.
 */
public class Note implements Serializable {
    private double wert;     // Der numerische Wert der Note in Punkten (typischerweise 0-15).
    private NoteTyp typ;     // Der Typ der Note (z.B. SCHRIFTLICH, MUENDLICH), definiert durch das NoteTyp-Enum.
    private long datum;      // Der Zeitstempel der Notenerfassung in Millisekunden seit der Unix-Epoche.

    /**
     * Hilfsmethode zur internen Validierung des Punktwerts einer Note.
     * Stellt sicher, dass der Wert immer innerhalb des zulässigen Bereichs von 0.0 bis 15.0 liegt.
     * Werte unter 0.0 werden auf 0.0 gesetzt, Werte über 15.0 auf 15.0.
     *
     * @param value Der zu validierende double-Wert.
     * @return Der validierte Punktwert, der garantiert zwischen 0.0 und 15.0 liegt.
     */
    private double validateWert(double value) {
        // Nutzt Math.max und Math.min, um den Wert effizient auf den Bereich [0.0, 15.0] zu begrenzen.
        return Math.max(0.0, Math.min(15.0, value));
    }

    /**
     * Konstruktor für eine Note, die mit dem aktuellen Systemdatum erstellt wird.
     * Konvertiert den Notentyp von einem String in das typsichere NoteTyp-Enum.
     *
     * @param wert Der Punktwert der Note (wird intern validiert, um im Bereich 0-15 zu liegen).
     * @param typString Der Notentyp als String (z.B. "schriftlich", "mündlich").
     * Wird von NoteTyp.fromString() in ein Enum umgewandelt.
     */
    public Note(double wert, String typString) {
        this.wert = validateWert(wert);           // Speichert den validierten Punktwert.
        this.typ = NoteTyp.fromString(typString); // Konvertiert den Typ-String in ein NoteTyp-Enum und speichert es.
        this.datum = System.currentTimeMillis();  // Setzt das aktuelle Systemdatum und die Uhrzeit als Erfassungsdatum.
    }

    /**
     * Optionaler Konstruktor für eine Note, bei der ein spezifisches Erfassungsdatum übergeben wird.
     * Nützlich beim Laden von Noten aus dem Speicher, wo das Datum bereits bekannt ist.
     *
     * @param wert Der Punktwert der Note (wird intern validiert).
     * @param typString Der Notentyp als String.
     * @param datum Der explizite Zeitstempel der Notenerfassung in Millisekunden.
     */
    public Note(double wert, String typString, long datum) {
        this.wert = validateWert(wert);           // Speichert den validierten Punktwert.
        this.typ = NoteTyp.fromString(typString); // Konvertiert den Typ-String in ein NoteTyp-Enum und speichert es.
        this.datum = datum;                       // Speichert das übergebene Datum.
    }

    /**
     * Gibt den Punktwert dieser Note zurück.
     * @return Der Punktwert der Note (zwischen 0.0 und 15.0).
     */
    public double getWert() {
        return wert;
    }

    /**
     * Gibt den anzuzeigenden String des Notentyps dieser Note zurück.
     * Dies ist der Wert, der in der UI oder in Logs angezeigt wird (z.B. "schriftlich").
     * @return Der Typ der Note als String.
     */
    public String getTyp() {
        return typ.getDisplayName(); // Ruft den Anzeigenamen des internen NoteTyp-Enums ab.
    }

    /**
     * Gibt den typsicheren NoteTyp Enum-Wert dieser Note zurück.
     * Diese Methode sollte intern für logische Vergleiche verwendet werden, um Typsicherheit zu gewährleisten.
     * @return Der NoteTyp Enum-Wert (SCHRIFTLICH, MUENDLICH, oder SONSTIG).
     */
    public NoteTyp getNoteTypEnum() {
        return typ;
    }

    /**
     * Gibt den Zeitstempel der Notenerfassung zurück.
     * @return Das Datum der Notenerfassung als Anzahl der Millisekunden seit der Unix-Epoche.
     */
    public long getDatum() {
        return datum;
    }

    /*
     * Setter-Methoden für Wert, Typ und Datum (derzeit auskommentiert).
     * Wenn diese Methoden aktiviert werden, sollten sie ebenfalls die notwendige Validierung enthalten,
     * insbesondere für den 'wert' und die Konvertierung von 'typ' mittels NoteTyp.fromString().
     * Sie sind nützlich, wenn Noten nach ihrer Erstellung bearbeitet werden können sollen.
     */
    // public void setWert(double wert) { this.wert = validateWert(wert); }
    // public void setTyp(String typString) { this.typ = NoteTyp.fromString(typString); }
    // public void setDatum(long datum) { this.datum = datum; }


    /**
     * Gibt eine benutzerfreundliche String-Repräsentation dieser Note zurück.
     * @return Eine formatierte Zeichenkette der Note, die ihren Wert und Typ anzeigt (z.B. "10.0 (schriftlich)").
     */
    @NonNull // Zeigt an, dass die Methode niemals null zurückgibt.
    @Override
    public String toString() {
        // Formatiert den Punktwert mit einer Nachkommastelle und den Anzeigenamen des Notentyps.
        // Verwendet Locale.GERMAN, um sicherzustellen, dass Dezimaltrennzeichen korrekt gehandhabt werden (Komma statt Punkt).
        return String.format(Locale.GERMAN, "%.1f (%s)", wert, typ.getDisplayName());
    }
}