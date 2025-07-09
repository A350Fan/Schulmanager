package com.example.schulmanager.models;

import com.google.gson.annotations.SerializedName;

import java.util.List; // Import für List

// Dieses Modell repräsentiert einen einzelnen Eintrag von Feiertagen oder Schulferien
public class OpenHoliday {

    @SerializedName("id")
    private String id;
    @SerializedName("startDate")
    private String startDate; // Datum im Format YYYY-MM-DD
    @SerializedName("endDate")
    private String endDate;   // Datum im Format YYYY-MM-DD
    @SerializedName("name")
    private List<HolidayName> name; // GEÄNDERT: Jetzt eine Liste von HolidayName-Objekten
    @SerializedName("note")
    private String note;      // Zusätzliche Notiz (optional)
    @SerializedName("type")
    private String type;      // Typ des Ereignisses (z.B. "PUBLIC_HOLIDAY", "SCHOOL_HOLIDAY")
    @SerializedName("federalStates")
    private String[] federalStates; // Bundesländer, für die dieses Ereignis gilt (z.B. ["DE-BY"])


    // Konstruktor (angepasst an neue "name"-Struktur)
    public OpenHoliday(String id, String startDate, String endDate, List<HolidayName> name, String note, String type, String[] federalStates) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.name = name;
        this.note = note;
        this.type = type;
        this.federalStates = federalStates;
    }

    // Getter-Methoden (angepasst an neue "name"-Struktur)
    public String getId() {
        return id;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    // GEÄNDERT: Gibt jetzt die Liste der HolidayName-Objekte zurück
    public List<HolidayName> getName() {
        return name;
    }

    public String getNote() {
        return note;
    }

    public String getType() {
        return type;
    }

    public String[] getFederalStates() {
        return federalStates;
    }

    // Setter-Methoden (angepasst an neue "name"-Struktur)
    public void setId(String id) {
        this.id = id;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    // GEÄNDERT: Erwartet jetzt eine Liste von HolidayName-Objekten
    public void setName(List<HolidayName> name) {
        this.name = name;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFederalStates(String[] federalStates) {
        this.federalStates = federalStates;
    }

    @Override
    public String toString() {
        return "OpenHoliday{" +
                "id='" + id + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", name=" + name + // Angepasst für die Liste
                ", type='" + type + '\'' +
                '}';
    }

    // NEUE INNERE KLASSE: HolidayName
    // Repräsentiert ein einzelnes Namens-Objekt innerhalb des "name"-Arrays
    public static class HolidayName {
        @SerializedName("languageId")
        private String languageId;
        @SerializedName("text")
        private String text;

        // Konstruktor
        public HolidayName(String languageId, String text) {
            this.languageId = languageId;
            this.text = text;
        }

        // Getter-Methoden
        public String getLanguageId() {
            return languageId;
        }

        public String getText() {
            return text;
        }

        // Setter-Methoden (optional)
        public void setLanguageId(String languageId) {
            this.languageId = languageId;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "HolidayName{" +
                    "languageId='" + languageId + '\'' +
                    ", text='" + text + '\'' +
                    '}';
        }
    }
}