package com.example.schulmanager.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.schulmanager.models.StundenplanEintrag;

import java.util.List;

/**
 * Data Access Object (DAO) für die {@link StundenplanEintrag}-Entität.
 * Dieses Interface definiert die Methoden für den Zugriff auf die Datenbankoperationen
 * für Stundenplaneinträge in der Room-Datenbank.
 */
@Dao // Definiert dieses Interface als Room DAO (Data Access Object)
public interface TimetableDAO {

    /**
     * Fügt einen einzelnen Stundenplan-Eintrag in die Datenbank ein.
     * Wenn ein Eintrag mit der gleichen Primärschlüssel-ID bereits existiert,
     * wird dieser durch den neuen Eintrag ersetzt (OnConflictStrategy.REPLACE).
     * @param stundenplanEintrag Der einzufügende StundenplanEintrag.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StundenplanEintrag stundenplanEintrag);

    /**
     * Fügt eine Liste von Stundenplan-Einträgen in die Datenbank ein.
     * Bei Konflikten mit bestehenden Primärschlüsseln werden die alten Einträge ersetzt.
     * @param stundenplanEintraege Die Liste der einzufügenden StundenplanEinträge.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StundenplanEintrag> stundenplanEintraege);

    /**
     * Aktualisiert einen oder mehrere bestehende Stundenplan-Einträge in der Datenbank.
     * Die Aktualisierung erfolgt anhand der Primärschlüssel-ID des übergebenen Objekts.
     * @param stundenplanEintrag Der zu aktualisierende StundenplanEintrag.
     */
    @Update
    void update(StundenplanEintrag stundenplanEintrag);

    /**
     * Löscht einen oder mehrere Stundenplan-Einträge aus der Datenbank.
     * Das zu löschende Objekt wird anhand seiner Primärschlüssel-ID gefunden.
     * @param stundenplanEintrag Der zu löschende StundenplanEintrag.
     */
    @Delete
    void delete(StundenplanEintrag stundenplanEintrag);

    /**
     * Fragt alle Stundenplan-Einträge aus der Datenbank ab.
     * Die Ergebnisse werden zuerst nach dem Wochentag (tag) und dann nach dem Stundenindex (stunden_index)
     * in aufsteigender Reihenfolge sortiert.
     * @return Eine Liste aller StundenplanEintrag-Objekte.
     */
    @Query("SELECT * FROM stundenplan_eintraege ORDER BY tag, stunden_index ASC")
    List<StundenplanEintrag> getAllStundenplanEintraege();

    /**
     * Fragt Stundenplan-Einträge für einen bestimmten Wochentag ab.
     * Die Ergebnisse werden nach dem Stundenindex in aufsteigender Reihenfolge sortiert.
     * Der Parameter `:tag` ist ein Platzhalter, der beim Aufruf der Methode durch den übergebenen String ersetzt wird.
     * @param tag Der Wochentag (z.B. "Montag", "Dienstag"), für den die Einträge abgerufen werden sollen.
     * @return Eine Liste von StundenplanEintrag-Objekten, die dem angegebenen Tag entsprechen.
     */
    @Query("SELECT * FROM stundenplan_eintraege WHERE tag = :tag ORDER BY stunden_index ASC")
    List<StundenplanEintrag> getStundenplanEintraegeForTag(String tag);

    /**
     * Löscht alle Stundenplan-Einträge aus der Datenbank.
     * Diese Methode ist nützlich für Tests oder zum Zurücksetzen der Daten.
     */
    @Query("DELETE FROM stundenplan_eintraege")
    void deleteAllStundenplanEintraege();
}