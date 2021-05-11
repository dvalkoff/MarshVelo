package ru.valkov.trackerapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import dagger.hilt.android.AndroidEntryPoint;
import ru.valkov.trackerapp.R;
import ru.valkov.trackerapp.ui.fragments.BluetoothFragment;
import ru.valkov.trackerapp.ui.fragments.StatisticsFragment;
import ru.valkov.trackerapp.ui.fragments.TrackingFragment;
import timber.log.Timber;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import static ru.valkov.trackerapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    Fragment currentFragment = null;
    FragmentTransaction ft;

    Fragment trackingFragment = null;
    Fragment statisticsFragment = null;
    Fragment bluetoothFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.e("Main Activity created");

        setContentView(R.layout.activity_main);

        ft = getSupportFragmentManager().beginTransaction();
        trackingFragment = TrackingFragment.getInstance();
        currentFragment = trackingFragment;
        ft.replace(R.id.flFragment, currentFragment);
        ft.commit();
        /*
        try {
            getSupportActionBar().hide();
        } catch (NullPointerException e) { }
         */

        navigateToTrackingFragmentIfNeed(getIntent());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        navigation.setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        navigateToTrackingFragmentIfNeed(intent);
    }

    private void navigateToTrackingFragmentIfNeed(Intent intent) {
        if (intent.getAction() == ACTION_SHOW_TRACKING_FRAGMENT) {
            getSupportFragmentManager().beginTransaction().hide(currentFragment);
            currentFragment = trackingFragment;
            getSupportFragmentManager().beginTransaction().show(trackingFragment).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            currentFragment = null;
            switch (item.getItemId()) {
                case R.id.fragment_tracking:
                    currentFragment = TrackingFragment.getInstance();
                    break;
                case R.id.fragment_statistics:
                    currentFragment = new StatisticsFragment();
                    break;
                case R.id.fragment_bluetooth:
                    currentFragment = new BluetoothFragment();
                    break;
            }
            if (currentFragment != null) {
                fragmentManager.beginTransaction().replace(R.id.flFragment, currentFragment).commit();
                return true;
            }
            return false;
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener OnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            switch (item.getItemId()) {
                case R.id.fragment_tracking:
                    fragmentManager.beginTransaction().hide(currentFragment).commit();
                    if (trackingFragment != null) {
                        Timber.e("MAIN ACTIVITY: open Tracking fragment");
                        fragmentManager.beginTransaction().show(trackingFragment).commit();
                    } else {
                        Timber.e("MAIN ACTIVITY: add new Tracking fragment");
                        trackingFragment = TrackingFragment.getInstance();
                        fragmentManager.beginTransaction().add(R.id.flFragment, trackingFragment).commit();
                    }
                    currentFragment = trackingFragment;
                    return true;
                case R.id.fragment_statistics:
                    fragmentManager.beginTransaction().hide(currentFragment).commit();
                    if (statisticsFragment != null) {
                        Timber.e("MAIN ACTIVITY: open statistics fragment");
                        fragmentManager.beginTransaction().show(statisticsFragment).commit();
                    } else {
                        Timber.e("MAIN ACTIVITY: create new statistics fragment");
                        statisticsFragment = new StatisticsFragment();
                        fragmentManager.beginTransaction().add(R.id.flFragment, statisticsFragment).commit();
                    }
                    currentFragment = statisticsFragment;
                    return true;
                case R.id.fragment_bluetooth:
                    fragmentManager.beginTransaction().hide(currentFragment).commit();
                    if (bluetoothFragment != null) {
                        fragmentManager.beginTransaction().show(bluetoothFragment).commit();
                    } else {
                        bluetoothFragment = new BluetoothFragment();
                        fragmentManager.beginTransaction().add(R.id.flFragment, bluetoothFragment).commit();
                    }
                    currentFragment = bluetoothFragment;
                    return true;
            }
            return false;
        }
    };
}