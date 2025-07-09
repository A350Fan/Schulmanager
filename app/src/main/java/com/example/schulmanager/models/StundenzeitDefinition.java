package com.example.schulmanager.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import android.os.Parcel; // NEU
import android.os.Parcelable; // NEU

@Entity(tableName = "stundenzeit_definition")
public class StundenzeitDefinition implements Parcelable { // NEU: Implementiere Parcelable

    @PrimaryKey(autoGenerate = true)
    private int id; // Primärschlüssel für Room

    private int stundenIndex; // Z.B. 0 für die erste Stunde, 1 für die zweite
    private String uhrzeitString; // Z.B. "08:00 - 08:45"

    public StundenzeitDefinition(int stundenIndex, String uhrzeitString) {
        this.stundenIndex = stundenIndex;
        this.uhrzeitString = uhrzeitString;
    }

    // --- Getter und Setter ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStundenIndex() {
        return stundenIndex;
    }

    public void setStundenIndex(int stundenIndex) {
        this.stundenIndex = stundenIndex;
    }

    public String getUhrzeitString() {
        return uhrzeitString;
    }

    public void setUhrzeitString(String uhrzeitString) {
        this.uhrzeitString = uhrzeitString;
    }

    // --- Parcelable Implementierung (Generiert oder manuell implementiert) ---
    // Dies ist der Constructor, der von Parcel verwendet wird
    protected StundenzeitDefinition(Parcel in) {
        id = in.readInt();
        stundenIndex = in.readInt();
        uhrzeitString = in.readString();
    }

    // Beschreibt den Inhalt des Parcelable (0 oder CONTENTS_FILE_DESCRIPTOR)
    @Override
    public int describeContents() {
        return 0;
    }

    // Schreibt das Objekt in ein Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(stundenIndex);
        dest.writeString(uhrzeitString);
    }

    // Creator für Parcelable, der für die Deserialisierung benötigt wird
    public static final Creator<StundenzeitDefinition> CREATOR = new Creator<StundenzeitDefinition>() {
        @Override
        public StundenzeitDefinition createFromParcel(Parcel in) {
            return new StundenzeitDefinition(in);
        }

        @Override
        public StundenzeitDefinition[] newArray(int size) {
            return new StundenzeitDefinition[size];
        }
    };
}