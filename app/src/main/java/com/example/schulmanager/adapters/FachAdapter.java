package com.example.schulmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schulmanager.R;
import com.example.schulmanager.models.Fach;
import com.example.schulmanager.utils.BerechnungUtil;

import java.util.List;

/**
 * Adapter für die Anzeige einer Liste von Fächern in einem RecyclerView.
 * Dieser Adapter ist verantwortlich für das Erstellen und Binden der Views für jedes Fach-Element
 * sowie für die Handhabung von Klick-Events auf einzelne Fächer.
 */
public class FachAdapter extends RecyclerView.Adapter<FachAdapter.FachViewHolder> {

    /**
     * Interface zur Definition von Callback-Methoden für Klick-Events auf Fach-Elemente.
     * Das Fragment oder die Activity, die diesen Adapter verwendet, muss dieses Interface implementieren,
     * um auf Klicks reagieren zu können.
     */
    public interface OnFachClickListener {
        /**
         * Wird aufgerufen, wenn auf ein Fach-Element in der Liste geklickt wird.
         *
         * @param fach Das Fach-Objekt, auf das geklickt wurde.
         */
        void onFachClick(Fach fach);
    }

    private final List<Fach> faecher; // Die Liste der Fach-Objekte, die angezeigt werden sollen.
    private final OnFachClickListener listener; // Der Listener für Klick-Events auf Fach-Elemente.

    /**
     * Konstruktor für den FachAdapter.
     *
     * @param faecher  Die Liste der Fächer, die der Adapter anzeigen soll.
     * @param listener Der Listener, der über Klick-Events auf Fach-Elemente benachrichtigt wird.
     */
    public FachAdapter(List<Fach> faecher, OnFachClickListener listener) {
        this.faecher = faecher;    // Speichert die übergebene Liste von Fächern.
        this.listener = listener;  // Speichert den übergebenen Klick-Listener.
    }

    /**
     * Wird vom RecyclerView-LayoutManager aufgerufen, wenn ein neuer ViewHolder benötigt wird.
     * Erstellt eine neue View für ein Listen-Item, indem das Layout 'item_fach.xml' inflatiert wird.
     *
     * @param parent   Die ViewGroup, in die die neue View eingefügt wird.
     * @param viewType Der View-Typ der neuen View (hier nicht verwendet, da nur ein Typ).
     * @return Ein neuer FachViewHolder, der die View für ein Fach-Element enthält.
     */
    @NonNull
    @Override
    public FachViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflatiert das Layout für ein einzelnes Fach-Element (item_fach.xml).
        // LayoutInflater.from(parent.getContext()) holt den LayoutInflater aus dem Kontext der übergeordneten View.
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fach, parent, false);
        // Erstellt und gibt einen neuen FachViewHolder zurück, der die inflatierte View kapselt.
        return new FachViewHolder(itemView);
    }

    /**
     * Wird vom RecyclerView-LayoutManager aufgerufen, um die Daten an eine bestimmte View (ViewHolder) zu binden.
     * Hier werden die Daten eines Fach-Objekts in die entsprechenden TextViews des ViewHolders geladen.
     *
     * @param holder   Der FachViewHolder, der aktualisiert werden soll.
     * @param position Die Position des Fachs in der Liste 'faecher'.
     */
    @Override
    public void onBindViewHolder(@NonNull FachViewHolder holder, int position) {
        // Holt das Fach-Objekt an der aktuellen Position aus der Liste.
        Fach fach = faecher.get(position);
        // Bindet die Daten des Fach-Objekts an die View des ViewHolders.
        // Außerdem wird der Klick-Listener an die View angehängt.
        holder.bind(fach, listener);
    }

    /**
     * Gibt die Gesamtzahl der Elemente in der Datenquelle des Adapters zurück.
     *
     * @return Die Anzahl der Fächer in der Liste 'faecher'.
     */
    @Override
    public int getItemCount() {
        return faecher.size(); // Gibt die Größe der Fächerliste zurück.
    }

    /**
     * Aktualisiert die Datenliste des Adapters und benachrichtigt den RecyclerView über die Änderung.
     * Diese Methode sollte aufgerufen werden, wenn sich die Liste der Fächer außerhalb des Adapters ändert.
     *
     * @param newFaecher Die neue Liste von Fächern, die angezeigt werden soll.
     */
    public void updateFaecher(List<Fach> newFaecher) {
        faecher.clear();          // Löscht alle vorhandenen Elemente aus der aktuellen Liste.
        faecher.addAll(newFaecher); // Fügt alle Elemente aus der neuen Liste hinzu.
        notifyDataSetChanged();   // Benachrichtigt den RecyclerView, dass sich die gesamte Datenmenge geändert hat
        // und dass alle Elemente neu gezeichnet werden müssen.
        // (Effizientere Methoden wie notifyItemInserted/Removed/Changed sind bei
        // vollständigem Austausch der Liste oft weniger praktikabel).
    }

    /**
     * ViewHolder-Klasse, die die Views für ein einzelnes Fach-Element enthält und verwaltet.
     * Ein ViewHolder verbessert die Performance des RecyclerViews, indem er Referenzen auf die Views
     * speichert, anstatt sie bei jedem Scrollvorgang neu zu suchen (findViewById).
     */
    static final class FachViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;       // TextView für den Namen des Fachs.
        private final TextView tvHalbjahr;   // TextView für das Halbjahr des Fachs.
        private final TextView tvNote;       // TextView für die durchschnittliche Note des Fachs.
        private final TextView tvPunkte;     // TextView für die durchschnittlichen Punkte des Fachs.

        /**
         * Konstruktor für den FachViewHolder.
         *
         * @param itemView Die gesamte View für ein einzelnes Fach-Element (item_fach.xml).
         */
        public FachViewHolder(View itemView) {
            super(itemView); // Ruft den Konstruktor der übergeordneten Klasse (RecyclerView.ViewHolder) auf.
            // Initialisiert die TextViews, indem sie über ihre IDs in der itemView gefunden werden.
            tvName = itemView.findViewById(R.id.tv_name);
            tvHalbjahr = itemView.findViewById(R.id.tv_halbjahr);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvPunkte = itemView.findViewById(R.id.tv_punkte);
        }

        /**
         * Bindet die Daten eines Fach-Objekts an die Views des ViewHolders.
         * Setzt außerdem den Klick-Listener für das gesamte Item.
         *
         * @param fach     Das Fach-Objekt, dessen Daten angezeigt werden sollen.
         * @param listener Der OnFachClickListener, der aufgerufen wird, wenn auf das Item geklickt wird.
         */
        public void bind(Fach fach, OnFachClickListener listener) {
            // Setzt den Namen des Fachs in die entsprechende TextView.
            tvName.setText(fach.getName());
            // Setzt das Halbjahr des Fachs. Verwendet eine String-Ressource für die Formatierung.
            // itemView.getContext() wird benötigt, um auf String-Ressourcen zuzugreifen.
            tvHalbjahr.setText(itemView.getContext().getString(R.string.fach_halbjahr_format, fach.getHalbjahr()));

            // Holt den ungerundeten Durchschnitt der Punkte des Fachs.
            double durchschnittPunkte = fach.getDurchschnitt();
            // Setzt die umgerechnete Note (von Punkten zu Notenwert 1.0-6.0) in die TextView.
            // BerechnungUtil.punkteZuNoteEinzelwert() wird verwendet, um die Umrechnung vorzunehmen.
            // Eine String-Ressource wird für die Formatierung verwendet.
            tvNote.setText(itemView.getContext().getString(R.string.fach_avg_note_format,
                    BerechnungUtil.punkteZuNoteEinzelwert(durchschnittPunkte)));
            // Setzt die gerundeten durchschnittlichen Punkte des Fachs in die TextView.
            // Eine String-Ressource wird für die Formatierung verwendet.
            tvPunkte.setText(itemView.getContext().getString(R.string.fach_avg_punkte_format, fach.getDurchschnittsPunkte()));

            // Setzt einen OnClickListener für die gesamte Item-View.
            // Bei einem Klick wird die onFachClick-Methode des Listeners aufgerufen und das aktuelle Fach übergeben.
            itemView.setOnClickListener(v -> listener.onFachClick(fach));
        }
    }
}