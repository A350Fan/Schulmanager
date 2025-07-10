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

/**
 * RecyclerView Adapter zur Anzeige einer Liste von StundenplanEinträgen.
 * Dieser Adapter verwaltet die Erstellung und Bindung von Views für jeden Stundenplaneintrag
 * und bietet eine Schnittstelle zur Handhabung von Item-Aktionen wie dem Löschen.
 */
public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.StundenplanViewHolder> {

    // Die Liste der Stundenplaneinträge, die angezeigt werden sollen.
    private List<StundenplanEintrag> stundenplanList;
    // Listener für die Behandlung von Klick- und Lösch-Aktionen auf Elemente.
    private OnItemActionListener listener;

    /**
     * Schnittstellendefinition für einen Callback, der aufgerufen wird,
     * wenn eine Item-Aktion stattfindet. Dies ermöglicht es der Activity oder dem Fragment,
     * das den Adapter verwendet, auf Benutzerinteraktionen zu reagieren.
     */
    public interface OnItemActionListener {
        /**
         * Wird aufgerufen, wenn die Löschaktion für einen bestimmten Stundenplaneintrag ausgelöst wird.
         * @param eintrag Das StundenplanEintrag-Objekt, das gelöscht werden soll.
         */
        void onDeleteClick(StundenplanEintrag eintrag);
        // void onItemClick(StundenplanEintrag eintrag); // Platzhalter für eine zukünftige Klick-Funktion (z.B. zum Bearbeiten)
    }

    /**
     * Konstruktor für den TimetableAdapter.
     *
     * @param stundenplanList Die Liste der anzuzeigenden StundenplanEintrag-Objekte.
     * @param listener Eine Implementierung des OnItemActionListener zur Handhabung von Item-Interaktionen.
     */
    public TimetableAdapter(List<StundenplanEintrag> stundenplanList, OnItemActionListener listener) {
        this.stundenplanList = stundenplanList;
        this.listener = listener; // Initialisiert den Listener.
    }

    /**
     * Wird vom RecyclerView aufgerufen, wenn ein neuer {@link StundenplanViewHolder}
     * des gegebenen Typs benötigt wird, um ein Element darzustellen.
     *
     * @param parent   Die ViewGroup, in die die neue View eingefügt wird, nachdem sie an eine Adapterposition gebunden wurde.
     * @param viewType Der View-Typ der neuen View.
     * @return Ein neuer StundenplanViewHolder, der eine View des gegebenen View-Typs enthält.
     */
    @NonNull
    @Override
    public StundenplanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Lädt das Layout für ein einzelnes Stundenplaneintrag-Element.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timetable_entry, parent, false);
        // Erstellt und gibt einen neuen ViewHolder für die geladene View zurück.
        return new StundenplanViewHolder(view);
    }

    /**
     * Wird vom RecyclerView aufgerufen, um die Daten an der angegebenen Position anzuzeigen.
     * Diese Methode aktualisiert den Inhalt des {@link StundenplanViewHolder#itemView},
     * um das Element an der gegebenen Position widerzuspiegeln.
     *
     * @param holder   Der ViewHolder, dessen Inhalt aktualisiert werden soll, um den Inhalt des
     * Elements an der gegebenen Position im Datensatz widerzuspiegeln.
     * @param position Die Position des Elements im Datensatz des Adapters.
     */
    @Override
    public void onBindViewHolder(@NonNull StundenplanViewHolder holder, int position) {
        // Ruft das aktuelle StundenplanEintrag-Objekt aus der Liste ab.
        StundenplanEintrag currentEntry = stundenplanList.get(position);
        // Bindet die Daten des aktuellen Eintrags an die Views des ViewHolders.
        holder.bind(currentEntry);
    }

    /**
     * Gibt die Gesamtzahl der Elemente im Datensatz zurück, die vom Adapter verwaltet werden.
     *
     * @return Die Gesamtzahl der Elemente in diesem Adapter.
     */
    @Override
    public int getItemCount() {
        return stundenplanList.size();
    }

    /**
     * Aktualisiert den Datensatz des Adapters mit einer neuen Liste von Stundenplaneinträgen
     * und benachrichtigt den RecyclerView, seine Ansichten zu aktualisieren.
     *
     * @param newStundenplanList Die neue Liste von StundenplanEintrag-Objekten.
     */
    public void updateData(List<StundenplanEintrag> newStundenplanList) {
        this.stundenplanList = newStundenplanList;
        // Benachrichtigt alle registrierten Beobachter, dass sich der Datensatz geändert hat.
        notifyDataSetChanged();
    }

    /**
     * Ruft das StundenplanEintrag-Objekt an einer bestimmten Position im Datensatz des Adapters ab.
     * Diese Methode wird typischerweise von {@link androidx.recyclerview.widget.ItemTouchHelper}
     * für Wischgesten zum Löschen oder Drag-and-Drop-Funktionen verwendet.
     *
     * @param position Die Position des abzurufenden Elements.
     * @return Das StundenplanEintrag-Objekt an der angegebenen Position.
     */
    public StundenplanEintrag getItemAtPosition(int position) {
        return stundenplanList.get(position);
    }

    /**
     * Ein ViewHolder beschreibt eine Elementansicht und Metadaten über ihren Platz im RecyclerView.
     * Er hält Referenzen auf die Views innerhalb jedes Listen-Item-Layouts.
     */
    public static class StundenplanViewHolder extends RecyclerView.ViewHolder {
        // TextViews zur Anzeige der Stundenplan-Eintragsdetails.
        private TextView tvUhrzeit;
        private TextView tvFach;
        private TextView tvRaum;
        private TextView tvLehrer;

        /**
         * Konstruktor für den StundenplanViewHolder.
         *
         * @param itemView Die Root-View eines einzelnen Listenelements.
         */
        public StundenplanViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialisiert die TextViews, indem sie im Layout des Elements gefunden werden.
            // WICHTIG: Stelle sicher, dass diese IDs mit denen in deiner 'item_timetable_entry.xml'-Datei übereinstimmen.
            tvUhrzeit = itemView.findViewById(R.id.tvStundenplanUhrzeit);
            tvFach = itemView.findViewById(R.id.tvStundenplanFach);
            tvRaum = itemView.findViewById(R.id.tvStundenplanRaum);
            tvLehrer = itemView.findViewById(R.id.tvStundenplanLehrer);
        }

        /**
         * Bindet die Daten eines {@link StundenplanEintrag}-Objekts an die TextViews im ViewHolder.
         *
         * @param eintrag Das StundenplanEintrag-Objekt, das die anzuzeigenden Daten enthält.
         */
        public void bind(StundenplanEintrag eintrag) {
            // Setzt den Text für jedes TextView mit den entsprechenden Daten aus dem Eintrag.
            tvUhrzeit.setText(eintrag.getUhrzeit());
            tvFach.setText(eintrag.getFach());
            tvRaum.setText(eintrag.getRaum());

            // Überprüft, ob die Lehrerinformation vorhanden und nicht leer ist.
            // Falls ja, wird sie angezeigt; andernfalls wird das TextView ausgeblendet,
            // um Platz zu sparen und eine leere Anzeige zu vermeiden.
            if (eintrag.getLehrer() != null && !eintrag.getLehrer().isEmpty()) {
                tvLehrer.setText(eintrag.getLehrer());
                tvLehrer.setVisibility(View.VISIBLE); // Macht das TextView sichtbar.
            } else {
                tvLehrer.setVisibility(View.GONE); // Blendet das TextView komplett aus (es nimmt keinen Platz ein).
            }
        }
    }
}