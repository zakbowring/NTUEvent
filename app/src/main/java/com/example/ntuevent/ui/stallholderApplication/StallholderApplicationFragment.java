package com.example.ntuevent.ui.stallholderApplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.ntuevent.R;
import com.example.ntuevent.ui.stallholderApplication.StallholderApplicationViewModel;

public class StallholderApplicationFragment extends Fragment {

    private StallholderApplicationViewModel stallholderApplicationViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        stallholderApplicationViewModel =
                ViewModelProviders.of(this).get(StallholderApplicationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_stallholder_application, container, false);
        final TextView textView = root.findViewById(R.id.text_stallholder_application);
        stallholderApplicationViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}
