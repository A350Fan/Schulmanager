package com.example.schulmanager.dialogs;

/**
 * Schnittstelle (Interface), das definiert, wie ein Dialog (z.B. AddTimetableEntryDialog)
 * Informationen über einen neu hinzugefügten Stundenplaneintrag an ein aufrufendes Fragment
 * oder eine Activity zurückgeben kann.
 * <p>
 * Das implementierende Fragment/die Activity muss diese Methode implementieren,
 * um die Daten vom Dialog zu empfangen.
 */
public interface OnTimetableEntryAddedListener {

    /**
     * Diese Methode wird aufgerufen, wenn ein Stundenplaneintrag erfolgreich
     * im Dialog erfasst und hinzugefügt wurde.
     *
     * @param fach Der Name des Faches (String).
     * @param raum Der Raum, in dem der Unterricht stattfindet (String).
     * @param lehrer Der Name des Lehrers (String, kann null oder leer sein).
     * @param stundenIndex Der 0-basierte Index der Stunde (int), z.B. 0 für die erste Stunde.
     */
    void onStundenplanEntryAdded(String fach, String raum, String lehrer, int stundenIndex);
}