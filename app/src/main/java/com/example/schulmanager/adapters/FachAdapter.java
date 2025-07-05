// adapters/FachAdapter.java
package com.example.schulmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schulmanager.R;
import com.example.schulmanager.models.Fach;
import com.example.schulmanager.utils.BerechnungUtil; // HINZUGEFÜGT: Import für BerechnungUtil

import java.util.List;

public class FachAdapter extends RecyclerView.Adapter<FachAdapter.FachViewHolder> {

    public interface OnFachClickListener {
        void onFachClick(Fach fach);
    }

    private final List<Fach> faecher;
    private final OnFachClickListener listener;

    public FachAdapter(List<Fach> faecher, OnFachClickListener listener) {
        this.faecher = faecher;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FachViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fach, parent, false);
        return new FachViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FachViewHolder holder, int position) {
        Fach fach = faecher.get(position);
        holder.bind(fach, listener);
    }

    @Override
    public int getItemCount() {
        return faecher.size();
    }

    static final class FachViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvHalbjahr;
        private final TextView tvNote;
        private final TextView tvPunkte;

        public FachViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvHalbjahr = itemView.findViewById(R.id.tv_halbjahr);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvPunkte = itemView.findViewById(R.id.tv_punkte);
        }

        public void bind(Fach fach, OnFachClickListener listener) {
            tvName.setText(fach.getName());
            tvHalbjahr.setText(itemView.getContext().getString(R.string.fach_halbjahr_format, fach.getHalbjahr()));

            double durchschnittPunkte = fach.getDurchschnitt();
            // GEÄNDERT: Aufruf der zentralen Methode in BerechnungUtil
            tvNote.setText(itemView.getContext().getString(R.string.fach_avg_note_format,
                    BerechnungUtil.punkteZuNoteEinzelwert(durchschnittPunkte)));
            tvPunkte.setText(itemView.getContext().getString(R.string.fach_avg_punkte_format, fach.getDurchschnittsPunkte()));

            itemView.setOnClickListener(v -> listener.onFachClick(fach));
        }

        // ENTFERNT: Die private Methode punkteZuNote wurde nach BerechnungUtil verschoben
        // private double punkteZuNote(double punkte) { ... }
    }
}