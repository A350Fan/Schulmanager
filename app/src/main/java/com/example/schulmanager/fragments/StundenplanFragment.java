package com.example.schulmanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.schulmanager.R;

public class StundenplanFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stundenplan, container, false);
        TextView textView = view.findViewById(R.id.text_temp);
        textView.setText("Stundenplan Funktion kommt später");
        return view;
    }
}
