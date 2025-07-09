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
import com.example.schulmanager.models.OpenHoliday; // Importiere dein Datenmodell

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

public class KalenderFragment extends Fragment {

    private CalendarView calendarView;
    private TextView tvHolidayInfo;

    private OkHttpClient httpClient; // HTTP-Client für Netzwerkanfragen
    private Gson gson;               // Gson für JSON-Parsen
    private List<OpenHoliday> fetchedHolidays = new ArrayList<>(); // Liste zum Speichern der abgerufenen Ferien

    public KalenderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        httpClient = new OkHttpClient(); // Initialisiere OkHttpClient
        gson = new Gson();               // Initialisiere Gson

        calendarView = view.findViewById(R.id.calendarView);
        tvHolidayInfo = view.findViewById(R.id.tvHolidayInfo);

        calendarView.setFirstDayOfWeek(Calendar.MONDAY);

        // Optional: Setze einen Listener für Datumsänderungen
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // Monat ist 0-basiert in CalendarView, d.h. Januar = 0
            String selectedDate = String.format(Locale.GERMAN, "%02d.%02d.%d", dayOfMonth, month + 1, year);
            Toast.makeText(getContext(), "Ausgewähltes Datum: " + selectedDate, Toast.LENGTH_SHORT).show();

            // Zeige hier die Ferieninformationen für das ausgewählte Datum an
            displayHolidaysForDate(year, month + 1, dayOfMonth);
        });

        // Lade die Ferientermine, sobald das Fragment erstellt wurde
        fetchHolidays();
    }

    private void fetchHolidays() {
        if (getContext() == null) {
            return; // Fragment ist noch nicht an Context gebunden
        }

        tvHolidayInfo.setText("Lade Ferieninformationen...");

        Calendar currentCalendar = Calendar.getInstance();
        int currentYear = currentCalendar.get(Calendar.YEAR);

        String url = String.format(Locale.US,
                "https://openholidaysapi.org/SchoolHolidays?countryIsoCode=DE&languageIsoCode=de&subdivisionCode=DE-BY&validFrom=%d-01-01&validTo=%d-12-31&publicHolidays=true&schoolHolidays=true",
                currentYear, currentYear);

        Request request = new Request.Builder()
                .url(url)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvHolidayInfo.setText("Fehler beim Laden der Ferien: " + e.getMessage());
                        Toast.makeText(getContext(), "Fehler beim Laden der Ferien!", Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    Type listType = new TypeToken<List<OpenHoliday>>() {}.getType();
                    fetchedHolidays = gson.fromJson(jsonResponse, listType);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (fetchedHolidays != null && !fetchedHolidays.isEmpty()) {
                                tvHolidayInfo.setText("Ferien und Feiertage für Bayern (" + currentYear + ") erfolgreich geladen.");
                                for (OpenHoliday holiday : fetchedHolidays) {
                                    // GEÄNDERT: Rufe den lokalisierten Namen ab
                                    String holidayName = getLocalizedHolidayName(holiday.getName());
                                    System.out.println("Gefundene Ferien: " + holidayName + " von " + holiday.getStartDate() + " bis " + holiday.getEndDate() + " (Typ: " + holiday.getType() + ")");
                                }
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

    // Methode zum Anzeigen von Ferieninformationen für ein bestimmtes Datum
    private void displayHolidaysForDate(int year, int month, int dayOfMonth) {
        String dateString = String.format(Locale.US, "%d-%02d-%02d", year, month, dayOfMonth);
        StringBuilder info = new StringBuilder();
        info.append("Informationen für ").append(String.format(Locale.GERMAN, "%02d.%02d.%d", dayOfMonth, month, year)).append(":\n");

        boolean found = false;
        if (fetchedHolidays != null) {
            for (OpenHoliday holiday : fetchedHolidays) {
                if (dateString.compareTo(holiday.getStartDate()) >= 0 && dateString.compareTo(holiday.getEndDate()) <= 0) {
                    // GEÄNDERT: Rufe den lokalisierten Namen ab
                    String holidayName = getLocalizedHolidayName(holiday.getName());
                    info.append("- ").append(holidayName);
                    if (holiday.getType().equals("PUBLIC_HOLIDAY")) {
                        info.append(" (Feiertag)");
                    } else if (holiday.getType().equals("SCHOOL_HOLIDAY")) {
                        info.append(" (Ferien)");
                    }
                    if (holiday.getNote() != null && !holiday.getNote().isEmpty()) {
                        info.append(": ").append(holiday.getNote());
                    }
                    info.append("\n");
                    found = true;
                }
            }
        }

        if (!found) {
            info.append("Keine bekannten Ferien oder Feiertage.");
        }
        tvHolidayInfo.setText(info.toString());
    }

    // NEUE HELFERMETHODE: Extrahiert den deutschen Namen aus der Liste der Namen
    private String getLocalizedHolidayName(List<OpenHoliday.HolidayName> names) {
        if (names == null || names.isEmpty()) {
            return "Unbekanntes Ereignis";
        }
        // Versuche, den deutschen Namen zu finden
        for (OpenHoliday.HolidayName nameObj : names) {
            if (nameObj.getLanguageId() != null && nameObj.getLanguageId().equals("de")) {
                return nameObj.getText();
            }
        }
        // Wenn kein deutscher Name gefunden wurde, nimm den ersten verfügbaren Namen
        return names.get(0).getText();
    }
}