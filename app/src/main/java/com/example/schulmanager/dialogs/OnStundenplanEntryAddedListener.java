package com.example.schulmanager.dialogs;

// Schnittstelle, um Daten an das aufrufende Fragment zurückzugeben
public interface OnStundenplanEntryAddedListener {
    void onStundenplanEntryAdded(String fach, String uhrzeit, String raum, String lehrer, int stundenIndex);
}
