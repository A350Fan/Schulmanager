package com.example.schulmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schulmanager.R;
import com.example.schulmanager.models.StundenplanEintrag;

import java.util.List;

public class StundenplanAdapter extends RecyclerView.Adapter<StundenplanAdapter.StundenplanViewHolder> {

    private List<StundenplanEintrag> stundenplanList;
    private OnItemActionListener listener; // NEU: Listener-Variable

    // NEU: Interface für Klick- und Lösch-Aktionen
    public interface OnItemActionListener {
        void onDeleteClick(StundenplanEintrag eintrag);
        // void onItemClick(StundenplanEintrag eintrag); // Für spätere Bearbeitungsfunktion
    }

    // Angepasster Konstruktor, der den Listener entgegennimmt
    public StundenplanAdapter(List<StundenplanEintrag> stundenplanList, OnItemActionListener listener) {
        this.stundenplanList = stundenplanList;
        this.listener = listener; // Initialisiere den Listener
    }

    @NonNull
    @Override
    public StundenplanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stundenplan_eintrag, parent, false);
        return new StundenplanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StundenplanViewHolder holder, int position) {
        StundenplanEintrag currentEntry = stundenplanList.get(position);
        holder.bind(currentEntry);
    }

    @Override
    public int getItemCount() {
        return stundenplanList.size();
    }

    public void updateData(List<StundenplanEintrag> newStundenplanList) {
        this.stundenplanList = newStundenplanList;
        notifyDataSetChanged();
    }

    // NEU: Methode, um ein Element an einer bestimmten Position zu erhalten
    // Wird vom ItemTouchHelper benötigt
    public StundenplanEintrag getItemAtPosition(int position) {
        return stundenplanList.get(position);
    }

    public static class StundenplanViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUhrzeit;
        private TextView tvFach;
        private TextView tvRaum;
        private TextView tvLehrer;

        public StundenplanViewHolder(@NonNull View itemView) {
            super(itemView);
            // HIER SIND DIE ANPASSUNGEN: Verwende die IDs aus deiner XML-Datei!
            tvUhrzeit = itemView.findViewById(R.id.tvStundenplanUhrzeit);
            tvFach = itemView.findViewById(R.id.tvStundenplanFach);
            tvRaum = itemView.findViewById(R.id.tvStundenplanRaum);
            tvLehrer = itemView.findViewById(R.id.tvStundenplanLehrer);
        }

        public void bind(StundenplanEintrag eintrag) {
            tvUhrzeit.setText(eintrag.getUhrzeit());
            tvFach.setText(eintrag.getFach());
            tvRaum.setText(eintrag.getRaum());
            // Prüfen, ob Lehrer vorhanden ist, ansonsten ausblenden oder leeren String setzen
            if (eintrag.getLehrer() != null && !eintrag.getLehrer().isEmpty()) {
                tvLehrer.setText(eintrag.getLehrer());
                tvLehrer.setVisibility(View.VISIBLE);
            } else {
                tvLehrer.setVisibility(View.GONE); // Oder View.INVISIBLE, je nach Wunsch
            }
        }
    }
}