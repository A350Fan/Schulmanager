package com.example.schulmanager.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Dieses Modell repräsentiert einen einzelnen Eintrag von Feiertagen oder Schulferien,
 * wie sie von der OpenHolidays API zurückgegeben werden.
 * Es ist darauf ausgelegt, JSON-Antworten mit Gson zu deserialisieren.
 */
public class OpenHoliday {

    /**
     * Die eindeutige ID des Feiertags oder der Ferien.
     * `@SerializedName` wird von Gson verwendet, um den JSON-Schlüssel 'id' auf dieses Feld zu mappen.
     */
    @SerializedName("id")
    private String id;

    /**
     * Das Startdatum des Ereignisses im Format YYYY-MM-DD.
     */
    @SerializedName("startDate")
    private String startDate;

    /**
     * Das Enddatum des Ereignisses im Format YYYY-MM-DD.
     */
    @SerializedName("endDate")
    private String endDate;

    /**
     * Der Name des Feiertags oder der Ferien.
     * Dies ist nun eine Liste von {@link HolidayName}-Objekten,
     * um mehrsprachige Namen zu unterstützen.
     */
    @SerializedName("name")
    private List<HolidayName> name;

    /**
     * Eine zusätzliche Notiz oder Beschreibung des Ereignisses (optional).
     */
    @SerializedName("note")
    private String note;

    /**
     * Der Typ des Ereignisses (z.B. "PUBLIC_HOLIDAY" für Feiertag, "SCHOOL_HOLIDAY" für Schulferien).
     */
    @SerializedName("type")
    private String type;

    /**
     * Ein Array von Strings, die die Bundesländer repräsentieren, für die dieses Ereignis gilt.
     * Beispiel: ["DE-BY"] für Bayern.
     */
    @SerializedName("federalStates")
    private String[] federalStates;


    /**
     * Konstruktor zur Initialisierung eines OpenHoliday-Objekts.
     * Die Parameter entsprechen den Feldern, die von der API zurückgegeben werden.
     *
     * @param id Die eindeutige ID des Ereignisses.
     * @param startDate Das Startdatum.
     * @param endDate Das Enddatum.
     * @param name Eine Liste von {@link HolidayName}-Objekten, die die Namen des Ereignisses enthalten.
     * @param note Eine optionale Notiz.
     * @param type Der Typ des Ereignisses.
     * @param federalStates Ein Array der betroffenen Bundesländer.
     */
    public OpenHoliday(String id, String startDate, String endDate, List<HolidayName> name, String note, String type, String[] federalStates) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.name = name;
        this.note = note;
        this.type = type;
        this.federalStates = federalStates;
    }

    // --- Getter-Methoden ---

    /**
     * Gibt die ID des Feiertags/der Ferien zurück.
     * @return Die ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gibt das Startdatum des Feiertags/der Ferien zurück.
     * @return Das Startdatum im Format YYYY-MM-DD.
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Gibt das Enddatum des Feiertags/der Ferien zurück.
     * @return Das Enddatum im Format YYYY-MM-DD.
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Gibt die Liste der {@link HolidayName}-Objekte zurück, die die Namen des Feiertags/der Ferien enthalten.
     * @return Eine Liste von HolidayName-Objekten.
     */
    public List<HolidayName> getName() {
        return name;
    }

    /**
     * Gibt die Notiz oder zusätzliche Beschreibung zurück.
     * @return Die Notiz als String, kann null sein.
     */
    public String getNote() {
        return note;
    }

    /**
     * Gibt den Typ des Ereignisses zurück (z.B. "PUBLIC_HOLIDAY", "SCHOOL_HOLIDAY").
     * @return Der Typ als String.
     */
    public String getType() {
        return type;
    }

    /**
     * Gibt die betroffenen Bundesländer zurück.
     * @return Ein Array von Strings, das die Bundesländer repräsentiert.
     */
    public String[] getFederalStates() {
        return federalStates;
    }

    // --- Setter-Methoden ---
    // (Setter für alle Felder sind hier nicht unbedingt nötig, da das Objekt oft nur von JSON deserialisiert wird,
    // aber sie sind nützlich, wenn das Objekt nachträglich geändert werden soll.)

    /**
     * Setzt die ID des Feiertags/der Ferien.
     * @param id Die zu setzende ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Setzt das Startdatum des Feiertags/der Ferien.
     * @param startDate Das zu setzende Startdatum im Format YYYY-MM-DD.
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Setzt das Enddatum des Feiertags/der Ferien.
     * @param endDate Das zu setzende Enddatum im Format YYYY-MM-DD.
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * Setzt die Liste der {@link HolidayName}-Objekte für den Namen des Feiertags/der Ferien.
     * @param name Die zu setzende Liste von HolidayName-Objekten.
     */
    public void setName(List<HolidayName> name) {
        this.name = name;
    }

    /**
     * Setzt die Notiz oder zusätzliche Beschreibung.
     * @param note Die zu setzende Notiz.
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Setzt den Typ des Ereignisses.
     * @param type Der zu setzende Typ.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Setzt die betroffenen Bundesländer.
     * @param federalStates Das zu setzende Array von Bundesländern.
     */
    public void setFederalStates(String[] federalStates) {
        this.federalStates = federalStates;
    }

    /**
     * Überschreibt die toString()-Methode, um eine lesbare String-Repräsentation des Objekts zu liefern.
     * Nützlich für Debugging-Zwecke.
     * @return Eine String-Repräsentation des OpenHoliday-Objekts.
     */
    @NonNull
    @Override
    public String toString() {
        return "OpenHoliday{" +
                "id='" + id + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", name=" + name + // Angepasst, um die Liste der Namen anzuzeigen
                ", type='" + type + '\'' +
                '}';
    }

    /**
     * NEUE INNERE KLASSE: HolidayName
     * Repräsentiert ein einzelnes Namens-Objekt innerhalb des "name"-Arrays in der JSON-Antwort.
     * Ermöglicht die Handhabung von mehrsprachigen Namen.
     */
    public static class HolidayName {
        /**
         * Die Sprach-ID für den Namen (z.B. "de" für Deutsch, "en" für Englisch).
         */
        @SerializedName("languageId")
        private String languageId;

        /**
         * Der tatsächliche Name des Feiertags/der Ferien in der entsprechenden Sprache.
         */
        @SerializedName("text")
        private String text;

        /**
         * Konstruktor für ein HolidayName-Objekt.
         *
         * @param languageId Die Sprach-ID.
         * @param text Der Name in dieser Sprache.
         */
        public HolidayName(String languageId, String text) {
            this.languageId = languageId;
            this.text = text;
        }

        // --- Getter-Methoden für HolidayName ---

        /**
         * Gibt die Sprach-ID des Namens zurück.
         * @return Die Sprach-ID.
         */
        public String getLanguageId() {
            return languageId;
        }

        /**
         * Gibt den Text des Namens in der entsprechenden Sprache zurück.
         * @return Der Name als String.
         */
        public String getText() {
            return text;
        }

        // --- Setter-Methoden für HolidayName ---

        /**
         * Setzt den Text des Namens.
         * @param text Der zu setzende Name.
         */
        public void setText(String text) {
            this.text = text;
        }

        /**
         * Überschreibt die toString()-Methode, um eine lesbare String-Repräsentation des HolidayName-Objekts zu liefern.
         * @return Eine String-Repräsentation des HolidayName-Objekts.
         */
        @NonNull
        @Override
        public String toString() {
            return "HolidayName{" +
                    "languageId='" + languageId + '\'' +
                    ", text='" + text + '\'' +
                    '}';
        }
    }
}