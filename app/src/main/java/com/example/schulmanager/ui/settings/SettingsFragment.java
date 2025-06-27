package com.example.schulmanager.ui.settings; // Ersetze dies mit deinem tatsächlichen Paketnamen

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Standard TextView
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.schulmanager.R;
import com.google.android.material.materialswitch.MaterialSwitch; // Import für MaterialSwitch

// Annahme: Dein Paketname ist com.example.schulmanager
// Stelle sicher, dass du die R-Klasse korrekt importierst, basierend auf deinem Paketnamen
// import com.example.schulmanager.R; // Beispielhafter Import für die R-Klasse

public class SettingsFragment extends Fragment {

    private MaterialSwitch switchDarkMode;
    private TextView textViewImpressum; // Standard TextView oder MaterialTextView
    private SharedPreferences sharedPreferences;

    // Verwende einen statischen String für den SharedPreferences-Namen und den Dark-Mode-Key
    private static final String PREFS_NAME = "settings_prefs";
    private static final String DARK_MODE_KEY = "dark_mode_enabled";

    public SettingsFragment() {
        // Erforderlicher leerer öffentlicher Konstruktor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        textViewImpressum = view.findViewById(R.id.textViewImpressum);

        // SharedPreferences zum Speichern der Dark Mode Einstellung
        // Verwende requireActivity().getSharedPreferences(...) um sicherzustellen, dass die Activity verfügbar ist
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Aktuellen Dark Mode Status laden und Switch setzen
        boolean isDarkModeEnabled = sharedPreferences.getBoolean(DARK_MODE_KEY, false);
        switchDarkMode.setChecked(isDarkModeEnabled);
        // applyDarkMode(isDarkModeEnabled); // Die Anwendung erfolgt initial durch das Theme der Activity

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(DARK_MODE_KEY, isChecked).apply();
            applyDarkMode(isChecked);
        });

        textViewImpressum.setOnClickListener(v -> {
            // Öffne hier dein Impressum (z.B. eine neue Activity, ein Dialog oder eine Webseite)
            // Beispiel: Öffnen einer Webseite
            String impressumUrl = "https://deine-impressum-url.de"; // Ersetze dies mit deiner URL
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(impressumUrl));
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        return view;
    }

    private void applyDarkMode(boolean isEnabled) {
        if (isEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        // Wichtig:
        // Damit die Änderung des Themes sofort wirksam wird, muss die Activity oft neu erstellt werden.
        // requireActivity().recreate(); // Dies würde die aktuelle Activity neu starten.
        // Alternativ kann die App so konfiguriert werden, dass sie beim nächsten Start das korrekte Theme lädt.
    }

    // Es ist eine gute Praxis, den Dark Mode Status auch in onResume zu überprüfen,
    // falls die Einstellung extern geändert wurde (obwohl in diesem einfachen Fall weniger wahrscheinlich).
    @Override
    public void onResume() {
        super.onResume();
        // Stelle sicher, dass der Switch den korrekten Zustand widerspiegelt,
        // falls die Activity neu erstellt wurde und der gespeicherte Zustand gilt.
        boolean isDarkModeEnabled = sharedPreferences.getBoolean(DARK_MODE_KEY, false);
        if (switchDarkMode.isChecked() != isDarkModeEnabled) {
            // Synchronisiere den Switch, falls nötig, aber das sollte durch die Activity-Neuerstellung
            // und onCreateView bereits korrekt sein. Die Anwendung des Themes selbst erfolgt
            // durch AppCompatDelegate und die Activity-Konfiguration.
        }
    }
}