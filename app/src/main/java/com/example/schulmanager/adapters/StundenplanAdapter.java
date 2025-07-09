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

    // Konstruktor
    public StundenplanAdapter(List<StundenplanEintrag> stundenplanList) {
        this.stundenplanList = stundenplanList;
    }

    @NonNull
    @Override
    public StundenplanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stundenplan_eintrag, parent, false);
        return new StundenplanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StundenplanViewHolder holder, int position) {
        StundenplanEintrag eintrag = stundenplanList.get(position);

        // Zeige die Stunde (z.B. "1. Stunde") und Uhrzeit
        holder.tvUhrzeit.setText(eintrag.getUhrzeit() + " (" + (eintrag.getStundenIndex() + 1) + ". Stunde)");
        holder.tvFach.setText(eintrag.getFach());
        holder.tvRaum.setText(eintrag.getRaum());
        holder.tvLehrer.setText(eintrag.getLehrer());
    }

    @Override
    public int getItemCount() {
        return stundenplanList.size();
    }

    public static class StundenplanViewHolder extends RecyclerView.ViewHolder {
        TextView tvUhrzeit, tvFach, tvRaum, tvLehrer;

        public StundenplanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUhrzeit = itemView.findViewById(R.id.tvStundenplanUhrzeit);
            tvFach = itemView.findViewById(R.id.tvStundenplanFach);
            tvRaum = itemView.findViewById(R.id.tvStundenplanRaum);
            tvLehrer = itemView.findViewById(R.id.tvStundenplanLehrer);
        }
    }

    // Methode zum Aktualisieren der Datenliste und Benachrichtigen des Adapters
    public void updateData(List<StundenplanEintrag> newList) {
        this.stundenplanList.clear(); // Alte Daten löschen
        this.stundenplanList.addAll(newList); // Neue Daten hinzufügen
        notifyDataSetChanged(); // RecyclerView neu zeichnen
    }
}