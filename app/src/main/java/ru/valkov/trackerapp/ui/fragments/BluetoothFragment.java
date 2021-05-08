package ru.valkov.trackerapp.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dagger.hilt.android.AndroidEntryPoint;
import ru.valkov.trackerapp.R;
import ru.valkov.trackerapp.ui.viewmodels.BluetoothViewModel;
import ru.valkov.trackerapp.ui.viewmodels.StatisticsViewModel;

@AndroidEntryPoint
public class BluetoothFragment extends Fragment {

    private BluetoothViewModel viewModel;
    // TODO: Singleton
    public BluetoothFragment() {
        super(R.layout.fragment_bluetooth);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(BluetoothViewModel.class);
    }
}