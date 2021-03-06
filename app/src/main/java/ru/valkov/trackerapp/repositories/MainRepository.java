package ru.valkov.trackerapp.repositories;

import androidx.lifecycle.LiveData;

import java.util.List;

import javax.inject.Inject;

import ru.valkov.trackerapp.database.Ride;
import ru.valkov.trackerapp.database.RideDAO;

public class MainRepository
{

    private RideDAO rideDAO;

    @Inject
    public MainRepository(RideDAO rideDAO) {
        this.rideDAO = rideDAO;
    }

    public void insertRide(Ride ride) {
        this.rideDAO.insertRide(ride);
    }

    public void deleteRide(Ride ride) {
        this.rideDAO.deleteRide(ride);
    }

    public LiveData<List<Ride>> getAllRidesSortedByDate() {
        return this.rideDAO.getAllRidesSortedByDate();
    }
}
