package com.example.schulmanager.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter; // NEU
import android.widget.Spinner;      // NEU
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.schulmanager.R;
import com.example.schulmanager.models.StundenzeitDefinition; // NEU
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList; // NEU
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors; // NEU (benötigt Java 8 oder höher, wenn Target SDK < 24)

public class AddStundenplanEntryDialog extends DialogFragment {

    private TextInputEditText etFach, etRaum, etLehrer; // etUhrzeit und etStundenIndex entfernt
    private Spinner spinnerUhrzeit; // NEU: Spinner für Uhrzeit-Auswahl
    private MaterialButton btnCancel, btnAdd;
    private OnStundenplanEntryAddedListener listener;

    // NEU: Liste der StundenzeitDefinitionen, die an den Dialog übergeben werden
    private List<StundenzeitDefinition> availableStundenzeiten;

    // Methode, um den Listener zu setzen
    public void setOnStundenplanEntryAddedListener(OnStundenplanEntryAddedListener listener) {
        this.listener = listener;
    }

    // NEU: Factory-Methode, um den Dialog mit Daten zu instanziieren
    public static AddStundenplanEntryDialog newInstance(List<StundenzeitDefinition> stundenzeiten) {
        AddStundenplanEntryDialog dialog = new AddStundenplanEntryDialog();
        Bundle args = new Bundle();
        // Hier muss man vorsichtig sein: Direktes Übergeben einer List<Parcelable> oder Serializable
        // ist möglich, aber bei großen Listen nicht effizient.
        // Für eine einfache Liste von StundenzeitDefinitionen könnte man sie als Parcelable machen.
        // Da es sich um eine relativ kleine, fixe Anzahl (11) handelt, ist es praktikabel.
        // Füge 'implements Parcelable' zu deiner StundenzeitDefinition-Klasse hinzu
        // und generiere die Parcelable-Implementierung.
        // ODER: Vereinfacht, falls die Liste nicht extrem groß wird und man keine Parcelable möchte,
        // kann man auch nur die Uhrzeit-Strings und Indizes übergeben.
        // Für diesen Beispielcode, nehmen wir an, dass StundenzeitDefinition Parcelable ist.
        // Wenn nicht, musst du einen anderen Weg finden (z.B. nur die Strings/Indizes übergeben).
        args.putParcelableArrayList("stundenzeiten", new ArrayList<>(stundenzeiten));
        dialog.setArguments(args);
        return dialog;
    }

    public AddStundenplanEntryDialog() {
        // Leerer Konstruktor erforderlich
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            availableStundenzeiten = getArguments().getParcelableArrayList("stundenzeiten");
            // Sortiere die Liste nach StundenIndex, falls sie es noch nicht ist
            if (availableStundenzeiten != null) {
                availableStundenzeiten.sort((def1, def2) -> Integer.compare(def1.getStundenIndex(), def2.getStundenIndex()));
            }
        } else {
            availableStundenzeiten = new ArrayList<>(); // Leere Liste, falls keine übergeben
            Toast.makeText(getContext(), "Fehler: Keine Stundenzeiten zum Auswählen verfügbar.", Toast.LENGTH_LONG).show();
            dismiss(); // Dialog schließen, wenn keine Daten vorhanden sind
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_timetable_entry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFach = view.findViewById(R.id.et_fach);
        // etUhrzeit wurde entfernt -> spinnerUhrzeit
        spinnerUhrzeit = view.findViewById(R.id.spinner_uhrzeit); // NEU: Initialisiere den Spinner
        etRaum = view.findViewById(R.id.et_raum);
        etLehrer = view.findViewById(R.id.et_lehrer);
        // etStundenIndex wurde entfernt
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnAdd = view.findViewById(R.id.btn_add);

        // Spinner mit den Stundenzeiten befüllen
        if (availableStundenzeiten != null && !availableStundenzeiten.isEmpty()) {
            // Extrahieren der Uhrzeit-Strings für den Spinner
            List<String> uhrzeitStrings = availableStundenzeiten.stream()
                    .map(StundenzeitDefinition::getUhrzeitString)
                    .collect(Collectors.toList());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, uhrzeitStrings);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerUhrzeit.setAdapter(adapter);
        } else {
            Toast.makeText(getContext(), "Keine Stundenzeiten zum Auswählen vorhanden. Bitte zuerst festlegen.", Toast.LENGTH_LONG).show();
            dismiss();
            return;
        }


        btnCancel.setOnClickListener(v -> dismiss()); // Dialog schließen

        btnAdd.setOnClickListener(v -> {
            String fach = Objects.requireNonNull(etFach.getText()).toString().trim();
            // Uhrzeit wird jetzt vom Spinner geholt, aber wir brauchen den Index
            int selectedSpinnerPosition = spinnerUhrzeit.getSelectedItemPosition();
            if (selectedSpinnerPosition == Spinner.INVALID_POSITION || availableStundenzeiten == null || selectedSpinnerPosition >= availableStundenzeiten.size()) {
                Toast.makeText(getContext(), "Bitte eine Uhrzeit auswählen.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Holen des StundenIndex aus der ausgewählten StundenzeitDefinition
            int stundenIndex = availableStundenzeiten.get(selectedSpinnerPosition).getStundenIndex();

            String raum = Objects.requireNonNull(etRaum.getText()).toString().trim();
            String lehrer = Objects.requireNonNull(etLehrer.getText()).toString().trim();
            // Stundenindex wird jetzt direkt vom Spinner-Objekt geholt, nicht von etStundenIndex

            if (fach.isEmpty() || raum.isEmpty()) { // Uhrzeit und Stundenindex sind jetzt immer ausgewählt
                Toast.makeText(getContext(), "Bitte alle Pflichtfelder ausfüllen (Fach, Raum)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Daten an das aufrufende Fragment zurückgeben
            if (listener != null) {
                // Die Uhrzeit als String wird NICHT mehr vom Dialog zurückgegeben,
                // sondern der StundenIndex. Das Fragment wird die Uhrzeit selbst holen.
                listener.onStundenplanEntryAdded(fach, raum, lehrer, stundenIndex); // Angepasste Signatur
            }
            dismiss(); // Dialog schließen
        });
    }
}