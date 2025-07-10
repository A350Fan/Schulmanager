package com.example.schulmanager.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schulmanager.R;
import com.example.schulmanager.adapters.StundenzeitDefinitionAdapter;
import com.example.schulmanager.database.AppDatabase;
import com.example.schulmanager.database.StundenzeitDefinitionDAO;
import com.example.schulmanager.models.StundenzeitDefinition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ein DialogFragment, das es dem Benutzer ermöglicht, die Uhrzeiten
 * für jede Schulstunde (z.B. 1. Stunde: 08:00 - 08:45) zu definieren.
 * Die definierten Stundenzeiten werden in einer Room-Datenbank gespeichert.
 */
public class DefineStundenzeitenDialog extends DialogFragment {

    // UI-Elemente
    private RecyclerView recyclerView; // RecyclerView zur Anzeige der Stundenzeit-Eingabefelder
    private StundenzeitDefinitionAdapter adapter; // Adapter für den RecyclerView
    private Button buttonSave; // Button zum Speichern der Änderungen

    // Datenbank-Komponenten
    private StundenzeitDefinitionDAO stundenzeitDefinitionDao; // DAO für den Zugriff auf Stundenzeit-Definitionen
    private ExecutorService databaseExecutor = Executors.newFixedThreadPool(4); // Executor für asynchrone DB-Operationen

    /**
     * Optionales Interface, um das aufrufende Fragment oder die Activity zu benachrichtigen,
     * wenn die Stundenzeiten erfolgreich gespeichert wurden.
     */
    public interface OnStundenzeitenDefinedListener {
        void onStundenzeitenSaved();
    }
    private OnStundenzeitenDefinedListener listener; // Instanz des Listeners

    /**
     * Setzt den Listener, der benachrichtigt wird, wenn die Stundenzeiten gespeichert wurden.
     * @param listener Die Implementierung des OnStundenzeitenDefinedListener.
     */
    public void setOnStundenzeitenDefinedListener(OnStundenzeitenDefinedListener listener) {
        this.listener = listener;
    }

    /**
     * Wird aufgerufen, um die View-Hierarchie des DialogFragments zu erstellen und zurückzugeben.
     * @param inflater Der LayoutInflater, der zum Inflaten von Views verwendet wird.
     * @param container Die übergeordnete View, an die die UI des Fragments angehängt werden soll.
     * @param savedInstanceState Der zuvor gespeicherte Zustand des Fragments.
     * @return Die View für die UI des Fragments.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Optional: setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth);
        // Diese Zeile könnte verwendet werden, um den Stil des Dialogs anzupassen (z.B. Vollbild oder ohne ActionBar).
        return inflater.inflate(R.layout.dialog_define_lessonhours, container, false);
    }

    /**
     * Wird direkt nach onCreateView() aufgerufen, um die View-Hierarchie zu initialisieren.
     * @param view Die von onCreateView() zurückgegebene View.
     * @param savedInstanceState Der zuvor gespeicherte Zustand des Fragments.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialisiere das DAO für den Datenbankzugriff
        stundenzeitDefinitionDao = AppDatabase.getDatabase(getContext()).stundenzeitDefinitionDao();

        // Referenzen zu den UI-Elementen erhalten
        recyclerView = view.findViewById(R.id.recyclerViewStundenzeiten);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Setze einen linearen LayoutManager

        // Initialisiere den Adapter mit einer leeren Liste. Die tatsächlichen Daten werden später geladen.
        adapter = new StundenzeitDefinitionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        buttonSave = view.findViewById(R.id.buttonSaveStundenzeiten);
        // Setze den Klick-Listener für den Speichern-Button
        buttonSave.setOnClickListener(v -> saveStundenzeiten());

        // Lade bestehende Stundenzeit-Definitionen aus der Datenbank, wenn der Dialog geöffnet wird
        loadStundenzeiten();
    }

    /**
     * Lädt bestehende Stundenzeit-Definitionen aus der Datenbank.
     * Es wird eine Liste von 11 Stunden erstellt (Index 0-10). Falls für einen Index bereits
     * eine Definition existiert, wird diese geladen; ansonsten wird eine leere Definition erstellt.
     * Die Liste wird anschließend nach dem StundenIndex sortiert und der Adapter aktualisiert.
     */
    private void loadStundenzeiten() {
        databaseExecutor.execute(() -> {
            // Lade alle vorhandenen Definitionen aus der Datenbank
            List<StundenzeitDefinition> existingDefinitions = stundenzeitDefinitionDao.getAllStundenzeitDefinitions();
            // Erstelle eine temporäre Liste, die an den Adapter übergeben wird
            List<StundenzeitDefinition> displayList = new ArrayList<>();

            // Schleife für 11 Stunden (z.B. 1. bis 11. Stunde, Index 0-10)
            for (int i = 0; i < 11; i++) {
                boolean found = false;
                // Suche, ob eine Definition für den aktuellen StundenIndex bereits existiert
                for (StundenzeitDefinition def : existingDefinitions) {
                    if (def.getStundenIndex() == i) {
                        displayList.add(def); // Füge die vorhandene Definition hinzu
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // Wenn keine Definition für diesen Index gefunden wurde, füge eine neue, leere Definition hinzu
                    displayList.add(new StundenzeitDefinition(i, ""));
                }
            }
            // Sortiere die Liste nach dem StundenIndex, um die korrekte Reihenfolge im RecyclerView zu gewährleisten
            displayList.sort(Comparator.comparingInt(StundenzeitDefinition::getStundenIndex));

            // Aktualisiere den Adapter im UI-Thread mit den vorbereiteten Daten
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.updateData(displayList));
            }
        });
    }

    /**
     * Speichert die vom Benutzer im Dialog eingegebenen Stundenzeit-Definitionen in die Datenbank.
     * Leere Einträge werden optional gelöscht, anstatt gespeichert zu werden.
     */
    private void saveStundenzeiten() {
        // Rufe die aktuell im Adapter gehaltenen und möglicherweise bearbeiteten Definitionen ab
        List<StundenzeitDefinition> definitionsToSave = adapter.getUpdatedStundenzeiten();
        databaseExecutor.execute(() -> {
            for (StundenzeitDefinition def : definitionsToSave) {
                // Speichere nur Einträge, die einen nicht-leeren Uhrzeit-String haben
                if (def.getUhrzeitString() != null && !def.getUhrzeitString().trim().isEmpty()) {
                    // Die `insert`-Methode von Room mit `OnConflictStrategy.REPLACE` wird ein existierendes
                    // Objekt aktualisieren oder ein neues einfügen, wenn die ID 0 ist oder noch nicht existiert.
                    stundenzeitDefinitionDao.insert(def);
                } else {
                    // Wenn der Benutzer einen Eintrag leer gelassen hat, lösche ihn aus der Datenbank, falls er existierte
                    stundenzeitDefinitionDao.delete(def);
                }
            }
            // Führe UI-Operationen im UI-Thread aus
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Stundenzeiten gespeichert!", Toast.LENGTH_SHORT).show();
                    // Benachrichtige den Listener, falls gesetzt, dass die Stundenzeiten gespeichert wurden
                    if (listener != null) {
                        listener.onStundenzeitenSaved();
                    }
                    dismiss(); // Schließe den Dialog nach dem Speichern
                });
            }
        });
    }

    /**
     * Wird aufgerufen, wenn die View-Hierarchie des Fragments zerstört wird.
     * Hier werden Ressourcen freigegeben, insbesondere der ExecutorService, um Speicherlecks zu vermeiden.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Fahre den ExecutorService ordnungsgemäß herunter, um zu verhindern, dass Threads weiterlaufen
        if (databaseExecutor != null && !databaseExecutor.isShutdown()) {
            databaseExecutor.shutdown();
        }
    }
}