// adapters/NoteAdapter.java
package com.example.schulmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.schulmanager.R;
import com.example.schulmanager.models.Note;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClick(Note note); // Für Bearbeitung (optional)
        void onNoteLongClick(Note note, int position); // Für Löschen
    }

    private final List<Note> noten;
    private final OnNoteClickListener listener;

    public NoteAdapter(List<Note> noten, OnNoteClickListener listener) {
        this.noten = noten;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false); // Wir brauchen noch item_note.xml
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noten.get(position);
        holder.bind(note, listener, position);
    }

    @Override
    public int getItemCount() {
        return noten.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNoteWert;
        private final TextView tvNoteTyp;
        private final TextView tvNoteDatum; // Optional: Datum anzeigen

        public NoteViewHolder(View itemView) {
            super(itemView);
            tvNoteWert = itemView.findViewById(R.id.tv_note_wert);
            tvNoteTyp = itemView.findViewById(R.id.tv_note_typ);
            tvNoteDatum = itemView.findViewById(R.id.tv_note_datum); // Füge dies in item_note.xml hinzu
        }

        public void bind(Note note, OnNoteClickListener listener, int position) {
            tvNoteWert.setText(String.format(Locale.GERMAN, "Wert: %.1f", note.getWert()));
            tvNoteTyp.setText("Typ: " + note.getTyp());

            // Optional: Datum formatieren und anzeigen
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
            tvNoteDatum.setText("Datum: " + sdf.format(note.getDatum()));

            // Optional: Click Listener für Bearbeitung
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNoteClick(note);
                }
            });

            // Long Click Listener für Löschen
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onNoteLongClick(note, position);
                }
                return true; // Wichtig: true zurückgeben, um Long-Click zu konsumieren
            });
        }
    }
}