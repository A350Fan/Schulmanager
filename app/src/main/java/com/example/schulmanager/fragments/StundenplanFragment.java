package com.example.schulmanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Für Toast-Nachrichten

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Import für FAB

import com.example.schulmanager.R;
import com.example.schulmanager.adapters.StundenplanAdapter;
import com.example.schulmanager.models.StundenplanEintrag;
import com.example.schulmanager.database.AppDatabase;
import com.example.schulmanager.database.StundenplanDAO;
import com.example.schulmanager.dialogs.AddStundenplanEntryDialog; // Import deines Dialogs
import com.example.schulmanager.dialogs.OnStundenplanEntryAddedListener; // Import deines Listeners

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StundenplanFragment extends Fragment implements OnStundenplanEntryAddedListener { // Implementiere das Interface

    private MaterialButtonToggleGroup toggleButtonGroupDays;
    private RecyclerView recyclerViewStundenplanTag;
    private StundenplanAdapter stundenplanAdapter;
    private StundenplanDAO stundenplanDao;
    private FloatingActionButton fabAddEntry; // Referenz auf den FAB

    private final String[] tage = {"Mo", "Di", "Mi", "Do", "Fr"};
    private final String[] stundenzeiten = {
            "08:00 - 08:45", "08:50 - 09:35", "09:50 - 10:35", "10:40 - 11:25", "11:30 - 12:15",
            "12:20 - 13:05", "13:10 - 13:55", "14:00 - 14:45", "14:50 - 15:35", "15:40 - 16:25", "16:30 - 17:15"
    };

    private String selectedDay = "Mo";

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

        stundenplanDao = AppDatabase.getDatabase(getContext()).stundenplanDao();

        toggleButtonGroupDays = view.findViewById(R.id.toggleButtonGroupDays);
        recyclerViewStundenplanTag = view.findViewById(R.id.recyclerViewStundenplanTag);
        fabAddEntry = view.findViewById(R.id.fab_add_stundenplan_entry); // FAB initialisieren

        recyclerViewStundenplanTag.setLayoutManager(new LinearLayoutManager(getContext()));
        stundenplanAdapter = new StundenplanAdapter(new ArrayList<>());
        recyclerViewStundenplanTag.setAdapter(stundenplanAdapter);

        toggleButtonGroupDays.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                MaterialButton checkedButton = view.findViewById(checkedId);
                selectedDay = checkedButton.getText().toString();
                loadStundenplanForSelectedDay();
            }
        });

        if (toggleButtonGroupDays.getChildCount() > 0) {
            ((MaterialButton)toggleButtonGroupDays.getChildAt(0)).setChecked(true);
        } else {
            loadStundenplanForSelectedDay();
        }

        // NEU: Listener für den FAB
        fabAddEntry.setOnClickListener(v -> {
            AddStundenplanEntryDialog dialog = new AddStundenplanEntryDialog();
            dialog.setOnStundenplanEntryAddedListener(this); // Setze dieses Fragment als Listener
            dialog.show(getParentFragmentManager(), "AddStundenplanEntryDialog");
        });

        // OPTIONAL: Initial Daten in die Datenbank einfügen, wenn sie leer ist (nur für ersten Start)
        // runOnceAfterDBSetup();
    }

    private void loadStundenplanForSelectedDay() {
        databaseWriteExecutor.execute(() -> {
            List<StundenplanEintrag> filteredList = stundenplanDao.getStundenplanEintraegeForTag(selectedDay);
            Collections.sort(filteredList, Comparator.comparingInt(StundenplanEintrag::getStundenIndex));

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
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Eintrag hinzugefügt!", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Methode zum Löschen eines Eintrags aus der DB (wird später von deinem Lösch-Dialog/Swipe aufgerufen)
    public void deleteStundenplanEntryFromDb(StundenplanEintrag eintrag) {
        databaseWriteExecutor.execute(() -> {
            stundenplanDao.delete(eintrag);
            loadStundenplanForSelectedDay();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Eintrag gelöscht!", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Methode zum Aktualisieren eines Eintrags in der DB (wird später von deinem Edit-Dialog aufgerufen)
    public void updateStundenplanEntryInDb(StundenplanEintrag eintrag) {
        databaseWriteExecutor.execute(() -> {
            stundenplanDao.update(eintrag);
            loadStundenplanForSelectedDay();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Eintrag aktualisiert!", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // NEU: Implementierung der Schnittstellenmethode
    @Override
    public void onStundenplanEntryAdded(String fach, String uhrzeit, String raum, String lehrer, int stundenIndex) {
        // Erstelle ein neues StundenplanEintrag-Objekt
        StundenplanEintrag newEntry = new StundenplanEintrag(selectedDay, uhrzeit, fach, raum, lehrer, stundenIndex);
        // Füge es der Datenbank hinzu
        addStundenplanEntryToDb(newEntry);
    }


    // --- OPTIONAL: Methode zum einmaligen Befüllen der DB mit Dummy-Daten beim ersten Start ---
    private void runOnceAfterDBSetup() {
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
        if (databaseWriteExecutor != null && !databaseWriteExecutor.isShutdown()) {
            databaseWriteExecutor.shutdown();
        }
    }
}