package com.example.schulmanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import com.example.schulmanager.R;
import com.example.schulmanager.adapters.StundenplanAdapter;
import com.example.schulmanager.models.StundenplanEintrag;
import com.example.schulmanager.database.AppDatabase; // Import der Datenbank
import com.example.schulmanager.database.StundenplanDAO; // Import des DAO

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; // Für asynchrone Datenbank-Operationen

public class StundenplanFragment extends Fragment {

    private MaterialButtonToggleGroup toggleButtonGroupDays;
    private RecyclerView recyclerViewStundenplanTag;
    private StundenplanAdapter stundenplanAdapter;
    private StundenplanDAO stundenplanDao; // Dein DAO
    private final String[] tage = {"Mo", "Di", "Mi", "Do", "Fr"};
    private final String[] stundenzeiten = { // 11 Stunden, anpassbar
            "08:00 - 08:45", "08:50 - 09:35", "09:50 - 10:35", "10:40 - 11:25", "11:30 - 12:15",
            "12:20 - 13:05", "13:10 - 13:55", "14:00 - 14:45", "14:50 - 15:35", "15:40 - 16:25", "16:30 - 17:15"
    };

    private String selectedDay = "Mo"; // Standardmäßig Montag auswählen

    // ExecutorService für asynchrone Datenbankoperationen
    private ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public StundenplanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stundenplan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Datenbank-Instanz und DAO holen
        stundenplanDao = AppDatabase.getDatabase(getContext()).stundenplanDao();

        toggleButtonGroupDays = view.findViewById(R.id.toggleButtonGroupDays);
        recyclerViewStundenplanTag = view.findViewById(R.id.recyclerViewStundenplanTag);

        // RecyclerView einrichten
        recyclerViewStundenplanTag.setLayoutManager(new LinearLayoutManager(getContext()));
        stundenplanAdapter = new StundenplanAdapter(new ArrayList<>()); // Adapter mit leerer Liste initialisieren
        recyclerViewStundenplanTag.setAdapter(stundenplanAdapter);

        // Listener für die Tagesauswahl-Buttons
        toggleButtonGroupDays.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                MaterialButton checkedButton = view.findViewById(checkedId);
                selectedDay = checkedButton.getText().toString();
                loadStundenplanForSelectedDay(); // Stundenplan für den neuen Tag aus DB laden
            }
        });

        // Den Standardtag "Montag" als ausgewählt markieren und den Stundenplan laden
        if (toggleButtonGroupDays.getChildCount() > 0) {
            // Dies löst den Listener aus und lädt die Daten für den Montag
            ((MaterialButton)toggleButtonGroupDays.getChildAt(0)).setChecked(true);
        } else {
            // Fallback, falls keine Buttons gefunden wurden
            loadStundenplanForSelectedDay();
        }

        // OPTIONAL: Initial Daten in die Datenbank einfügen, wenn sie leer ist (nur für ersten Start)
        // Du kannst dies später entfernen, wenn du einen "Hinzufügen"-Dialog hast.
        // runOnceAfterDBSetup();
    }

    // Lädt Stundenplan-Daten aus der Datenbank für den aktuell ausgewählten Tag
    private void loadStundenplanForSelectedDay() {
        databaseWriteExecutor.execute(() -> {
            List<StundenplanEintrag> filteredList = stundenplanDao.getStundenplanEintraegeForTag(selectedDay);
            // Sortiere die gefilterte Liste nach StundenIndex
            Collections.sort(filteredList, Comparator.comparingInt(StundenplanEintrag::getStundenIndex));

            // UI-Update muss auf dem Haupt-Thread erfolgen
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    stundenplanAdapter.updateData(filteredList);
                });
            }
        });
    }

    // Methode zum Hinzufügen eines Eintrags zur DB (wird später von deinem Add-Dialog aufgerufen)
    public void addStundenplanEntryToDb(StundenplanEintrag eintrag) {
        databaseWriteExecutor.execute(() -> {
            stundenplanDao.insert(eintrag);
            // Nach dem Einfügen Daten neu laden, um die UI zu aktualisieren
            loadStundenplanForSelectedDay();
        });
    }

    // Methode zum Löschen eines Eintrags aus der DB (wird später von deinem Lösch-Dialog/Swipe aufgerufen)
    public void deleteStundenplanEntryFromDb(StundenplanEintrag eintrag) {
        databaseWriteExecutor.execute(() -> {
            stundenplanDao.delete(eintrag);
            loadStundenplanForSelectedDay();
        });
    }

    // Methode zum Aktualisieren eines Eintrags in der DB (wird später von deinem Edit-Dialog aufgerufen)
    public void updateStundenplanEntryInDb(StundenplanEintrag eintrag) {
        databaseWriteExecutor.execute(() -> {
            stundenplanDao.update(eintrag);
            loadStundenplanForSelectedDay();
        });
    }


    // --- OPTIONAL: Methode zum einmaligen Befüllen der DB mit Dummy-Daten beim ersten Start ---
    // Diese Methode dient nur dazu, beim ERSTEN Start der App ein paar Daten zu haben.
    // Entferne sie, sobald du den "Hinzufügen"-Dialog implementiert hast.
    private void runOnceAfterDBSetup() {
        // Überprüfe, ob bereits Daten vorhanden sind
        databaseWriteExecutor.execute(() -> {
            if (stundenplanDao.getAllStundenplanEintraege().isEmpty()) {
                List<StundenplanEintrag> initialData = new ArrayList<>();
                initialData.add(new StundenplanEintrag("Mo", stundenzeiten[0], "Mathe", "A201", "Hr. Schmidt", 0));
                initialData.add(new StundenplanEintrag("Mo", stundenzeiten[1], "Deutsch", "B102", "Fr. Meier", 1));
                initialData.add(new StundenplanEintrag("Mo", stundenzeiten[3], "Sport", "Turnhalle", "", 3));

                initialData.add(new StundenplanEintrag("Di", stundenzeiten[0], "Englisch", "C303", "Ms. Johnson", 0));
                initialData.add(new StundenplanEintrag("Di", stundenzeiten[5], "Physik", "Lab 1", "Fr. Dr. Wagner", 5));

                initialData.add(new StundenplanEintrag("Mi", stundenzeiten[2], "Chemie", "Lab 2", "Hr. Kuhn", 2));
                initialData.add(new StundenplanEintrag("Mi", stundenzeiten[7], "Musik", "Musikraum", "Hr. Klang", 7));

                initialData.add(new StundenplanEintrag("Do", stundenzeiten[4], "Geschichte", "G105", "Hr. Becker", 4));

                initialData.add(new StundenplanEintrag("Fr", stundenzeiten[0], "Kunst", "Kreativraum", "Fr. Schulz", 0));
                initialData.add(new StundenplanEintrag("Fr", stundenzeiten[6], "IT", "PC-Raum", "Hr. Lange", 6));

                stundenplanDao.insertAll(initialData);

                // Nach dem Einfügen die UI aktualisieren
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        loadStundenplanForSelectedDay();
                    });
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Executor Service herunterfahren, wenn das Fragment zerstört wird
        if (databaseWriteExecutor != null && !databaseWriteExecutor.isShutdown()) {
            databaseWriteExecutor.shutdown();
        }
    }
}