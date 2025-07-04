// fragments/NotenmanagerFragment.java (Angepasst)
package com.example.schulmanager.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
//import android.text.InputType;
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

public class NotenmanagerFragment extends Fragment implements NoteAdapter.OnNoteClickListener { // NEU: NoteAdapter.OnNoteClickListener implementieren

    private FachAdapter adapter;
    private AlertDialog currentDialog;
    private List<Fach> alleFaecher = new ArrayList<>();
    private final List<Fach> gefilterteFaecher = new ArrayList<>();
    private int aktuellesHalbjahr = 1; // Standard: HJ1
    private Spinner halbjahrSpinner;
    // private ArrayAdapter<CharSequence> spinnerAdapter; // Nicht mehr als Klassenfeld nötig, da lokal initialisiert

    // NEU: Für den Noten-Dialog
    private NoteAdapter noteAdapter;
    private Fach currentFachForNotes; // Das Fach, dessen Noten gerade bearbeitet werden

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notenmanager, container, false);

        /// Spinner initialisieren
        halbjahrSpinner = view.findViewById(R.id.halbjahr_filter);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.halbjahre_array,
                android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        halbjahrSpinner.setAdapter(spinnerAdapter);
        halbjahrSpinner.setSelection(0); // Standardmäßig HJ1 ausgewählt
        halbjahrSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aktuellesHalbjahr = position + 1;
                filterFaecher();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FachAdapter(gefilterteFaecher, this::showEditDialog);
        recyclerView.setAdapter(adapter);

        // FAB Funktionalität
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> showAddDialog());

        // Prüfungsnoten-Button
        Button btnPruefungen = view.findViewById(R.id.btn_pruefungen);
        btnPruefungen.setOnClickListener(v -> showPruefungenDialog());
        // Abi-Berechnen-Button
        Button btnBerechnen = view.findViewById(R.id.btn_berechnen);
        btnBerechnen.setOnClickListener(v -> berechneUndZeigeAbi());
        // Halbjahres-Notes berechnen Button
        Button btnSchnitt = view.findViewById(R.id.btn_halbjahr_schnitt);
        btnSchnitt.setOnClickListener(v -> zeigeHalbjahrSchnitt());

        loadData();

        return view;
    }

    private static final String PREF_LAST_HALBJAHR_ADD = "lastHalbjahrAdd";

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_fach, null);

        // Dialogfelder
        EditText etName = dialogView.findViewById(R.id.dialog_name);
        Spinner spHalbjahr = dialogView.findViewById(R.id.sp_halbjahr);

        // Adapter mit eigenem Layout für Dropdown
        ArrayAdapter<CharSequence> spinnerArrayAdapter = ArrayAdapter.createFromResource( // ÄNDERUNG: Name
                requireContext(),
                R.array.halbjahre_array,
                R.layout.spinner_item
        );
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spHalbjahr.setAdapter(spinnerArrayAdapter);

        SharedPreferences prefs = requireContext().getSharedPreferences("NotenManager", Context.MODE_PRIVATE);
        int lastSelectedHalbjahrPosition = prefs.getInt(PREF_LAST_HALBJAHR_ADD, 0);
        spHalbjahr.setSelection(lastSelectedHalbjahrPosition);

        CheckBox cbAbitur = dialogView.findViewById(R.id.cb_abitur);
        // ACHTUNG: Die Felder für schriftlich/mündlich wurden aus dialog_fach.xml entfernt
        // EditText etSchriftlich = dialogView.findViewById(R.id.dialog_schriftlich);
        // EditText etMuendlich = dialogView.findViewById(R.id.dialog_muendlich);

        // NEU: Button für Notenverwaltung im Fach-Dialog
        Button btnEditNotes = dialogView.findViewById(R.id.btn_edit_notes);
        btnEditNotes.setVisibility(View.GONE); // Zuerst unsichtbar, da noch kein Fach existiert

        builder.setView(dialogView)
                .setTitle("Neues Fach hinzufügen")
                .setPositiveButton("Hinzufügen", (dialog, id) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Fachname benötigt", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int selectedHalbjahrPosition = spHalbjahr.getSelectedItemPosition();
                    Fach fach = new Fach(
                            name,
                            selectedHalbjahrPosition + 1,
                            cbAbitur.isChecked()
                    );

                    // KEINE direkt Noten hier mehr eingeben

                    alleFaecher.add(fach);
                    saveAndUpdate(fach);

                    prefs.edit().putInt(PREF_LAST_HALBJAHR_ADD, selectedHalbjahrPosition).apply();

                    // Optional: Nach dem Hinzufügen direkt den Noten-Dialog öffnen
                    // showNotesDialog(fach); // Das ist Geschmackssache, kann man machen
                })
                .setNegativeButton("Abbrechen", null);

        currentDialog = builder.create();
        currentDialog.show();
    }

    // ACHTUNG: ensureValidPoints wird jetzt nur für einzelne Notenwerte verwendet
    private double ensureValidPoints(String input) {
        if (input == null || input.isEmpty()) return 0.0;
        double points = Double.parseDouble(input);
        return Math.max(0.0, Math.min(15.0, points));
    }


    private void showEditDialog(Fach fach) {
        final int position = alleFaecher.indexOf(fach);
        if (position == -1) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_fach, null);

        EditText etName = dialogView.findViewById(R.id.dialog_name);
        Spinner spHalbjahr = dialogView.findViewById(R.id.sp_halbjahr);
        CheckBox cbAbitur = dialogView.findViewById(R.id.cb_abitur);
        // ACHTUNG: Schriftlich/mündlich Felder existieren hier nicht mehr
        // EditText etSchriftlich = dialogView.findViewById(R.id.dialog_schriftlich);
        // EditText etMuendlich = dialogView.findViewById(R.id.dialog_muendlich);

        // NEU: Button für Notenverwaltung
        Button btnEditNotes = dialogView.findViewById(R.id.btn_edit_notes);
        btnEditNotes.setVisibility(View.VISIBLE); // Sichtbar machen
        btnEditNotes.setOnClickListener(v -> {
            currentDialog.dismiss(); // Aktuellen Fach-Dialog schließen
            showNotesDialog(fach); // Noten-Dialog öffnen
        });

        ArrayAdapter<CharSequence> dialogSpinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.halbjahre_array,
                R.layout.spinner_item
        );
        dialogSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spHalbjahr.setAdapter(dialogSpinnerAdapter);

        // Vorbelegung
        etName.setText(fach.getName());
        spHalbjahr.setSelection(fach.getHalbjahr() - 1);
        cbAbitur.setChecked(fach.isAbiturfach());
        // Die Notenfelder werden hier nicht mehr vorbelegt

        builder.setView(dialogView)
                .setTitle("Fach bearbeiten")
                .setPositiveButton("Speichern", (dialog, id) -> {
                    String newName = etName.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(requireContext(), "Fachname darf nicht leer sein", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    fach.setName(newName);
                    fach.setHalbjahr(spHalbjahr.getSelectedItemPosition() + 1);
                    fach.setAbiturfach(cbAbitur.isChecked());
                    // Noten werden nicht mehr direkt hier gesetzt

                    saveData(); // Speichert alle Fächer, inkl. der Notenlisten
                    adapter.notifyItemChanged(position); // Aktualisiert das UI des Fachs
                })
                .setNegativeButton("Löschen", (dialog, id) -> {
                    alleFaecher.remove(position);
                    saveData();
                    adapter.notifyItemRemoved(position);
                })
                .setNeutralButton("Abbrechen", null);

        currentDialog = builder.create();
        currentDialog.show();
    }

    // NEU: Methode zum Anzeigen des Notenverwaltungs-Dialogs
    private void showNotesDialog(Fach fach) {
        currentFachForNotes = fach; // Das Fach setzen, dessen Noten bearbeitet werden

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_note_add, null);

        TextView tvTitle = dialogView.findViewById(R.id.dialog_note_title);
        tvTitle.setText(String.format(Locale.GERMAN, "Noten für %s verwalten", fach.getName()));

        EditText etNoteWert = dialogView.findViewById(R.id.et_note_wert);
        RadioGroup rgNoteTyp = dialogView.findViewById(R.id.rg_note_typ);
        RadioButton rbSchriftlich = dialogView.findViewById(R.id.rb_schriftlich);
        RadioButton rbMuendlich = dialogView.findViewById(R.id.rb_muendlich);
        RadioButton rbSonstig = dialogView.findViewById(R.id.rb_sonstig);

        RecyclerView rvCurrentNotes = dialogView.findViewById(R.id.rv_current_notes);
        rvCurrentNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        // Hier den NoteAdapter initialisieren und setzen
        noteAdapter = new NoteAdapter(fach.getNoten(), this); // 'this' weil NotenmanagerFragment OnNoteClickListener implementiert
        rvCurrentNotes.setAdapter(noteAdapter);

        builder.setView(dialogView)
                .setTitle("Noten hinzufügen/bearbeiten") // Generischer Titel
                .setPositiveButton("Note hinzufügen", (dialog, id) -> {
                    String wertStr = etNoteWert.getText().toString().trim();
                    if (wertStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Punktwert benötigt", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double wert = ensureValidPoints(wertStr);
                        String typ;
                        int selectedId = rgNoteTyp.getCheckedRadioButtonId();
                        if (selectedId == rbSchriftlich.getId()) {
                            typ = "schriftlich";
                        } else if (selectedId == rbMuendlich.getId()) {
                            typ = "muendlich";
                        } else if (selectedId == rbSonstig.getId()) {
                            typ = "sonstig";
                        } else {
                            typ = "unbekannt"; // Fallback
                        }

                        Note neueNote = new Note(wert, typ);
                        fach.addNote(neueNote);
                        saveData(); // Speichern der aktualisierten Fach-Liste

                        noteAdapter.notifyItemInserted(fach.getNoten().size() - 1); // Fügt das zuletzt hinzugefügte Element hinzu
                        rvCurrentNotes.scrollToPosition(fach.getNoten().size() - 1); // Optional: Zum neuen Eintrag scrollen
                        adapter.notifyItemChanged(alleFaecher.indexOf(fach)); // Aktualisiert das Fach im Haupt-RecyclerView
                        etNoteWert.setText(""); // Feld leeren für nächste Eingabe
                        Toast.makeText(requireContext(), "Note hinzugefügt", Toast.LENGTH_SHORT).show();

                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Gültige Zahl (0-15) eingeben", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Fertig", (dialog, id) -> {
                    // Der Dialog wird geschlossen, wenn auf "Fertig" geklickt wird
                    // Keine Aktion notwendig, da Änderungen bereits gespeichert wurden
                });

        currentDialog = builder.create();
        currentDialog.show();
    }

    // NEU: Implementierung des OnNoteClickListener Interface
    @Override
    public void onNoteClick(Note note) {
        // Optional: Hier könnte ein weiterer Dialog zum Bearbeiten einer einzelnen Note geöffnet werden
        Toast.makeText(requireContext(), "Note geklickt: " + note.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNoteLongClick(Note note, int position) {
        // Logik zum Löschen einer Note bei Long-Click
        new AlertDialog.Builder(requireContext())
                .setTitle("Note löschen?")
                .setMessage("Möchtest du diese Note wirklich löschen?")
                .setPositiveButton("Ja", (dialog, which) -> {
                    if (currentFachForNotes != null) {
                        currentFachForNotes.removeNote(note);
                        saveData(); // Speichern der aktualisierten Fach-Liste
                        noteAdapter.notifyItemRemoved(position); // Aktualisiert die Notenliste im Dialog
                        noteAdapter.notifyItemRangeChanged(position, currentFachForNotes.getNoten().size());
                        adapter.notifyItemChanged(alleFaecher.indexOf(currentFachForNotes)); // Aktualisiert das Fach im Haupt-RecyclerView
                        Toast.makeText(requireContext(), "Note gelöscht", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Nein", null)
                .show();
    }


    private void loadData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("NotenManager", Context.MODE_PRIVATE);
        String json = prefs.getString("faecher", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Fach>>(){}.getType();
            alleFaecher = new Gson().fromJson(json, type);
            // Gson kann leere Listen als null deserialisieren, daher sicherstellen
            for (Fach fach : alleFaecher) {
                if (fach.getNoten() == null) {
                    fach.getNoten(); // Ruft den Getter auf, der eine neue Liste initialisiert
                }
            }
            filterFaecher();
        }
    }
    private void saveData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("NotenManager", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("faecher", new Gson().toJson(alleFaecher))
                .apply();

        filterFaecher();
    }

    @Override
    public void onDestroyView() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        super.onDestroyView();
    }

    private void saveAndUpdate(Fach fach) {
        saveData();
        adapter.notifyItemInserted(alleFaecher.indexOf(fach));
    }
    // deleteAndUpdate bleibt gleich

    private void filterFaecher() {
        gefilterteFaecher.clear();
        for (Fach fach : alleFaecher) {
            if (fach.getHalbjahr() == aktuellesHalbjahr) {
                gefilterteFaecher.add(fach);
            }
        }
        adapter.notifyDataSetChanged(); //passt trotz Fehler hier perfekt
    }

    // berechneUndZeigeAbi bleibt gleich (nutzt getDurchschnittsPunkte() des Fachs)
    private void berechneUndZeigeAbi() {
        int[] pruefungsNoten = loadPruefungsNoten();
        BerechnungUtil.AbiErgebnis ergebnis = BerechnungUtil.berechneAbi(alleFaecher, pruefungsNoten);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Abiturberechnung")
                .setMessage(
                        "Halbjahresleistungen: " + ergebnis.halbjahresPunkte + "/600\n" +
                                "Prüfungsleistungen: " + ergebnis.pruefungsPunkte + "/300\n" +
                                "Gesamtpunkte: " + ergebnis.gesamtPunkte + "/900\n\n" +
                                "Abiturschnitt: " + ergebnis.abiSchnitt)
                .setPositiveButton("OK", null)
                .show();
    }

    // showPruefungenDialog, loadPruefungsNoten, savePruefungsNoten bleiben gleich

    private void showPruefungenDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pruefungen, null);
        EditText[] pruefungFields = {
                dialogView.findViewById(R.id.pruefung1),
                dialogView.findViewById(R.id.pruefung2),
                dialogView.findViewById(R.id.pruefung3),
                dialogView.findViewById(R.id.mdl_pruefung1),
                dialogView.findViewById(R.id.mdl_pruefung2)
        };
        // Vorbelegung mit gespeicherten Werten
        int[] gespeicherteNoten = loadPruefungsNoten();
        for (int i = 0; i < pruefungFields.length; i++) {
            if (i < gespeicherteNoten.length) {
                pruefungFields[i].setText(String.valueOf(gespeicherteNoten[i]));
            }
        }

        builder.setView(dialogView)
                .setTitle("Abiturprüfungsnoten eingeben")
                .setPositiveButton("Speichern", (dialog, which) -> {
                    int[] neueNoten = new int[5];
                    try {
                        for (int i = 0; i < pruefungFields.length; i++) {
                            String input = pruefungFields[i].getText().toString();
                            neueNoten[i] = input.isEmpty() ? 0 : Integer.parseInt(input);
                            if (neueNoten[i] < 0 || neueNoten[i] > 15) {
                                throw new NumberFormatException();
                            }
                        }
                        savePruefungsNoten(neueNoten);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(),
                                "Bitte nur Werte zwischen 0-15 eingeben!", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private int[] loadPruefungsNoten() {
        SharedPreferences prefs = requireContext().getSharedPreferences("NotenManager", Context.MODE_PRIVATE);
        String json = prefs.getString("pruefungen", null);
        if (json != null) {
            return new Gson().fromJson(json, int[].class);
        }
        return new int[5]; // Standard: alle 0
    }
    private void savePruefungsNoten(int[] noten) {
        SharedPreferences prefs = requireContext().getSharedPreferences("NotenManager", Context.MODE_PRIVATE);
        prefs.edit().putString("pruefungen", new Gson().toJson(noten)).apply();
        Toast.makeText(requireContext(), "Prüfungsnoten wurden gespeichert", Toast.LENGTH_SHORT).show();
    }


    // zeigeHalbjahrSchnitt bleibt gleich (nutzt getDurchschnittsPunkte() des Fachs)
    private void zeigeHalbjahrSchnitt() {
        int aktuellesHalbjahr = halbjahrSpinner.getSelectedItemPosition() + 1;

        BerechnungUtil.HalbjahrErgebnis ergebnis =
                BerechnungUtil.berechneHalbjahrSchnitt(alleFaecher, aktuellesHalbjahr);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Durchschnitt HJ " + aktuellesHalbjahr)
                .setMessage(
                        "Anzahl Fächer: " + ergebnis.anzahlFaecher + "\n" +
                                "Durchschnittspunktzahl: " + String.format(Locale.GERMAN, "%.2f", ergebnis.durchschnitt) + "\n" +
                                "Entspricht Note: " + punkteZuNote(ergebnis.durchschnitt))
                .setPositiveButton("OK", null)
                .show();
    }

    private String punkteZuNote(double punkte) {
        // Umrechnung Punkte (0-15) → Note (1-6)
        double note = 17.0 / 3.0 - punkte * 2.0 / 9.0;
        return String.format(Locale.GERMAN, "%.2f", Math.max(1.0, Math.min(6.0, note)));
    }
}