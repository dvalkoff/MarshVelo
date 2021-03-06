package ru.valkov.trackerapp.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import kotlin.collections.CollectionsKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.DispatchedKt;
import ru.valkov.trackerapp.R;
import ru.valkov.trackerapp.other.TrackingUtility;
import ru.valkov.trackerapp.ui.MainActivity;
import timber.log.Timber;

import static android.app.NotificationManager.IMPORTANCE_LOW;
import static ru.valkov.trackerapp.other.Constants.ACTION_PAUSE_SERVICE;
import static ru.valkov.trackerapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT;
import static ru.valkov.trackerapp.other.Constants.ACTION_START_OR_RESUME_SERVICE;
import static ru.valkov.trackerapp.other.Constants.ACTION_STOP_SERVICE;
import static ru.valkov.trackerapp.other.Constants.FASTEST_LOCATION_INTERVAL;
import static ru.valkov.trackerapp.other.Constants.LOCATION_UPDATE_INTERVAL;
import static ru.valkov.trackerapp.other.Constants.NOTIFICATION_CHANNEL_ID;
import static ru.valkov.trackerapp.other.Constants.NOTIFICATION_CHANNEL_NAME;
import static ru.valkov.trackerapp.other.Constants.NOTIFICATION_ID;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class TrackingService extends LifecycleService {

    private static boolean isFirstRun = true;

    private static MutableLiveData<Long> timeRideInSeconds = new MutableLiveData<>();

    public static MutableLiveData<Long> timeRideInMillis = new MutableLiveData<>();
    public static MutableLiveData<Boolean> isTracking = new MutableLiveData<>();
    public static MutableLiveData<ArrayList<ArrayList<LatLng>>> pathPoints = new MutableLiveData<>();
    private FusedLocationProviderClient fusedLocationProviderClient;

/*
    private NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) // Notification always active
            .setOngoing(true) // Notification can't be swiped away
            .setSmallIcon(R.drawable.bike)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent());

    private NotificationCompat.Builder currentNotificationBuilder;

 */

    private void postInitialValues() {
        Timber.d("TRACKING_SERVICE: Tracking LiveData initialized");
        timeRideInMillis.postValue(0L);
        timeRideInSeconds.postValue(0L);
        isTracking.postValue(false);
        pathPoints.setValue(new ArrayList<>());
        pathPoints.postValue(pathPoints.getValue());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        postInitialValues();

        // currentNotificationBuilder = notificationBuilder;

        fusedLocationProviderClient = new FusedLocationProviderClient(this);
        isTracking.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Timber.d("TRACKING_SERVICE: Tracking observed");
                updateLocationTracking(aBoolean);
                // updateNotificationTrackingState(aBoolean);
            }
        });
    }

    private boolean isTimerEnabled = false;
    private long pauseStartTimeInMillis = 0;
    private long pauseStopTimeInMillis = 0;
    private long timePauseInMillis = 0;
    private long timeStarted = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            timeRideInMillis.postValue(System.currentTimeMillis() - timeStarted - timePauseInMillis);
            // timeRideInSeconds.postValue(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - timeStarted));
            timerHandler.postDelayed(this, 10);
        }
    };

    private void startTimer() {
        Timber.e("TRACKING SERVICE: timer started");
        pauseStopTimeInMillis = System.currentTimeMillis();
        if (pauseStartTimeInMillis == 0) {
            timePauseInMillis = 0;
        } else {
            timePauseInMillis += pauseStopTimeInMillis - pauseStartTimeInMillis;
        }
        if (timeStarted == 0) {
            timeStarted = System.currentTimeMillis();
        }
        timerHandler.postDelayed(timerRunnable, 0);
        isTimerEnabled = true;
    }

    /*
    private void updateNotificationTrackingState(boolean isTracking) {
        String notificationActionText = isTracking? "pause" : "resume";
        PendingIntent intent;
        if (isTracking) {
            Intent pauseIntent = new Intent(this, TrackingService.class);
            pauseIntent.setAction(ACTION_PAUSE_SERVICE);
            intent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            Intent resumeIntent = new Intent(this, TrackingService.class);
            resumeIntent.setAction(ACTION_START_OR_RESUME_SERVICE);
            intent = PendingIntent.getService(this, 1, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            Field declaredField = currentNotificationBuilder.getClass().getDeclaredField("mActions");
            declaredField.setAccessible(true);
            declaredField.set(currentNotificationBuilder, new ArrayList<NotificationCompat.Action>());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        currentNotificationBuilder = notificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, intent);
        notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build());
    }

     */

    private void pauseService() {
        pauseStartTimeInMillis = System.currentTimeMillis();
        timerHandler.removeCallbacks(timerRunnable);
        isTracking.postValue(false);
        // 11 12 , 11 13, 11 13, 11 13, 11 14,
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_START_OR_RESUME_SERVICE:
                if (isFirstRun) {
                    startForegroundService();
                    isFirstRun = false;
                    Timber.d("TRACKING_SERVICE: Start TrackingService");
                } else {
                    startForegroundService();
                    Timber.d("TRACKING_SERVICE: Resume service");
                }
                break;
            case ACTION_PAUSE_SERVICE:
                pauseService();
                Timber.d("TRACKING_SERVICE: ACTION_PAUSE_SERVICE");
                break;
            case ACTION_STOP_SERVICE:
                Timber.d("TRACKING_SERVICE: ACTION_STOP_SERVICE");
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void addEmptyPolyline() {
        ArrayList polylines =  pathPoints.getValue();
        if (polylines != null) {
            polylines.add(new ArrayList<>());
            pathPoints.postValue(polylines);
            Timber.e("TRACKING_SERVICE: empty PolyLine added");
        }
    }

    private void addPathPoint(Location location) {
        if (location != null) {
            Timber.d("TRACKING_SERVICE: trying to add path point");
            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
            ArrayList polylines = pathPoints.getValue(); //     ArrayList<ArrayList<LatLng>>
            if (polylines != null) {
                ArrayList polyline = (ArrayList) polylines.get(polylines.size() - 1);
                polyline.add(pos);
                // pathPoints.postValue(polylines);
                pathPoints.postValue(polylines);
                Timber.d("TRACKING_SERVICE: path point is added");
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void updateLocationTracking(boolean isTracking) {
        Timber.d("TRACKING_SERVICE: trying to update Location Tracking");
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                LocationRequest request = new LocationRequest();
                request.setInterval(LOCATION_UPDATE_INTERVAL);
                request.setFastestInterval(FASTEST_LOCATION_INTERVAL);
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                fusedLocationProviderClient.requestLocationUpdates(
                        request,
                        locationCallback(),
                        Looper.getMainLooper()
                );
                Timber.d("TRACKING_SERVICE: Location Tracking is updated");
            }
        } else  {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback());
        }
    }

    private LocationCallback locationCallback() {
        LocationCallback locationCallback = new LocationCallback() {
            /*
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (isTracking.getValue()) {
                    Timber.e("TRACKING_SERVICE: NEW LOVATION AGAIN WTF");
                    List<Location> locations = locationResult.getLocations();
                    if (locations != null && !locations.isEmpty()) {
                        ArrayList polylines = (ArrayList) pathPoints.getValue();
                        if (pathPoints != null && !polylines.isEmpty()) {
                            ArrayList polyline = (ArrayList) polylines.get(polylines.size() - 1);
                            if (!polyline.isEmpty()) {
                                LatLng lastLatLng = (LatLng) polyline.get(polyline.size() - 1);
                                Location location = locations.get(locations.size() - 1);
                                if (location.getLongitude() != lastLatLng.longitude || location.getLatitude() != lastLatLng.latitude) {
                                    addPathPoint(locations.get(locations.size() - 1));
                                    Timber.d("TRACKING_SERVICE: NEW LOCATION: " + locations.get(locations.size() - 1).getLatitude() + ", " + locations.get(locations.size() - 1).getLongitude());
                                }
                            } else {
                                addPathPoint(locations.get(locations.size() - 1));
                            }
                        } else if (polylines.isEmpty()){
                            addPathPoint(locations.get(locations.size() - 1));
                        }
                    }
                }
            }

             */
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (isTracking.getValue()) {
                    List<Location> locations = locationResult.getLocations();
                    for (Location location: locations) {
                        addPathPoint(location);
                        Timber.d("TRACKING_SERVICE: NEW LOCATION: " + location.getLatitude() + ", " + location.getLongitude());
                    }
                }
            }

            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        };
        return locationCallback;
    }

    private void startForegroundService() {
        Timber.d("TRACKING_SERVICE: Starting foreground Service");
        // startTimer();
        addEmptyPolyline();
        isTracking.postValue(true);
        /*
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createNotificationChannel(notificationManager);
        }
        */
        // startForeground(NOTIFICATION_ID, notificationBuilder.build());

        /*
        timeRideInSeconds.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                // NotificationCompat.Builder notification = currentNotificationBuilder.setContentText(TrackingUtility.getFormattedStopWath(aLong, false));
                // notificationManager.notify(NOTIFICATION_ID, notification.build());
            }
        });
         */
    }

    /*
    // Notification functions

    private PendingIntent getMainActivityPendingIntent() {
        Intent intent  = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_SHOW_TRACKING_FRAGMENT);
        return PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }

     */
}
