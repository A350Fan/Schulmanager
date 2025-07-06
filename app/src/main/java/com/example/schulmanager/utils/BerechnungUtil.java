// utils/BerechnungUtil.java

package com.example.schulmanager.utils;

import com.example.schulmanager.models.Fach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Eine Dienstprogrammklasse zur Berechnung von Abitur- und Halbjahresdurchschnitten.
 * Diese Klasse enthält statische Methoden, die die komplexe Logik für die Punkteberechnung
 * und die Ermittlung des Bestehensstatus im Abitur kapseln.
 */
public class BerechnungUtil {

    /**
     * Eine private Konstante, die die Punktetabelle für die Umrechnung von Gesamtpunkten
     * in eine Abiturnote (Abiturschnitt) speichert.
     * Jede innere Array repräsentiert eine Zeile: {obere_punktgrenze, untere_punktgrenze, notenwert_multipliziert_mit_10}.
     * Beispiel: {900, 823, 10} bedeutet, dass 823-900 Punkte einer Note von 1,0 entsprechen (10/10).
     */
    private static final int[][] PUNKTE_TABELLE = {
            {900, 823, 10}, // 1,0
            {822, 805, 11}, // 1,1
            {804, 787, 12}, // 1,2
            {786, 769, 13}, // 1,3
            {768, 751, 14}, // 1,4
            {750, 733, 15}, // 1,5
            {732, 715, 16}, // 1,6
            {714, 697, 17}, // 1,7
            {696, 679, 18}, // 1,8
            {678, 661, 19}, // 1,9
            {660, 643, 20}, // 2,0
            {642, 625, 21}, // 2,1
            {624, 607, 22}, // 2,2
            {606, 589, 23}, // 2,3
            {588, 571, 24}, // 2,4
            {570, 553, 25}, // 2,5
            {552, 535, 26}, // 2,6
            {534, 517, 27}, // 2,7
            {516, 499, 28}, // 2,8
            {498, 481, 29}, // 2,9
            {480, 463, 30}, // 3,0
            {462, 445, 31}, // 3,1
            {444, 427, 32}, // 3,2
            {426, 409, 33}, // 3,3
            {408, 391, 34}, // 3,4
            {390, 373, 35}, // 3,5
            {372, 355, 36}, // 3,6
            {354, 337, 37}, // 3,7
            {336, 319, 38}, // 3,8
            {318, 301, 39}, // 3,9
            {300, 300, 40}  // 4,0 (Mindestpunktzahl für Bestehen)
    };

    /**
     * Eine innere statische Klasse, die die Ergebnisse der Abiturberechnung kapselt.
     * Enthält alle relevanten Informationen nach einer Abitur-Gesamtberechnung.
     */
    public static class AbiErgebnis {
        /** Gesamtpunktzahl aus den Halbjahresleistungen (maximal 600). */
        public int halbjahresPunkte;
        /** Gesamtpunktzahl aus den 5 Abiturprüfungen (maximal 300). */
        public int pruefungsPunkte;
        /** Die Summe aus Halbjahres- und Prüfungsleistungen (maximal 900). */
        public int gesamtPunkte;
        /** Der berechnete Abiturschnitt als String (z.B. "1,0" oder "4,0"). */
        public String abiSchnitt;
        /** Boolean-Wert, der angibt, ob das Abitur bestanden wurde. */
        public boolean bestanden;
        /** Eine Nachricht, die den Bestehensstatus detailliert beschreibt (z.B. "Herzlichen Glückwunsch!"). */
        public String bestandenNachricht;
    }

    /**
     * Berechnet den Abischnitt basierend auf Halbjahresleistungen und Prüfungsnoten.
     * Diese Methode berücksichtigt die Regeln für die Einbringung von Leistungen,
     * die Berechnung der Gesamtpunktzahl und die Kriterien für das Bestehen,
     * einschließlich der maximalen Anzahl von Unterpunktungen.
     *
     * @param faecher Eine Liste aller Fächer-Objekte, die die Halbjahresleistungen enthalten.
     * @param pruefungsNoten Ein Array der 5 Abiturprüfungsnoten (Punkte von 0-15).
     * @return Ein {@link AbiErgebnis}-Objekt mit allen berechneten Punkten, dem Schnitt und dem Bestehensstatus.
     */
    public static AbiErgebnis berechneAbi(List<Fach> faecher, int[] pruefungsNoten) {
        AbiErgebnis ergebnis = new AbiErgebnis();

        // 1. Halbjahresleistungen sammeln und absteigend sortieren, um die besten Leistungen zu identifizieren.
        List<Integer> halbjahresLeistungen = new ArrayList<>();
        for (Fach fach : faecher) {
            halbjahresLeistungen.add(fach.getDurchschnittsPunkte());
        }
        halbjahresLeistungen.sort(Collections.reverseOrder()); // Sortiert von höchsten zu niedrigsten Punkten

        // NEU: Anzahl der Unterpunktungen in den relevanten Halbjahresleistungen zählen.
        // Dies ist ein wichtiges Kriterium für das Bestehen des Abiturs.
        int unterpunktungenCount = 0;
        // Es werden maximal 40 Halbjahresleistungen in die Abiturwertung eingebracht.
        int anzahlRelevanterLeistungen = Math.min(40, halbjahresLeistungen.size());

        for (int i = 0; i < anzahlRelevanterLeistungen; i++) {
            // Eine Leistung ist eine Unterpunktung, wenn sie 4 Punkte oder weniger beträgt.
            if (halbjahresLeistungen.get(i) <= 4) {
                unterpunktungenCount++;
            }
        }

        // 2. Halbjahresleistungen Punkte berechnen (max. 600 Punkte).
        // Die Logik für die genaue Berechnung ist in der privaten Methode berechneHalbjahresPunkte gekapselt.
        ergebnis.halbjahresPunkte = berechneHalbjahresPunkte(halbjahresLeistungen);

        // 3. Prüfungsleistungen berechnen (5 Prüfungen à max. 60 Punkte, gesamt max. 300 Punkte).
        ergebnis.pruefungsPunkte = 0;
        for (int note : pruefungsNoten) {
            // Jede Prüfungsnote wird mit 4 multipliziert (entspricht der Umrechnung von 15 Punkten in 60 Punkte).
            // Math.min(60, ...) stellt sicher, dass die maximale Punktzahl pro Prüfung nicht überschritten wird.
            ergebnis.pruefungsPunkte += Math.min(60, note * 4);
        }
        // Math.min(300, ...) stellt sicher, dass die maximale Gesamtpunktzahl der Prüfungen nicht überschritten wird.
        ergebnis.pruefungsPunkte = Math.min(300, ergebnis.pruefungsPunkte);

        // 4. Gesamtpunkte berechnen (Halbjahresleistungen + Prüfungsleistungen).
        ergebnis.gesamtPunkte = ergebnis.halbjahresPunkte + ergebnis.pruefungsPunkte;

        // 5. Abischnitt ermitteln.
        // Auch wenn das Abitur nicht bestanden ist, wird eine 4,0 zurückgegeben.
        ergebnis.abiSchnitt = punkteZuNoteGesamt(ergebnis.gesamtPunkte);

        // 6. Bestehensstatus festlegen. Die Reihenfolge der Prüfungen ist hier wichtig,
        // da die Unterpunktungen eine harte Ausschlussregel sein können.

        // Zuerst die Unterpunktungen prüfen. Sind es mehr als 8, ist das Abitur nicht bestanden.
        if (unterpunktungenCount > 8) {
            ergebnis.bestanden = false;
            ergebnis.bestandenNachricht = String.format(Locale.GERMAN,
                    "Leider nicht bestanden. Es gibt %d Unterpunktungen (< 5 Punkte) in den 40 Halbjahresleistungen (erlaubt: max. 8).",
                    unterpunktungenCount);
        }
        // Dann die Gesamtpunktzahl prüfen. Sind es weniger als 300 Punkte, ist das Abitur nicht bestanden.
        else if (ergebnis.gesamtPunkte < 300) {
            ergebnis.bestanden = false;
            ergebnis.bestandenNachricht = "Leider nicht bestanden. Die Gesamtpunktzahl ist zu gering (mind. 300 Punkte benötigt).";
        }
        // Wenn keine der oben genannten Bedingungen zutrifft, ist das Abitur bestanden.
        else {
            ergebnis.bestanden = true;
            ergebnis.bestandenNachricht = "Herzlichen Glückwunsch! Abitur bestanden!";
        }

        return ergebnis;
    }

    /**
     * Berechnet die Punkte für die Halbjahresleistungen.
     * Es werden die besten bis zu 40 Leistungen berücksichtigt.
     * Falls weniger als 40 Leistungen vorhanden sind, wird der Durchschnitt der vorhandenen Leistungen
     * auf 40 Leistungen hochgerechnet, um eine vergleichbare Basis zu schaffen.
     *
     * @param leistungen Eine Liste der Halbjahresleistungen (bereits absteigend sortiert, Punkte von 0-15).
     * @return Die Gesamtpunktzahl für die Halbjahresleistungen (maximal 600 Punkte).
     */
    private static int berechneHalbjahresPunkte(List<Integer> leistungen) {
        if (leistungen.isEmpty()) return 0; // Wenn keine Leistungen vorhanden sind, ist die Punktzahl 0.

        int summe = 0;
        // Die Anzahl der tatsächlich einzubeziehenden Leistungen (maximal 40 oder weniger, falls nicht so viele vorhanden).
        int anzahlDerLeistungen = Math.min(40, leistungen.size());

        // Nur die besten 'anzahlDerLeistungen' (maximal 40) werden summiert.
        for (int i = 0; i < anzahlDerLeistungen; i++) {
            summe += leistungen.get(i);
        }

        // Hochrechnung: Wenn weniger als 40 Leistungen eingebracht wurden, wird der Durchschnitt
        // auf die Basis von 40 Leistungen skaliert. Dies stellt sicher, dass die Punktzahl
        // vergleichbar ist, auch wenn nicht alle 40 "Slots" belegt sind.
        if (anzahlDerLeistungen < 40 && anzahlDerLeistungen > 0) { // anzahlDerLeistungen > 0, um Division durch Null zu vermeiden
            double durchschnitt = (double) summe / anzahlDerLeistungen;
            summe = (int) Math.round(durchschnitt * 40);
        }

        // Stellt sicher, dass die Gesamtpunktzahl für Halbjahresleistungen 600 nicht überschreitet.
        return Math.min(600, summe);
    }

    /**
     * Wandelt die Gesamtpunktzahl des Abiturs in eine Noten-String (z.B. "2,5") um.
     * Diese Methode verwendet die {@link #PUNKTE_TABELLE}, um die entsprechende Note zu finden.
     * Wenn die Gesamtpunktzahl unter 300 liegt (nicht bestanden), wird "4,0" zurückgegeben.
     *
     * @param gesamtPunkte Die erreichte Gesamtpunktzahl im Abitur.
     * @return Der Abischnitt als String (z.B. "1,0" bis "4,0").
     */
    private static String punkteZuNoteGesamt(int gesamtPunkte) {
        // Wenn die Gesamtpunktzahl unter dem Minimum zum Bestehen liegt, ist die Note 4,0.
        if (gesamtPunkte < 300) {
            return "4,0";
        }

        // Durchläuft die Punktetabelle, um den passenden Notenbereich zu finden.
        for (int[] eintrag : PUNKTE_TABELLE) {
            if (gesamtPunkte <= eintrag[0] && gesamtPunkte >= eintrag[1]) {
                // Konvertiert den gespeicherten Notenwert (multipliziert mit 10) in einen formatierten String.
                return noteToString(eintrag[2]);
            }
        }
        // Fallback: Sollte bei einer korrekten und vollständigen Tabelle nicht erreicht werden.
        return "4,0";
    }

    /**
     * Hilfsmethode zur Formatierung eines Notenwerts (als Integer, z.B. 10 für 1,0) in einen String
     * mit Dezimaltrennzeichen (z.B. "1,0" unter Verwendung des deutschen Locales).
     *
     * @param noteWert Der Notenwert, multipliziert mit 10 (z.B. 10 für 1,0; 25 für 2,5).
     * @return Der formatierte Noten-String.
     */
    private static String noteToString(int noteWert) {
        int ganzzahl = noteWert / 10;
        int nachkomma = noteWert % 10;
        return String.format(Locale.GERMAN, "%d,%d", ganzzahl, nachkomma);
    }

    /**
     * Wandelt eine einzelne Punktzahl (0-15 Punkte) in einen Notenwert (1.0-6.0) um.
     * Diese Umrechnung wird oft für einzelne Leistungsnachweise oder Prüfungen verwendet.
     * Die Formel ist in der Regel Note = 6 - (Punkte / 3).
     *
     * @param punkte Die Punktzahl (0-15), die umgerechnet werden soll.
     * @return Die umgerechnete Note als double. Die Werte sind auf 1.0 (beste) und 6.0 (schlechteste) begrenzt.
     */
    public static double punkteZuNoteEinzelwert(double punkte) {
        double note = 6.0 - (punkte / 3.0);
        // Stellt sicher, dass die Note im gültigen Bereich von 1.0 bis 6.0 liegt.
        return Math.max(1.0, Math.min(6.0, note));
    }

    /**
     * Eine innere statische Klasse, die die Ergebnisse der Halbjahresdurchschnittsberechnung kapselt.
     */
    public static class HalbjahrErgebnis {
        /** Das Halbjahr, für das der Durchschnitt berechnet wurde. */
        public int halbjahr;
        /** Der berechnete Durchschnitt der Punkte für das spezifische Halbjahr. */
        public double durchschnitt;
        /** Die Anzahl der Fächer, die in die Berechnung dieses Halbjahresdurchschnitts eingeflossen sind. */
        public int anzahlFaecher;
    }

    /**
     * Berechnet den Punktdurchschnitt für ein bestimmtes Halbjahr.
     * Es werden alle Fächer berücksichtigt, die dem angegebenen Halbjahr zugeordnet sind.
     *
     * @param alleFaecher Eine Liste aller Fächer, aus denen der Halbjahresdurchschnitt berechnet werden soll.
     * @param halbjahr Die Nummer des Halbjahres (z.B. 1, 2, 3, 4), für das der Durchschnitt berechnet werden soll.
     * @return Ein {@link HalbjahrErgebnis}-Objekt mit dem Durchschnitt, dem Halbjahr und der Anzahl der Fächer.
     */
    public static HalbjahrErgebnis berechneHalbjahrSchnitt(List<Fach> alleFaecher, int halbjahr) {
        HalbjahrErgebnis ergebnis = new HalbjahrErgebnis();
        ergebnis.halbjahr = halbjahr;
        ergebnis.anzahlFaecher = 0;
        double summe = 0;

        // Durchläuft alle Fächer und summiert die Punkte der Fächer, die zum gesuchten Halbjahr gehören.
        for (Fach fach : alleFaecher) {
            if (fach.getHalbjahr() == halbjahr) {
                summe += fach.getDurchschnittsPunkte();
                ergebnis.anzahlFaecher++;
            }
        }

        // Berechnet den Durchschnitt, wenn Fächer vorhanden sind, ansonsten ist der Durchschnitt 0.
        ergebnis.durchschnitt = (ergebnis.anzahlFaecher > 0) ? summe / ergebnis.anzahlFaecher : 0;
        return ergebnis;
    }
}