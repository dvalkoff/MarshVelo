package ru.valkov.trackerapp.ui.viewmodels;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ru.valkov.trackerapp.repositories.MainRepository;

@HiltViewModel
public class BluetoothViewModel extends ViewModel {
    private MainRepository mainRepository;

    @Inject
    public BluetoothViewModel(MainRepository mainRepository)
    {
        this.mainRepository = mainRepository;
    }
}
