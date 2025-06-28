import com.example.schulmanager.models.Fach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BerechnungUtil {

    private static final List<AbiLevel> PUNKTE_TABELLE = Arrays.asList(
            new AbiLevel(900, 823, "1,0"),
            new AbiLevel(822, 805, "1,1"),
            // ... alle Stufen bis
            new AbiLevel(300, 0, "4,0")
    );

    public static class AbiLevel {
        int max;
        int min;
        String note;

        AbiLevel(int max, int min, String note) {
            this.max = max;
            this.min = min;
            this.note = note;
        }
    }

    public static class AbiErgebnis {
        public int beste40;
        public int gesamtPunkte;
        public String abiSchnitt;

        public AbiErgebnis(int beste40, int gesamtPunkte, String abiSchnitt) {
            this.beste40 = beste40;
            this.gesamtPunkte = gesamtPunkte;
            this.abiSchnitt = abiSchnitt;
        }
    }

    public static AbiErgebnis berechneAbi(List<Fach> faecher, List<Integer> pruefungsNoten) {
        // Halbjahresleistungen sammeln
        List<Integer> leistungen = new ArrayList<>();
        for (Fach fach : faecher) {
            leistungen.add(fach.getPunkte());
        }

        // Beste 40 Leistungen (max 600 Punkte)
        Collections.sort(leistungen, Collections.reverseOrder());
        int sum = 0;
        for (int i = 0; i < Math.min(40, leistungen.size()); i++) {
            sum += leistungen.get(i);
        }
        int beste40 = Math.min(sum, 600);

        // Prüfungspunkte (max 300 Punkte)
        int pruefungSum = 0;
        for (Integer punkt : pruefungsNoten) {
            pruefungSum += punkt;
        }
        int pruefungPunkte = Math.min(pruefungSum, 300);

        // Gesamtpunkte
        int gesamt = beste40 + pruefungPunkte;

        // Abischnitt ermitteln
        String abiSchnitt = "4,0";
        for (AbiLevel level : PUNKTE_TABELLE) {
            if (gesamt <= level.max && gesamt >= level.min) {
                abiSchnitt = level.note;
                break;
            }
        }

        return new AbiErgebnis(beste40, gesamt, abiSchnitt);
    }
}
