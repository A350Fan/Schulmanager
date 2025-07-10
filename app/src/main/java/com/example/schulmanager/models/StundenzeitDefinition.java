package com.example.schulmanager.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import android.os.Parcel; // NEU: Import für die Parcel-Klasse
import android.os.Parcelable; // NEU: Import für das Parcelable-Interface

/**
 * Entität zur Definition der Uhrzeiten für einzelne Schulstunden.
 * Diese Klasse repräsentiert eine Zeile in der Datenbanktabelle "stundenzeit_definition"
 * und ist zudem Parcelable, um Objekte einfach zwischen Android-Komponenten übergeben zu können.
 */
@Entity(tableName = "stundenzeit_definition") // Definiert diese Klasse als Room-Entität und den Tabellennamen
public class StundenzeitDefinition implements Parcelable { // Implementiert Parcelable, um Objekte serialisierbar zu machen

    @PrimaryKey(autoGenerate = true) // Definiert 'id' als Primärschlüssel und aktiviert die automatische Generierung
    private int id; // Eindeutiger Primärschlüssel für die Datenbanktabelle

    private int stundenIndex; // Der Index der Stunde (z.B. 0 für die erste Stunde, 1 für die zweite usw.)
    private String uhrzeitString; // Der String, der die Uhrzeit der Stunde darstellt (z.B. "08:00 - 08:45")

    /**
     * Konstruktor zur Erstellung eines neuen StundenzeitDefinition-Objekts.
     * Wird verwendet, wenn ein neues Objekt ohne vergebene ID (z.B. vor dem Speichern in der DB) erstellt wird.
     *
     * @param stundenIndex Der 0-basierte Index der Stunde.
     * @param uhrzeitString Der String, der die Uhrzeit repräsentiert.
     */
    public StundenzeitDefinition(int stundenIndex, String uhrzeitString) {
        this.stundenIndex = stundenIndex;
        this.uhrzeitString = uhrzeitString;
    }

    // --- Getter und Setter ---

    /**
     * Gibt die ID dieses StundenzeitDefinition-Objekts zurück.
     * @return Die eindeutige ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Setzt die ID für dieses StundenzeitDefinition-Objekt.
     * Diese Methode wird typischerweise von Room verwendet, nachdem ein Objekt in die Datenbank eingefügt wurde
     * und die auto-generierte ID vergeben wurde.
     * @param id Die zu setzende ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gibt den Stundenindex dieses StundenzeitDefinition-Objekts zurück.
     * @return Der 0-basierte Index der Stunde.
     */
    public int getStundenIndex() {
        return stundenIndex;
    }

    /**
     * Setzt den Stundenindex für dieses StundenzeitDefinition-Objekt.
     * @param stundenIndex Der zu setzende 0-basierte Index der Stunde.
     */
    public void setStundenIndex(int stundenIndex) {
        this.stundenIndex = stundenIndex;
    }

    /**
     * Gibt den Uhrzeit-String dieses StundenzeitDefinition-Objekts zurück.
     * @return Der Uhrzeit-String (z.B. "08:00 - 08:45").
     */
    public String getUhrzeitString() {
        return uhrzeitString;
    }

    /**
     * Setzt den Uhrzeit-String für dieses StundenzeitDefinition-Objekt.
     * @param uhrzeitString Der zu setzende Uhrzeit-String.
     */
    public void setUhrzeitString(String uhrzeitString) {
        this.uhrzeitString = uhrzeitString;
    }

    // --- Parcelable Implementierung (Generiert oder manuell implementiert) ---

    /**
     * Spezieller Konstruktor, der verwendet wird, um ein Objekt dieser Klasse aus einem Parcel zu rekonstruieren.
     * Die Reihenfolge der read-Methoden muss der Reihenfolge der write-Methoden in writeToParcel entsprechen.
     *
     * @param in Das Parcel, aus dem die Daten gelesen werden.
     */
    protected StundenzeitDefinition(Parcel in) {
        id = in.readInt(); // Liest die ID aus dem Parcel
        stundenIndex = in.readInt(); // Liest den Stundenindex aus dem Parcel
        uhrzeitString = in.readString(); // Liest den Uhrzeit-String aus dem Parcel
    }

    /**
     * Beschreibt den Inhalt des Parcelable.
     * In den meisten Fällen ist der Rückgabewert 0, es sei denn, das Objekt enthält FileDescriptors.
     *
     * @return Ein Bitmask, der die Arten der speziellen Objekte beschreibt, die von dieser Parcelable-Instanz marshallt werden.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Schreibt die Daten des Objekts in ein Parcel.
     * Die Reihenfolge der write-Methoden muss der Reihenfolge der read-Methoden in dem Parcel-Konstruktor entsprechen.
     *
     * @param dest Das Parcel, in das das Objekt geschrieben werden soll.
     * @param flags Zusätzliche Flags bezüglich der Art und Weise, wie das Objekt geschrieben werden soll.
     * Entweder 0 oder {@link android.os.Parcelable#PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id); // Schreibt die ID in das Parcel
        dest.writeInt(stundenIndex); // Schreibt den Stundenindex in das Parcel
        dest.writeString(uhrzeitString); // Schreibt den Uhrzeit-String in das Parcel
    }

    /**
     * Creator für Parcelable-Objekte.
     * Dies ist ein statisches Feld, das die Parcelable.Creator-Schnittstelle implementiert
     * und zum Erzeugen von Instanzen der Parcelable-Klasse aus einem Parcel verwendet wird.
     */
    public static final Creator<StundenzeitDefinition> CREATOR = new Creator<StundenzeitDefinition>() {
        /**
         * Erstellt eine neue Instanz der Parcelable-Klasse aus einem Parcel,
         * wobei die zuvor in {@link #writeToParcel} geschriebenen Daten verwendet werden.
         *
         * @param in Das Parcel, aus dem das Objekt gelesen werden soll.
         * @return Eine neue Instanz der Parcelable-Klasse.
         */
        @Override
        public StundenzeitDefinition createFromParcel(Parcel in) {
            return new StundenzeitDefinition(in);
        }

        /**
         * Erstellt ein neues Array der Parcelable-Klasse.
         *
         * @param size Die Größe des Arrays.
         * @return Ein Array der Parcelable-Klasse, mit jedem Element auf null initialisiert.
         */
        @Override
        public StundenzeitDefinition[] newArray(int size) {
            return new StundenzeitDefinition[size];
        }
    };
}