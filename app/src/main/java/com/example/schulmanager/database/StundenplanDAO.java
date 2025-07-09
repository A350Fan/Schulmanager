package com.example.schulmanager.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.schulmanager.models.StundenplanEintrag;

import java.util.List;

// Definiert dieses Interface als DAO
@Dao
public interface StundenplanDAO {

    // Fügen einen oder mehrere Stundenplan-Einträge ein.
    // OnConflictStrategy.REPLACE bedeutet, dass ein bestehender Eintrag mit der gleichen Primär-ID ersetzt wird.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StundenplanEintrag stundenplanEintrag);

    // Fügt eine Liste von Stundenplan-Einträgen ein
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StundenplanEintrag> stundenplanEintraege);

    // Aktualisiert einen oder mehrere Stundenplan-Einträge.
    @Update
    void update(StundenplanEintrag stundenplanEintrag);

    // Löscht einen oder mehrere Stundenplan-Einträge.
    @Delete
    void delete(StundenplanEintrag stundenplanEintrag);

    // Fragt alle Stundenplan-Einträge ab.
    @Query("SELECT * FROM stundenplan_eintraege ORDER BY tag, stunden_index ASC")
    List<StundenplanEintrag> getAllStundenplanEintraege();

    // Fragt Stundenplan-Einträge für einen bestimmten Tag ab.
    // Beachte den Doppelpunkt vor ':tag' - das ist ein Platzhalter für den Parameter.
    @Query("SELECT * FROM stundenplan_eintraege WHERE tag = :tag ORDER BY stunden_index ASC")
    List<StundenplanEintrag> getStundenplanEintraegeForTag(String tag);

    // Löscht alle Einträge (Nützlich für Tests oder Reset)
    @Query("DELETE FROM stundenplan_eintraege")
    void deleteAllStundenplanEintraege();
}