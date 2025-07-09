package com.example.schulmanager.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schulmanager.R;
import com.example.schulmanager.adapters.FachAdapter;
import com.example.schulmanager.adapters.NoteAdapter;
import com.example.schulmanager.models.Fach;
import com.example.schulmanager.models.Note;
import com.example.schulmanager.utils.BerechnungUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Das NotenmanagerFragment ist das Haupt-Fragment für die Verwaltung von Fächern und Noten.
 * Es zeigt eine Liste von Fächern an, ermöglicht das Hinzufügen, Bearbeiten und Löschen von Fächern
 * sowie das Verwalten der Noten innerhalb eines Fachs.
 * Zudem bietet es Funktionen zur Berechnung des Halbjahresschnitts und des Abitur-Gesamtschnitts.
 */
public class NotenmanagerFragment extends Fragment implements NoteAdapter.OnNoteClickListener {

    // --- Konstanten für SharedPreferences ---
    private static final String PREF_NAME = "NotenManager";
    private static final String KEY_FAECHER = "faecher";
    private static final String KEY_PRUEFUNGEN = "pruefungen";
    private static final String PREF_LAST_HALBJAHR_ADD = "lastHalbjahrAdd";

    // --- Adapter-Instanzen ---
    private FachAdapter fachAdapter; // Adapter für die Anzeige der Fächer-Liste.
    private NoteAdapter noteAdapter; // Adapter für die Anzeige der Noten-Liste innerhalb des Noten-Dialogs.

    // --- Datenlisten ---
    private List<Fach> alleFaecher = new ArrayList<>(); // Enthält alle Fächer der Anwendung, unabhängig vom Halbjahr.
    private final List<Fach> gefilterteFaecher = new ArrayList<>(); // Enthält die Fächer, die dem aktuell ausgewählten Halbjahr entsprechen.

    // --- UI-Elemente und Zustandsvariablen ---
    private AlertDialog currentDialog; // Referenz auf den aktuell geöffneten AlertDialog, um ihn bei Bedarf zu schließen.
    private int aktuellesHalbjahr = 1; // Speichert das aktuell im Spinner ausgewählte Halbjahr (Standard: 1. Halbjahr).
    private Spinner halbjahrSpinner; // Spinner zur Auswahl des Halbjahres zum Filtern der Fächer.
    private Fach currentFachForNotes; // Speichert das Fach, dessen Noten gerade im Noten-Dialog verwaltet werden.

    /**
     * Wird aufgerufen, um die View-Hierarchie des Fragments zu erstellen und zurückzugeben.
     * Hier werden die UI-Elemente initialisiert, Listener gesetzt und Daten geladen.
     *
     * @param inflater           Der LayoutInflater-Objekt, das zum Inflating von Layouts verwendet werden kann.
     * @param container          Die übergeordnete ViewGroup, an die die View des Fragments angehängt werden soll.
     * @param savedInstanceState Wenn das Fragment neu erstellt wird, nachdem sein Zustand gespeichert wurde,
     *                           ist dies der Zustand.
     * @return Die Root-View des Fragments.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflatiert das Layout für dieses Fragment (fragment_notenmanager.xml).
        View view = inflater.inflate(R.layout.fragment_grademanager, container, false);

        // --- Spinner für Halbjahresfilter initialisieren ---
        HalbjahrsfilterInitialisieren(view);

        // --- RecyclerView für Fächer initialisieren ---
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        // Setzt einen LinearLayoutManager, um die Elemente vertikal anzuordnen.
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialisiert den FachAdapter mit der gefilterten Fächerliste und dem Klick-Listener.
        // `this::showEditDialog` ist eine Methodenreferenz, die sicherstellt, dass beim Klick auf ein Fach
        // die Methode showEditDialog mit dem geklickten Fach aufgerufen wird.
        fachAdapter = new FachAdapter(gefilterteFaecher, this::showEditDialog);
        // Weist den Adapter dem RecyclerView zu.
        recyclerView.setAdapter(fachAdapter);

        // --- FloatingActionButton (FAB) Funktionalität ---
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        // Setzt einen OnClickListener für den FAB, um den Dialog zum Hinzufügen eines neuen Fachs anzuzeigen.
        fabAdd.setOnClickListener(v -> showAddDialog());

        // --- Buttons Funktionalität ---
        Button btnPruefungen = view.findViewById(R.id.btn_pruefungen);
        // Zeigt den Dialog zur Eingabe/Bearbeitung von Abiturprüfungsnoten an.
        btnPruefungen.setOnClickListener(v -> showPruefungenDialog());

        Button btnBerechnen = view.findViewById(R.id.btn_berechnen);
        // Berechnet und zeigt den Abitur-Gesamtschnitt an.
        btnBerechnen.setOnClickListener(v -> berechneUndZeigeAbi());

        Button btnSchnitt = view.findViewById(R.id.btn_halbjahr_schnitt);
        // Berechnet und zeigt den Durchschnitt des aktuell ausgewählten Halbjahres an.
        btnSchnitt.setOnClickListener(v -> zeigeHalbjahrSchnitt());

        // Lädt die gespeicherten Daten (Fächer und ggf. Prüfungsnoten) beim Start des Fragments.
        loadData();

        return view; // Gibt die erstellte View des Fragments zurück.
    }

    /**
     * Hilfsmethode zur Initialisierung des Halbjahrsfilter-Spinners.
     *
     * @param view Die Root-View des Fragments, in der sich der Spinner befindet.
     */
    private void HalbjahrsfilterInitialisieren(View view) {
        // Findet den Spinner im Layout über seine ID.
        halbjahrSpinner = view.findViewById(R.id.halbjahr_filter);
        // Erstellt einen ArrayAdapter, um die Daten (Halbjahre aus strings.xml) für den Spinner bereitzustellen.
        // Verwendet benutzerdefinierte Layouts für das Spinner-Item und das Dropdown-Item.
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.halbjahre_array,     // Array mit den Halbjahres-Optionen.
                R.layout.spinner_item);      // Layout für das ausgewählte Item im Spinner.
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item); // Layout für die Elemente im Dropdown-Menü.
        // Weist den Adapter dem Spinner zu.
        halbjahrSpinner.setAdapter(spinnerAdapter);
        // Setzt die Standardauswahl auf das erste Element (Halbjahr 1).
        halbjahrSpinner.setSelection(0);
        // Setzt einen Listener, der auf Änderungen der Spinner-Auswahl reagiert.
        halbjahrSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Aktualisiert das aktuell ausgewählte Halbjahr (position ist 0-basiert, Halbjahre sind 1-basiert).
                aktuellesHalbjahr = position + 1;
                // Filtert die Fächerliste basierend auf dem neuen Halbjahr und aktualisiert den RecyclerView.
                filterFaecher();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Diese Methode wird aufgerufen, wenn die Auswahl im Spinner leer ist (selten in diesem Kontext).
            }
        });
    }

    /**
     * Zeigt einen Dialog zum Hinzufügen eines neuen Fachs an.
     * Der Dialog enthält Felder für den Fachnamen, das Halbjahr und ob es ein Abiturfach ist.
     */
    private void showAddDialog() {
        // Erstellt einen AlertDialog.Builder für den Dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        // Inflatiert das Layout für den Dialog (dialog_fach.xml).
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_subject, null);

        // Referenzen auf die UI-Elemente im Dialog-Layout.
        EditText etName = dialogView.findViewById(R.id.dialog_name);
        Spinner spHalbjahr = dialogView.findViewById(R.id.sp_halbjahr);
        CheckBox cbAbitur = dialogView.findViewById(R.id.cb_abitur);
        Button btnEditNotes = dialogView.findViewById(R.id.btn_edit_notes);

        // Der "Noten bearbeiten"-Button ist im Add-Dialog nicht sichtbar, da noch keine Noten existieren.
        btnEditNotes.setVisibility(View.GONE);

        // Konfiguriert den Spinner für die Halbjahresauswahl im Dialog.
        ArrayAdapter<CharSequence> spinnerArrayAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.halbjahre_array,
                R.layout.spinner_item
        );
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spHalbjahr.setAdapter(spinnerArrayAdapter);

        // Lädt die zuletzt ausgewählte Halbjahrsposition aus SharedPreferences und setzt sie im Spinner.
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int lastSelectedHalbjahrPosition = prefs.getInt(PREF_LAST_HALBJAHR_ADD, 0);
        spHalbjahr.setSelection(lastSelectedHalbjahrPosition);

        // Setzt das Dialog-Layout und den Titel.
        builder.setView(dialogView)
                .setTitle("Neues Fach hinzufügen")
                // Da wir eine manuelle Validierung und Schließung steuern wollen,
                // setzen wir den Positive Button Listener zunächst auf null.
                .setPositiveButton("Hinzufügen", null)
                .setNegativeButton("Abbrechen", (dialog, id) -> {
                    // Der Dialog wird beim Klick auf "Abbrechen" einfach geschlossen.
                    dialog.dismiss();
                });

        currentDialog = builder.create(); // Erstellt den AlertDialog.

        // Überschreibt den OnClickListener des Positive Buttons, um eine manuelle Validierung zu ermöglichen.
        // Dies verhindert, dass der Dialog bei ungültiger Eingabe sofort geschlossen wird.
        currentDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = currentDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String name = etName.getText().toString().trim(); // Holt den eingegebenen Fachnamen.
                // Prüft, ob der Fachname leer ist.
                if (name.isEmpty()) {
                    // Zeigt eine Toast-Nachricht an und schließt den Dialog NICHT.
                    Toast.makeText(requireContext(), "Fachname benötigt", Toast.LENGTH_SHORT).show();
                    return; // Beendet die Methode hier, sodass der Dialog offen bleibt.
                }

                int selectedHalbjahrPosition = spHalbjahr.getSelectedItemPosition(); // Holt die ausgewählte Halbjahrsposition.
                // Erstellt ein neues Fach-Objekt mit den eingegebenen Daten.
                // Das Halbjahr ist 1-basiert, daher +1.
                Fach fach = new Fach(
                        name,
                        selectedHalbjahrPosition + 1,
                        cbAbitur.isChecked() // Status der Abitur-Checkbox.
                );

                alleFaecher.add(fach); // Fügt das neue Fach zur globalen Liste hinzu.
                saveData(); // Speichert alle Fächer in SharedPreferences und aktualisiert den Adapter (durch filterFaecher()).
                // Keine separate notifyItemInserted hier, da saveData() -> filterFaecher() -> notifyDataSetChanged() übernimmt.

                // Speichert die zuletzt ausgewählte Halbjahrsposition für den nächsten "Fach hinzufügen"-Dialog.
                prefs.edit().putInt(PREF_LAST_HALBJAHR_ADD, selectedHalbjahrPosition).apply();

                Toast.makeText(requireContext(), "Fach hinzugefügt", Toast.LENGTH_SHORT).show();
                currentDialog.dismiss(); // Schließt den Dialog nur, wenn die Eingabe gültig war.
            });
        });

        currentDialog.show(); // Zeigt den Dialog an.
    }

    /**
     * Zeigt einen Dialog zum Bearbeiten eines bestehenden Fachs an.
     * Der Dialog ermöglicht die Änderung von Fachname, Halbjahr und Abiturfach-Status.
     * Außerdem bietet er Optionen zum Löschen des Fachs oder zum Wechsel in den Noten-Dialog dieses Fachs.
     *
     * @param fach Das Fach-Objekt, das bearbeitet werden soll.
     */
    private void showEditDialog(Fach fach) {
        // Findet die Position des Fachs in der Liste 'alleFaecher'. Wichtig für adapter.notifyItemRemoved/Changed.
        final int position = alleFaecher.indexOf(fach);
        if (position == -1)
            return; // Falls das Fach aus irgendeinem Grund nicht gefunden wird, abbrechen.

        // Erstellt einen AlertDialog.Builder für den Dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        // Inflatiert das Layout für den Dialog (dialog_fach.xml).
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_subject, null);

        // Referenzen auf die UI-Elemente im Dialog-Layout.
        EditText etName = dialogView.findViewById(R.id.dialog_name);
        Spinner spHalbjahr = dialogView.findViewById(R.id.sp_halbjahr);
        CheckBox cbAbitur = dialogView.findViewById(R.id.cb_abitur);
        Button btnEditNotes = dialogView.findViewById(R.id.btn_edit_notes);

        // Im Edit-Dialog ist der "Noten bearbeiten"-Button sichtbar.
        btnEditNotes.setVisibility(View.VISIBLE);
        // Setzt einen Klick-Listener für den "Noten bearbeiten"-Button.
        btnEditNotes.setOnClickListener(v -> {
            currentDialog.dismiss(); // Schließt den aktuellen Fach-Bearbeiten-Dialog.
            showNotesDialog(fach);   // Öffnet den Noten-Dialog für das ausgewählte Fach.
        });

        // Konfiguriert den Spinner für die Halbjahresauswahl im Dialog.
        ArrayAdapter<CharSequence> dialogSpinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.halbjahre_array,
                R.layout.spinner_item
        );
        dialogSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spHalbjahr.setAdapter(dialogSpinnerAdapter);

        // Füllt die UI-Elemente des Dialogs mit den aktuellen Daten des Fachs.
        etName.setText(fach.getName());
        spHalbjahr.setSelection(fach.getHalbjahr() - 1); // Halbjahr ist 1-basiert, Spinner-Position 0-basiert.
        cbAbitur.setChecked(fach.isAbiturfach());

        // Setzt das Dialog-Layout und den Titel.
        builder.setView(dialogView)
                .setTitle("Fach bearbeiten")
                .setPositiveButton("Speichern", null) // Wieder null für manuelle Validierung.
                .setNegativeButton("Löschen", (dialog, id) -> {
                    // Bei Klick auf "Löschen": Entfernt das Fach aus der Liste.
                    alleFaecher.remove(position);
                    saveData(); // Speichert die aktualisierte Liste.
                    // Durch saveData() wird filterFaecher() aufgerufen, das notifyDataSetChanged() macht.
                    // Kein notifyItemRemoved() hier, da es vom vollständigen Refresh abgedeckt wird.
                    Toast.makeText(requireContext(), "Fach gelöscht", Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); // Schließt den Dialog.
                })
                .setNeutralButton("Abbrechen", (dialog, id) -> {
                    // Der Dialog wird beim Klick auf "Abbrechen" einfach geschlossen.
                    dialog.dismiss();
                });

        currentDialog = builder.create(); // Erstellt den AlertDialog.

        // Überschreibt den OnClickListener des Positive Buttons ("Speichern") für Validierung.
        currentDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = currentDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String newName = etName.getText().toString().trim(); // Holt den neuen Fachnamen.
                // Prüft, ob der neue Fachname leer ist.
                if (newName.isEmpty()) {
                    Toast.makeText(requireContext(), "Fachname darf nicht leer sein", Toast.LENGTH_SHORT).show();
                    return; // Dialog bleibt offen.
                }

                // Aktualisiert die Eigenschaften des Fach-Objekts mit den neuen Werten.
                fach.setName(newName);
                fach.setHalbjahr(spHalbjahr.getSelectedItemPosition() + 1);
                fach.setAbiturfach(cbAbitur.isChecked());

                saveData(); // Speichert die Änderungen an allen Fächern.
                // notifyItemChanged ist weiterhin sinnvoll, um sicherzustellen,
                // dass der spezielle Eintrag im RecyclerView sofort aktualisiert wird,
                // auch wenn filterFaecher() später notifyDataSetChanged() aufruft.
                fachAdapter.notifyItemChanged(position);
                Toast.makeText(requireContext(), "Fach gespeichert", Toast.LENGTH_SHORT).show();
                currentDialog.dismiss(); // Schließt den Dialog nur, wenn die Eingabe gültig war.
            });
        });

        currentDialog.show(); // Zeigt den Dialog an.
    }


    /**
     * Zeigt einen Dialog zur Verwaltung der Noten eines spezifischen Fachs an.
     * Innerhalb dieses Dialogs können Noten hinzugefügt werden und eine Liste der vorhandenen Noten
     * des Fachs wird angezeigt, die über Long-Click gelöscht werden können.
     *
     * @param fach Das Fach-Objekt, dessen Noten verwaltet werden sollen.
     */
    private void showNotesDialog(Fach fach) {
        currentFachForNotes = fach; // Speichert das aktuelle Fach, um im Long-Click-Listener darauf zugreifen zu können.

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_grade_add, null);

        // Referenzen auf die UI-Elemente im Dialog-Layout.
        TextView tvTitle = dialogView.findViewById(R.id.dialog_note_title);
        EditText etNoteWert = dialogView.findViewById(R.id.et_note_wert);
        // Referenz auf das EditText für die Gewichtung
        EditText etNoteGewichtung = dialogView.findViewById(R.id.et_note_gewichtung);
        RadioGroup rgNoteTyp = dialogView.findViewById(R.id.rg_note_typ);
        RadioButton rbSchriftlich = dialogView.findViewById(R.id.rb_schriftlich);
        RadioButton rbMuendlich = dialogView.findViewById(R.id.rb_muendlich);

        // Setzt den Titel des Dialogs dynamisch, basierend auf dem Fachnamen.
        tvTitle.setText(String.format(Locale.GERMAN, "Noten für %s verwalten", fach.getName()));

        // Initialisiert den RecyclerView zur Anzeige der Noten des Fachs.
        RecyclerView rvCurrentNotes = dialogView.findViewById(R.id.rv_current_notes);
        rvCurrentNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialisiert den NoteAdapter mit der Notenliste des aktuellen Fachs und dem Fragment als Listener.
        noteAdapter = new NoteAdapter(fach.getNoten(), this);
        rvCurrentNotes.setAdapter(noteAdapter);

        builder.setView(dialogView)
                .setTitle("Noten hinzufügen/bearbeiten")
                .setPositiveButton("Note hinzufügen", null) // Null für manuelle Validierung.
                .setNegativeButton("Fertig", (dialog, id) -> {
                    // Der Dialog wird beim Klick auf "Fertig" einfach geschlossen.
                    dialog.dismiss();
                });
        currentDialog = builder.create(); // Erstellt den AlertDialog.

        // Überschreibt den OnClickListener des Positive Buttons ("Note hinzufügen") für Validierung.
        currentDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = currentDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String wertStr = etNoteWert.getText().toString().trim(); // Holt den eingegebenen Punktwert.
                // Holt den eingegebenen Gewichtungswert
                String gewichtungStr = etNoteGewichtung.getText().toString().trim();

                // Prüft, ob der Punktwert leer ist.
                if (wertStr.isEmpty()) {
                    Toast.makeText(requireContext(), "Punktwert benötigt", Toast.LENGTH_SHORT).show();
                    return; // Dialog bleibt offen.
                }

                try {
                    // Versucht, den Punktwert in ein Double umzuwandeln.
                    double wert = Double.parseDouble(wertStr);
                    // Zusätzliche Validierung des Punktwerts auf den Bereich 0-15.
                    if (wert < 0 || wert > 15) {
                        Toast.makeText(requireContext(), "Gültigen Punktwert zwischen 0 und 15 eingeben", Toast.LENGTH_SHORT).show();
                        return; // Dialog bleibt offen.
                    }

                    // Gewichtung parsen und validieren
                    double gewichtung;
                    if (gewichtungStr.isEmpty()) {
                        gewichtung = 1.0; // Standardwert, falls Feld leer gelassen wird
                    } else {
                        gewichtung = Double.parseDouble(gewichtungStr);
                        if (gewichtung <= 0) { // Gewichtung sollte positiv sein
                            Toast.makeText(requireContext(), "Gewichtung muss größer als 0 sein", Toast.LENGTH_SHORT).show();
                            return; // Dialog bleibt offen.
                        }
                    }

                    String typ;
                    int selectedId = rgNoteTyp.getCheckedRadioButtonId(); // Holt die ID des ausgewählten RadioButtons.
                    // Bestimmt den Notentyp basierend auf der ausgewählten RadioButton-ID.
                    if (selectedId == rbSchriftlich.getId()) {
                        typ = "schriftlich";
                    } else if (selectedId == rbMuendlich.getId()) {
                        typ = "muendlich";
                    } else {
                        typ = "unbekannt"; // Fallback, sollte bei korrekter UI-Logik nicht erreicht werden.
                    }

                    // Erstellt ein neues Note-Objekt mit der Gewichtung.
                    Note neueNote = new Note(wert, typ, gewichtung);
                    fach.addNote(neueNote); // Fügt die neue Note zur Liste des Fachs hinzu.
                    saveData(); // Speichert die aktualisierten Daten.

                    // Benachrichtigt den NoteAdapter über die hinzugefügte Note.
                    noteAdapter.notifyItemInserted(fach.getNoten().size() - 1);
                    rvCurrentNotes.scrollToPosition(fach.getNoten().size() - 1);
                    // Benachrichtigt den FachAdapter, dass sich die Daten dieses Fachs geändert haben (Durchschnitt).
                    fachAdapter.notifyItemChanged(alleFaecher.indexOf(fach));

                    etNoteWert.setText("");
                    etNoteGewichtung.setText("1.0"); // Gewichtungsfeld zurücksetzen auf Standard
                    Toast.makeText(requireContext(), "Note hinzugefügt", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    // Zeigt eine Fehlermeldung an, wenn die Eingabe keine gültige Zahl ist. [cite: 354]
                    Toast.makeText(requireContext(), "Gültige Zahl für Punktwert oder Gewichtung eingeben (z.B. 10 oder 7.5)", Toast.LENGTH_LONG).show();
                    // Dialog bleibt offen.
                }
            });
        });

        currentDialog.show(); // Zeigt den Dialog an.
    }

    /**
     * Implementierung der onNoteClick-Methode aus dem NoteAdapter.OnNoteClickListener Interface.
     * Wird aufgerufen, wenn auf eine Note in der Liste geklickt wird.
     * Aktuell zeigt diese Methode nur einen Toast an.
     *
     * @param note Die geklickte Note.
     */
    @Override
    public void onNoteClick(Note note) {
        // Hier könnte später Logik zum Bearbeiten der Note implementiert werden,
        // z.B. das Öffnen eines Bearbeitungsdialogs für die Note.
        Toast.makeText(requireContext(), "Note geklickt: " + note.toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Implementierung der onNoteLongClick-Methode aus dem NoteAdapter.OnNoteClickListener Interface.
     * Wird aufgerufen, wenn eine Note in der Liste lange gedrückt wird.
     * Zeigt einen Bestätigungsdialog zum Löschen der Note an.
     *
     * @param note     Die Note, die lange gedrückt wurde.
     * @param position Die Position der Note in der Liste.
     */
    @Override
    public void onNoteLongClick(Note note, int position) {
        // Erstellt einen Bestätigungsdialog zum Löschen der Note.
        new AlertDialog.Builder(requireContext())
                .setTitle("Note löschen?")
                .setMessage("Möchtest du diese Note wirklich löschen?")
                .setPositiveButton("Ja", (dialog, which) -> {
                    // Stellt sicher, dass ein Fach zur Bearbeitung der Noten ausgewählt ist.
                    if (currentFachForNotes != null) {
                        // Entfernt die Note aus der Liste des aktuellen Fachs.
                        currentFachForNotes.removeNote(note);
                        saveData(); // Speichert die aktualisierten Daten.

                        // Benachrichtigt den NoteAdapter über die entfernte Note und die Verschiebung der nachfolgenden Elemente.
                        noteAdapter.notifyItemRemoved(position);
                        noteAdapter.notifyItemRangeChanged(position, currentFachForNotes.getNoten().size());
                        // Benachrichtigt den FachAdapter, dass sich die Daten dieses Fachs geändert haben (Durchschnitt).
                        fachAdapter.notifyItemChanged(alleFaecher.indexOf(currentFachForNotes));
                        Toast.makeText(requireContext(), "Note gelöscht", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Nein", null) // Schließt den Dialog ohne Aktion.
                .show(); // Zeigt den Bestätigungsdialog an.
    }

    /**
     * Lädt die gespeicherten Fächer und Prüfungsnoten aus den SharedPreferences.
     * Diese Methode wird beim Erstellen des Fragments aufgerufen, um den letzten Zustand wiederherzustellen.
     */
    private void loadData() {
        // Holt die SharedPreferences-Instanz für die App.
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // --- Fächer laden ---
        String jsonFaecher = prefs.getString(KEY_FAECHER, null); // Holt den JSON-String der Fächer.
        if (jsonFaecher != null) {
            // Definiert den Typ für die Deserialisierung (eine ArrayList von Fach-Objekten).
            Type type = new TypeToken<ArrayList<Fach>>() {
            }.getType();
            // Deserialisiert den JSON-String in eine Liste von Fach-Objekten.
            alleFaecher = new Gson().fromJson(jsonFaecher, type);
            // Iteriert durch alle geladenen Fächer, um sicherzustellen, dass ihre Notenlisten nicht null sind.
            // Dies ist wichtig, wenn Gson ein leeres Array als null interpretiert oder wenn Fächer ohne Noten gespeichert wurden.
            for (Fach fach : alleFaecher) {
                if (fach.getNoten() == null) {
                    fach.setNoten(new ArrayList<>()); // Initialisiert eine leere Liste, falls null.
                }
            }
            // Filtert die geladenen Fächer basierend auf dem aktuell ausgewählten Halbjahr.
            filterFaecher();
        } else {
            // Wenn keine Fächer gespeichert sind, stelle sicher, dass die Liste leer ist.
            alleFaecher = new ArrayList<>();
            filterFaecher(); // Auch hier filterFaecher aufrufen, um leere Liste zu setzen und Adapter zu aktualisieren.
        }

        // --- Prüfungsnoten laden ---
        // (Wird im showPruefungenDialog direkt geladen, hier nur zur Vollständigkeit erwähnt)
        // String jsonPruefungen = prefs.getString(KEY_PRUEFUNGEN, null);
        // int[] pruefungen = (jsonPruefungen != null) ? new Gson().fromJson(jsonPruefungen, int[].class) : new int[5];
        // Hier nicht direkt in eine Instanzvariable laden, da sie nur im Dialog benötigt werden.
    }

    /**
     * Speichert die aktuelle Liste aller Fächer in den SharedPreferences.
     * Diese Methode sollte nach jeder Änderung an der `alleFaecher`-Liste aufgerufen werden,
     * um die Daten persistent zu machen.
     * Ruft anschließend `filterFaecher()` auf, um die UI zu aktualisieren.
     */
    private void saveData() {
        // Holt den SharedPreferences-Editor.
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // Wandelt die Liste aller Fächer in einen JSON-String um und speichert ihn.
        prefs.edit()
                .putString(KEY_FAECHER, new Gson().toJson(alleFaecher))
                .apply(); // apply() speichert asynchron im Hintergrund.

        filterFaecher(); // Aktualisiert die angezeigten Fächer im RecyclerView nach dem Speichern.
        // Ruft notifyDataSetChanged() im Adapter auf.
    }

    /**
     * Wird aufgerufen, wenn die View-Hierarchie des Fragments zerstört wird.
     * Hier wird der aktuell offene Dialog geschlossen, um Memory Leaks zu vermeiden.
     */
    @Override
    public void onDestroyView() {
        // Wenn ein Dialog geöffnet ist, schließe ihn, bevor das Fragment zerstört wird.
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        super.onDestroyView(); // Ruft die Methode der Superklasse auf.
    }

    /**
     * Filtert die Liste aller Fächer basierend auf dem aktuell ausgewählten Halbjahr.
     * Aktualisiert die `gefilterteFaecher`-Liste und benachrichtigt den FachAdapter über die Änderung.
     */
    private void filterFaecher() {
        gefilterteFaecher.clear(); // Löscht alle Elemente aus der aktuell gefilterten Liste.
        // Iteriert durch alle Fächer.
        for (Fach fach : alleFaecher) {
            // Fügt ein Fach zur gefilterten Liste hinzu, wenn sein Halbjahr dem aktuell ausgewählten entspricht.
            if (fach.getHalbjahr() == aktuellesHalbjahr) {
                gefilterteFaecher.add(fach);
            }
        }
        // Benachrichtigt den FachAdapter, dass sich die Daten geändert haben.
        // Die zuvor hinzugefügte `updateFaecher`-Methode im Adapter würde hier auch passen.
        // Wenn `updateFaecher` im Adapter genutzt wird: fachAdapter.updateFaecher(gefilterteFaecher);
        // Ansonsten wie gehabt:
        fachAdapter.notifyDataSetChanged();
    }

    /**
     * Berechnet den Abitur-Gesamtschnitt und zeigt die Ergebnisse in einem AlertDialog an.
     * Nutzt die `BerechnungUtil`-Klasse für die komplexe Logik.
     */
    private void berechneUndZeigeAbi() {
        // Lädt die Abiturprüfungsnoten aus SharedPreferences.
        int[] pruefungsNoten = loadPruefungsNoten();
        // Ruft die Methode in BerechnungUtil auf, um das Abitur zu berechnen.
        BerechnungUtil.AbiErgebnis ergebnis = BerechnungUtil.berechneAbi(alleFaecher, pruefungsNoten);

        // Erstellt die Nachricht für den Dialog, basierend auf den berechneten Ergebnissen.
        // Die Nachricht enthält Punkte für Halbjahresleistungen, Prüfungen, Gesamtpunkte, den Abischnitt
        // und eine spezifische Nachricht zum Bestehensstatus.
        String message =
                getString(R.string.abi_halbjahresleistungen_format, ergebnis.halbjahresPunkte) + "\n" +
                        getString(R.string.abi_pruefungsleistungen_format, ergebnis.pruefungsPunkte) + "\n" +
                        getString(R.string.abi_gesamtpunkte_format, ergebnis.gesamtPunkte) + "\n\n" +
                        getString(R.string.abi_schnitt_format, ergebnis.abiSchnitt) + "\n\n" +
                        ergebnis.bestandenNachricht; // Die Nachricht zum Bestehensstatus kommt direkt aus dem Ergebnis-Objekt.

        // Zeigt die Ergebnisse in einem AlertDialog an.
        new AlertDialog.Builder(requireContext())
                .setTitle("Abiturberechnung")
                .setMessage(message)
                .setPositiveButton("OK", null) // Schließt den Dialog beim Klick auf OK.
                .show();
    }

    /**
     * Zeigt einen Dialog an, in dem die Abiturprüfungsnoten eingegeben oder bearbeitet werden können.
     * Die Noten werden in SharedPreferences gespeichert.
     */
    private void showPruefungenDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_exams, null);

        // Referenzen auf die EditText-Felder für die 5 Prüfungsnoten.
        final EditText[] pruefungFields = {
                dialogView.findViewById(R.id.pruefung1),
                dialogView.findViewById(R.id.pruefung2),
                dialogView.findViewById(R.id.pruefung3),
                dialogView.findViewById(R.id.mdl_pruefung1),
                dialogView.findViewById(R.id.mdl_pruefung2)
        };

        // Lädt die zuvor gespeicherten Prüfungsnoten und füllt die EditText-Felder.
        int[] gespeicherteNoten = loadPruefungsNoten();
        for (int i = 0; i < pruefungFields.length; i++) {
            // Sicherstellen, dass die Indizes gültig sind.
            if (i < gespeicherteNoten.length) {
                // Setzt den Text der EditText-Felder auf die geladenen Noten.
                pruefungFields[i].setText(String.valueOf(gespeicherteNoten[i]));
            }
        }

        builder.setView(dialogView)
                .setTitle("Abiturprüfungsnoten eingeben")
                .setPositiveButton("Speichern", null) // Null für manuelle Validierung.
                .setNegativeButton("Abbrechen", (dialog, which) -> {
                    // Der Dialog wird beim Klick auf "Abbrechen" einfach geschlossen.
                    dialog.dismiss();
                });

        currentDialog = builder.create(); // Erstellt den AlertDialog.

        // Überschreibt den OnClickListener des Positive Buttons ("Speichern") für Validierung.
        currentDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = currentDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                int[] neueNoten = new int[5]; // Array für die neuen Noten.
                boolean inputValid = true; // Flag zur Überprüfung der Validität der Eingaben.

                // Iteriert durch alle Prüfungsfelder, um die Eingaben zu lesen und zu validieren.
                for (int i = 0; i < pruefungFields.length; i++) {
                    String input = pruefungFields[i].getText().toString().trim();
                    try {
                        // Wenn das Feld leer ist, wird 0 angenommen, ansonsten wird der Wert geparst.
                        neueNoten[i] = input.isEmpty() ? 0 : Integer.parseInt(input);
                        // Validiert, ob der Wert im Bereich von 0 bis 15 liegt.
                        if (neueNoten[i] < 0 || neueNoten[i] > 15) {
                            inputValid = false; // Setzt das Flag auf false, wenn der Wert ungültig ist.
                            break; // Beendet die Schleife, da ein Fehler gefunden wurde.
                        }
                    } catch (NumberFormatException e) {
                        inputValid = false; // Setzt das Flag auf false, wenn die Eingabe keine gültige Zahl ist.
                        break; // Beendet die Schleife.
                    }
                }

                // Wenn alle Eingaben gültig sind:
                if (inputValid) {
                    savePruefungsNoten(neueNoten); // Speichert die gültigen Noten.
                    Toast.makeText(requireContext(), "Prüfungsnoten wurden gespeichert", Toast.LENGTH_SHORT).show();
                    currentDialog.dismiss(); // Schließt den Dialog.
                } else {
                    // Wenn Eingaben ungültig sind, zeige einen Toast an und lass den Dialog offen.
                    Toast.makeText(requireContext(), "Bitte nur Werte zwischen 0-15 eingeben!", Toast.LENGTH_LONG).show();
                }
            });
        });

        currentDialog.show(); // Zeigt den Dialog an.
    }

    /**
     * Lädt die gespeicherten Abiturprüfungsnoten aus den SharedPreferences.
     *
     * @return Ein Array von 5 Integer-Werten, die die Prüfungsnoten darstellen.
     * Gibt ein leeres Array zurück, wenn keine Noten gespeichert sind.
     */
    private int[] loadPruefungsNoten() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PRUEFUNGEN, null); // Holt den JSON-String der Prüfungsnoten.
        if (json != null) {
            // Deserialisiert den JSON-String in ein Integer-Array.
            return new Gson().fromJson(json, int[].class);
        }
        return new int[5]; // Gibt ein Array mit 5 Nullen zurück, wenn keine Noten gespeichert sind.
    }

    /**
     * Speichert die übergebenen Abiturprüfungsnoten in den SharedPreferences.
     *
     * @param noten Ein Array von 5 Integer-Werten, die die zu speichernden Prüfungsnoten sind.
     */
    private void savePruefungsNoten(int[] noten) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // Wandelt das Integer-Array in einen JSON-String um und speichert ihn.
        prefs.edit().putString(KEY_PRUEFUNGEN, new Gson().toJson(noten)).apply();
    }

    /**
     * Berechnet den Durchschnitt der Fächer für das aktuell ausgewählte Halbjahr
     * und zeigt das Ergebnis in einem AlertDialog an.
     */
    private void zeigeHalbjahrSchnitt() {
        // Holt das aktuell ausgewählte Halbjahr vom Spinner (1-basiert).
        int halbjahrZuBerechnen = halbjahrSpinner.getSelectedItemPosition() + 1;

        // Ruft die Methode in BerechnungUtil auf, um den Halbjahresschnitt zu berechnen.
        BerechnungUtil.HalbjahrErgebnis ergebnis =
                BerechnungUtil.berechneHalbjahrSchnitt(alleFaecher, halbjahrZuBerechnen);

        // Erstellt die Nachricht für den Dialog.
        // Formatiert den Durchschnitt auf zwei Nachkommastellen.
        String message =
                getString(R.string.halbjahr_schnitt_anzahl_faecher, ergebnis.anzahlFaecher) + "\n" +
                        getString(R.string.halbjahr_schnitt_durchschnitt_punkte, String.format(Locale.GERMAN, "%.2f", ergebnis.durchschnitt)) + "\n" +
                        getString(R.string.halbjahr_schnitt_entspricht_note, String.format(Locale.GERMAN, "%.2f", BerechnungUtil.punkteZuNoteEinzelwert(ergebnis.durchschnitt))); // Umrechnung Punkte in Note.

        // Zeigt die Ergebnisse in einem AlertDialog an.
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.halbjahr_schnitt_title_format, ergebnis.halbjahr))
                .setMessage(message)
                .setPositiveButton("OK", null) // Schließt den Dialog beim Klick auf OK.
                .show();
    }
}