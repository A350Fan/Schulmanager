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
import com.example.schulmanager.dialogs.AddTimetableEntryDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.schulmanager.R;
import com.example.schulmanager.models.StundenplanEintrag;
import com.example.schulmanager.models.StundenzeitDefinition;
import com.example.schulmanager.database.AppDatabase;
import com.example.schulmanager.database.StundenzeitDefinitionDAO;
import com.example.schulmanager.dialogs.OnTimetableEntryAddedListener;
import com.example.schulmanager.dialogs.DefineStundenzeitenDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ein Fragment zur Anzeige und Verwaltung des Stundenplans.
 * Es ermöglicht das Hinzufügen, Anzeigen und Löschen von Stundenplaneinträgen
 * sowie das Definieren von Stundenzeiten (z.B. 1. Stunde: 08:00-08:45).
 * <p>
 * Implementiert mehrere Listener-Interfaces für die Interaktion mit Dialogen und dem Adapter.
 */
public class TimetableFragment extends Fragment implements
        OnTimetableEntryAddedListener, // Listener für den Dialog zum Hinzufügen von Stundenplaneinträgen
        TimetableAdapter.OnItemActionListener, // Listener für Aktionen auf RecyclerView-Elementen (z.B. Löschen)
        DefineStundenzeitenDialog.OnStundenzeitenDefinedListener { // Listener für den Dialog zum Definieren von Stundenzeiten

    // Adapter und DAOs (Data Access Objects) für die Datenbankinteraktion
    private TimetableAdapter timetableAdapter;
    private TimetableDAO timetableDao; // DAO für Stundenplan-Einträge
    private StundenzeitDefinitionDAO stundenzeitDefinitionDao; // DAO für Stundenzeit-Definitionen

    // Aktuell ausgewählter Wochentag (Standard: "Mo" - Montag)
    private String selectedDay = "Mo";

    // ExecutorService für asynchrone Datenbankoperationen, um die UI nicht zu blockieren
    private ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    /**
     * Erforderlicher leerer öffentlicher Konstruktor.
     */
    public TimetableFragment() {
        // Konstruktor muss leer sein.
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
        // Inflate das Layout für dieses Fragment
        return inflater.inflate(R.layout.fragment_timetable, container, false);
    }

    /**
     * Wird direkt nach onCreateView() aufgerufen und gibt dir die Möglichkeit, die View-Hierarchie des Fragments
     * zu initialisieren oder Operationen durchzuführen, die die View erfordern.
     * @param view Die View, die von onCreateView() zurückgegeben wurde.
     * @param savedInstanceState Wenn nicht null, dieses Fragment wird von einem zuvor gespeicherten Zustand rekonstituiert.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialisiere die DAOs für den Datenbankzugriff
        timetableDao = AppDatabase.getDatabase(getContext()).stundenplanDao();
        stundenzeitDefinitionDao = AppDatabase.getDatabase(getContext()).stundenzeitDefinitionDao();

        // Referenzen zu den UI-Elementen aus dem Layout erhalten

        // Gruppe von Buttons zur Auswahl des Wochentags
        MaterialButtonToggleGroup toggleButtonGroupDays = view.findViewById(R.id.toggleButtonGroupDays);
        // RecyclerView zur Anzeige der Stundenplaneinträge für den ausgewählten Tag
        RecyclerView recyclerViewStundenplanTag = view.findViewById(R.id.recyclerViewStundenplanTag);
        // Floating Action Button zum Hinzufügen eines neuen Eintrags
        FloatingActionButton fabAddEntry = view.findViewById(R.id.fab_add_stundenplan_entry);
        // Floating Action Button zum Definieren der Stundenzeiten
        FloatingActionButton fabDefineStundenzeiten = view.findViewById(R.id.fabDefineStundenzeiten);

        // Konfiguriere den RecyclerView
        recyclerViewStundenplanTag.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialisiere den Adapter mit einer leeren Liste und dem Fragment als ItemActionListener
        timetableAdapter = new TimetableAdapter(new ArrayList<>(), this);
        recyclerViewStundenplanTag.setAdapter(timetableAdapter);

        // Konfiguriere ItemTouchHelper für Swipe-to-Dismiss-Funktionalität im RecyclerView
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            /**
             * Wird aufgerufen, um eine Drag & Drop-Operation zu verwalten. Hier nicht verwendet.
             * @return false, da keine Drag & Drop-Funktionalität implementiert ist.
             */
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Keine Drag & Drop-Funktionalität
            }

            /**
             * Wird aufgerufen, wenn ein Element im RecyclerView gewischt wird.
             * @param viewHolder Der ViewHolder des Elements, das gewischt wurde.
             * @param direction Die Richtung, in die das Element gewischt wurde (LEFT oder RIGHT).
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition(); // Position des gewischten Elements
                StundenplanEintrag entryToDelete = timetableAdapter.getItemAtPosition(position); // Das zu löschende Objekt abrufen
                deleteStundenplanEntryFromDb(entryToDelete); // Eintrag aus der Datenbank löschen
            }
        }).attachToRecyclerView(recyclerViewStundenplanTag); // ItemTouchHelper an den RecyclerView anhängen

        // Listener für die Toggle-Button-Gruppe der Wochentage
        toggleButtonGroupDays.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) { // Nur reagieren, wenn ein Button ausgewählt wurde
                MaterialButton checkedButton = view.findViewById(checkedId);
                selectedDay = checkedButton.getText().toString(); // Den ausgewählten Tag aktualisieren
                loadStundenplanForSelectedDay(); // Stundenplan für den neuen Tag laden
            }
        });

        // Setzt den ersten Button in der Toggle-Gruppe als standardmäßig ausgewählt
        if (toggleButtonGroupDays.getChildCount() > 0) {
            ((MaterialButton)toggleButtonGroupDays.getChildAt(0)).setChecked(true);
        } else {
            // Falls keine Buttons vorhanden sind, lade den Stundenplan trotzdem (z.B. Standardtag)
            loadStundenplanForSelectedDay();
        }

        // Klick-Listener für den "Eintrag hinzufügen"-FAB
        fabAddEntry.setOnClickListener(v -> {
            // Lade Stundenzeit-Definitionen asynchron aus der Datenbank
            databaseWriteExecutor.execute(() -> {
                List<StundenzeitDefinition> definedStundenzeiten = stundenzeitDefinitionDao.getAllStundenzeitDefinitions();
                // Wechsle zurück zum UI-Thread, um UI-Operationen durchzuführen
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (definedStundenzeiten == null || definedStundenzeiten.isEmpty()) {
                            // Wenn keine Stundenzeiten definiert sind, zeige eine Toast-Nachricht an
                            Toast.makeText(getContext(), "Bitte zuerst Stundenzeiten festlegen!", Toast.LENGTH_LONG).show();
                        } else {
                            // Wenn Stundenzeiten definiert sind, öffne den Dialog zum Hinzufügen eines Eintrags
                            // Erstelle den Dialog über die neue newInstance Factory-Methode, die die Stundenzeiten übergibt
                            AddTimetableEntryDialog dialog = AddTimetableEntryDialog.newInstance(definedStundenzeiten);
                            dialog.setOnStundenplanEntryAddedListener(this); // Setze dieses Fragment als Listener
                            dialog.show(getParentFragmentManager(), "AddTimetableEntryDialog");
                        }
                    });
                }
            });
        });

        // Klick-Listener für den "Stundenzeiten festlegen"-FAB
        fabDefineStundenzeiten.setOnClickListener(v -> {
            // Öffne den Dialog zum Definieren der Stundenzeiten
            DefineStundenzeitenDialog dialog = new DefineStundenzeitenDialog();
            dialog.setOnStundenzeitenDefinedListener(this); // Setze dieses Fragment als Listener
            dialog.show(getParentFragmentManager(), "define_stundenzeiten_dialog");
        });

        // runOnceAfterDBSetup(); // Aufruf einer Methode zum Hinzufügen von Initialdaten für Testzwecke
    }

    /**
     * Lädt die Stundenplaneinträge für den aktuell ausgewählten Wochentag aus der Datenbank
     * und aktualisiert den RecyclerView. Die Daten werden nach StundenIndex sortiert.
     */
    private void loadStundenplanForSelectedDay() {
        databaseWriteExecutor.execute(() -> {
            // Stundenplan-Einträge für den ausgewählten Tag aus der Datenbank abrufen
            List<StundenplanEintrag> filteredList = timetableDao.getStundenplanEintraegeForTag(selectedDay);
            // Sortiere die Liste nach dem StundenIndex aufsteigend, um die korrekte Reihenfolge zu gewährleisten
            Collections.sort(filteredList, Comparator.comparingInt(StundenplanEintrag::getStundenIndex));

            // Aktualisiere den Adapter im UI-Thread, da UI-Operationen dort ausgeführt werden müssen
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> timetableAdapter.updateData(filteredList));
            }
        });
    }

    /**
     * Fügt einen neuen Stundenplaneintrag in die Datenbank ein und aktualisiert anschließend die Anzeige.
     * @param eintrag Der hinzuzufügende StundenplanEintrag.
     */
    public void addStundenplanEntryToDb(StundenplanEintrag eintrag) {
        databaseWriteExecutor.execute(() -> {
            timetableDao.insert(eintrag); // Eintrag in die Datenbank einfügen
            loadStundenplanForSelectedDay(); // UI aktualisieren, indem die Daten neu geladen werden
            // Zeige eine Toast-Nachricht im UI-Thread an
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Eintrag hinzugefügt!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Löscht einen Stundenplaneintrag aus der Datenbank und aktualisiert anschließend die Anzeige.
     * @param eintrag Der zu löschende StundenplanEintrag.
     */
    public void deleteStundenplanEntryFromDb(StundenplanEintrag eintrag) {
        databaseWriteExecutor.execute(() -> {
            timetableDao.delete(eintrag); // Eintrag aus der Datenbank löschen
            loadStundenplanForSelectedDay(); // UI aktualisieren, indem die Daten neu geladen werden
            // Zeige eine Toast-Nachricht im UI-Thread an
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Eintrag gelöscht!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Implementierung der Schnittstellenmethode {@link OnTimetableEntryAddedListener#onStundenplanEntryAdded(String, String, String, int)}.
     * Wird vom AddTimetableEntryDialog aufgerufen, nachdem ein neuer Eintrag erfolgreich hinzugefügt wurde.
     *
     * @param fach Das Fach des neuen Eintrags.
     * @param raum Der Raum des neuen Eintrags.
     * @param lehrer Der Lehrer des neuen Eintrags.
     * @param stundenIndex Der Stundenindex des neuen Eintrags.
     */
    @Override
    public void onStundenplanEntryAdded(String fach, String raum, String lehrer, int stundenIndex) {
        // Lade die entsprechende Uhrzeit-Definition basierend auf dem StundenIndex aus der Datenbank
        databaseWriteExecutor.execute(() -> {
            StundenzeitDefinition definition = stundenzeitDefinitionDao.getStundenzeitDefinitionByIndex(stundenIndex);
            // Nutze die definierte Uhrzeit oder einen Fallback-Wert, falls keine Definition gefunden wurde
            String uhrzeit = (definition != null) ? definition.getUhrzeitString() : "Uhrzeit unbekannt";

            // Erstelle einen neuen StundenplanEintrag mit den erhaltenen Daten und der ermittelten Uhrzeit
            StundenplanEintrag newEntry = new StundenplanEintrag(selectedDay, uhrzeit, fach, raum, lehrer, stundenIndex);
            addStundenplanEntryToDb(newEntry); // Füge den neuen Eintrag der Datenbank hinzu
        });
    }

    /**
     * Implementierung der Schnittstellenmethode {@link TimetableAdapter.OnItemActionListener#onDeleteClick(StundenplanEintrag)}.
     * Diese Methode ist hier primär als Teil des {@link TimetableAdapter.OnItemActionListener}-Interfaces enthalten.
     * Im aktuellen Setup mit {@link ItemTouchHelper} für Swipe-to-Dismiss wird der Löschvorgang
     * direkt in der `onSwiped`-Methode des ItemTouchHelper behandelt.
     * <p>
     * Falls man z.B. einen expliziten Lösch-Button im Listenelement hätte,
     * würde dieser Listener vom Adapter aufgerufen und diese Methode würde die Löschlogik initiieren.
     * Aktuell dient sie eher als Platzhalter oder für zukünftige Erweiterungen (z.B. Bestätigungsdialog vor dem Löschen).
     * @param eintrag Der StundenplanEintrag, für den die Löschaktion ausgelöst wurde.
     */
    @Override
    public void onDeleteClick(StundenplanEintrag eintrag) {
        // Diese Methode wird tatsächlich nicht direkt vom Adapter aufgerufen, wenn du Swipe-to-Dismiss nutzt.
        // Der ItemTouchHelper ruft direkt timetableAdapter.getItemAtPosition() auf und verarbeitet das Löschen in onSwiped().
        // Du könntest hier jedoch einen Bestätigungsdialog anzeigen, bevor du deleteStundenplanEntryFromDb aufrufst,
        // falls onDeleteClick von einem expliziten Lösch-Button im Listenelement aufgerufen würde.
    }

    /**
     * Implementierung der Schnittstellenmethode {@link DefineStundenzeitenDialog.OnStundenzeitenDefinedListener#onStundenzeitenSaved()}.
     * Wird vom DefineStundenzeitenDialog aufgerufen, nachdem die Stundenzeiten erfolgreich gespeichert wurden.
     */
    @Override
    public void onStundenzeitenSaved() {
        // Zeigt eine Bestätigungsnachricht an, dass die Stundenzeiten festgelegt wurden.
        Toast.makeText(getContext(), "Stundenzeiten erfolgreich festgelegt!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Wird aufgerufen, wenn das Fragment zerstört wird.
     * Hier werden Ressourcen freigegeben, insbesondere der ExecutorService, um Speicherlecks zu vermeiden.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // ExecutorService ordnungsgemäß herunterfahren, um zu verhindern, dass Threads weiterlaufen
        if (databaseWriteExecutor != null && !databaseWriteExecutor.isShutdown()) {
            databaseWriteExecutor.shutdown();
        }
    }
}