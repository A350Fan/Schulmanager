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
import com.example.schulmanager.adapters.StundenzeitDefinitionAdapter; // Diese Klasse werden wir als Nächstes erstellen
import com.example.schulmanager.database.AppDatabase;
import com.example.schulmanager.database.StundenzeitDefinitionDAO;
import com.example.schulmanager.models.StundenzeitDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefineStundenzeitenDialog extends DialogFragment {

    private RecyclerView recyclerView;
    private StundenzeitDefinitionAdapter adapter;
    private Button buttonSave;
    private StundenzeitDefinitionDAO stundenzeitDefinitionDao;
    private ExecutorService databaseExecutor = Executors.newFixedThreadPool(4);

    // Optionaler Listener, falls das aufrufende Fragment benachrichtigt werden soll
    public interface OnStundenzeitenDefinedListener {
        void onStundenzeitenSaved();
    }
    private OnStundenzeitenDefinedListener listener;

    public void setOnStundenzeitenDefinedListener(OnStundenzeitenDefinedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Setzt den Stil des Dialogs, um einen vollen Dialog zu erhalten
        // setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth);
        return inflater.inflate(R.layout.dialog_define_stundenzeiten, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stundenzeitDefinitionDao = AppDatabase.getDatabase(getContext()).stundenzeitDefinitionDao();

        recyclerView = view.findViewById(R.id.recyclerViewStundenzeiten);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialisiere den Adapter mit einer leeren Liste
        // Der Adapter wird erst im nächsten Schritt genauer definiert, da er eine eigene Logik braucht
        adapter = new StundenzeitDefinitionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        buttonSave = view.findViewById(R.id.buttonSaveStundenzeiten);
        buttonSave.setOnClickListener(v -> saveStundenzeiten());

        loadStundenzeiten(); // Lade bestehende Definitionen beim Öffnen
    }

    private void loadStundenzeiten() {
        databaseExecutor.execute(() -> {
            List<StundenzeitDefinition> existingDefinitions = stundenzeitDefinitionDao.getAllStundenzeitDefinitions();
            // Erstelle eine Liste von 11 leeren/initialen Definitionen
            List<StundenzeitDefinition> displayList = new ArrayList<>();
            for (int i = 0; i < 11; i++) { // Von 0 bis 10 für 11 Stunden
                boolean found = false;
                for (StundenzeitDefinition def : existingDefinitions) {
                    if (def.getStundenIndex() == i) {
                        displayList.add(def);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // Füge eine leere Definition hinzu, wenn keine vorhanden ist
                    displayList.add(new StundenzeitDefinition(i, ""));
                }
            }
            // Sortiere die Liste nach StundenIndex
            displayList.sort((d1, d2) -> Integer.compare(d1.getStundenIndex(), d2.getStundenIndex()));


            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.updateData(displayList);
                });
            }
        });
    }

    private void saveStundenzeiten() {
        List<StundenzeitDefinition> definitionsToSave = adapter.getUpdatedStundenzeiten(); // Methode im Adapter, die wir noch erstellen
        databaseExecutor.execute(() -> {
            for (StundenzeitDefinition def : definitionsToSave) {
                // Speichere nur Einträge, die nicht leer sind
                if (def.getUhrzeitString() != null && !def.getUhrzeitString().trim().isEmpty()) {
                    stundenzeitDefinitionDao.insert(def); // insert mit REPLACE Strategie aktualisiert auch
                } else {
                    // Optional: Lösche Definitionen, die der Benutzer leer gelassen hat (falls sie existierten)
                    stundenzeitDefinitionDao.delete(def);
                }
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Stundenzeiten gespeichert!", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onStundenzeitenSaved();
                    }
                    dismiss(); // Dialog schließen
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseExecutor != null && !databaseExecutor.isShutdown()) {
            databaseExecutor.shutdown();
        }
    }
}