// adapters/FachAdapter.java (Angepasst)
package com.example.schulmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schulmanager.R;
import com.example.schulmanager.models.Fach;

import java.util.List;
import java.util.Locale; // NEU

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
        // ACHTUNG: tvPunkte.setText(fach.getFormattedPunkte()); ist hier in bind() doppelt
        // Es reicht, wenn es in bind() gesetzt wird.
        // holder.tvPunkte.setText(fach.getFormattedPunkte()); // DIESE ZEILE KANN ENTFERNT WERDEN
    }

    @Override
    public int getItemCount() {
        return faecher.size();
    }

    static final class FachViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvHalbjahr;
        private final TextView tvNote; // ÄNDERUNG: Jetzt zeigt dies den 0-15 Durchschnitt an
        private final TextView tvPunkte; // ÄNDERUNG: Dies zeigt auch den 0-15 Durchschnitt an, vielleicht umbenennen?

        public FachViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvHalbjahr = itemView.findViewById(R.id.tv_halbjahr);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvPunkte = itemView.findViewById(R.id.tv_punkte);
            //CardView cardView = itemView.findViewById(R.id.card_view);
        }

        public void bind(Fach fach, OnFachClickListener listener) {
            tvName.setText(fach.getName());
            tvHalbjahr.setText(itemView.getContext().getString(R.string.fach_halbjahr_format, fach.getHalbjahr()));

            // NEU: Anzeige des Durchschnitts (0-15 Punkte)
            // Du hast tvNote und tvPunkte. Lasst uns tvNote für den runden Punktedurchschnitt nehmen
            // und tvPunkte für die umgerechnete klassische Note 1-6, oder umgekehrt.
            // basierend auf den Namen ist tvNote die klassische Schulnote.
            // Die Berechnung dafür ist in NotenmanagerFragment, punkteZuNote(double punkte)
            // oder wir zeigen beides an, wie es jetzt ist.
            // Ich nehme an, dass tvNote die Note (1.0-6.0) und tvPunkte die Punkte (0-15) anzeigen soll.
            double durchschnittPunkte = fach.getDurchschnitt();
            tvNote.setText(itemView.getContext().getString(R.string.fach_avg_note_format, punkteZuNote(durchschnittPunkte)));
            tvPunkte.setText(itemView.getContext().getString(R.string.fach_avg_punkte_format, fach.getDurchschnittsPunkte()));


            itemView.setOnClickListener(v -> listener.onFachClick(fach));
        }

        // NEU: Hilfsmethode, um Punkte (0-15) in Note (1-6) umzurechnen, hier lokal für die Anzeige
        private double punkteZuNote(double punkte) {
            // Umrechnung Punkte (0-15) → Note (1-6)
            double note = 17.0 / 3.0 - punkte * 2.0 / 9.0;
            return Math.max(1.0, Math.min(6.0, note));
        }
    }

}