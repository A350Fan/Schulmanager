package com.example.schulmanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.schulmanager.models.Fach;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Eine Utility-Klasse für das Speichern und Laden von Anwendungsdaten (Fächer und Noten)
 * unter Verwendung von Android SharedPreferences.
 * Diese Klasse nutzt Gson zur Serialisierung und Deserialisierung von Listen von Fach-Objekten in JSON-Strings.
 */
public class DataStorageUtil {

    private static final String TAG = "DataStorageUtil"; // Tag für Logcat-Ausgaben
    private static final String PREF_NAME = "schulmanager_prefs"; // Name der SharedPreferences-Datei
    private static final String KEY_FAECHER = "faecher_data"; // Schlüssel für die Fächerliste

    /**
     * Privater Konstruktor, um die Instanziierung der Utility-Klasse zu verhindern.
     */
    private DataStorageUtil() {
        // Keine Aktion notwendig.
    }

    /**
     * Speichert eine Liste von Fach-Objekten in den SharedPreferences.
     * Die Liste wird zuerst in einen JSON-String konvertiert.
     *
     * @param context Der Anwendungskontext, benötigt für den Zugriff auf SharedPreferences.
     * @param faecher Die Liste der Fach-Objekte, die gespeichert werden sollen.
     */
    public static void saveFaecher(Context context, List<Fach> faecher) {
        // Sicherstellen, dass Context und Fächer nicht null sind.
        if (context == null) {
            Log.e(TAG, "Context ist null in saveFaecher.");
            return;
        }
        if (faecher == null) {
            Log.w(TAG, "Fächerliste ist null, speichere leere Liste.");
            faecher = new ArrayList<>(); // Speichere eine leere Liste, um Konsistenz zu gewährleisten.
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        // Konvertiere die Liste der Fächer in einen JSON-String.
        String json = gson.toJson(faecher);
        editor.putString(KEY_FAECHER, json); // Speichere den JSON-String unter dem definierten Schlüssel.
        editor.apply(); // Asynchron speichern, bevorzugt für UI-Threads. commit() wäre synchron.

        Log.d(TAG, "Fächer gespeichert: " + faecher.size() + " Objekte.");
    }

    /**
     * Lädt eine Liste von Fach-Objekten aus den SharedPreferences.
     * Der gespeicherte JSON-String wird in eine Liste von Fach-Objekten zurückkonvertiert.
     *
     * @param context Der Anwendungskontext, benötigt für den Zugriff auf SharedPreferences.
     * @return Die geladene Liste von Fach-Objekten oder eine leere Liste, wenn keine Daten gefunden wurden
     * oder ein Fehler auftrat. Gibt null zurück, wenn der Kontext null ist.
     */
    public static List<Fach> loadFaecher(Context context) {
        if (context == null) {
            Log.e(TAG, "Context ist null in loadFaecher.");
            return null; // Kann hier null zurückgeben, um anzuzeigen, dass der Ladevorgang nicht möglich war.
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();

        // Lade den JSON-String unter dem definierten Schlüssel.
        // Wenn kein String gefunden wird, ist der Standardwert eine leere String-Repräsentation eines leeren JSON-Arrays.
        String json = sharedPreferences.getString(KEY_FAECHER, "[]");

        // Definiere den Typ, in den der JSON-String konvertiert werden soll (Liste von Fach-Objekten).
        Type type = new TypeToken<List<Fach>>() {}.getType();

        List<Fach> faecher = new ArrayList<>();
        try {
            // Konvertiere den JSON-String zurück in eine Liste von Fach-Objekten.
            faecher = gson.fromJson(json, type);
            if (faecher == null) { // Falls fromJson null zurückgibt (z.B. bei ungültigem JSON, aber "[]" sollte das verhindern)
                faecher = new ArrayList<>();
            }
            Log.d(TAG, "Fächer geladen: " + faecher.size() + " Objekte.");
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Laden oder Deserialisieren der Fächer: " + e.getMessage());
            faecher = new ArrayList<>(); // Bei Fehler eine leere Liste zurückgeben.
        }

        return faecher;
    }
}