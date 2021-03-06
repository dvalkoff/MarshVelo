package ru.valkov.trackerapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;

import dagger.hilt.android.AndroidEntryPoint;
import ru.valkov.trackerapp.R;
import ru.valkov.trackerapp.ui.fragments.BluetoothFragment;
import ru.valkov.trackerapp.ui.fragments.StatisticsFragment;
import ru.valkov.trackerapp.ui.fragments.TrackingFragment;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import static ru.valkov.trackerapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    Fragment currentFragment = null;
    FragmentTransaction ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            getSupportActionBar().hide();
        } catch (NullPointerException e) { }

        navigateToTrackingFragmentIfNeed(getIntent());

        ft = getSupportFragmentManager().beginTransaction();
        currentFragment = TrackingFragment.getInstance();
        ft.replace(R.id.flFragment, currentFragment);
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
}