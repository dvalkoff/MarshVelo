package ru.valkov.trackerapp.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dagger.hilt.android.AndroidEntryPoint;
import ru.valkov.trackerapp.R;
import ru.valkov.trackerapp.ui.viewmodels.MainViewModel;

@AndroidEntryPoint
public class TrackingFragment extends Fragment {

    private MainViewModel viewModel;

    public TrackingFragment() {
        super(R.layout.fragment_tracking);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

}
