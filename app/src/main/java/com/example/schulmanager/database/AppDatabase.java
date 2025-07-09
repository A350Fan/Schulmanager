package com.example.schulmanager.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.schulmanager.models.StundenplanEintrag;
import com.example.schulmanager.models.StundenzeitDefinition; // NEU: Importiere die neue Entität

/**
 * Die Room-Datenbank für die Schulmanager-App.
 * Definiert die Datenbankversion und die Entitäten.
 */
@Database(entities = {StundenplanEintrag.class, StundenzeitDefinition.class}, version = 2, exportSchema = false) // NEU: StundenzeitDefinition.class hinzufügen
public abstract class AppDatabase extends RoomDatabase {

    public abstract StundenplanDAO stundenplanDao();
    public abstract StundenzeitDefinitionDAO stundenzeitDefinitionDao(); // NEU: Abstraktes Getter für das neue DAO

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "schulmanager_database")
                            // TODO: Entferne allowMainThreadQueries() in der Produktionsversion!
                            // Dies ist nur für einfache Tests während der Entwicklung.
                            // Datenbankoperationen sollten immer asynchron durchgeführt werden.
                            .allowMainThreadQueries()
                            // TODO: Entferne fallbackToDestructiveMigration() in der Produktionsversion!
                            // Bei Schemaänderungen sollten echte Migrationen implementiert werden,
                            // um Benutzerdaten nicht zu verlieren.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}