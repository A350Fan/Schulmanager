package com.example.schulmanager.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.schulmanager.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddStundenplanEntryDialog extends DialogFragment {

    private TextInputEditText etFach, etUhrzeit, etRaum, etLehrer, etStundenIndex;
    private MaterialButton btnCancel, btnAdd;
    private OnStundenplanEntryAddedListener listener;

    // Methode, um den Listener zu setzen
    public void setOnStundenplanEntryAddedListener(OnStundenplanEntryAddedListener listener) {
        this.listener = listener;
    }

    public AddStundenplanEntryDialog() {
        // Leerer Konstruktor erforderlich
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_stundenplan_entry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFach = view.findViewById(R.id.et_fach);
        etUhrzeit = view.findViewById(R.id.et_uhrzeit);
        etRaum = view.findViewById(R.id.et_raum);
        etLehrer = view.findViewById(R.id.et_lehrer);
        etStundenIndex = view.findViewById(R.id.et_stunden_index);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnAdd = view.findViewById(R.id.btn_add);

        // Optional: Vorbelegung, falls der Dialog zum Bearbeiten verwendet wird
        // Bundle args = getArguments();
        // if (args != null) { /* Felder vorbelegen */ }

        btnCancel.setOnClickListener(v -> dismiss()); // Dialog schließen

        btnAdd.setOnClickListener(v -> {
            String fach = etFach.getText().toString().trim();
            String uhrzeit = etUhrzeit.getText().toString().trim();
            String raum = etRaum.getText().toString().trim();
            String lehrer = etLehrer.getText().toString().trim();
            String stundenIndexStr = etStundenIndex.getText().toString().trim();

            if (fach.isEmpty() || uhrzeit.isEmpty() || raum.isEmpty() || stundenIndexStr.isEmpty()) {
                Toast.makeText(getContext(), "Bitte alle Pflichtfelder ausfüllen (Fach, Uhrzeit, Raum, Stundenindex)", Toast.LENGTH_SHORT).show();
                return;
            }

            int stundenIndex;
            try {
                stundenIndex = Integer.parseInt(stundenIndexStr);
                if (stundenIndex < 0 || stundenIndex > 10) { // Annahme: Stundenindex 0-10
                    Toast.makeText(getContext(), "Stundenindex muss zwischen 0 und 10 liegen.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Ungültiger Stundenindex. Bitte eine Zahl eingeben.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Daten an das aufrufende Fragment zurückgeben
            if (listener != null) {
                listener.onStundenplanEntryAdded(fach, uhrzeit, raum, lehrer, stundenIndex);
            }
            dismiss(); // Dialog schließen
        });
    }
}