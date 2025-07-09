package com.example.schulmanager.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schulmanager.R;
import com.example.schulmanager.models.StundenzeitDefinition;

import java.util.List;

public class StundenzeitDefinitionAdapter extends RecyclerView.Adapter<StundenzeitDefinitionAdapter.StundenzeitViewHolder> {

    private List<StundenzeitDefinition> stundenzeitList;

    public StundenzeitDefinitionAdapter(List<StundenzeitDefinition> stundenzeitList) {
        this.stundenzeitList = stundenzeitList;
    }

    @NonNull
    @Override
    public StundenzeitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stundenzeit_definition, parent, false);
        return new StundenzeitViewHolder(view, new CustomTextWatcher()); // TextWatcher übergeben
    }

    @Override
    public void onBindViewHolder(@NonNull StundenzeitViewHolder holder, int position) {
        StundenzeitDefinition currentDef = stundenzeitList.get(position);

        // Sicherstellen, dass der TextWatcher die korrekte Position kennt
        holder.textWatcher.updatePosition(position);

        // Stundenindex anzeigen (z.B. "1. Stunde:")
        holder.tvStundenIndex.setText((currentDef.getStundenIndex() + 1) + ". Stunde:"); // +1, da Index 0-basiert ist

        // Uhrzeit-String setzen
        // WICHTIG: Den TextWatcher temporär entfernen, um eine Endlosschleife zu vermeiden,
        // da setText() den TextWatcher auslösen würde.
        holder.etUhrzeitString.removeTextChangedListener(holder.textWatcher);
        holder.etUhrzeitString.setText(currentDef.getUhrzeitString());
        holder.etUhrzeitString.addTextChangedListener(holder.textWatcher);
    }

    @Override
    public int getItemCount() {
        return stundenzeitList.size();
    }

    public void updateData(List<StundenzeitDefinition> newStundenzeitList) {
        this.stundenzeitList = newStundenzeitList;
        notifyDataSetChanged();
    }

    /**
     * Gibt die aktualisierte Liste der StundenzeitDefinitionen zurück,
     * einschließlich der vom Benutzer eingegebenen Werte.
     */
    public List<StundenzeitDefinition> getUpdatedStundenzeiten() {
        return stundenzeitList;
    }

    public static class StundenzeitViewHolder extends RecyclerView.ViewHolder {
        TextView tvStundenIndex;
        EditText etUhrzeitString;
        CustomTextWatcher textWatcher; // Referenz auf den TextWatcher

        public StundenzeitViewHolder(@NonNull View itemView, CustomTextWatcher textWatcher) {
            super(itemView);
            tvStundenIndex = itemView.findViewById(R.id.tvStundenIndex);
            etUhrzeitString = itemView.findViewById(R.id.etUhrzeitString);
            this.textWatcher = textWatcher;
            etUhrzeitString.addTextChangedListener(textWatcher);
        }
    }

    /**
     * Ein CustomTextWatcher, der die Änderungen im EditText direkt in der Liste des Adapters speichert.
     */
    private class CustomTextWatcher implements TextWatcher {
        private int position; // Die Position des Elements, das dieser Watcher überwacht

        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Nicht benötigt
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Nicht benötigt
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Aktualisiere das StundenzeitDefinition-Objekt in der Liste,
            // wenn der Text im EditText geändert wird
            if (position < stundenzeitList.size()) {
                stundenzeitList.get(position).setUhrzeitString(s.toString());
            }
        }
    }
}