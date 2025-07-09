package com.example.schulmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schulmanager.R;
import com.example.schulmanager.models.Grade;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter für die Anzeige einer Liste von Noten in einem RecyclerView.
 * Dieser Adapter ist verantwortlich für das Erstellen und Binden der Views für jedes Noten-Element
 * sowie für die Handhabung von Klick- und Long-Klick-Events auf einzelne Noten.
 */
public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.NoteViewHolder> {

    /**
     * Interface zur Definition von Callback-Methoden für Klick- und Long-Klick-Events auf Noten-Elemente.
     * Das Fragment oder die Activity, die diesen Adapter verwendet, muss dieses Interface implementieren,
     * um auf die Events reagieren zu können.
     */
    public interface OnNoteClickListener {
        /**
         * Wird aufgerufen, wenn auf ein Noten-Element in der Liste geklickt wird.
         * Kann für eine Detailansicht oder Bearbeitung verwendet werden.
         *
         * @param grade Das Grade-Objekt, auf das geklickt wurde.
         */
        void onNoteClick(Grade grade);

        /**
         * Wird aufgerufen, wenn ein Noten-Element in der Liste lange gedrückt wird.
         * Wird typischerweise für Löschaktionen verwendet.
         *
         * @param grade     Das Grade-Objekt, das lange gedrückt wurde.
         * @param position Die Position des Grade-Objekts in der Liste.
         */
        void onNoteLongClick(Grade grade, int position);
    }

    private final List<Grade> noten; // Die Liste der Grade-Objekte, die angezeigt werden sollen.
    private final OnNoteClickListener listener; // Der Listener für Klick-Events auf Noten-Elemente.

    /**
     * Konstruktor für den GradeAdapter.
     *
     * @param noten    Die Liste der Noten, die der Adapter anzeigen soll.
     * @param listener Der Listener, der über Klick- und Long-Klick-Events auf Noten-Elemente benachrichtigt wird.
     */
    public GradeAdapter(List<Grade> noten, OnNoteClickListener listener) {
        this.noten = noten;    // Speichert die übergebene Liste von Noten.
        this.listener = listener;  // Speichert den übergebenen Klick-Listener.
    }

    /**
     * Wird vom RecyclerView-LayoutManager aufgerufen, wenn ein neuer ViewHolder benötigt wird.
     * Erstellt eine neue View für ein Listen-Item, indem das Layout 'item_note.xml' inflatiert wird.
     *
     * @param parent   Die ViewGroup, in die die neue View eingefügt wird.
     * @param viewType Der View-Typ der neuen View (hier nicht verwendet, da nur ein Typ).
     * @return Ein neuer NoteViewHolder, der die View für ein Noten-Element enthält.
     */
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflatiert das Layout für ein einzelnes Noten-Element (item_note.xml).
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grade, parent, false);
        // Erstellt und gibt einen neuen NoteViewHolder zurück, der die inflatierte View kapselt.
        return new NoteViewHolder(itemView);
    }

    /**
     * Wird vom RecyclerView-LayoutManager aufgerufen, um die Daten an eine bestimmte View (ViewHolder) zu binden.
     * Hier werden die Daten eines Grade-Objekts in die entsprechenden TextViews des ViewHolders geladen.
     *
     * @param holder   Der NoteViewHolder, der aktualisiert werden soll.
     * @param position Die Position der Grade in der Liste 'noten'.
     */
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        // Holt das Grade-Objekt an der aktuellen Position aus der Liste.
        Grade grade = noten.get(position);
        // Bindet die Daten des Grade-Objekts an die View des ViewHolders.
        // Außerdem werden die Klick-Listener an die View angehängt.
        holder.bind(grade, listener, position);
    }

    /**
     * Gibt die Gesamtzahl der Elemente in der Datenquelle des Adapters zurück.
     *
     * @return Die Anzahl der Noten in der Liste 'noten'.
     */
    @Override
    public int getItemCount() {
        return noten.size(); // Gibt die Größe der Notenliste zurück.
    }

    /**
     * Aktualisiert die Datenliste des Adapters und benachrichtigt den RecyclerView über die Änderung.
     * Diese Methode sollte aufgerufen werden, wenn sich die Liste der Noten außerhalb des Adapters ändert.
     *
     * @param newNoten Die neue Liste von Noten, die angezeigt werden soll.
     */
    public void updateNoten(List<Grade> newNoten) {
        noten.clear();          // Löscht alle vorhandenen Elemente aus der aktuellen Liste.
        noten.addAll(newNoten); // Fügt alle Elemente aus der neuen Liste hinzu.
        notifyDataSetChanged(); // Benachrichtigt den RecyclerView, dass sich die gesamte Datenmenge geändert hat.
    }


    /**
     * ViewHolder-Klasse, die die Views für ein einzelnes Noten-Element enthält und verwaltet.
     * Ein ViewHolder verbessert die Performance des RecyclerViews, indem er Referenzen auf die Views
     * speichert, anstatt sie bei jedem Scrollvorgang neu zu suchen (findViewById).
     */
    static final class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNoteWert;  // TextView für den Punktwert der Grade.
        private final TextView tvNoteTyp;   // TextView für den Typ der Grade (z.B. "schriftlich").
        private final TextView tvNoteDatum; // TextView für das Datum der Notenerfassung.

        /**
         * Konstruktor für den NoteViewHolder.
         *
         * @param itemView Die gesamte View für ein einzelnes Noten-Element (item_note.xml).
         */
        public NoteViewHolder(View itemView) {
            super(itemView); // Ruft den Konstruktor der übergeordneten Klasse (RecyclerView.ViewHolder) auf.
            // Initialisiert die TextViews, indem sie über ihre IDs in der itemView gefunden werden.
            tvNoteWert = itemView.findViewById(R.id.tv_note_wert);
            tvNoteTyp = itemView.findViewById(R.id.tv_note_typ);
            tvNoteDatum = itemView.findViewById(R.id.tv_note_datum);
        }

        /**
         * Bindet die Daten eines Grade-Objekts an die Views des ViewHolders.
         * Setzt außerdem die Klick- und Long-Klick-Listener für das gesamte Item.
         *
         * @param grade     Das Grade-Objekt, dessen Daten angezeigt werden sollen.
         * @param listener Der OnNoteClickListener, der aufgerufen wird, wenn auf das Item geklickt oder lange gedrückt wird.
         * @param position Die Position des Grade-Objekts in der Liste (wichtig für Long-Click zum Löschen).
         */
        public void bind(Grade grade, OnNoteClickListener listener, int position) {
            // Setzt den Punktwert der Grade in die entsprechende TextView.
            // Verwendet eine String-Ressource für die Formatierung.
            tvNoteWert.setText(itemView.getContext().getString(R.string.note_value_format, grade.getWert()));
            // Setzt den Typ der Grade in die entsprechende TextView.
            // Verwendet eine String-Ressource für die Formatierung.
            tvNoteTyp.setText(itemView.getContext().getString(R.string.note_type_format, grade.getTyp()));

            // Erstellt ein SimpleDateFormat-Objekt, um das Datum zu formatieren.
            // "dd.MM.yyyy" für das Format Tag.Monat.Jahr und Locale.GERMAN für die deutsche Lokalisierung.
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
            // Formatiert das Datum (das als long-Timestamp gespeichert ist) und setzt es in die TextView.
            // Verwendet eine String-Ressource für die Gesamtformatierung.
            tvNoteDatum.setText(itemView.getContext().getString(R.string.note_date_format, sdf.format(grade.getDatum())));

            // Setzt einen OnClickListener für die gesamte Item-View.
            itemView.setOnClickListener(v -> {
                // Prüft, ob ein Listener vorhanden ist, um NullPointerExceptions zu vermeiden.
                if (listener != null) {
                    // Ruft die onNoteClick-Methode des Listeners auf und übergibt die aktuelle Grade.
                    listener.onNoteClick(grade);
                }
            });

            // Setzt einen OnLongClickListener für die gesamte Item-View.
            itemView.setOnLongClickListener(v -> {
                // Prüft, ob ein Listener vorhanden ist.
                if (listener != null) {
                    // Ruft die onNoteLongClick-Methode des Listeners auf und übergibt die aktuelle Grade und ihre Position.
                    listener.onNoteLongClick(grade, position);
                }
                // Gibt 'true' zurück, um anzuzeigen, dass das Long-Click-Event konsumiert wurde
                // und kein normales Klick-Event mehr ausgelöst werden soll.
                return true;
            });
        }
    }
}