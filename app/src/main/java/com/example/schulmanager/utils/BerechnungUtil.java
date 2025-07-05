// utils/BerechnungUtil.java
package com.example.schulmanager.utils;

import com.example.schulmanager.models.Fach;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BerechnungUtil {

    private static final int[][] PUNKTE_TABELLE = {
            {900, 823, 10}, // 1,0
            {822, 805, 11}, // 1,1
            {804, 787, 12}, // 1,2
            {786, 769, 13}, // 1,3
            {768, 751, 14}, // 1,4
            {750, 733, 15}, // 1,5
            {732, 715, 16}, // 1,6
            {714, 697, 17}, // 1,7
            {696, 679, 18}, // 1,9
            {678, 661, 19}, // 2,0
            {660, 643, 20}, // 2,1
            {642, 625, 21}, // 2,2
            {624, 607, 22}, // 2,3
            {606, 589, 23}, // 2,4
            {588, 571, 24}, // 2,5
            {570, 553, 25}, // 2,6
            {552, 535, 26}, // 2,7
            {534, 517, 27}, // 2,8
            {516, 499, 28}, // 2,9
            {498, 481, 29}, // 3,0
            {480, 463, 30}, // 3,1
            {462, 445, 31}, // 3,2
            {444, 427, 32}, // 3,3
            {426, 409, 33}, // 3,3
            {408, 391, 34}, // 3,4
            {390, 373, 35}, // 3,5
            {372, 355, 36}, // 3,6
            {354, 337, 37}, // 3,7
            {336, 319, 38}, // 3,8
            {318, 301, 39}, // 3,9
            {300, 300, 40}  // 4,0 (Mindestpunktzahl für Bestehen)
    };

    public static class AbiErgebnis {
        public int halbjahresPunkte;
        public int pruefungsPunkte;
        public int gesamtPunkte;
        public String abiSchnitt;
        public boolean bestanden;
        public String bestandenNachricht;
    }

    /**
     * Berechnet den Abischnitt basierend auf Halbjahresleistungen und Prüfungsnoten.
     * Berücksichtigt auch die Regel der maximalen Unterpunktungen.
     *
     * @param faecher Eine Liste aller Fächer.
     * @param pruefungsNoten Ein Array der 5 Abiturprüfungsnoten (0-15 Punkte).
     * @return Ein AbiErgebnis-Objekt mit den berechneten Punkten, dem Schnitt und dem Bestehensstatus.
     */
    public static AbiErgebnis berechneAbi(List<Fach> faecher, int[] pruefungsNoten) {
        AbiErgebnis ergebnis = new AbiErgebnis();

        // 1. Halbjahresleistungen sammeln und sortieren (absteigend)
        List<Integer> halbjahresLeistungen = new ArrayList<>();
        for (Fach fach : faecher) {
            halbjahresLeistungen.add(fach.getDurchschnittsPunkte());
        }
        halbjahresLeistungen.sort(Collections.reverseOrder());

        // NEU: Anzahl der Unterpunktungen in den relevanten Halbjahresleistungen zählen
        int unterpunktungenCount = 0;
        int anzahlRelevanterLeistungen = Math.min(40, halbjahresLeistungen.size()); // Max 40 Leistungen einbringen

        for (int i = 0; i < anzahlRelevanterLeistungen; i++) {
            if (halbjahresLeistungen.get(i) <= 4) { // Punkte von 4 oder weniger sind Unterpunktungen
                unterpunktungenCount++;
            }
        }

        // 2. Halbjahresleistungen Punkte berechnen (max. 600 Punkte)
        ergebnis.halbjahresPunkte = berechneHalbjahresPunkte(halbjahresLeistungen);

        // 3. Prüfungsleistungen berechnen (5 Prüfungen à max. 60 Punkte, gesamt max. 300 Punkte)
        ergebnis.pruefungsPunkte = 0;
        for (int note : pruefungsNoten) {
            ergebnis.pruefungsPunkte += Math.min(60, note * 4);
        }
        ergebnis.pruefungsPunkte = Math.min(300, ergebnis.pruefungsPunkte);

        // 4. Gesamtpunkte berechnen (Halbjahresleistungen + Prüfungsleistungen)
        ergebnis.gesamtPunkte = ergebnis.halbjahresPunkte + ergebnis.pruefungsPunkte;

        // 5. Abischnitt ermitteln (gibt jetzt IMMER eine Note zurück, auch wenn 4,0 bei Nichtbestehen)
        ergebnis.abiSchnitt = punkteZuNoteGesamt(ergebnis.gesamtPunkte);

        // 6. Bestehensstatus festlegen (Reihenfolge wichtig!)
        // Zuerst die Unterpunktungen prüfen, da dies oft eine harte Ausschlussregel ist.
        if (unterpunktungenCount > 8) {
            ergebnis.bestanden = false;
            ergebnis.bestandenNachricht = String.format(Locale.GERMAN,
                    "Leider nicht bestanden. Es gibt %d Unterpunktungen (< 5 Punkte) in den 40 Halbjahresleistungen (erlaubt: max. 8).",
                    unterpunktungenCount);
        }
        // Dann die Gesamtpunktzahl prüfen
        else if (ergebnis.gesamtPunkte < 300) {
            ergebnis.bestanden = false;
            ergebnis.bestandenNachricht = "Leider nicht bestanden. Die Gesamtpunktzahl ist zu gering (mind. 300 Punkte benötigt).";
        }
        // Wenn keine der Misserfolgsbedingungen zutrifft
        else {
            ergebnis.bestanden = true;
            ergebnis.bestandenNachricht = "Herzlichen Glückwunsch! Abitur bestanden!";
        }

        return ergebnis;
    }

    /**
     * Berechnet die Punkte für die Halbjahresleistungen (beste 40 Leistungen).
     * Falls weniger als 40 Leistungen vorhanden sind, wird hochgerechnet.
     * @param leistungen Liste der Halbjahresleistungen (bereits sortiert, 0-15 Punkte).
     * @return Gesamtpunkte für die Halbjahresleistungen (max. 600).
     */
    private static int berechneHalbjahresPunkte(List<Integer> leistungen) {
        if (leistungen.isEmpty()) return 0;

        int summe = 0;
        int anzahlDerLeistungen = Math.min(40, leistungen.size());

        // Nur die besten 'anzahlDerLeistungen' (max. 40) berücksichtigen
        for (int i = 0; i < anzahlDerLeistungen; i++) {
            summe += leistungen.get(i);
        }

        // Hochrechnung nur, wenn weniger als 40 Leistungen tatsächlich eingebracht wurden
        if (anzahlDerLeistungen < 40 && anzahlDerLeistungen > 0) { // Muss auch > 0 sein, um Division durch 0 zu vermeiden
            double durchschnitt = (double)summe / anzahlDerLeistungen;
            summe = (int) Math.round(durchschnitt * 40);
        }

        return Math.min(600, summe);
    }

    /**
     * Wandelt die Gesamtpunktzahl des Abiturs in eine Noten-String (z.B. "2,5") um.
     * Basierend auf der PUNKTE_TABELLE. Gibt "4,0" zurück, wenn nicht bestanden.
     * Die explizite "Nicht bestanden"-Meldung wird über AbiErgebnis.bestandenNachricht geliefert.
     * @param gesamtPunkte Die erreichte Gesamtpunktzahl.
     * @return Der Abischnitt als String ("1,0" bis "4,0").
     */
    private static String punkteZuNoteGesamt(int gesamtPunkte) {
        if (gesamtPunkte < 300) {
            return "4,0";
        }

        for (int[] eintrag : PUNKTE_TABELLE) {
            if (gesamtPunkte <= eintrag[0] && gesamtPunkte >= eintrag[1]) {
                return noteToString(eintrag[2]);
            }
        }
        return "4,0"; // Fallback, sollte nicht erreicht werden bei korrekter Tabelle
    }

    private static String noteToString(int noteWert) {
        int ganzzahl = noteWert / 10;
        int nachkomma = noteWert % 10;
        return String.format(Locale.GERMAN, "%d,%d", ganzzahl, nachkomma);
    }

    /**
     * Wandelt eine Punktzahl (0-15) in eine Notenwert (1.0-6.0) um.
     *
     * @param punkte Die Punktzahl (0-15).
     * @return Die umgerechnete Note als double.
     */
    public static double punkteZuNoteEinzelwert(double punkte) {
        double note = 6.0 - (punkte / 3.0);
        return Math.max(1.0, Math.min(6.0, note));
    }

    public static class HalbjahrErgebnis {
        public int halbjahr;
        public double durchschnitt;
        public int anzahlFaecher;
    }

    public static HalbjahrErgebnis berechneHalbjahrSchnitt(List<Fach> alleFaecher, int halbjahr) {
        HalbjahrErgebnis ergebnis = new HalbjahrErgebnis();
        ergebnis.halbjahr = halbjahr;
        ergebnis.anzahlFaecher = 0;
        double summe = 0;

        for (Fach fach : alleFaecher) {
            if (fach.getHalbjahr() == halbjahr) {
                summe += fach.getDurchschnittsPunkte();
                ergebnis.anzahlFaecher++;
            }
        }

        ergebnis.durchschnitt = (ergebnis.anzahlFaecher > 0) ? summe / ergebnis.anzahlFaecher : 0;
        return ergebnis;
    }
}