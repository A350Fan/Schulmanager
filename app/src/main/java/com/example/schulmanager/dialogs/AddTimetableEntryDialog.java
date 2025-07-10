package com.example.schulmanager.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.schulmanager.R;
import com.example.schulmanager.models.StundenzeitDefinition;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Ein DialogFragment, das dem Benutzer ermöglicht, einen neuen Stundenplaneintrag hinzuzufügen.
 * Der Dialog bietet Eingabefelder für Fach, Raum, Lehrer und eine Auswahl für die Uhrzeit/Stunde.
 * Die verfügbaren Stundenzeiten werden von einem aufrufenden Fragment übergeben.
 */
public class AddTimetableEntryDialog extends DialogFragment {

    // UI-Elemente
    private TextInputEditText etFach, etRaum, etLehrer; // Eingabefelder für Fach, Raum und Lehrer
    private Spinner spinnerUhrzeit; // Spinner zur Auswahl der Stundenzeit

    // Listener, um die eingegebenen Daten an das aufrufende Fragment/die Activity zurückzugeben
    private OnTimetableEntryAddedListener listener;

    // Liste der verfügbaren StundenzeitDefinitionen, die dem Spinner als Optionen dienen
    private List<StundenzeitDefinition> availableStundenzeiten;

    /**
     * Setzt den Listener, der benachrichtigt wird, wenn ein Stundenplaneintrag hinzugefügt wurde.
     * @param listener Die Implementierung des OnTimetableEntryAddedListener.
     */
    public void setOnStundenplanEntryAddedListener(OnTimetableEntryAddedListener listener) {
        this.listener = listener;
    }

    /**
     * Factory-Methode, um eine neue Instanz des AddTimetableEntryDialogs zu erstellen
     * und eine Liste von StundenzeitDefinitionen als Argumente zu übergeben.
     * Dies ist die empfohlene Methode, um Argumente an ein Fragment zu übergeben.
     *
     * @param stundenzeiten Die Liste der verfügbaren StundenzeitDefinitionen, die im Spinner angezeigt werden sollen.
     * @return Eine neue Instanz von AddTimetableEntryDialog.
     */
    public static AddTimetableEntryDialog newInstance(List<StundenzeitDefinition> stundenzeiten) {
        AddTimetableEntryDialog dialog = new AddTimetableEntryDialog();
        Bundle args = new Bundle();
        // Übergabe der Liste als ParcelableArrayList.
        // Die Klasse StundenzeitDefinition MUSS das Parcelable-Interface implementieren,
        // damit dies funktioniert. (Siehe StundenzeitDefinition.java)
        args.putParcelableArrayList("stundenzeiten", new ArrayList<>(stundenzeiten));
        dialog.setArguments(args);
        return dialog;
    }

    /**
     * Leerer Konstruktor erforderlich für DialogFragment.
     */
    public AddTimetableEntryDialog() {
        // Leerer Konstruktor erforderlich
    }

    /**
     * Wird vor onCreateView() aufgerufen. Hier werden die Argumente (Stundenzeiten) abgerufen.
     * @param savedInstanceState Der zuvor gespeicherte Zustand des Fragments.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Prüfe, ob Argumente übergeben wurden
        if (getArguments() != null) {
            // Hole die Liste der StundenzeitDefinitionen aus den Argumenten
            availableStundenzeiten = getArguments().getParcelableArrayList("stundenzeiten");
            // Sortiere die Liste nach StundenIndex, falls sie noch nicht sortiert ist.
            // Dies stellt sicher, dass die Stunden im Spinner in der richtigen Reihenfolge erscheinen.
            if (availableStundenzeiten != null) {
                availableStundenzeiten.sort((def1, def2) -> Integer.compare(def1.getStundenIndex(), def2.getStundenIndex()));
            }
        } else {
            // Wenn keine Stundenzeiten übergeben wurden, initialisiere eine leere Liste und zeige einen Toast.
            availableStundenzeiten = new ArrayList<>();
            Toast.makeText(getContext(), "Fehler: Keine Stundenzeiten zum Auswählen verfügbar.", Toast.LENGTH_LONG).show();
            dismiss(); // Schließe den Dialog, da er ohne Stundenzeiten nicht sinnvoll ist.
        }
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
        return inflater.inflate(R.layout.dialog_add_timetable_entry, container, false);
    }

    /**
     * Wird direkt nach onCreateView() aufgerufen, um die View-Hierarchie zu initialisieren und Listener zu setzen.
     * @param view Die von onCreateView() zurückgegebene View.
     * @param savedInstanceState Der zuvor gespeicherte Zustand des Fragments.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Referenzen zu den UI-Elementen erhalten
        etFach = view.findViewById(R.id.et_fach);
        spinnerUhrzeit = view.findViewById(R.id.spinner_uhrzeit); // Initialisiere den Spinner
        etRaum = view.findViewById(R.id.et_raum);
        etLehrer = view.findViewById(R.id.et_lehrer);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);
        // Buttons zum Abbrechen und Hinzufügen
        MaterialButton btnAdd = view.findViewById(R.id.btn_add);

        // Den Spinner mit den verfügbaren Stundenzeiten befüllen
        if (availableStundenzeiten != null && !availableStundenzeiten.isEmpty()) {
            // Extrahiere nur die Uhrzeit-Strings aus den StundenzeitDefinition-Objekten für den Spinner-Adapter.
            List<String> uhrzeitStrings = availableStundenzeiten.stream()
                    .map(StundenzeitDefinition::getUhrzeitString)
                    .collect(Collectors.toList());

            // Erstelle einen ArrayAdapter, um die Strings im Spinner anzuzeigen
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, uhrzeitStrings);
            // Setze das Layout für die Dropdown-Ansicht des Spinners
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerUhrzeit.setAdapter(adapter);
        } else {
            // Wenn keine Stundenzeiten verfügbar sind, zeige eine Fehlermeldung und schließe den Dialog.
            Toast.makeText(getContext(), "Keine Stundenzeiten zum Auswählen vorhanden. Bitte zuerst festlegen.", Toast.LENGTH_LONG).show();
            dismiss();
            return; // Beende die Methode hier, da der Spinner nicht befüllt werden kann.
        }

        // Klick-Listener für den Abbrechen-Button: Schließt den Dialog
        btnCancel.setOnClickListener(v -> dismiss());

        // Klick-Listener für den Hinzufügen-Button
        btnAdd.setOnClickListener(v -> {
            String fach = Objects.requireNonNull(etFach.getText()).toString().trim(); // Fach-Text holen
            String raum = Objects.requireNonNull(etRaum.getText()).toString().trim(); // Raum-Text holen
            String lehrer = Objects.requireNonNull(etLehrer.getText()).toString().trim(); // Lehrer-Text holen

            // Hole die ausgewählte Position im Spinner
            int selectedSpinnerPosition = spinnerUhrzeit.getSelectedItemPosition();

            // Gültigkeitsprüfung für die Spinner-Auswahl und die Liste der Stundenzeiten
            if (selectedSpinnerPosition == Spinner.INVALID_POSITION || availableStundenzeiten == null || selectedSpinnerPosition >= availableStundenzeiten.size()) {
                Toast.makeText(getContext(), "Bitte eine Uhrzeit auswählen.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hole den StundenIndex aus dem ausgewählten StundenzeitDefinition-Objekt.
            // Die eigentliche Uhrzeit wird nicht direkt hier gebraucht, da das aufrufende Fragment
            // diese anhand des Indexes aus der Datenbank abrufen kann.
            int stundenIndex = availableStundenzeiten.get(selectedSpinnerPosition).getStundenIndex();

            // Prüfe, ob die Pflichtfelder (Fach, Raum) ausgefüllt sind
            if (fach.isEmpty() || raum.isEmpty()) {
                Toast.makeText(getContext(), "Bitte alle Pflichtfelder ausfüllen (Fach, Raum)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Daten an das aufrufende Fragment über den Listener zurückgeben
            if (listener != null) {
                // Übergib die relevanten Daten, einschließlich des StundenIndex.
                // Die Uhrzeit als String wird vom Listener NICHT mehr direkt übergeben,
                // da sie anhand des StundenIndex im aufrufenden Fragment (oder dessen ViewModel)
                // aus den StundenzeitDefinitionen abgerufen werden sollte.
                listener.onStundenplanEntryAdded(fach, raum, lehrer, stundenIndex);
            }
            dismiss(); // Schließe den Dialog nach dem Hinzufügen
        });
    }
}