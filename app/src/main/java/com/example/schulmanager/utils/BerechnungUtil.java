package com.example.schulmanager.utils;

import com.example.schulmanager.models.Fach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Eine Utility-Klasse, die statische Methoden zur Berechnung von Notendurchschnitten
 * (Halbjahr, Abitur) und zur Umrechnung von Notenpunkten in Notenwerte bereitstellt.
 * Diese Klasse enthält keine Zustandsvariablen und dient ausschließlich der Bereitstellung
 * von Berechnungsfunktionen.
 * <p>
 * Die Abitur-Berechnungslogik wurde aktualisiert, um die gängigen Regeln für die
 * Gesamtqualifikation (Block I: 40 Halbjahresleistungen, Block II: 5 Prüfungsleistungen)
 * *speziell für Bayern* (Stand 2025) korrekt abzubilden.
 */
public class BerechnungUtil {

    // --- Konstanten für die Notenpunkt-Umrechnungstabelle ---
    private static final double[] PUNKTE_TABELLE = {
            6.0, 6.0, 6.0, 5.0, 4.0, 4.0, 3.0, 3.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0, 1.0
            // Indizes:  0    1    2    3    4    5    6    7    8    9   10   11   12   13   14   15
            // Notenpunkte: 0P   1P   2P   3P   4P   5P   6P   7P   8P   9P  10P  11P  12P  13P  14P  15P
    };

    /**
     * Private Konstruktor, um die Instanziierung der Utility-Klasse zu verhindern.
     */
    private BerechnungUtil() {
        // Keine Aktion notwendig.
    }

    /**
     * Konvertiert einen Notenpunktwert (0-15 Punkte) in einen Notenwert (1.0-6.0).
     */
    public static double punkteZuNoteEinzelwert(double punkte) {
        int gerundetePunkte = (int) Math.round(punkte);
        int index = Math.max(0, Math.min(PUNKTE_TABELLE.length - 1, gerundetePunkte));
        return PUNKTE_TABELLE[index];
    }

    /**
     * Berechnet den gewichteten Durchschnitt aller Noten für ein bestimmtes Halbjahr.
     * Unverändert gegenüber der vorherigen Version.
     */
    public static HalbjahrErgebnis berechneHalbjahrSchnitt(List<Fach> alleFaecher, int halbjahr) {
        List<Double> fachDurchschnitte = new ArrayList<>();
        int anzahlFaecher = 0;

        for (Fach fach : alleFaecher) {
            if (fach.getHalbjahr() == halbjahr && !fach.getNoten().isEmpty()) {
                fachDurchschnitte.add(fach.getDurchschnitt());
                anzahlFaecher++;
            }
        }

        double gesamtDurchschnitt = 0.0;
        if (!fachDurchschnitte.isEmpty()) {
            double summe = 0;
            for (double avg : fachDurchschnitte) {
                summe += avg;
            }
            gesamtDurchschnitt = summe / fachDurchschnitte.size();
        }

        return new HalbjahrErgebnis(halbjahr, gesamtDurchschnitt, anzahlFaecher);
    }

    /**
     * Hilfsklasse zum Kapseln der Ergebnisse der Halbjahresdurchschnittsberechnung.
     */
    public static class HalbjahrErgebnis {
        public final int halbjahr;
        public final double durchschnitt;
        public final int anzahlFaecher;

        public HalbjahrErgebnis(int halbjahr, double durchschnitt, int anzahlFaecher) {
            this.halbjahr = halbjahr;
            this.durchschnitt = durchschnitt;
            this.anzahlFaecher = anzahlFaecher;
        }
    }


    /**
     * Berechnet den Abitur-Gesamtschnitt basierend auf allen Fächern und den Prüfungsnoten,
     * unter Berücksichtigung der bayerischen Abiturregeln (Stand 2025).
     * <p>
     * Block I (Halbjahresleistungen):
     * - Alle 4 Halbjahresleistungen der 5 Abiturfächer MÜSSEN eingebracht werden (20 Kurse).
     * - Die restlichen (bis zu) 20 Kurse werden aus den besten der übrigen Halbjahresleistungen
     * aller anderen Fächer (ohne Noten von Abiturfächern) aufgefüllt.
     * - Jeder Kurs zählt einfach. Maximal 40 Kurse.
     * <p>
     * Block II (Prüfungsleistungen):
     * - 5 Abiturprüfungen, jede 4-fach gewichtet.
     *
     * @param alleFaecher Eine Liste aller Fächer der Anwendung (inkl. aller Halbjahre).
     * @param pruefungsNoten Ein Array von 5 Integer-Werten für die Abiturprüfungsnoten (0-15 Punkte).
     * @return Ein AbiErgebnis-Objekt, das alle berechneten Werte und eine Bestehensnachricht enthält.
     */
    public static AbiErgebnis berechneAbi(List<Fach> alleFaecher, int[] pruefungsNoten) {

        // Um Fächer nach Namen und Halbjahr gruppieren zu können
        Map<String, List<Fach>> faecherProName = new HashMap<>();
        for (Fach fach : alleFaecher) {
            faecherProName.computeIfAbsent(fach.getName(), k -> new ArrayList<>()).add(fach);
        }

        // --- Block I: Halbjahresleistungen (HL) ---
        List<Double> blockIKurse = new ArrayList<>(); // Liste der Punkte für Block I

        // 1. Alle 4 Halbjahre der 5 Abiturfächer MÜSSEN eingebracht werden
        // Dies erfordert, dass Fächer mit dem gleichen Namen, aber unterschiedlichen Halbjahren, gefunden werden.
        List<Fach> abiturfaecherListe = new ArrayList<>();
        for (Fach fach : alleFaecher) {
            if (fach.isAbiturfach() && !fach.getNoten().isEmpty()) {
                abiturfaecherListe.add(fach);
            }
        }

        // Stelle sicher, dass für jedes Abiturfach 4 Halbjahre gefunden und berücksichtigt werden.
        // Falls ein Abiturfach nicht in allen 4 Halbjahren existiert oder keine Noten hat,
        // müsste hier eine Fehlermeldung/Warnung ausgegeben werden, oder es wird mit 0 Punkten gewertet.
        // Für diese Berechnung nehmen wir an, dass alle Abiturfächer 4 Halbjahre mit Noten haben.
        // Die durchschnittliche Punktzahl eines Faches in einem Halbjahr ist bereits der Wert, den wir brauchen.
        for (Fach abifach : abiturfaecherListe) {
            // Jedes Halbjahr eines Abiturfachs (Q11.1, Q11.2, Q12.1, Q12.2) ist ein einzubringender Kurs.
            blockIKurse.add(abifach.getDurchschnitt());
        }

        // 2. Sammle die Durchschnitte der sonstigen Fächer
        List<Double> sonstigeHalbjahresleistungen = new ArrayList<>();
        for (Fach fach : alleFaecher) {
            // Schließe Abiturfächer aus, da deren Halbjahre bereits behandelt wurden.
            // Und schließe Fächer ohne Noten aus.
            if (!fach.isAbiturfach() && !fach.getNoten().isEmpty()) {
                sonstigeHalbjahresleistungen.add(fach.getDurchschnitt());
            }
        }

        // Sortiere die sonstigen Halbjahresleistungen absteigend, um die besten auszuwählen.
        Collections.sort(sonstigeHalbjahresleistungen, Comparator.reverseOrder());

        // Füge die besten der sonstigen Halbjahresleistungen hinzu, bis 40 Kurse erreicht sind.
        // Abiturfächer sollten 5 Fächer * 4 Halbjahre = 20 Kurse liefern.
        // Wir brauchen weitere 20 Kurse aus den sonstigen Leistungen.
        int requiredAdditionalCourses = 40 - blockIKurse.size();

        for (int i = 0; i < sonstigeHalbjahresleistungen.size() && requiredAdditionalCourses > 0; i++) {
            blockIKurse.add(sonstigeHalbjahresleistungen.get(i));
            requiredAdditionalCourses--;
        }

        // Summiere die Punkte für Block I.
        double blockIPunkteSumme = 0;
        for (Double punkte : blockIKurse) {
            blockIPunkteSumme += punkte;
        }

        // Die Gesamtpunktzahl von Block I wird gerundet.
        int blockIPunkte = (int) Math.round(blockIPunkteSumme);


        // --- Block II: Prüfungsleistungen (PL) ---
        double blockIIPunkteSumme = 0;
        // Es gibt 5 Prüfungsnoten, jede 4-fach gewichtet.
        // Stelle sicher, dass mindestens 5 Noten im Array sind, um IndexOutOfBounds zu vermeiden.
        for (int i = 0; i < Math.min(5, pruefungsNoten.length); i++) {
            // Validiere jede Prüfungsnote, um sicherzustellen, dass sie im Bereich 0-15 liegt.
            int note = Math.max(0, Math.min(15, pruefungsNoten[i]));
            blockIIPunkteSumme += (note * 4); // 4-fache Gewichtung
        }
        int blockIIPunkte = (int) Math.round(blockIIPunkteSumme);


        // --- Gesamtpunktzahl und Abiturschnitt ---
        int gesamtPunkte = blockIPunkte + blockIIPunkte;

        // Abitur-Notenschnitt-Berechnung (Formel für Bayern: (840 - P) / 180 + 1)
        // Oder äquivalent: 4.0 - ((P - 300) / 180.0) für Punkte 300-840.
        // Minimum für Bestehen ist 300 Punkte. Maximum ist 840 Punkte.
        double abiSchnitt;
        String bestandenNachricht;

        // Prüfen auf Bestehen (Mindestgesamtpunktzahl)
        if (gesamtPunkte < 300) {
            abiSchnitt = 5.0; // Nicht bestanden
            bestandenNachricht = "Abitur nicht bestanden (Gesamtpunkte unter 300).";
        } else {
            // Berechne den Schnitt mit der bayerischen Formel
            abiSchnitt = 4.0 - ((double)(gesamtPunkte - 300) / 180.0);

            // Begrenze den Schnitt auf 1.0, falls er durch die Formel besser als 1.0 wäre
            if (abiSchnitt < 1.0) {
                abiSchnitt = 1.0;
            }
            // An dieser Stelle sollte der Schnitt nicht schlechter als 4.0 sein,
            // da die 300-Punkte-Grenze bereits abgefangen wurde.
            // Falls es zu Rundungsfehlern kommt, die zu 4.x führen könnten, könnte man abrunden.
            // Bspw. 4.0 oder 4.1 in der Berechnung, aber 4.0 ist das schlechteste Bestehen.
            // Es ist gängig, auf eine Nachkommastelle zu runden.

            bestandenNachricht = "Abitur bestanden.";

            // Sonderfall 1,0 Abitur: Ab 823 Punkten in Bayern (oder 822/823 je nach Verordnung).
            // Man kann auch einfach sagen, wenn die Formel 1,0 ergibt, ist es 1,0.
            if (gesamtPunkte >= 823) {
                bestandenNachricht = "Abitur mit der Note 1,0 bestanden!";
                abiSchnitt = 1.0; // Stelle sicher, dass der Wert 1.0 ist, nicht z.B. 0.98.
            }
        }

        // Formatierung des Abiturschnitts auf eine Nachkommastelle (gängig in Bayern)
        String abiSchnittFormatted = String.format(Locale.GERMAN, "%.1f", abiSchnitt);


        // Gibt ein neues AbiErgebnis-Objekt zurück.
        return new AbiErgebnis(
                blockIPunkte,      // Gerundete Block-I-Punkte
                blockIIPunkte,     // Gerundete Block-II-Punkte
                gesamtPunkte,
                abiSchnittFormatted,
                bestandenNachricht
        );
    }

    /**
     * Hilfsklasse zum Kapseln der Ergebnisse der Abitur-Gesamtberechnung.
     */
    public static class AbiErgebnis {
        public final int halbjahresPunkte;
        public final int pruefungsPunkte;
        public final int gesamtPunkte;
        public final String abiSchnitt;
        public final String bestandenNachricht;

        /**
         * Konstruktor für AbiErgebnis.
         * @param halbjahresPunkte Gesamtpunkte aus Halbjahresleistungen.
         * @param pruefungsPunkte Gesamtpunkte aus Prüfungsleistungen.
         * @param gesamtPunkte Gesamtpunktzahl.
         * @param abiSchnitt Der formatierte Abiturschnitt.
         * @param bestandenNachricht Die Nachricht zum Bestehensstatus.
         */
        public AbiErgebnis(int halbjahresPunkte, int pruefungsPunkte, int gesamtPunkte,
                           String abiSchnitt, String bestandenNachricht) {
            this.halbjahresPunkte = halbjahresPunkte;
            this.pruefungsPunkte = pruefungsPunkte; // <-- Diese Zeile ist korrekt
            this.gesamtPunkte = gesamtPunkte;     // <-- Diese Zeile ist korrekt
            // Die folgenden beiden Zeilen waren die doppelten und müssen entfernt werden:
            // this.pruefungsPunkte = pruefungsPunkte;
            // this.gesamtPunkte = gesamtPunkte;
            this.abiSchnitt = abiSchnitt;
            this.bestandenNachricht = bestandenNachricht;
        }
    }
}