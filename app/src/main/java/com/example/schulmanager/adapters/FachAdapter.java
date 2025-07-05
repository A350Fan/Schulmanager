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

    static final class FachViewHolder extends RecyclerView.ViewHolder { // final HINZUGEFÜGT
        private final TextView tvName;
        private final TextView tvHalbjahr;
        private final TextView tvNote;
        private final TextView tvPunkte;
        // private final CardView cardView; // DIESE ZEILE WURDE WIE ZUVOR BESCHRIEBEN ENTFERNT

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
            // ÄNDERUNG: Angepasste Formel für punkteZuNote
            tvNote.setText(itemView.getContext().getString(R.string.fach_avg_note_format, punkteZuNote(durchschnittPunkte)));
            tvPunkte.setText(itemView.getContext().getString(R.string.fach_avg_punkte_format, fach.getDurchschnittsPunkte()));

            itemView.setOnClickListener(v -> listener.onFachClick(fach));
        }

        // NEU/GEÄNDERT: Hilfsmethode, um Punkte (0-15) in Note (1-6) umzurechnen
        private double punkteZuNote(double punkte) {
            // Umrechnung Punkte (0-15) → Note (1-6)
            // 15 Punkte = 1,0
            // 0 Punkte = 6,0
            // Formel: Note = (17 - Punkte) / 3
            double note = (17.0 - punkte) / 3.0;
            // Sicherstellen, dass die Note im Bereich 1.0 bis 6.0 bleibt
            return Math.max(1.0, Math.min(6.0, note));
        }
    }
}