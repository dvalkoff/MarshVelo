package ru.valkov.trackerapp.services;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleService;

import timber.log.Timber;

import static ru.valkov.trackerapp.other.Constants.ACTION_PAUSE_SERVICE;
import static ru.valkov.trackerapp.other.Constants.ACTION_START_OR_RESUME_SERVICE;
import static ru.valkov.trackerapp.other.Constants.ACTION_STOP_SERVICE;

public class TrackingService extends LifecycleService {

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

            switch (intent.getAction()) {
                case ACTION_START_OR_RESUME_SERVICE:
                    Timber.d("ACTION_START_OR_RESUME_SERVICE");
                    break;
                case ACTION_PAUSE_SERVICE:
                    Timber.d("ACTION_PAUSE_SERVICE");
                    break;
                case ACTION_STOP_SERVICE:
                    Timber.d("ACTION_STOP_SERVICE");
                    break;
            }

        return super.onStartCommand(intent, flags, startId);
    }
}
