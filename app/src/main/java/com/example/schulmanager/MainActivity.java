package com.example.schulmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.schulmanager.fragments.NotenmanagerFragment;
import com.example.schulmanager.fragments.StundenplanFragment;
import com.example.schulmanager.fragments.KalenderFragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Hauptaktivität der Schulmanager-App.
 * Diese Aktivität ist für die Einrichtung der Benutzeroberfläche zuständig,
 * insbesondere für die Navigation zwischen verschiedenen App-Bereichen
 * mittels eines ViewPagers und eines TabLayouts.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- Statusleiste transparent machen ---
        // Setzt die Farbe der Statusleiste auf Transparent.
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        // Konfiguriert die System-UI-Sichtbarkeit, damit der Inhalt der App
        // den Bereich der Statusleiste und Navigationsleiste ausfüllen kann.
        // SYSTEM_UI_FLAG_LAYOUT_STABLE sorgt dafür, dass die Layout-Größe stabil bleibt,
        // auch wenn sich die Systemleisten ändern.
        // SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN lässt das Layout bis unter die Statusleiste reichen.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        // --- Ende transparente Statusleiste ---

        // Setzt das Layout für diese Aktivität.
        setContentView(R.layout.activity_main);

        // Referenzen zu den UI-Elementen aus dem Layout abrufen.
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        // Die auskommentierten Zeilen zeigen eine frühere Implementierung mit einer separaten
        // ViewPagerAdapter-Klasse. Die aktuelle Implementierung verwendet einen Inline-Adapter
        // für den ViewPager2.
//        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
//        adapter.addFragment(new NotenmanagerFragment(), "Noten");
//        adapter.addFragment(new StundenplanFragment(), "Stundenplan");
//        adapter.addFragment(new KalenderFragment(), "Prüfungen");
//
//        viewPager.setAdapter(adapter);
//        new TabLayoutMediator(tabLayout, viewPager,
//                (tab, position) -> tab.setText(adapter.getPageTitle(position))
//        ).attach();

        // --- Adapter für ViewPager2 konfigurieren ---
        // Hier wird ein anonymer Inner-Class-Adapter vom Typ FragmentStateAdapter erstellt.
        // Dieser Adapter ist optimal für eine große Anzahl von Fragmenten, da er Fragmente,
        // die nicht sichtbar sind, zerstören kann, um Speicher zu sparen.
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            /**
             * Erstellt und gibt das Fragment für die gegebene Position zurück.
             * @param position Die Position des Fragments im ViewPager.
             * @return Das Fragment, das an dieser Position angezeigt werden soll.
             */
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                // Je nach Position wird das entsprechende Fragment zurückgegeben.
                switch (position) {
                    case 0:
                        return new NotenmanagerFragment(); // Erstes Tab: Notenmanager
                    case 1:
                        return new StundenplanFragment();  // Zweites Tab: Stundenplan
                    case 2:
                        return new KalenderFragment();   // Drittes Tab: Prüfungen
                    default:
                        return new Fragment();            // Fallback für unbekannte Positionen
                }
            }

            /**
             * Gibt die Gesamtzahl der Elemente (Fragmente) im Adapter zurück.
             * @return Die Anzahl der Fragmente.
             */
            @Override
            public int getItemCount() {
                return 3; // Es gibt 3 Tabs/Fragmente in dieser App.
            }
        });
        // --- Ende Adapter Konfiguration ---

        // --- TabLayout mit ViewPager2 verbinden ---
        // Der TabLayoutMediator ist dafür zuständig, die Tabs im TabLayout mit den Seiten
        // im ViewPager2 zu synchronisieren.
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Hier wird der Text für jeden Tab basierend auf seiner Position gesetzt.
            switch (position) {
                case 0:
                    tab.setText(R.string.fragment1);
                    break;
                case 1:
                    tab.setText(R.string.fragment2);
                    break;
                case 2:
                    tab.setText(R.string.fragment3);
                    break;
            }
        }).attach(); // Wichtig: attach() muss aufgerufen werden, um die Verbindung herzustellen.
        // --- Ende TabLayout Verbindung ---
    }

    /**
     * Diese statische innere Klasse ist ein Beispiel für einen {@link FragmentStateAdapter},
     * der eine Liste von Fragmenten und deren Titel verwaltet.
     * Obwohl in der aktuellen onCreate-Methode ein anonymer Adapter verwendet wird,
     * könnte diese Klasse verwendet werden, um den Adapter in eine separate, wiederverwendbare Klasse
     * auszulagern.
     * <p>
     * In der aktuellen Implementierung der MainActivity wird dieser Adapter nicht direkt genutzt,
     * da ein anonymer FragmentStateAdapter direkt im onCreate eingerichtet wurde.
     */
    public static class ViewPagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragments = new ArrayList<>(); // Liste der Fragments
        private final List<String> fragmentTitles = new ArrayList<>(); // Liste der Titel für die Fragments

        /**
         * Konstruktor für den ViewPagerAdapter.
         *
         * @param fa Die FragmentActivity, die diesen Adapter hostet.
         */
        public ViewPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        /**
         * Fügt dem Adapter ein neues Fragment und dessen Titel hinzu.
         *
         * @param fragment Das hinzuzufügende Fragment.
         * @param title    Der Titel des Fragments, der im TabLayout angezeigt wird.
         */
        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        /**
         * Gibt den Titel des Fragments an der angegebenen Position zurück.
         * Diese Methode wird vom TabLayoutMediator verwendet, um den Tab-Text zu setzen.
         *
         * @param position Die Position des Fragments.
         * @return Der Titel des Fragments.
         */
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }

        /**
         * Erstellt und gibt das Fragment an der angegebenen Position zurück.
         *
         * @param position Die Position des Fragments.
         * @return Das Fragment.
         */
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        /**
         * Gibt die Gesamtzahl der Elemente (Fragmente) im Adapter zurück.
         *
         * @return Die Anzahl der Fragmente.
         */
        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}