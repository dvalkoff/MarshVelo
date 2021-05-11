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


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import static ru.valkov.trackerapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    Fragment currentFragment = null;
    Fragment trackingFragment = null;
    Fragment statisticsFragment = null;
    Fragment bluetoothFragment = null;
    FragmentTransaction ft;
    boolean[] alreadyCreated = {true, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ft = getSupportFragmentManager().beginTransaction();
        trackingFragment = new TrackingFragment();
        currentFragment = trackingFragment;
        ft.add(R.id.flFragment, currentFragment);

        setContentView(R.layout.activity_main);

        try {
            getSupportActionBar().hide();
        } catch (NullPointerException e) { }
        // navigateToTrackingFragmentIfNeed(getIntent());


        // ft.replace(R.id.flFragment, currentFragment);
        ft.commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

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
            currentFragment = TrackingFragment.getInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, currentFragment).commit();
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
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, currentFragment).commit();
                return true;
            }
            return false;
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener OnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fm = getSupportFragmentManager();
            currentFragment = null;
            switch (item.getItemId()) {
                case R.id.fragment_tracking:
                    if (trackingFragment != null){
                        trackingFragment.getView().setVisibility(View.VISIBLE);
                        /*

                        Timber.e("MAIN ACTIVITY: TrackingFragment already exists, show that");
                        fm
                                .beginTransaction()
                                .show(trackingFragment)
                                .commit();

                         */
                    } else {
                        Timber.e("MAIN ACTIVITY: create new TrackingFragment");
                        trackingFragment = new TrackingFragment();
                        currentFragment = trackingFragment;
                        fm
                                .beginTransaction()
                                .add(R.id.flFragment, currentFragment)
                                .commit();
                        alreadyCreated[0] = true;
                    }
                    if (statisticsFragment != null){

                        Timber.e("MAIN ACTIVITY: hide statisticsFragment");
                        statisticsFragment.getView().setVisibility(View.GONE);
                        // fm.beginTransaction().hide(statisticsFragment).commit();
                    }
                    if (bluetoothFragment != null){
                        Timber.e("MAIN ACTIVITY: hide bluetoothFragment");
                        bluetoothFragment.getView().setVisibility(View.GONE);
                        // fm.beginTransaction().hide(bluetoothFragment).commit();
                    }
                    return true;
                case R.id.fragment_statistics:
                    if (statisticsFragment != null) {
                        statisticsFragment.getView().setVisibility(View.VISIBLE);
                        /*
                        Timber.e("MAIN ACTIVITY: statisticsFragment already exists, show that");
                        fm
                                .beginTransaction()
                                .show(statisticsFragment)
                                .commit();
                         */
                    } else {
                        Timber.e("MAIN ACTIVITY: create new StatisticsFragment");
                        statisticsFragment = new StatisticsFragment();
                        currentFragment = statisticsFragment;
                        fm.beginTransaction().add(R.id.flFragment, currentFragment).commit();
                    }
                    if (trackingFragment != null) {
                        Timber.e("Hide tracking fragment");
                        trackingFragment.getView().setVisibility(View.GONE);
                        // fm.beginTransaction().hide((TrackingFragment) fm.findFragmentById(R.id.fragment_tracking)).commit();
                    }
                    if (bluetoothFragment != null){
                        bluetoothFragment.getView().setVisibility(View.GONE);
                        // fm.beginTransaction().hide(bluetoothFragment).commit();
                    }
                    return true;
                case R.id.fragment_bluetooth:
                    if (bluetoothFragment != null) {
                        Timber.e("MAIN ACTIVITY: bluetoothFragment already exists, show that");
                        bluetoothFragment.getView().setVisibility(View.VISIBLE);
                        // fm.beginTransaction().show(bluetoothFragment).commit();
                    } else {
                        bluetoothFragment = new BluetoothFragment();
                        currentFragment = bluetoothFragment;
                        fm.beginTransaction().add(R.id.flFragment, currentFragment).commit();
                    }
                    if (trackingFragment != null){
                        trackingFragment.getView().setVisibility(View.GONE);
                        // fm.beginTransaction().hide(trackingFragment).commit();
                    }
                    if (statisticsFragment != null){
                        statisticsFragment.getView().setVisibility(View.GONE);
                        // fm.beginTransaction().hide(statisticsFragment).commit();
                    }
                    return true;
            }
            return false;
        }
    };
}