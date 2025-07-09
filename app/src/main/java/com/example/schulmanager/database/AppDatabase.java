package com.example.schulmanager.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase; // Benötigt für Callback

import com.example.schulmanager.models.StundenplanEintrag;
import com.example.schulmanager.models.StundenzeitDefinition;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {StundenplanEintrag.class, StundenzeitDefinition.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TimetableDAO stundenplanDao();
    public abstract StundenzeitDefinitionDAO stundenzeitDefinitionDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    // Verwende einen separaten Executor für die Initialisierung, um Deadlocks zu vermeiden,
    // da der Haupt-databaseWriteExecutor möglicherweise erst initialisiert wird.
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "schulmanager_database")
                            .addCallback(sRoomDatabaseCallback) // Füge den Callback hier hinzu!
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback für Room-Datenbank-Operationen.
     * Hier fügen wir Standardzeiten ein, wenn die Datenbank zum ersten Mal erstellt wird.
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Führe die Initialisierung der Datenbank in einem Hintergrund-Thread aus
            databaseWriteExecutor.execute(() -> {
                // Hole DAOs, um Daten einzufügen
                StundenzeitDefinitionDAO dao = INSTANCE.stundenzeitDefinitionDao();

                // Lösche alle vorhandenen Definitionen, um Duplikate bei Neuinstallation zu vermeiden,
                // falls der Callback aus irgendeinem Grund mehrmals ausgeführt würde (was bei onCreate nicht der Fall sein sollte,
                // aber eine gute defensive Programmierung ist).
                // Bei "onCreate" ist die DB leer, also ist dieser Schritt eigentlich nicht notwendig, schadet aber auch nicht.
                dao.deleteAll();

                // Definiere die Standardzeiten
                String[] defaultTimes = {
                        "08:00 - 08:45", "08:50 - 09:35", "09:50 - 10:35", "10:40 - 11:25", "11:30 - 12:15",
                        "12:20 - 13:05", "13:10 - 13:55", "14:00 - 14:45", "14:50 - 15:35", "15:40 - 16:25", "16:30 - 17:15"
                };

                // Füge jede Standardzeit als StundenzeitDefinition-Objekt ein
                for (int i = 0; i < defaultTimes.length; i++) {
                    StundenzeitDefinition definition = new StundenzeitDefinition(i, defaultTimes[i]);
                    dao.insert(definition);
                }
            });
        }
    };
}