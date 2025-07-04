package com.example.schulmanager.utils;

import com.example.schulmanager.models.Fach;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BerechnungUtil {

    // Punkte-Tabelle für die Abiturnote (Punktebereich → Note)
    private static final int[][] PUNKTE_TABELLE = {
            {900, 823, 10}, {822, 805, 11}, {804, 787, 12}, {786, 769, 13},
            {768, 751, 14}, {750, 733, 15}, {732, 715, 16}, {714, 697, 17},
            {696, 679, 18}, {678, 661, 19}, {660, 643, 20}, {642, 625, 21},
            {624, 607, 22}, {606, 589, 23}, {588, 571, 24}, {570, 553, 25},
            {552, 535, 26}, {534, 517, 27}, {516, 499, 28}, {498, 481, 29},
            {480, 463, 30}, {462, 445, 31}, {444, 427, 32}, {426, 409, 33},
            {408, 391, 34}, {390, 373, 35}, {372, 355, 36}, {354, 337, 37},
            {336, 319, 38}, {318, 301, 39}, {300, 0, 40}
    };

    public static class AbiErgebnis {
        public int halbjahresPunkte;
        public int pruefungsPunkte;
        public int gesamtPunkte;
        public String abiSchnitt;
    }

    public static AbiErgebnis berechneAbi(List<Fach> faecher, int[] pruefungsNoten) {
        AbiErgebnis ergebnis = new AbiErgebnis();

        // 1. Halbjahresleistungen berechnen
        List<Integer> halbjahresLeistungen = new ArrayList<>();
        for (Fach fach : faecher) {
            halbjahresLeistungen.add(fach.getDurchschnittsPunkte());
        }

        // Sortieren (beste zuerst)
        Collections.sort(halbjahresLeistungen, Collections.reverseOrder());

        // Beste 40 Leistungen (ggf. hochrechnen)
        ergebnis.halbjahresPunkte = berechneHalbjahresPunkte(halbjahresLeistungen);

        // 2. Prüfungsleistungen (5 Prüfungen à max. 60 Punkte)
        ergebnis.pruefungsPunkte = 0;
        for (int note : pruefungsNoten) {
            ergebnis.pruefungsPunkte += Math.min(60, note * 4); // 15 Punkte * 4 = 60
        }
        ergebnis.pruefungsPunkte = Math.min(300, ergebnis.pruefungsPunkte); // Max. 300

        // 3. Gesamtpunkte
        ergebnis.gesamtPunkte = ergebnis.halbjahresPunkte + ergebnis.pruefungsPunkte;

        // 4. Abischnitt ermitteln
        ergebnis.abiSchnitt = punkteZuNote(ergebnis.gesamtPunkte);

        return ergebnis;
    }

    private static int berechneHalbjahresPunkte(List<Integer> leistungen) {
        if (leistungen.isEmpty()) return 0;

        int summe = 0;
        int anzahl = Math.min(40, leistungen.size());

        // Beste Leistungen summieren
        for (int i = 0; i < anzahl; i++) {
            summe += leistungen.get(i);
        }

        // Falls weniger als 40: Hochrechnen
        if (anzahl < 40) {
            double durchschnitt = (double)summe / anzahl;
            summe = (int)(durchschnitt * 40);
        }

        return Math.min(600, summe); // Max. 600 Punkte
    }

    private static String punkteZuNote(int punkte) {
        for (int[] eintrag : PUNKTE_TABELLE) {
            if (punkte <= eintrag[0] && punkte >= eintrag[1]) {
                return noteToString(eintrag[2]);
            }
        }
        return "4,0";
    }

    private static String noteToString(int noteWert) {
        int ganzzahl = noteWert / 10;
        int nachkomma = noteWert % 10;
        return ganzzahl + "," + nachkomma;
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
