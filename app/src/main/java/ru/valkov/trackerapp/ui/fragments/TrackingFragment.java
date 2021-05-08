package ru.valkov.trackerapp.ui.fragments;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import ru.valkov.trackerapp.R;
import ru.valkov.trackerapp.other.TrackingUtility;
import ru.valkov.trackerapp.ui.MainActivity;
import ru.valkov.trackerapp.ui.viewmodels.MainViewModel;

import static ru.valkov.trackerapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION;

@AndroidEntryPoint
public class TrackingFragment extends Fragment implements  EasyPermissions.PermissionCallbacks, OnMapReadyCallback {

    private MainViewModel viewModel;
    private GoogleMap map = null;
    private MapView mapView;

    public TrackingFragment() {
        super(R.layout.fragment_tracking);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = getView().findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        // Request permissions from user
        requestPermission();
        // Create an instance of ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        mapView.getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
    }


    @AfterPermissionGranted(REQUEST_CODE_LOCATION_PERMISSION)
    private void requestPermission() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                    this,
                    "You need to accept location permissions to use MarshVelo",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            );
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "You need to accept location permissions to use MarshVelo",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            );
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            requestPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions, grantResults, MainActivity.class);
    }
}
