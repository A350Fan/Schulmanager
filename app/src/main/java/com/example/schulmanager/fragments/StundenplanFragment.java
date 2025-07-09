package com.example.schulmanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schulmanager.adapters.TimetableAdapter;
import com.example.schulmanager.database.TimetableDAO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.schulmanager.R;
import com.example.schulmanager.models.StundenplanEintrag;
import com.example.schulmanager.models.StundenzeitDefinition; // NEU
import com.example.schulmanager.database.AppDatabase;
import com.example.schulmanager.database.StundenzeitDefinitionDAO; // NEU
import com.example.schulmanager.dialogs.AddStundenplanEntryDialog;
import com.example.schulmanager.dialogs.OnStundenplanEntryAddedListener;
import com.example.schulmanager.dialogs.DefineStundenzeitenDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Implementiere beide Interfaces: OnStundenplanEntryAddedListener und OnItemActionListener,
// sowie das neue OnStundenzeitenDefinedListener
public class StundenplanFragment extends Fragment implements
        OnStundenplanEntryAddedListener,
        TimetableAdapter.OnItemActionListener,
        DefineStundenzeitenDialog.OnStundenzeitenDefinedListener {

    private MaterialButtonToggleGroup toggleButtonGroupDays;
    private RecyclerView recyclerViewStundenplanTag;
    private TimetableAdapter timetableAdapter;
    private TimetableDAO timetableDao;
    private StundenzeitDefinitionDAO stundenzeitDefinitionDao; // NEU: DAO für StundenzeitDefinitionen
    private FloatingActionButton fabAddEntry;
    private FloatingActionButton fabDefineStundenzeiten;
    // Die feste stundenzeiten-Array ist jetzt obsolet, da sie aus der DB kommt.
    // private final String[] stundenzeiten = { ... };

    private String selectedDay = "Mo";

    private ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public StundenplanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timetable, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timetableDao = AppDatabase.getDatabase(getContext()).stundenplanDao();
        stundenzeitDefinitionDao = AppDatabase.getDatabase(getContext()).stundenzeitDefinitionDao(); // NEU: Initialisiere StundenzeitDefinitionDAO

        toggleButtonGroupDays = view.findViewById(R.id.toggleButtonGroupDays);
        recyclerViewStundenplanTag = view.findViewById(R.id.recyclerViewStundenplanTag);
        fabAddEntry = view.findViewById(R.id.fab_add_stundenplan_entry);
        fabDefineStundenzeiten = view.findViewById(R.id.fabDefineStundenzeiten);

        recyclerViewStundenplanTag.setLayoutManager(new LinearLayoutManager(getContext()));
        timetableAdapter = new TimetableAdapter(new ArrayList<>(), this);
        recyclerViewStundenplanTag.setAdapter(timetableAdapter);

        // ItemTouchHelper für Swipe-to-Dismiss konfigurieren und an RecyclerView anbinden
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Keine Drag & Drop-Funktionalität
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                StundenplanEintrag entryToDelete = timetableAdapter.getItemAtPosition(position);
                deleteStundenplanEntryFromDb(entryToDelete);
            }
        }).attachToRecyclerView(recyclerViewStundenplanTag);

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

        fabAddEntry.setOnClickListener(v -> {
            // NEU: Stundenzeit-Definitionen laden und an den Dialog übergeben
            databaseWriteExecutor.execute(() -> {
                List<StundenzeitDefinition> definedStundenzeiten = stundenzeitDefinitionDao.getAllStundenzeitDefinitions();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (definedStundenzeiten == null || definedStundenzeiten.isEmpty()) {
                            Toast.makeText(getContext(), "Bitte zuerst Stundenzeiten festlegen!", Toast.LENGTH_LONG).show();
                            // Optional: Direkt den DefineStundenzeitenDialog öffnen
                            // DefineStundenzeitenDialog defineDialog = new DefineStundenzeitenDialog();
                            // defineDialog.setOnStundenzeitenDefinedListener(this);
                            // defineDialog.show(getParentFragmentManager(), "define_stundenzeiten_dialog");
                        } else {
                            // Erstelle den Dialog über die neue newInstance Factory-Methode
                            AddStundenplanEntryDialog dialog = AddStundenplanEntryDialog.newInstance(definedStundenzeiten);
                            dialog.setOnStundenplanEntryAddedListener(this);
                            dialog.show(getParentFragmentManager(), "AddStundenplanEntryDialog");
                        }
                    });
                }
            });
        });

        // Klick-Listener für den "Stundenzeiten festlegen"-Button
        fabDefineStundenzeiten.setOnClickListener(v -> {
            DefineStundenzeitenDialog dialog = new DefineStundenzeitenDialog();
            dialog.setOnStundenzeitenDefinedListener(this);
            dialog.show(getParentFragmentManager(), "define_stundenzeiten_dialog");
        });


        // runOnceAfterDBSetup(); // Optional: Initialdaten für Testzwecke
    }

    private void loadStundenplanForSelectedDay() {
        databaseWriteExecutor.execute(() -> {
            List<StundenplanEintrag> filteredList = timetableDao.getStundenplanEintraegeForTag(selectedDay);
            // Sortiere nach StundenIndex aufsteigend
            Collections.sort(filteredList, Comparator.comparingInt(StundenplanEintrag::getStundenIndex));

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> timetableAdapter.updateData(filteredList));
            }
        });
    }

    public void addStundenplanEntryToDb(StundenplanEintrag eintrag) {
        databaseWriteExecutor.execute(() -> {
            timetableDao.insert(eintrag);
            loadStundenplanForSelectedDay(); // Daten neu laden, um die UI zu aktualisieren
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Eintrag hinzugefügt!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void deleteStundenplanEntryFromDb(StundenplanEintrag eintrag) {
        databaseWriteExecutor.execute(() -> {
            timetableDao.delete(eintrag);
            loadStundenplanForSelectedDay(); // Daten neu laden
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Eintrag gelöscht!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Implementierung der Schnittstellenmethode für das Hinzufügen (vom Dialog)
    @Override
    public void onStundenplanEntryAdded(String fach, String raum, String lehrer, int stundenIndex) { // Angepasste Signatur
        // NEU: Uhrzeit anhand des StundenIndex aus den Definitionen laden
        databaseWriteExecutor.execute(() -> {
            StundenzeitDefinition definition = stundenzeitDefinitionDao.getStundenzeitDefinitionByIndex(stundenIndex);
            String uhrzeit = (definition != null) ? definition.getUhrzeitString() : "Uhrzeit unbekannt"; // Fallback-Wert

            StundenplanEintrag newEntry = new StundenplanEintrag(selectedDay, uhrzeit, fach, raum, lehrer, stundenIndex);
            addStundenplanEntryToDb(newEntry);
        });
    }

    // Implementierung der Schnittstellenmethode für das Löschen (vom Adapter via ItemTouchHelper)
    @Override
    public void onDeleteClick(StundenplanEintrag eintrag) {
        // Diese Methode wird tatsächlich nicht direkt vom Adapter aufgerufen, wenn du Swipe-to-Dismiss nutzt.
        // Der ItemTouchHelper ruft direkt timetableAdapter.getItemAtPosition() auf.
        // Die onDeleteClick Methode wäre relevanter, wenn du z.B. einen expliziten Lösch-Button im item_stundenplan_eintrag hättest.
        // In diesem Fall, da wir ItemTouchHelper verwenden, ist der Aufruf in onSwiped() ausreichend.
        // Du könntest hier jedoch einen Bestätigungsdialog anzeigen, bevor du deleteStundenplanEntryFromDb aufrufst.
    }

    // Implementierung der Schnittstellenmethode für das Speichern von Stundenzeiten
    @Override
    public void onStundenzeitenSaved() {
        Toast.makeText(getContext(), "Stundenzeiten erfolgreich festgelegt!", Toast.LENGTH_SHORT).show();
        // Da die Uhrzeiten im StundenplanEintrag selbst gespeichert sind, ist keine Neuladung
        // des Stundenplans direkt hier notwendig. Wenn ein StundenplanEintrag die Uhrzeit
        // nur als Index speichern würde, müsste man hier loadStundenplanForSelectedDay() aufrufen.
        // Die FabAddEntry wird die neuesten Definitionen laden, wenn sie das nächste Mal geöffnet wird.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseWriteExecutor != null && !databaseWriteExecutor.isShutdown()) {
            databaseWriteExecutor.shutdown();
        }
    }
}