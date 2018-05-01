package edu.sjsu.emerson.alphafitness;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class WorkoutTrackerService extends Service implements SensorEventListener
{
    public static final String BROADCAST_STEP_COUNTER = "BroadcastStepCounter";
    public static final String BROADCAST_LOCATION_CHANGE = "BroadcastLocationChange";
    public static final String STEP_COUNT = "StepCount";
    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "longitude";

    private static final String TAG = "WorkoutTrackerService";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private LocationManager mLocationManager = null;
    private int stepCount = 0;
    private int stepsBeforeStart = 0;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {

        Sensor sensor = sensorEvent.sensor;

        // Sensor counter returns number of steps since reboot
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int stepsReported = (int) sensorEvent.values[0];
            if (stepCount == 0) {
                stepsBeforeStart = stepsReported;
            }
            stepCount = stepsReported - stepsBeforeStart;
            // Broadcast intent with step data
            Intent intent = new Intent(BROADCAST_STEP_COUNTER);
            intent.putExtra(STEP_COUNT, stepCount);
            sendBroadcast(intent);
            Toast.makeText(this, "stepCount:  " + stepCount, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            Intent intent = new Intent(BROADCAST_LOCATION_CHANGE);
            intent.putExtra(LATITUDE, mLastLocation.getLatitude());
            intent.putExtra(LONGITUDE, mLastLocation.getLongitude());
            sendBroadcast(intent);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners) {
                try {
                    mLocationManager.removeUpdates(mLocationListener);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager()
    {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
