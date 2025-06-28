package com.example.notenmanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.schulmanager.R;

public class PruefungenFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pruefungen, container, false);
        TextView textView = view.findViewById(R.id.text_temp);
        textView.setText("Prüfungsplanung kommt später");
        return view;
    }
}
