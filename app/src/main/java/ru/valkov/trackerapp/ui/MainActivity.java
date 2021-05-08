package ru.valkov.trackerapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import ru.valkov.trackerapp.R;
import ru.valkov.trackerapp.database.RideDAO;
import timber.log.Timber;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    public RideDAO rideDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.d("RIDEDAO: %s", rideDAO.hashCode());
    }
}