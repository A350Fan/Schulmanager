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
 * DAO für den Zugriff auf StundenzeitDefinitionen in der Datenbank.
 */
@Dao
public interface StundenzeitDefinitionDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Bei Konflikt (gleicher PrimaryKey) ersetzen
    void insert(StundenzeitDefinition definition);

    @Update
    void update(StundenzeitDefinition definition);

    @Delete
    void delete(StundenzeitDefinition definition);

    @Query("SELECT * FROM stundenzeit_definition ORDER BY stundenIndex ASC")
    List<StundenzeitDefinition> getAllStundenzeitDefinitions();

    @Query("SELECT * FROM stundenzeit_definition WHERE stundenIndex = :index LIMIT 1")
    StundenzeitDefinition getStundenzeitDefinitionByIndex(int index);
}