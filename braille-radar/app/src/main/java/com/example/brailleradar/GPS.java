package com.example.brailleradar;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.os.HandlerThread;

import androidx.annotation.NonNull;

import com.example.brailleradar.models.Coordinates;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GPS {
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationRequest locationRequest;
    private final LocationCallback locationCallback;
    private final Coordinates currentLocation;
    private HandlerThread handlerThread;

    public GPS(Activity activity) {
        //not important: update priority, interval, and minDistance by checking battery saver, figure out clean code comment
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        int priority = Priority.PRIORITY_HIGH_ACCURACY;
        long interval = 100;
        float minDistance = 0;
        locationRequest = new LocationRequest.Builder(interval)
                .setPriority(priority)
                .setMinUpdateDistanceMeters(minDistance)
                .setWaitForAccurateLocation(true)
                .build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                updateLocation(locationResult.getLastLocation());
            }
        };
        // I don't remember what this is, probably also not important at all
        // Not necessary for prototype but recommended for cleaner code
//        SettingsClient client = LocationServices.getSettingsClient(this);
//        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        currentLocation = new Coordinates();
    }
    public interface LocationUpdateListener {
        void onLocationUpdated();
    }
    private final List<LocationUpdateListener> listeners = new CopyOnWriteArrayList<>();
    public void setLocationUpdateListener(LocationUpdateListener listener) {
        this.listeners.add(listener);
    }

    public void removeLocationUpdateListener(LocationUpdateListener listener) {
        if (!listeners.isEmpty()) {
            listeners.remove(listener);
        }
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        handlerThread = new HandlerThread("GPS Thread");
        handlerThread.start();
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, handlerThread.getLooper());
    }

    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        if (handlerThread != null && handlerThread.isAlive()) {
            handlerThread.quitSafely();
        }
    }

    public Coordinates getCurrentLocation() {
        return new Coordinates(currentLocation.latitude, currentLocation.longitude);
    }

    public static float[] calculateDistance(Coordinates location1, Coordinates location2) {
        float[] results = new float[2];
        Location.distanceBetween(location1.latitude, location1.longitude,
                location2.latitude, location2.longitude, results);
        return results;
    }

    public float[] calculateDistanceFromCurrent(Coordinates location) {
        return calculateDistance(currentLocation, location);
    }

    private void updateLocation(Location location) {
        if (location == null) return;
        currentLocation.latitude = location.getLatitude();
        currentLocation.longitude = location.getLongitude();
        if (!listeners.isEmpty()) {
            for(LocationUpdateListener listener : listeners) {
                listener.onLocationUpdated();
            }
        }
    }
}