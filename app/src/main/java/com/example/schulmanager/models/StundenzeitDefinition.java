package com.example.schulmanager.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entität für die Definition einer festen Stundenzeit im Stundenplan.
 * Jede Instanz repräsentiert die Uhrzeit für eine bestimmte Stunde (z.B. 1. Stunde, 2. Stunde).
 */
@Entity(tableName = "stundenzeit_definition")
public class StundenzeitDefinition {

    @PrimaryKey
    private int stundenIndex; // Der Index der Stunde (z.B. 0 für die 1. Stunde, 1 für die 2. Stunde, bis 10 für die 11. Stunde)

    private String uhrzeitString; // Die zugehörige Uhrzeit als String (z.B. "08:00 - 08:45")

    public StundenzeitDefinition(int stundenIndex, String uhrzeitString) {
        this.stundenIndex = stundenIndex;
        this.uhrzeitString = uhrzeitString;
    }

    public int getStundenIndex() {
        return stundenIndex;
    }

    public String getUhrzeitString() {
        return uhrzeitString;
    }

    public void setUhrzeitString(String uhrzeitString) {
        this.uhrzeitString = uhrzeitString;
    }
}