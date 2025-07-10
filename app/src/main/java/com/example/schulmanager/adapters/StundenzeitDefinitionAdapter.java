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

/**
 * Adapter für die Anzeige und Bearbeitung von StundenzeitDefinition-Objekten in einem RecyclerView.
 * Dieser Adapter ermöglicht es dem Benutzer, die Uhrzeit-Strings für jede Stunde zu definieren.
 */
public class StundenzeitDefinitionAdapter extends RecyclerView.Adapter<StundenzeitDefinitionAdapter.StundenzeitViewHolder> {

    // Liste der StundenzeitDefinition-Objekte, die vom Adapter verwaltet werden
    private List<StundenzeitDefinition> stundenzeitList;

    /**
     * Konstruktor für den StundenzeitDefinitionAdapter.
     *
     * @param stundenzeitList Die Liste der StundenzeitDefinition-Objekte, die angezeigt werden sollen.
     */
    public StundenzeitDefinitionAdapter(List<StundenzeitDefinition> stundenzeitList) {
        this.stundenzeitList = stundenzeitList;
    }

    /**
     * Erstellt neue ViewHolders (und deren Views), wenn sie vom LayoutManager benötigt werden.
     *
     * @param parent   Die ViewGroup, in die die neue View eingefügt wird, nachdem sie an eine Position gebunden wurde.
     * @param viewType Der View-Typ der neuen View.
     * @return Ein neuer StundenzeitViewHolder, der eine View für das Stundenzeit-Element enthält.
     */
    @NonNull
    @Override
    public StundenzeitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Layout für ein einzelnes Stundenzeit-Element inflaten
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lessonhours_definition, parent, false);
        // Einen neuen ViewHolder mit einem neuen CustomTextWatcher erstellen und zurückgeben
        return new StundenzeitViewHolder(view, new CustomTextWatcher());
    }

    /**
     * Ersetzt den Inhalt einer vorhandenen View mit Daten.
     * Diese Methode wird vom LayoutManager aufgerufen.
     *
     * @param holder   Der ViewHolder, dessen Inhalt aktualisiert werden soll.
     * @param position Die Position des Elements in der Datenliste des Adapters.
     */
    @Override
    public void onBindViewHolder(@NonNull StundenzeitViewHolder holder, int position) {
        // Das aktuelle StundenzeitDefinition-Objekt basierend auf der Position abrufen
        StundenzeitDefinition currentDef = stundenzeitList.get(position);

        // Sicherstellen, dass der TextWatcher die korrekte Position des aktuellen Elements kennt.
        // Dies ist wichtig, da ViewHolder
        // wiederverwendet werden und der TextWatcher wissen muss, welches Datenobjekt er aktualisieren soll.
        holder.textWatcher.updatePosition(position);

        // Den Stundenindex im TextView anzeigen (z.B. "1. Stunde:", "2. Stunde:", etc.).
        // Der Index wird um 1 erhöht, da er 0-basiert ist, aber für den Benutzer bei 1 beginnen soll.
        holder.tvStundenIndex.setText((currentDef.getStundenIndex() + 1) + ". Stunde:");

        // Den Text des EditTexts setzen.
        // WICHTIG: Den TextWatcher temporär entfernen, bevor setText() aufgerufen wird,
        // um eine Endlosschleife zu vermeiden. setText() würde den TextWatcher auslösen,
        // der wiederum versuchen würde, das Datenmodell zu aktualisieren, was unnötig ist und Probleme verursachen könnte.
        holder.etUhrzeitString.removeTextChangedListener(holder.textWatcher);
        holder.etUhrzeitString.setText(currentDef.getUhrzeitString());
        // Den TextWatcher nach dem Setzen des Textes wieder hinzufügen
        holder.etUhrzeitString.addTextChangedListener(holder.textWatcher);
    }

    /**
     * Gibt die Gesamtzahl der Elemente in der Datenmenge des Adapters zurück.
     *
     * @return Die Gesamtanzahl der Elemente in der Liste.
     */
    @Override
    public int getItemCount() {
        return stundenzeitList.size();
    }

    /**
     * Aktualisiert die Datenliste des Adapters und benachrichtigt den RecyclerView,
     * dass die Daten geändert wurden, sodass die UI aktualisiert werden kann.
     *
     * @param newStundenzeitList Die neue Liste von StundenzeitDefinition-Objekten.
     */
    public void updateData(List<StundenzeitDefinition> newStundenzeitList) {
        this.stundenzeitList = newStundenzeitList;
        // Benachrichtigt alle registrierten Beobachter, dass die Datenmenge geändert wurde.
        notifyDataSetChanged();
    }

    /**
     * Gibt die aktualisierte Liste der StundenzeitDefinitionen zurück,
     * einschließlich der vom Benutzer in den EditText-Feldern vorgenommenen Änderungen.
     *
     * @return Die aktuelle Liste der StundenzeitDefinition-Objekte.
     */
    public List<StundenzeitDefinition> getUpdatedStundenzeiten() {
        return stundenzeitList;
    }

    /**
     * Ein ViewHolder beschreibt eine Elementansicht und Metadaten über ihren Platz im RecyclerView.
     * Er hält Referenzen auf die Views der einzelnen Listenelemente.
     */
    public static class StundenzeitViewHolder extends RecyclerView.ViewHolder {
        // TextView zur Anzeige des Stundenindex (z.B. "1. Stunde:")
        TextView tvStundenIndex;
        // EditText zur Eingabe und Anzeige des Uhrzeit-Strings
        EditText etUhrzeitString;
        // Referenz auf den CustomTextWatcher, der diesem ViewHolder zugeordnet ist
        CustomTextWatcher textWatcher;

        /**
         * Konstruktor für den StundenzeitViewHolder.
         *
         * @param itemView Die View für ein einzelnes Listenelement.
         * @param textWatcher Der CustomTextWatcher, der für dieses Element verwendet werden soll.
         */
        public StundenzeitViewHolder(@NonNull View itemView, CustomTextWatcher textWatcher) {
            super(itemView);
            // Referenzen zu den Views im Layout erhalten
            tvStundenIndex = itemView.findViewById(R.id.tvStundenIndex);
            etUhrzeitString = itemView.findViewById(R.id.etUhrzeitString);
            this.textWatcher = textWatcher;
            // Den TextWatcher zum EditText hinzufügen, um Änderungen zu überwachen
            etUhrzeitString.addTextChangedListener(textWatcher);
        }
    }

    /**
     * Ein CustomTextWatcher, der die Änderungen im EditText direkt in der Liste des Adapters speichert.
     * Dies ermöglicht eine bidirektionale Datenbindung: Änderungen in der UI werden direkt im Datenmodell reflektiert.
     */
    private class CustomTextWatcher implements TextWatcher {
        // Die Position des Elements, das dieser Watcher überwacht.
        // Diese muss aktualisiert werden, da ViewHolder recycelt werden.
        private int position;

        /**
         * Aktualisiert die Position, die dieser TextWatcher überwacht.
         * Dies ist notwendig, wenn Viewholder wiederverwendet werden.
         *
         * @param position Die neue Position des Elements in der Liste.
         */
        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Diese Methode wird aufgerufen, bevor der Text im EditText geändert wird.
            // In diesem Fall nicht benötigt.
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Diese Methode wird aufgerufen, während der Text im EditText geändert wird.
            // In diesem Fall nicht benötigt.
        }

        /**
         * Diese Methode wird aufgerufen, nachdem der Text im EditText geändert wurde.
         * Hier wird das entsprechende StundenzeitDefinition-Objekt in der Liste aktualisiert.
         *
         * @param s Das Editable-Objekt, das den neuen Text enthält.
         */
        @Override
        public void afterTextChanged(Editable s) {
            // Überprüfen, ob die aktuelle Position noch innerhalb der Grenzen der Liste liegt.
            // Dies verhindert IndexOutOfBounds-Ausnahmen, falls sich die Liste schnell ändert.
            if (position < stundenzeitList.size()) {
                // Das StundenzeitDefinition-Objekt an der aktuellen Position mit dem neuen Uhrzeit-String aktualisieren.
                stundenzeitList.get(position).setUhrzeitString(s.toString());
            }
        }
    }
}