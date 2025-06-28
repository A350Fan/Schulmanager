package com.example.schulmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.schulmanager.fragments.NotenmanagerFragment;
import com.example.schulmanager.fragments.StundenplanFragment;
import com.example.schulmanager.fragments.PruefungenFragment;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Statusleiste transparent machen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        setContentView(R.layout.activity_main);

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

//        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
//        adapter.addFragment(new NotenmanagerFragment(), "Noten");
//        adapter.addFragment(new StundenplanFragment(), "Stundenplan");
//        adapter.addFragment(new PruefungenFragment(), "Prüfungen");
//
//        viewPager.setAdapter(adapter);
//        new TabLayoutMediator(tabLayout, viewPager,
//                (tab, position) -> tab.setText(adapter.getPageTitle(position))
//        ).attach();

        // Adapter mit FragmentStateAdapter korrigieren
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch(position) {
                    case 0: return new NotenmanagerFragment();
                    case 1: return new StundenplanFragment();
                    case 2: return new PruefungenFragment();
                    default: return new Fragment();
                }
            }
            @Override
            public int getItemCount() {
                return 3;
            }
        });

        // TabLayout mit ViewPager verbinden
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch(position) {
                case 0: tab.setText("Noten"); break;
                case 1: tab.setText("Stundenplan"); break;
                case 2: tab.setText("Prüfungen"); break;
            }
        }).attach();
    }


    public static class ViewPagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();

        public ViewPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}
