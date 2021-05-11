package ru.valkov.trackerapp.ui.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import ru.valkov.trackerapp.R;
import ru.valkov.trackerapp.database.Ride;
import ru.valkov.trackerapp.other.TrackingUtility;
import ru.valkov.trackerapp.services.TrackingService;
import ru.valkov.trackerapp.ui.MainActivity;
import ru.valkov.trackerapp.ui.viewmodels.MainViewModel;
import timber.log.Timber;

import static ru.valkov.trackerapp.other.Constants.ACTION_PAUSE_SERVICE;
import static ru.valkov.trackerapp.other.Constants.ACTION_START_OR_RESUME_SERVICE;
import static ru.valkov.trackerapp.other.Constants.ACTION_STOP_SERVICE;
import static ru.valkov.trackerapp.other.Constants.MAP_ZOOM;
import static ru.valkov.trackerapp.other.Constants.POLYLINE_COLOR;
import static ru.valkov.trackerapp.other.Constants.POLYLINE_WIDTH;
import static ru.valkov.trackerapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION;

@AndroidEntryPoint
public class TrackingFragment extends Fragment implements  EasyPermissions.PermissionCallbacks, OnMapReadyCallback {

    private static TrackingFragment trackingFragment = null;

    private boolean serviceKilled = true;
    private MainViewModel viewModel;
    private GoogleMap map = null;
    private MapView mapView;
    private Button btnToggleRide;
    private Button btnFinishRun;
    private TextView tvTimer;

    // private Menu menu = null;

    private long currentTimeInMillis= 0;
    private static boolean isTracking = false;
    private static ArrayList<ArrayList<LatLng>> pathPoints = new ArrayList<>();

    public static TrackingFragment getInstance() {
        if (trackingFragment == null) {
            trackingFragment = new TrackingFragment();
        }
        return trackingFragment;
    }

    public TrackingFragment() {
        super(R.layout.fragment_tracking);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.e("On create");
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = getView().findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        Timber.e("View created");
        btnToggleRide = getView().findViewById(R.id.btnToggleRun);
        btnFinishRun = getView().findViewById(R.id.btnFinishRun);
        tvTimer = getView().findViewById(R.id.tvTimer);

        mapView.getMapAsync(this);
        subscribeToObservers();

        // Request permissions from user
        requestPermission();
        // Create an instance of ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        btnToggleRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRide();
            }
        });

        btnFinishRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToSeeWholeTrack();
                endAndSaveToDatabase("name");
                btnToggleRide.setText("Start");
                // menu.getItem(0).setVisible(true);
                btnFinishRun.setVisibility(getView().GONE);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                trackingFragment = new TrackingFragment();
                MainActivity.setCurrentFragment(trackingFragment);
                MainActivity.setTrackingFragment(trackingFragment);
                ft.replace(R.id.flFragment, trackingFragment).addToBackStack(null).commit();
            }
        });


        // setHasOptionsMenu(true);

    }

    /*
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.toolbar_tracking_menu, menu);
        this.menu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentTimeInMillis > 0) {
            this.menu.getItem(0).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        showCancelTrackingDialog();
        return super.onOptionsItemSelected(item);
    }

     */

    /*
    private void showCancelTrackingDialog() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.Theme_AppCompat_Light_Dialog)
                .setTitle("Cancel the ride?")
                .setIcon(R.drawable.bike);
        dialog.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopRide();
            }
        });
        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

     */

    private void stopRide() {
        sendCommandToService(ACTION_STOP_SERVICE);
    }

    private void subscribeToObservers() {
        TrackingService.serviceKilled.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                serviceKilled = aBoolean;
            }
        });
        TrackingService.isTracking.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                updateTracking(aBoolean);
            }
        });
        TrackingService.pathPoints.observe(getViewLifecycleOwner(), new Observer<ArrayList<ArrayList<LatLng>>>() {
            @Override
            public void onChanged(ArrayList<ArrayList<LatLng>> arrayLists) {
                pathPoints = arrayLists;
                addLatestPolyline();
                moveCameraToUser();
            }
        });
        TrackingService.timeRideInMillis.observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                currentTimeInMillis = aLong;
                tvTimer.setText(TrackingUtility.getFormattedStopWath(aLong, true));
            }
        });
    }

    private void toggleRide() {
        if (isTracking) {
            // menu.getItem(0).setVisible(true);
            sendCommandToService(ACTION_PAUSE_SERVICE);
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE);
            // getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
        }
    }

    private void updateTracking(boolean isTrack) {
        isTracking = isTrack;
        if (!isTracking && !serviceKilled) {
            Timber.e("Here is a problem");
            btnToggleRide.setText("Resume");
            btnFinishRun.setVisibility(getView().VISIBLE);
            btnFinishRun.setText("Finish");
        } else if (!serviceKilled) {
            btnToggleRide.setText("Stop");
            // menu.getItem(0).setVisible(true);
            btnFinishRun.setVisibility(getView().GONE);
        } else if (serviceKilled) {
            btnToggleRide.setText("Start");
            // menu.getItem(0).setVisible(true);
            btnFinishRun.setVisibility(getView().GONE);
        }
    }

    private void zoomToSeeWholeTrack() {
        LatLngBounds.Builder bounds = LatLngBounds.builder();
        for (ArrayList<LatLng> polyline: pathPoints) {
            for (LatLng pos: polyline) {
                bounds.include(pos);
            }
        }

        if (map != null) {
            map.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                            bounds.build(),
                            mapView.getWidth(),
                            mapView.getHeight(),
                            (int) (mapView.getHeight() * 0.05f)
                    )
            );
        }
    }

    private void endAndSaveToDatabase(String name) {
        map.snapshot(bmp -> {
            int distanceInMeters = 0;
            for (ArrayList<LatLng> polyline: pathPoints) {
                distanceInMeters += (int) TrackingUtility.calculatePolylineLength(polyline);
            }
            long dateTimeStamp = Calendar.getInstance().getTimeInMillis();
            Ride ride = new Ride(name, bmp, dateTimeStamp, distanceInMeters, currentTimeInMillis);
            viewModel.insertRide(ride);
            Snackbar.make(
                    requireActivity().findViewById(R.id.rootView),
                    "Ride saved successfully",
                    Snackbar.LENGTH_SHORT).show();
        });
        stopRide();
    }

    private void moveCameraToUser() {
        if (!pathPoints.isEmpty() && !pathPoints.get(pathPoints.size() - 1).isEmpty()) {
            if (map != null) {
                ArrayList<LatLng> polyline = pathPoints.get(pathPoints.size() - 1);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        polyline.get(polyline.size() - 1),
                        MAP_ZOOM
                ));
            }
        }
    }

    private void addAllPolylines() {
        for (ArrayList polyline : pathPoints) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)
                    .addAll(polyline);
            if (map != null) {
                map.addPolyline(polylineOptions);
            }
        }
    }
    private void addLatestPolyline() {
        if (!pathPoints.isEmpty()) {
            ArrayList<LatLng> polyline = pathPoints.get(pathPoints.size() - 1);
            if (polyline.size() > 1) {
                LatLng preLastLng = polyline.get(polyline.size() - 2);
                LatLng lastLng = polyline.get(polyline.size() - 1);
                PolylineOptions polylineOptions = new PolylineOptions()
                        .color(POLYLINE_COLOR)
                        .width(POLYLINE_WIDTH)
                        .add(preLastLng)
                        .add(lastLng);
                if (map != null) {
                    map.addPolyline(polylineOptions);
                }
            }
        }
    }

    private void sendCommandToService(String action) {
        Intent intent = new Intent(requireContext(), TrackingService.class);
        intent.setAction(action);
        requireContext().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        addAllPolylines();
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
