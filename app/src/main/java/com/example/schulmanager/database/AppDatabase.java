package com.example.schulmanager.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.schulmanager.models.StundenplanEintrag;
import com.example.schulmanager.models.StundenzeitDefinition;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Die Hauptdatenbankklasse für die Anwendung, die Room verwendet.
 * Definiert die Entitäten {@link StundenplanEintrag} und {@link StundenzeitDefinition}
 * und bietet Zugriff auf deren DAOs.
 * <p>
 * version = 3: Dies ist die Versionsnummer der Datenbank. Muss erhöht werden, wenn sich das Schema ändert.
 * exportSchema = false: Deaktiviert das Exportieren des Schemas in eine Datei, was für
 * kleinere Projekte oder Entwicklung in Ordnung ist. Für Produktions-Apps
 * sollte es auf 'true' gesetzt und das Schema versioniert werden.
 */
@Database(entities = {StundenplanEintrag.class, StundenzeitDefinition.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Abstrakte Methode, um das DAO für Stundenplan-Einträge abzurufen.
     * Room generiert die Implementierung zur Laufzeit.
     * @return Das TimetableDAO-Objekt.
     */
    public abstract TimetableDAO stundenplanDao();

    /**
     * Abstrakte Methode, um das DAO für Stundenzeit-Definitionen abzurufen.
     * Room generiert die Implementierung zur Laufzeit.
     * @return Das StundenzeitDefinitionDAO-Objekt.
     */
    public abstract StundenzeitDefinitionDAO stundenzeitDefinitionDao();

    // Singleton-Instanz der Datenbank
    private static volatile AppDatabase INSTANCE;

    // ExecutorService für Datenbankoperationen im Hintergrund-Thread-Pool
    private static final int NUMBER_OF_THREADS = 4;
    // Verwende einen separaten Executor für die Initialisierung/onCreate-Callback,
    // um mögliche Deadlocks zu vermeiden, falls der Haupt-databaseWriteExecutor
    // noch nicht vollständig initialisiert ist oder andere Abhängigkeiten hat.
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Gibt die Singleton-Instanz der AppDatabase zurück.
     * Wenn die Instanz noch nicht existiert, wird sie erstellt.
     * Die Erstellung ist synchronisiert, um Thread-Sicherheit zu gewährleisten (Double-Checked Locking).
     *
     * @param context Der Anwendungskontext, wird von Room für die Datenbankerstellung benötigt.
     * @return Die Singleton-Instanz von AppDatabase.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) { // Prüfe außerhalb des synchronisierten Blocks, um Performance zu verbessern
            synchronized (AppDatabase.class) { // Synchronisiere den Block, um Race Conditions zu vermeiden
                if (INSTANCE == null) { // Zweite Prüfung innerhalb des synchronisierten Blocks
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), // Erstelle die Datenbank-Instanz
                                    AppDatabase.class, "schulmanager_database") // Datenbank-Name
                            .addCallback(sRoomDatabaseCallback) // Füge den Callback für Initialisierungen hinzu
                            // .fallbackToDestructiveMigration() // OPTIONAL: Löscht die DB bei Versionsänderung und migriert nicht
                            // .allowMainThreadQueries() // NICHT EMPFOHLEN für Produktivanwendungen, nur für Debugging/Tests
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback für Room-Datenbank-Operationen.
     * Diese Implementierung wird verwendet, um die Datenbank beim ersten Erstellen
     * mit Standard-Stundenzeit-Definitionen zu befüllen.
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        /**
         * Wird aufgerufen, wenn die Datenbank zum ersten Mal erstellt wird.
         * Ideal, um initiale Daten einzufügen.
         * @param db Die SupportSQLiteDatabase-Instanz.
         */
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Führe die Initialisierung der Datenbank in einem Hintergrund-Thread aus,
            // um den UI-Thread nicht zu blockieren.
            databaseWriteExecutor.execute(() -> {
                // Hole das DAO für StundenzeitDefinitionen von der INSTANCE
                StundenzeitDefinitionDAO dao = INSTANCE.stundenzeitDefinitionDao();

                // Lösche alle vorhandenen Definitionen. Bei onCreate ist die DB leer,
                // daher ist dieser Schritt redundant, aber schadet nicht und
                // dient als defensive Programmierung.
                dao.deleteAll();

                // Definiere die Standard-Uhrzeiten für die Schulstunden
                String[] defaultTimes = {
                        "08:00 - 08:45", "08:50 - 09:35", "09:50 - 10:35", "10:40 - 11:25", "11:30 - 12:15",
                        "12:20 - 13:05", "13:10 - 13:55", "14:00 - 14:45", "14:50 - 15:35", "15:40 - 16:25", "16:30 - 17:15"
                };

                // Füge jede Standardzeit als StundenzeitDefinition-Objekt in die Datenbank ein.
                // Der StundenIndex ist der Array-Index 'i'.
                for (int i = 0; i < defaultTimes.length; i++) {
                    StundenzeitDefinition definition = new StundenzeitDefinition(i, defaultTimes[i]);
                    dao.insert(definition);
                }
            });
        }

        // Du könntest hier auch die onOpen()-Methode überschreiben,
        // um Aktionen auszuführen, wenn die Datenbank geöffnet wird.
        // @Override
        // public void onOpen(@NonNull SupportSQLiteDatabase db) {
        //     super.onOpen(db);
        // }

        // Wenn du eine Migration von einer alten zu einer neuen Datenbankversion durchführen möchtest,
        // würdest du hier auch die onUpgrade()-Methode oder spezielle Migrationen definieren.
    };
}