package com.example.schulmanager.dialogs;

// Schnittstelle, um Daten an das aufrufende Fragment zurückzugeben
public interface OnStundenplanEntryAddedListener {
    // Uhrzeit-String wurde entfernt, nur noch der StundenIndex wird übergeben
    void onStundenplanEntryAdded(String fach, String raum, String lehrer, int stundenIndex);
}