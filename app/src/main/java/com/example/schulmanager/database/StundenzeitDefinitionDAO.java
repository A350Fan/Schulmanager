package com.example.schulmanager.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.schulmanager.models.StundenzeitDefinition;

import java.util.List;

/**
 * Data Access Object (DAO) für die {@link StundenzeitDefinition}-Entität.
 * Dieses Interface definiert die Methoden für den Zugriff auf die Datenbankoperationen
 * für die Definitionen von Stundenzeiten in der Room-Datenbank.
 */
@Dao // Definiert dieses Interface als Room DAO
public interface StundenzeitDefinitionDAO {

    /**
     * Fügt eine StundenzeitDefinition in die Datenbank ein.
     * Wenn ein Konflikt aufgrund eines bereits existierenden Primärschlüssels auftritt,
     * wird der alte Eintrag durch den neuen ersetzt (z.B. wenn eine Definition für einen
     * Stundenindex bereits existiert und aktualisiert werden soll).
     * @param definition Die einzufügende oder zu aktualisierende StundenzeitDefinition.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StundenzeitDefinition definition);

    /**
     * Aktualisiert eine bestehende StundenzeitDefinition in der Datenbank.
     * Die Aktualisierung erfolgt anhand der Primärschlüssel-ID des übergebenen Objekts.
     * @param definition Die zu aktualisierende StundenzeitDefinition.
     */
    @Update
    void update(StundenzeitDefinition definition);

    /**
     * Löscht eine StundenzeitDefinition aus der Datenbank.
     * Das zu löschende Objekt wird anhand seiner Primärschlüssel-ID gefunden.
     * @param definition Die zu löschende StundenzeitDefinition.
     */
    @Delete
    void delete(StundenzeitDefinition definition);

    /**
     * Fragt alle StundenzeitDefinitionen aus der Datenbank ab.
     * Die Ergebnisse werden nach dem Stundenindex in aufsteigender Reihenfolge sortiert.
     * @return Eine Liste aller StundenzeitDefinition-Objekte, sortiert nach StundenIndex.
     */
    @Query("SELECT * FROM stundenzeit_definition ORDER BY stundenIndex ASC")
    List<StundenzeitDefinition> getAllStundenzeitDefinitions();

    /**
     * Ruft eine einzelne StundenzeitDefinition anhand ihres Stundenindexes ab.
     * Es wird `LIMIT 1` verwendet, da erwartet wird, dass pro StundenIndex nur ein Eintrag existiert.
     * @param index Der Stundenindex (z.B. 0 für die erste Stunde), für den die Definition abgerufen werden soll.
     * @return Die StundenzeitDefinition für den angegebenen Index oder null, wenn keine gefunden wurde.
     */
    @Query("SELECT * FROM stundenzeit_definition WHERE stundenIndex = :index LIMIT 1")
    StundenzeitDefinition getStundenzeitDefinitionByIndex(int index);

    /**
     * Löscht alle StundenzeitDefinitionen aus der Datenbank.
     * Nützlich für einen Reset oder Testzwecke.
     */
    @Query("DELETE FROM stundenzeit_definition")
    void deleteAll();
}