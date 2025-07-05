package com.example.schulmanager.models;

import java.io.Serializable;

/**
 * Repräsentiert einen Typ einer Note, z.B. schriftlich, mündlich, sonstig.
 * Dieses Enum verbessert die Typsicherheit und vermeidet Fehler, die bei der Verwendung von Strings
 * für vordefinierte Kategorien auftreten könnten (z.B. Tippfehler, Groß-/Kleinschreibung).
 * Es ist Serializable, damit es zusammen mit der Note-Klasse gespeichert werden kann.
 */
public enum NoteTyp implements Serializable {
    // Enum-Konstanten mit ihren zugehörigen Anzeigestrings.
    SCHRIFTLICH("schriftlich"), // Repräsentiert eine schriftliche Note.
    MUENDLICH("mündlich"),   // Repräsentiert eine mündliche Note.
    SONSTIG("sonstig");       // Repräsentiert eine sonstige Note.

    private final String displayName; // Interner String, der den "anzeigbaren" Namen des Notentyps speichert.

    /**
     * Konstruktor für die Enum-Konstanten von NoteTyp.
     * @param displayName Der String, der für diesen Notentyp in der Benutzeroberfläche oder
     * bei der Speicherung/Darstellung verwendet werden soll.
     */
    NoteTyp(String displayName) {
        this.displayName = displayName; // Speichert den übergebenen Anzeigenamen.
    }

    /**
     * Gibt den human-readable (anzeigbaren) Namen des Notentyps zurück.
     * Dies ist nützlich für die Anzeige in der Benutzeroberfläche oder für die Serialisierung.
     * @return Der String, der diesen Notentyp repräsentiert (z.B. "schriftlich").
     */
    public String getDisplayName() {
        return displayName; // Liefert den zuvor im Konstruktor zugewiesenen Anzeigenamen.
    }

    /**
     * Wandelt einen gegebenen String-Wert in den entsprechenden NoteTyp Enum-Wert um.
     * Diese Methode ist nützlich, wenn Notentypen als Strings (z.B. aus der Datenbank oder UI-Eingabe)
     * empfangen und in den typsicheren Enum-Typ konvertiert werden müssen.
     *
     * @param text Der String-Wert des Notentyps (z.B. "schriftlich", "mündlich", "sonstig").
     * @return Der passende NoteTyp Enum-Wert.
     * @throws IllegalArgumentException Wenn der übergebene String keinem bekannten Notentyp entspricht.
     */
    public static NoteTyp fromString(String text) {
        // Durchläuft alle definierten Enum-Konstanten von NoteTyp.
        for (NoteTyp typ : NoteTyp.values()) {
            // Vergleicht den übergebenen Text (ignorierend Groß-/Kleinschreibung) mit dem displayName
            // jeder Enum-Konstante.
            if (typ.displayName.equalsIgnoreCase(text)) {
                return typ; // Gibt die passende Enum-Konstante zurück, sobald eine Übereinstimmung gefunden wird.
            }
        }
        // Wenn nach Überprüfung aller Konstanten keine Übereinstimmung gefunden wurde,
        // wird eine IllegalArgumentException geworfen, um auf einen ungültigen Eingabewert hinzuweisen.
        throw new IllegalArgumentException("Unbekannter Notentyp: " + text);
    }
}