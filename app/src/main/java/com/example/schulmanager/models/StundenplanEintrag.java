package com.example.schulmanager.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Definiert diese Klasse als eine Room-Entität (Tabelle in der DB)
// tableName ist optional, aber gut zur expliziten Benennung der Tabelle
@Entity(tableName = "stundenplan_eintraege")
public class StundenplanEintrag {

    // Definiert die Primärschlüssel-Spalte. autoGenerate = true bedeutet, Room generiert IDs automatisch.
    @PrimaryKey(autoGenerate = true)
    private int id; // Jede Entität braucht einen eindeutigen Primärschlüssel

    // @ColumnInfo ist optional, wenn der Feldname dem Spaltennamen entsprechen soll.
    // Kann verwendet werden, um Spaltennamen zu ändern (z.B. name = "tag_des_eintrags")
    @ColumnInfo(name = "tag")
    private String tag;      // z.B. "Montag", "Dienstag"

    @ColumnInfo(name = "uhrzeit")
    private String uhrzeit;  // z.B. "08:00 - 08:45"

    @ColumnInfo(name = "fach")
    private String fach;     // z.B. "Mathematik"

    @ColumnInfo(name = "raum")
    private String raum;     // z.B. "A 201"

    @ColumnInfo(name = "lehrer")
    private String lehrer;   // Optional: z.B. "Herr Müller"

    @ColumnInfo(name = "stunden_index")
    private int stundenIndex; // Wichtig: Für die Reihenfolge der Stunden (0-basiert)

    // Konstruktor: Room benötigt einen Konstruktor, der alle Felder außer der ID (wenn autoGenerate=true) initialisiert.
    // Alternativ kann Room auch leere Konstruktoren und Setter/Getter verwenden, aber dieser ist expliziter.
    public StundenplanEintrag(String tag, String uhrzeit, String fach, String raum, String lehrer, int stundenIndex) {
        this.tag = tag;
        this.uhrzeit = uhrzeit;
        this.fach = fach;
        this.raum = raum;
        this.lehrer = lehrer;
        this.stundenIndex = stundenIndex;
    }

    // --- Getter und Setter für alle Felder, einschließlich der ID ---
    // Room benötigt Getter und Setter für alle Felder, die in der Datenbank gespeichert werden sollen.

    public int getId() {
        return id;
    }

    public void setId(int id) { // Setter für ID ist wichtig, damit Room die generierte ID setzen kann
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUhrzeit() {
        return uhrzeit;
    }

    public void setUhrzeit(String uhrzeit) {
        this.uhrzeit = uhrzeit;
    }

    public String getFach() {
        return fach;
    }

    public void setFach(String fach) {
        this.fach = fach;
    }

    public String getRaum() {
        return raum;
    }

    public void setRaum(String raum) {
        this.raum = raum;
    }

    public String getLehrer() {
        return lehrer;
    }

    public void setLehrer(String lehrer) {
        this.lehrer = lehrer;
    }

    public int getStundenIndex() {
        return stundenIndex;
    }

    public void setStundenIndex(int stundenIndex) {
        this.stundenIndex = stundenIndex;
    }
}