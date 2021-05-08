package ru.valkov.trackerapp.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;




import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import ru.valkov.trackerapp.R;
import ru.valkov.trackerapp.database.RideDAO;
import ru.valkov.trackerapp.ui.fragments.BluetoothFragment;
import ru.valkov.trackerapp.ui.fragments.StatisticsFragment;
import ru.valkov.trackerapp.ui.fragments.TrackingFragment;
import timber.log.Timber;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    Fragment currentFragment = null;
    FragmentTransaction ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ft = getSupportFragmentManager().beginTransaction();
        currentFragment = new TrackingFragment();
        ft.replace(R.id.flFragment, currentFragment);
        ft.commit();

        BottomNavigationView navigation = (BottomNavigationView)
                findViewById(R.id.bottomNavigationView); navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener(){

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.fragment_tracking:
                    currentFragment = new TrackingFragment();
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.flFragment, currentFragment);
                    ft.commit();
                    return true;
                case R.id.fragment_statistics:
                    currentFragment = new StatisticsFragment();
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.flFragment, currentFragment);
                    ft.commit();
                    return true;
                case R.id.fragment_bluetooth:
                    currentFragment = new BluetoothFragment();
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.flFragment, currentFragment);
                    ft.commit();
                    return true;
            }

            return false;
        }

    };
}