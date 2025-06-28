package com.example.schulmanager.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schulmanager.R;
import com.example.schulmanager.adapters.FachAdapter;
import com.example.schulmanager.models.Fach;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotenmanagerFragment extends Fragment {

    private List<Fach> faecher = new ArrayList<>();
    private FachAdapter adapter;
    private AlertDialog currentDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notenmanager, container, false);

        // RecyclerView Setup
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FachAdapter(faecher, this::showEditDialog);
        recyclerView.setAdapter(adapter);

        // FAB Funktionalität
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> showAddDialog());

        loadData();

        return view;
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_fach, null);

        // Dialogfelder
        EditText etName = dialogView.findViewById(R.id.dialog_name);
        Spinner spHalbjahr = dialogView.findViewById(R.id.sp_halbjahr);

        // Adapter mit eigenem Layout für Dropdown
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.halbjahre_array,
                R.layout.spinner_item // Eigenes Layout für ausgewählten Eintrag
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item); // Eigenes Layout für Dropdown

        spHalbjahr.setAdapter(adapter);
        CheckBox cbAbitur = dialogView.findViewById(R.id.cb_abitur);
        EditText etSchriftlich = dialogView.findViewById(R.id.dialog_schriftlich);
        EditText etMuendlich = dialogView.findViewById(R.id.dialog_muendlich);

        // Eingabe auf Zahlen 0-15 beschränken
        etSchriftlich.setInputType(InputType.TYPE_CLASS_NUMBER);
        etMuendlich.setInputType(InputType.TYPE_CLASS_NUMBER);

        builder.setView(dialogView)
                .setTitle("Neues Fach hinzufügen")
                .setPositiveButton("Hinzufügen", (dialog, id) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Fachname benötigt", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Fach fach = new Fach(
                            name,
                            spHalbjahr.getSelectedItemPosition() + 1,
                            cbAbitur.isChecked()
                    );

                    try {
                        fach.setSchriftlich(ensureValidPoints(etSchriftlich.getText().toString()));
                        fach.setMuendlich(ensureValidPoints(etMuendlich.getText().toString()));
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Nur Zahlen 0-15 eingeben", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    faecher.add(fach);
                    saveAndUpdate(fach);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Abbrechen", null);

        currentDialog = builder.create();
        currentDialog.show();
    }

//    private int parsePoints(String input) {
//        if (input.isEmpty()) return 0;
//        int points = Integer.parseInt(input);
//        return Math.max(0, Math.min(15, points));
//    }



    private double ensureValidPoints(String input) {
        if (input == null || input.isEmpty()) return 0.0;
        double points = Double.parseDouble(input);
        return Math.max(0.0, Math.min(15.0, points)); // Noten zwischen 1.0 und 6.0
    }

    private void showEditDialog(Fach fach) {
        final int position = faecher.indexOf(fach);
        if (position == -1) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_fach, null);

        EditText etName = dialogView.findViewById(R.id.dialog_name);
        Spinner spHalbjahr = dialogView.findViewById(R.id.sp_halbjahr);
        CheckBox cbAbitur = dialogView.findViewById(R.id.cb_abitur);
        EditText etSchriftlich = dialogView.findViewById(R.id.dialog_schriftlich);
        EditText etMuendlich = dialogView.findViewById(R.id.dialog_muendlich);

        // Vorbelegung
        etName.setText(fach.getName());
        spHalbjahr.setSelection(fach.getHalbjahr() - 1);
        cbAbitur.setChecked(fach.isAbiturfach());
        etSchriftlich.setText(String.valueOf(fach.getSchriftlich()));
        etMuendlich.setText(String.valueOf(fach.getMuendlich()));

        builder.setView(dialogView)
                .setTitle("Fach bearbeiten")
                .setPositiveButton("Speichern", (dialog, id) -> {
                    try {
                        String newName = etName.getText().toString().trim();
                        if (newName.isEmpty()) {
                            Toast.makeText(requireContext(), "Fachname darf nicht leer sein", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        fach.setName(newName);
                        fach.setHalbjahr(spHalbjahr.getSelectedItemPosition() + 1);
                        fach.setAbiturfach(cbAbitur.isChecked());
                        fach.setSchriftlich(ensureValidPoints(etSchriftlich.getText().toString()));
                        fach.setMuendlich(ensureValidPoints(etMuendlich.getText().toString()));

                        saveData();
                        adapter.notifyItemChanged(position);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Nur Zahlen zwischen 0-15 eingeben", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Löschen", (dialog, id) -> {
                    faecher.remove(position);
                    saveData();
                    adapter.notifyItemRemoved(position);
                })
                .setNeutralButton("Abbrechen", null);

        currentDialog = builder.create();
        currentDialog.show();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void loadData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("NotenManager", Context.MODE_PRIVATE);
        String jsonFaecher = prefs.getString("faecher", null);

        if (jsonFaecher != null) {
            Type type = new TypeToken<ArrayList<Fach>>(){}.getType();
            List<Fach> savedFaecher = new Gson().fromJson(jsonFaecher, type);
            if (savedFaecher != null) {
                faecher.clear();
                faecher.addAll(savedFaecher);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void saveData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("NotenManager", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("faecher", new Gson().toJson(faecher));
        editor.apply();
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
        adapter.notifyItemInserted(faecher.indexOf(fach));
        // Oder alternativ:
        // adapter.notifyDataSetChanged();
    }
    private void deleteAndUpdate(int position) {
        faecher.remove(position);
        saveData();
        adapter.notifyItemRemoved(position);
    }
}
