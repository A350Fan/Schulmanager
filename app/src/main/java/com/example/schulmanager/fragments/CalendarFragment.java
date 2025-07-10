package com.example.schulmanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.schulmanager.R;
import com.example.schulmanager.models.OpenHoliday;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Ein Fragment zur Anzeige eines Kalenders und relevanter Informationen zu Feiertagen und Schulferien.
 * Es ruft Daten von der OpenHolidays API ab und zeigt diese für das ausgewählte Datum an.
 */
public class CalendarFragment extends Fragment {

    // UI-Elemente
    private CalendarView calendarView; // Der Kalender zur Datumsauswahl
    private TextView tvHolidayInfo;    // TextView zur Anzeige von Ferieninformationen

    // Netzwerk- und JSON-Bibliotheken
    private OkHttpClient httpClient; // HTTP-Client für Netzwerkanfragen zur API
    private Gson gson;               // Gson-Instanz für das Parsen von JSON-Antworten
    private List<OpenHoliday> fetchedHolidays = new ArrayList<>(); // Liste zum Speichern der abgerufenen Ferien-/Feiertagsdaten

    /**
     * Erforderlicher leerer öffentlicher Konstruktor.
     */
    public CalendarFragment() {
        // Konstruktor muss leer sein
    }

    /**
     * Wird aufgerufen, um die View-Hierarchie des Fragments zu erstellen und zurückzugeben.
     * @param inflater Der LayoutInflater-Objekt, das verwendet werden kann, um Views in diesem Fragment zu inflaten.
     * @param container Wenn nicht null, ist dies die übergeordnete View, an die die Fragment-UI angehängt werden soll.
     * @param savedInstanceState Wenn nicht null, dieses Fragment wird von einem zuvor gespeicherten Zustand rekonstituiert.
     * @return Die View für die UI des Fragments oder null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate das Layout für dieses Fragment (fragment_calender.xml)
        return inflater.inflate(R.layout.fragment_calender, container, false);
    }

    /**
     * Wird direkt nach onCreateView() aufgerufen und gibt die Möglichkeit, die View-Hierarchie des Fragments
     * zu initialisieren oder Operationen durchzuführen, die die View erfordern.
     * @param view Die View, die von onCreateView() zurückgegeben wurde.
     * @param savedInstanceState Wenn nicht null, dieses Fragment wird von einem zuvor gespeicherten Zustand rekonstituiert.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialisiere OkHttpClient und Gson-Instanzen
        httpClient = new OkHttpClient();
        gson = new Gson();

        // Referenzen zu den UI-Elementen aus dem Layout erhalten
        calendarView = view.findViewById(R.id.calendarView);
        tvHolidayInfo = view.findViewById(R.id.tvHolidayInfo);

        // Setze den ersten Wochentag im Kalender auf Montag.
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);

        // Setze einen Listener für Datumsänderungen im Kalender.
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // Der Monat ist in CalendarView 0-basiert (Januar = 0), daher +1 für die Anzeige.
            String selectedDate = String.format(Locale.GERMAN, "%02d.%02d.%d", dayOfMonth, month + 1, year);
            Toast.makeText(getContext(), "Ausgewähltes Datum: " + selectedDate, Toast.LENGTH_SHORT).show();

            // Zeige die Ferieninformationen für das aktuell ausgewählte Datum an.
            displayHolidaysForDate(year, month + 1, dayOfMonth);
        });

        // Lade die Ferientermine von der API, sobald das Fragment erstellt und die View initialisiert wurde.
        fetchHolidays();
    }

    /**
     * Ruft Feiertags- und Ferieninformationen von der OpenHolidays API ab.
     * Die Daten werden für das aktuelle Jahr und das Bundesland Bayern (DE-BY) angefordert.
     */
    private void fetchHolidays() {
        // Überprüfe, ob das Fragment an einen Kontext gebunden ist, bevor Netzwerkanfragen gestellt werden.
        if (getContext() == null) {
            return;
        }

        // Zeige eine Lade-Nachricht in der TextView an.
        tvHolidayInfo.setText("Lade Ferieninformationen...");

        // Ermittle das aktuelle Jahr, um es in die API-Anfrage einzufügen.
        Calendar currentCalendar = Calendar.getInstance();
        int currentYear = currentCalendar.get(Calendar.YEAR);

        // Erstelle die URL für die API-Anfrage.
        // Parameter: Land (DE), Sprache (de), Bundesland (DE-BY), Gültigkeitsbereich (aktuelles Jahr),
        // und ob öffentliche Feiertage und Schulferien enthalten sein sollen.
        String url = String.format(Locale.US,
                "https://openholidaysapi.org/SchoolHolidays?countryIsoCode=DE&languageIsoCode=de&subdivisionCode=DE-BY&validFrom=%d-01-01&validTo=%d-12-31&publicHolidays=true&schoolHolidays=true",
                currentYear, currentYear);

        // Erstelle eine HTTP-Anfrage mit der generierten URL.
        Request request = new Request.Builder()
                .url(url)
                .build();

        // Führe die HTTP-Anfrage asynchron aus und verarbeite die Antwort über einen Callback.
        httpClient.newCall(request).enqueue(new Callback() {
            /**
             * Wird aufgerufen, wenn die Netzwerkanfrage fehlschlägt (z.B. keine Internetverbindung).
             * @param call Der aufgerufene Call.
             * @param e Die aufgetretene IOException.
             */
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace(); // Stacktrace im Log ausgeben
                // Führe UI-Updates im UI-Thread aus
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvHolidayInfo.setText("Fehler beim Laden der Ferien: " + e.getMessage());
                        Toast.makeText(getContext(), "Fehler beim Laden der Ferien!", Toast.LENGTH_LONG).show();
                    });
                }
            }

            /**
             * Wird aufgerufen, wenn eine HTTP-Antwort empfangen wird, unabhängig davon, ob sie erfolgreich war oder nicht.
             * @param call Der aufgerufene Call.
             * @param response Die HTTP-Antwort.
             * @throws IOException Wenn beim Lesen des Antwortkörpers ein Problem auftritt.
             */
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) { // Überprüfe, ob die Antwort erfolgreich war (HTTP 2xx Statuscode)
                    String jsonResponse = response.body().string(); // Den JSON-Antwortkörper als String lesen
                    // Definiere den Typ für Gson, um eine Liste von OpenHoliday-Objekten zu deserialisieren
                    Type listType = new TypeToken<List<OpenHoliday>>() {}.getType();
                    fetchedHolidays = gson.fromJson(jsonResponse, listType); // JSON in die Liste deserialisieren

                    // Führe UI-Updates im UI-Thread aus
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (fetchedHolidays != null && !fetchedHolidays.isEmpty()) {
                                tvHolidayInfo.setText("Ferien und Feiertage für Bayern (" + currentYear + ") erfolgreich geladen.");
                                // Debug-Ausgabe der geladenen Ferien im System-Log
                                for (OpenHoliday holiday : fetchedHolidays) {
                                    String holidayName = getLocalizedHolidayName(holiday.getName()); // Lokalisierten Namen abrufen
                                    System.out.println("Gefundene Ferien: " + holidayName + " von " + holiday.getStartDate() + " bis " + holiday.getEndDate() + " (Typ: " + holiday.getType() + ")");
                                }
                                // Zeige die Ferieninformationen für das aktuell im Kalender ausgewählte Datum an.
                                // Dies ist wichtig, falls der Benutzer den Kalender vor dem Laden der Daten bereits bedient hat.
                                long selectedDateMillis = calendarView.getDate();
                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(selectedDateMillis);
                                displayHolidaysForDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
                            } else {
                                tvHolidayInfo.setText("Keine Ferien oder Feiertage für Bayern (" + currentYear + ") gefunden.");
                            }
                        });
                    }
                } else {
                    // Wenn die Antwort nicht erfolgreich war, zeige den HTTP-Statuscode und die Nachricht an.
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            tvHolidayInfo.setText("Fehler beim Laden der Ferien: " + response.code() + " " + response.message());
                            Toast.makeText(getContext(), "API-Fehler: " + response.code(), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            }
        });
    }

    /**
     * Zeigt Feiertags- und Ferieninformationen für ein bestimmtes Datum an.
     * Die Methode durchsucht die abgerufene Liste der Feiertage/Ferien und prüft,
     * ob das gegebene Datum in den Zeitraum eines Eintrags fällt.
     *
     * @param year Das Jahr des ausgewählten Datums.
     * @param month Der Monat des ausgewählten Datums (1-basiert).
     * @param dayOfMonth Der Tag des Monats des ausgewählten Datums.
     */
    private void displayHolidaysForDate(int year, int month, int dayOfMonth) {
        // Formatiere das gegebene Datum in den String-Format YYYY-MM-DD zur Vergleichbarkeit mit den API-Daten.
        String dateString = String.format(Locale.US, "%d-%02d-%02d", year, month, dayOfMonth);
        StringBuilder info = new StringBuilder();
        // Füge die Kopfzeile für die Datumsinfo hinzu.
        info.append("Informationen für ").append(String.format(Locale.GERMAN, "%02d.%02d.%d", dayOfMonth, month, year)).append(":\n");

        boolean found = false; // Flag, ob ein Feiertag/Ferientag gefunden wurde.
        if (fetchedHolidays != null) {
            for (OpenHoliday holiday : fetchedHolidays) {
                // Überprüfe, ob das ausgewählte Datum innerhalb des Start- und Enddatums des Feiertags/der Ferien liegt.
                // compareTo gibt 0 zurück, wenn Strings gleich sind, < 0 wenn der erste String lexikografisch kleiner ist,
                // > 0 wenn der erste String lexikografisch größer ist.
                if (dateString.compareTo(holiday.getStartDate()) >= 0 && dateString.compareTo(holiday.getEndDate()) <= 0) {
                    String holidayName = getLocalizedHolidayName(holiday.getName()); // Lokalisierten Namen abrufen
                    info.append("- ").append(holidayName); // Namen hinzufügen
                    // Füge den Typ des Ereignisses hinzu (Feiertag/Ferien).
                    if (holiday.getType().equals("PUBLIC_HOLIDAY")) {
                        info.append(" (Feiertag)");
                    } else if (holiday.getType().equals("SCHOOL_HOLIDAY")) {
                        info.append(" (Ferien)");
                    }
                    // Füge eine eventuelle Notiz hinzu.
                    if (holiday.getNote() != null && !holiday.getNote().isEmpty()) {
                        info.append(": ").append(holiday.getNote());
                    }
                    info.append("\n");
                    found = true; // Setze Flag auf true, da ein Eintrag gefunden wurde.
                }
            }
        }

        // Wenn keine Ferien oder Feiertage für das Datum gefunden wurden, zeige entsprechende Nachricht an.
        if (!found) {
            info.append("Keine bekannten Ferien oder Feiertage.");
        }
        tvHolidayInfo.setText(info.toString()); // Aktualisiere die TextView mit den Informationen.
    }

    /**
     * Hilfsmethode, die den lokalisierten (deutschen) Namen eines Feiertags oder einer Ferien aus
     * einer Liste von {@link OpenHoliday.HolidayName}-Objekten extrahiert.
     * Wenn ein deutscher Name gefunden wird, wird dieser zurückgegeben. Andernfalls wird der erste
     * verfügbare Name in der Liste zurückgegeben.
     *
     * @param names Eine Liste von {@link OpenHoliday.HolidayName}-Objekten, die verschiedene Sprachversionen des Namens enthalten.
     * @return Der lokalisierte Name (Deutsch bevorzugt) oder "Unbekanntes Ereignis", wenn keine Namen vorhanden sind.
     */
    private String getLocalizedHolidayName(List<OpenHoliday.HolidayName> names) {
        if (names == null || names.isEmpty()) {
            return "Unbekanntes Ereignis"; // Fallback, wenn keine Namen vorhanden sind
        }
        // Versuche, den deutschen Namen ("de") in der Liste zu finden
        for (OpenHoliday.HolidayName nameObj : names) {
            if (nameObj.getLanguageId() != null && nameObj.getLanguageId().equals("de")) {
                return nameObj.getText(); // Gebe den deutschen Namen zurück
            }
        }
        // Wenn kein deutscher Name gefunden wurde, nimm den ersten verfügbaren Namen als Fallback
        return names.get(0).getText();
    }
}