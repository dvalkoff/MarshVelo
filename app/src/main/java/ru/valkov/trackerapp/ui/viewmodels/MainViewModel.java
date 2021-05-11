package ru.valkov.trackerapp.ui.viewmodels;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ru.valkov.trackerapp.database.Ride;
import ru.valkov.trackerapp.repositories.MainRepository;
import ru.valkov.trackerapp.ui.fragments.TrackingFragment;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private MainRepository mainRepository;

    @Inject
    public MainViewModel(MainRepository mainRepository) {
        this.mainRepository = mainRepository;
    }

    public void insertRide(Ride ride) {
        mainRepository.insertRide(ride);
    }

}
