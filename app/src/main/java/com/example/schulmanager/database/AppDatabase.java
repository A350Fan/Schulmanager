package com.example.schulmanager.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.schulmanager.models.StundenplanEintrag;

// Definiert die Datenbank-Konfiguration
// entities: Liste der Entitätsklassen (Tabellen) in dieser Datenbank
// version: Versionsnummer der Datenbank. Muss bei Schema-Änderungen inkrementiert werden.
// exportSchema: Setze auf false in Produktions-Apps, true für Schema-Export (für Migrationsplanung)
@Database(entities = {StundenplanEintrag.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Abstrakte Methode, um Zugriff auf das DAO zu ermöglichen
    public abstract StundenplanDAO stundenplanDao();

    // Singleton-Pattern, um sicherzustellen, dass nur eine Instanz der Datenbank existiert
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) { // Synchronisiert, um Race Conditions zu vermeiden
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "schulmanager_database") // Name deiner Datenbank-Datei
                            .allowMainThreadQueries() // NICHT EMPFOHLEN FÜR PRODUKTION (blockiert UI), aber gut für einfache Tests.
                            // Später mit AsyncTasks, Executors oder Coroutines/Flows (Kotlin) umstellen.
                            .fallbackToDestructiveMigration() // Löscht die DB bei Versionsänderung (gut für Entwicklung, nicht für Produktion)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}