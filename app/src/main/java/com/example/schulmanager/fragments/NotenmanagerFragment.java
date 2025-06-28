package com.example.schulmanager.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schulmanager.R;
import com.example.schulmanager.adapters.FachAdapter;
import com.example.schulmanager.models.Fach;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotenmanagerFragment extends Fragment {

    private List<Fach> faecher = new ArrayList<>();
    private FachAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notenmanager, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FachAdapter(faecher, fach -> showEditDialog(fach));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> showAddDialog());

        loadData();

        return view;
    }

    private void showAddDialog() {
        // Implementierung wie zuvor
    }

    private void showEditDialog(Fach fach) {
        // Implementierung wie zuvor
    }

    private void loadData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("NotenManager", 0);
        String jsonFaecher = prefs.getString("faecher", null);

        if (jsonFaecher != null) {
            Type type = new TypeToken<ArrayList<Fach>>(){}.getType();
            List<Fach> savedFaecher = new Gson().fromJson(jsonFaecher, type);
            faecher.clear();
            faecher.addAll(savedFaecher);
            adapter.notifyDataSetChanged();
        }
    }

    private void saveData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("NotenManager", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("faecher", new Gson().toJson(faecher));
        editor.apply();
    }
}
