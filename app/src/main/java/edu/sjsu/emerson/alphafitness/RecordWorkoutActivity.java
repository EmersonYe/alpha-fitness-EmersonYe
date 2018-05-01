package edu.sjsu.emerson.alphafitness;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import edu.sjsu.emerson.alphafitness.Utils.LocationUtils;

import static edu.sjsu.emerson.alphafitness.WorkoutTrackerService.BROADCAST_LOCATION_CHANGE;
import static edu.sjsu.emerson.alphafitness.WorkoutTrackerService.BROADCAST_STEP_COUNTER;
import static edu.sjsu.emerson.alphafitness.WorkoutTrackerService.LATITUDE;
import static edu.sjsu.emerson.alphafitness.WorkoutTrackerService.LONGITUDE;
import static edu.sjsu.emerson.alphafitness.RecordWorkoutPortraitFragment.BROADCAST_NEW_WORKOUT;

public class RecordWorkoutActivity extends AppCompatActivity
{
    private static final String TAG = "RecordWorkoutActivity";
    onNewLocationListener mListener;
    private static ArrayList<LatLng> locationsToDraw = new ArrayList<>();
    private double distance;

    // test values
    private static final LatLng ADELAIDE = new LatLng(-34.92873, 138.59995);
    private static final LatLng DARWIN = new LatLng(-12.4258647, 130.7932231);
    private static final LatLng MELBOURNE = new LatLng(-37.81319, 144.96298);
    private static final LatLng PERTH = new LatLng(-31.95285, 115.85734);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_workout);

        /* add test values to locations to draw
        locationsToDraw.add(ADELAIDE);
        locationsToDraw.add(DARWIN);
        locationsToDraw.add(MELBOURNE);
        locationsToDraw.add(PERTH);
        */
    }

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, "Intent received. Action: " + intent.getAction());
            switch (intent.getAction()) {
                case BROADCAST_LOCATION_CHANGE:
                    double latitude = intent.getDoubleExtra(LATITUDE, 0);
                    double longitude = intent.getDoubleExtra(LONGITUDE, 0);
                    LatLng newLocation = new LatLng(latitude, longitude);
                    // Calculate distance from last to new location
                    double newDistance = 0;
                    if (!locationsToDraw.isEmpty()) {
                        LatLng lastLocation = locationsToDraw.get(locationsToDraw.size()-1);
                        newDistance = LocationUtils.distanceBetween(lastLocation,newLocation);
                    }
                    distance += newDistance;
                    locationsToDraw.add(newLocation);
                    mListener.onNewLocation(locationsToDraw, distance);
                    break;
                case BROADCAST_STEP_COUNTER:
                    Toast.makeText(RecordWorkoutActivity.this, "Received step update", Toast.LENGTH_LONG).show();
                    break;
                case BROADCAST_NEW_WORKOUT:
                    locationsToDraw.clear();
                    distance = 0;
                    Log.i(TAG, "locationsToDraw cleared");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter intentFilterStep = new IntentFilter(BROADCAST_STEP_COUNTER);
        IntentFilter intentFilterLocation = new IntentFilter(BROADCAST_LOCATION_CHANGE);
        IntentFilter intentFilterNewWorkout = new IntentFilter(BROADCAST_NEW_WORKOUT);

        registerReceiver(receiver, intentFilterStep);
        registerReceiver(receiver, intentFilterLocation);
        registerReceiver(receiver, intentFilterNewWorkout);

        // TODO: make timer resume if service is running
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    interface onNewLocationListener
    {
        void onNewLocation(ArrayList<LatLng> locationsToDraw, double distance);
    }

    /**
     * Called when a fragment is attached to the activity.
     * <p>
     * <p>This is called after the attached fragment's <code>onAttach</code> and before
     * the attached fragment's <code>onCreate</code> if the fragment has not yet had a previous
     * call to <code>onCreate</code>.</p>
     *
     * @param fragment fragment to attach as a listener
     */
    @Override
    public void onAttachFragment(Fragment fragment)
    {
        super.onAttachFragment(fragment);
        if (fragment instanceof onNewLocationListener) {
            mListener = (onNewLocationListener) fragment;
            Log.e(TAG, "Fragment attached and listening for new locations ");
        }
    }

}
