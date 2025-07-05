// utils/BerechnungUtil.java
package com.example.schulmanager.utils;

import com.example.schulmanager.models.Fach;
//import com.example.schulmanager.models.Note;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale; // Für String.format

public class BerechnungUtil {

    // Abitur Noten- und Punkte-Tabelle (Referenz aus Oberstufe / G9 in Bayern, kann variieren)
    // {Gesamtpunkte min, Gesamtpunkte max, Notenwert (z.B. 40 für 4.0)}
    // WICHTIG: Die Reihenfolge ist entscheidend (absteigend nach Punkten)
    private static final int[][] PUNKTE_TABELLE = {
            {900, 823, 10},
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
            // Nicht bestanden unter 300 siehe unten
    };

    public static class AbiErgebnis {
        public int halbjahresPunkte;
        public int pruefungsPunkte;
        public int gesamtPunkte;
        public String abiSchnitt;
    }

    /**
     * Berechnet den Abischnitt basierend auf Halbjahresleistungen und Prüfungsnoten.
     *
     * @param faecher Eine Liste aller Fächer.
     * @param pruefungsNoten Ein Array der 5 Abiturprüfungsnoten (0-15 Punkte).
     * @return Ein AbiErgebnis-Objekt mit den berechneten Punkten und dem Schnitt.
     */
    public static AbiErgebnis berechneAbi(List<Fach> faecher, int[] pruefungsNoten) {
        AbiErgebnis ergebnis = new AbiErgebnis();

        // 1. Halbjahresleistungen berechnen (max. 600 Punkte)
        List<Integer> halbjahresLeistungen = new ArrayList<>();
        for (Fach fach : faecher) {
            // WICHTIG: fach.getDurchschnittsPunkte() liefert nun den gerundeten Durchschnitt
            // der Notenliste des Fachs.
            halbjahresLeistungen.add(fach.getDurchschnittsPunkte());
        }

        // Sortieren der Leistungen absteigend, um die besten 40 zu finden
        halbjahresLeistungen.sort(Collections.reverseOrder());

        // Berechnung der besten 40 Leistungen
        ergebnis.halbjahresPunkte = berechneHalbjahresPunkte(halbjahresLeistungen);

        // 2. Prüfungsleistungen berechnen (5 Prüfungen à max. 60 Punkte, gesamt max. 300 Punkte)
        ergebnis.pruefungsPunkte = 0;
        for (int note : pruefungsNoten) {
            // Jede Prüfungsnote (0-15 Punkte) wird * 4 multipliziert (max. 60 Punkte pro Prüfung)
            ergebnis.pruefungsPunkte += Math.min(60, note * 4);
        }
        // Gesamtsumme der Prüfungsleistungen auf maximal 300 begrenzen
        ergebnis.pruefungsPunkte = Math.min(300, ergebnis.pruefungsPunkte);

        // 3. Gesamtpunkte berechnen (Halbjahresleistungen + Prüfungsleistungen)
        ergebnis.gesamtPunkte = ergebnis.halbjahresPunkte + ergebnis.pruefungsPunkte;

        // 4. Abischnitt ermitteln
        ergebnis.abiSchnitt = punkteZuNote(ergebnis.gesamtPunkte);

        return ergebnis;
    }

    /**
     * Berechnet die Punkte für die Halbjahresleistungen (beste 40 Leistungen).
     * Falls weniger als 40 Leistungen vorhanden sind, wird hochgerechnet.
     * @param leistungen Liste der Halbjahresleistungen (0-15 Punkte).
     * @return Gesamtpunkte für die Halbjahresleistungen (max. 600).
     */
    private static int berechneHalbjahresPunkte(List<Integer> leistungen) {
        if (leistungen.isEmpty()) return 0;

        int summe = 0;
        // Wähle die Anzahl der zu berücksichtigenden Leistungen (max. 40)
        int anzahlDerLeistungen = Math.min(40, leistungen.size());

        // Summiere die besten Leistungen
        for (int i = 0; i < anzahlDerLeistungen; i++) {
            summe += leistungen.get(i);
        }

        // Falls weniger als 40 Leistungen vorhanden sind, wird der Durchschnitt
        // auf 40 Leistungen hochgerechnet.
        if (anzahlDerLeistungen < 40) {
            double durchschnitt = (double)summe / anzahlDerLeistungen;
            summe = (int) Math.round(durchschnitt * 40); // Aufrunden auf ganze Punkte
        }

        // Begrenze die Gesamtpunkte auf maximal 600
        return Math.min(600, summe);
    }

    /**
     * Wandelt die Gesamtpunktzahl des Abiturs in eine Noten-String (z.B. "2,5") um.
     * Basierend auf der PUNKTE_TABELLE.
     * @param gesamtPunkte Die erreichte Gesamtpunktzahl.
     * @return Der Abischnitt als String.
     */
    private static String punkteZuNote(int gesamtPunkte) {
        for (int[] eintrag : PUNKTE_TABELLE) {
            if (gesamtPunkte <= eintrag[0] && gesamtPunkte >= eintrag[1]) {
                return noteToString(eintrag[2]);
            }
            if(gesamtPunkte < 300)
            {
                return "Nicht bestanden";
            }
        }
        // Fallback, falls Punkte außerhalb der Tabelle liegen (sollte nicht passieren bei 0-900)
        return "4,0"; // Standardmäßig 4,0 bei Nichtbestehen oder Ungültigkeit
    }

    /**
     * Wandelt einen Noten-Integer (z.B. 10 für 1,0, 25 für 2,5) in einen String um.
     * @param noteWert Der Notenwert als Integer.
     * @return Die Note als formatierter String (z.B. "1,0").
     */
    private static String noteToString(int noteWert) {
        int ganzzahl = noteWert / 10;
        int nachkomma = noteWert % 10;
        return String.format(Locale.GERMAN, "%d,%d", ganzzahl, nachkomma);
    }

    public static class HalbjahrErgebnis {
        public int halbjahr;
        public double durchschnitt;
        public int anzahlFaecher;
    }

    /**
     * Berechnet den Durchschnitt aller Fächer eines bestimmten Halbjahres.
     * @param alleFaecher Eine Liste aller Fächer.
     * @param halbjahr Das Halbjahr, für das der Durchschnitt berechnet werden soll.
     * @return Ein HalbjahrErgebnis-Objekt mit dem Durchschnitt und der Anzahl der Fächer.
     */
    public static HalbjahrErgebnis berechneHalbjahrSchnitt(List<Fach> alleFaecher, int halbjahr) {
        HalbjahrErgebnis ergebnis = new HalbjahrErgebnis();
        ergebnis.halbjahr = halbjahr;
        ergebnis.anzahlFaecher = 0;
        double summe = 0;

        for (Fach fach : alleFaecher) {
            if (fach.getHalbjahr() == halbjahr) {
                // Auch hier wird fach.getDurchschnittsPunkte() verwendet,
                // das intern die Notenliste berücksichtigt.
                summe += fach.getDurchschnittsPunkte();
                ergebnis.anzahlFaecher++;
            }
        }

        ergebnis.durchschnitt = (ergebnis.anzahlFaecher > 0) ? summe / ergebnis.anzahlFaecher : 0;
        return ergebnis;
    }
}