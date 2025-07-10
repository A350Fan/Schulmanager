package com.example.schulmanager.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entität, die einen einzelnen Eintrag im Stundenplan darstellt.
 * Diese Klasse ist als Room-Entität deklariert, was bedeutet,
 * dass sie einer Tabelle in der SQLite-Datenbank entspricht.
 */
@Entity(tableName = "stundenplan_eintraege") // Definiert diese Klasse als Room-Entität und den Tabellennamen in der Datenbank.
public class StundenplanEintrag {

    /**
     * Der Primärschlüssel für die Datenbanktabelle.
     * `autoGenerate = true` weist Room an, für jeden neuen Eintrag automatisch eine eindeutige ID zu generieren.
     */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * Der Wochentag des Stundenplaneintrags (z.B. "Montag", "Dienstag").
     * `@ColumnInfo(name = "tag")` ist optional, wenn der Feldname (`tag`)
     * als Spaltenname in der Datenbank verwendet werden soll.
     * Es dient dazu, den Spaltennamen explizit zu definieren, falls er vom Feldnamen abweicht.
     */
    @ColumnInfo(name = "tag")
    private String tag;

    /**
     * Die Uhrzeit des Stundenplaneintrags (z.B. "08:00 - 08:45").
     */
    @ColumnInfo(name = "uhrzeit")
    private String uhrzeit;

    /**
     * Das Fach des Stundenplaneintrags (z.B. "Mathematik").
     */
    @ColumnInfo(name = "fach")
    private String fach;

    /**
     * Der Raum, in dem der Unterricht stattfindet (z.B. "A 201").
     */
    @ColumnInfo(name = "raum")
    private String raum;

    /**
     * Der Name des Lehrers (optional, z.B. "Herr Müller").
     * Dieser Wert kann null oder leer sein, wenn kein Lehrer angegeben ist.
     */
    @ColumnInfo(name = "lehrer")
    private String lehrer;

    /**
     * Der 0-basierte Index der Stunde innerhalb des Tages.
     * Dies ist wichtig, um die Stunden in der korrekten Reihenfolge anzuzeigen und zu sortieren.
     * Zum Beispiel wäre die 1. Stunde `stundenIndex = 0`, die 2. Stunde `stundenIndex = 1` usw.
     */
    @ColumnInfo(name = "stunden_index")
    private int stundenIndex;

    /**
     * Konstruktor zur Erstellung eines neuen StundenplanEintrag-Objekts.
     * Room benötigt einen Konstruktor, der alle persistenten Felder (außer der automatisch generierten ID) initialisiert.
     *
     * @param tag Der Wochentag des Eintrags.
     * @param uhrzeit Die Uhrzeit des Eintrags.
     * @param fach Das Fach des Eintrags.
     * @param raum Der Raum des Eintrags.
     * @param lehrer Der Lehrer des Eintrags (kann null sein).
     * @param stundenIndex Der 0-basierte Index der Stunde.
     */
    public StundenplanEintrag(String tag, String uhrzeit, String fach, String raum, String lehrer, int stundenIndex) {
        this.tag = tag;
        this.uhrzeit = uhrzeit;
        this.fach = fach;
        this.raum = raum;
        this.lehrer = lehrer;
        this.stundenIndex = stundenIndex;
    }


    /**
     * ---
     * ## Getter und Setter
     * Room benötigt Getter und Setter für alle Felder, die in der Datenbank gespeichert werden sollen.
     * ---
     */


    /**
     * Gibt die eindeutige ID dieses StundenplanEintrag-Objekts zurück.
     * @return Die ID des Eintrags.
     */
    public int getId() {
        return id;
    }

    /**
     * Setzt die ID für dieses StundenplanEintrag-Objekt.
     * Dieser Setter ist wichtig, da Room die automatisch generierte ID nach dem Einfügen
     * eines Objekts in die Datenbank hier einfügt.
     * @param id Die zu setzende ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gibt den Wochentag des Stundenplaneintrags zurück.
     * @return Der Wochentag als String (z.B. "Montag").
     */
    public String getTag() {
        return tag;
    }

    /**
     * Setzt den Wochentag für den Stundenplaneintrag.
     * @param tag Der zu setzende Wochentag.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Gibt die Uhrzeit des Stundenplaneintrags zurück.
     * @return Die Uhrzeit als String (z.B. "08:00 - 08:45").
     */
    public String getUhrzeit() {
        return uhrzeit;
    }

    /**
     * Setzt die Uhrzeit für den Stundenplaneintrag.
     * @param uhrzeit Die zu setzende Uhrzeit.
     */
    public void setUhrzeit(String uhrzeit) {
        this.uhrzeit = uhrzeit;
    }

    /**
     * Gibt das Fach des Stundenplaneintrags zurück.
     * @return Das Fach als String (z.B. "Mathematik").
     */
    public String getFach() {
        return fach;
    }

    /**
     * Setzt das Fach für den Stundenplaneintrag.
     * @param fach Das zu setzende Fach.
     */
    public void setFach(String fach) {
        this.fach = fach;
    }

    /**
     * Gibt den Raum des Stundenplaneintrags zurück.
     * @return Der Raum als String (z.B. "A 201").
     */
    public String getRaum() {
        return raum;
    }

    /**
     * Setzt den Raum für den Stundenplaneintrag.
     * @param raum Der zu setzende Raum.
     */
    public void setRaum(String raum) {
        this.raum = raum;
    }

    /**
     * Gibt den Namen des Lehrers für den Stundenplaneintrag zurück.
     * @return Der Lehrername als String (z.B. "Herr Müller"), kann null oder leer sein.
     */
    public String getLehrer() {
        return lehrer;
    }

    /**
     * Setzt den Namen des Lehrers für den Stundenplaneintrag.
     * @param lehrer Der zu setzende Lehrername.
     */
    public void setLehrer(String lehrer) {
        this.lehrer = lehrer;
    }

    /**
     * Gibt den 0-basierten Stundenindex des Stundenplaneintrags zurück.
     * @return Der Stundenindex.
     */
    public int getStundenIndex() {
        return stundenIndex;
    }

    /**
     * Setzt den Stundenindex für den Stundenplaneintrag.
     * @param stundenIndex Der zu setzende Stundenindex.
     */
    public void setStundenIndex(int stundenIndex) {
        this.stundenIndex = stundenIndex;
    }
}