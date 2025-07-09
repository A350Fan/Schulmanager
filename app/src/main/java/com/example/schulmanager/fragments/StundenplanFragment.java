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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.schulmanager.R;
import com.example.schulmanager.adapters.StundenplanAdapter;
import com.example.schulmanager.models.StundenplanEintrag;
import com.example.schulmanager.database.AppDatabase;
import com.example.schulmanager.database.StundenplanDAO;
import com.example.schulmanager.dialogs.AddStundenplanEntryDialog;
import com.example.schulmanager.dialogs.OnStundenplanEntryAddedListener;
import com.example.schulmanager.dialogs.DefineStundenzeitenDialog; // NEU: Import für den Stundenzeiten-Dialog

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
        StundenplanAdapter.OnItemActionListener,
        DefineStundenzeitenDialog.OnStundenzeitenDefinedListener { // NEU: Implementiere das Interface

    private MaterialButtonToggleGroup toggleButtonGroupDays;
    private RecyclerView recyclerViewStundenplanTag;
    private StundenplanAdapter stundenplanAdapter;
    private StundenplanDAO stundenplanDao;
    private FloatingActionButton fabAddEntry;
    private FloatingActionButton fabDefineStundenzeiten; // NEU: Für den neuen Button
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
        fabAddEntry = view.findViewById(R.id.fab_add_stundenplan_entry);
        fabDefineStundenzeiten = view.findViewById(R.id.fabDefineStundenzeiten); // NEU: Initialisiere den neuen Button

        recyclerViewStundenplanTag.setLayoutManager(new LinearLayoutManager(getContext()));
        stundenplanAdapter = new StundenplanAdapter(new ArrayList<>(), this);
        recyclerViewStundenplanTag.setAdapter(stundenplanAdapter);

        // ItemTouchHelper für Swipe-to-Dismiss konfigurieren und an RecyclerView anbinden
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Keine Drag & Drop-Funktionalität
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                StundenplanEintrag entryToDelete = stundenplanAdapter.getItemAtPosition(position);
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
            AddStundenplanEntryDialog dialog = new AddStundenplanEntryDialog();
            dialog.setOnStundenplanEntryAddedListener(this);
            dialog.show(getParentFragmentManager(), "AddStundenplanEntryDialog");
        });

        // NEU: Klick-Listener für den "Stundenzeiten festlegen"-Button
        fabDefineStundenzeiten.setOnClickListener(v -> {
            DefineStundenzeitenDialog dialog = new DefineStundenzeitenDialog();
            dialog.setOnStundenzeitenDefinedListener(this); // Wichtig: Setze den Listener
            dialog.show(getParentFragmentManager(), "define_stundenzeiten_dialog");
        });


        // runOnceAfterDBSetup(); // Optional: Initialdaten für Testzwecke
    }

    private void loadStundenplanForSelectedDay() {
        databaseWriteExecutor.execute(() -> {
            List<StundenplanEintrag> filteredList = stundenplanDao.getStundenplanEintraegeForTag(selectedDay);
            // Sortiere nach StundenIndex aufsteigend
            Collections.sort(filteredList, Comparator.comparingInt(StundenplanEintrag::getStundenIndex));

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> stundenplanAdapter.updateData(filteredList));
            }
        });
    }

    public void addStundenplanEntryToDb(StundenplanEintrag eintrag) {
        databaseWriteExecutor.execute(() -> {
            stundenplanDao.insert(eintrag);
            loadStundenplanForSelectedDay(); // Daten neu laden, um die UI zu aktualisieren
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Eintrag hinzugefügt!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void deleteStundenplanEntryFromDb(StundenplanEintrag eintrag) {
        databaseWriteExecutor.execute(() -> {
            stundenplanDao.delete(eintrag);
            loadStundenplanForSelectedDay(); // Daten neu laden
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Eintrag gelöscht!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Implementierung der Schnittstellenmethode für das Hinzufügen (vom Dialog)
    @Override
    public void onStundenplanEntryAdded(String fach, String uhrzeit, String raum, String lehrer, int stundenIndex) {
        StundenplanEintrag newEntry = new StundenplanEintrag(selectedDay, uhrzeit, fach, raum, lehrer, stundenIndex);
        addStundenplanEntryToDb(newEntry);
    }

    // Implementierung der Schnittstellenmethode für das Löschen (vom Adapter via ItemTouchHelper)
    @Override
    public void onDeleteClick(StundenplanEintrag eintrag) {
        // Diese Methode wird tatsächlich nicht direkt vom Adapter aufgerufen, wenn du Swipe-to-Dismiss nutzt.
        // Der ItemTouchHelper ruft direkt stundenplanAdapter.getItemAtPosition() auf.
        // Die onDeleteClick Methode wäre relevanter, wenn du z.B. einen expliziten Lösch-Button im item_stundenplan_eintrag hättest.
        // In diesem Fall, da wir ItemTouchHelper verwenden, ist der Aufruf in onSwiped() ausreichend.
        // Du könntest hier jedoch einen Bestätigungsdialog anzeigen, bevor du deleteStundenplanEntryFromDb aufrufst.
    }

    // NEU: Implementierung der Schnittstellenmethode für das Speichern von Stundenzeiten
    @Override
    public void onStundenzeitenSaved() {
        Toast.makeText(getContext(), "Stundenzeiten erfolgreich festgelegt!", Toast.LENGTH_SHORT).show();
        // Hier könntest du weitere Aktionen ausführen, z.B. wenn die Anzeige des Stundenplans
        // direkt von den Stundenzeit-Definitionen abhängt, müsstest du ihn aktualisieren.
        // Aktuell ist die "Uhrzeit" im StundenplanEintrag selbst gespeichert, daher
        // ist keine direkte Aktualisierung hier notwendig, außer du änderst das Verhalten.
    }


    // OPTIONAL: Methode zum einmaligen Befüllen der DB mit Dummy-Daten beim ersten Start
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
                    getActivity().runOnUiThread(() -> loadStundenplanForSelectedDay());
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Korrektur: Nutze den korrekten Variablennamen databaseWriteExecutor
        if (databaseWriteExecutor != null && !databaseWriteExecutor.isShutdown()) {
            databaseWriteExecutor.shutdown();
        }
    }
}