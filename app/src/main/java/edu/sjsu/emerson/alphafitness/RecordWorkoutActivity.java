package edu.sjsu.emerson.alphafitness;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.emerson.alphafitness.Utils.LocationUtils;
import edu.sjsu.emerson.alphafitness.database.MyContentProvider;
import edu.sjsu.emerson.alphafitness.database.workoutDbHelper;

import static edu.sjsu.emerson.alphafitness.RecordWorkoutPortraitFragment.BROADCAST_STOP_WORKOUT;
import static edu.sjsu.emerson.alphafitness.WorkoutTrackerService.BROADCAST_LOCATION_CHANGE;
import static edu.sjsu.emerson.alphafitness.WorkoutTrackerService.BROADCAST_STEP_COUNTER;
import static edu.sjsu.emerson.alphafitness.WorkoutTrackerService.LATITUDE;
import static edu.sjsu.emerson.alphafitness.WorkoutTrackerService.LONGITUDE;
import static edu.sjsu.emerson.alphafitness.RecordWorkoutPortraitFragment.BROADCAST_NEW_WORKOUT;

public class RecordWorkoutActivity extends AppCompatActivity
{
    private static final String TAG = "RecordWorkoutActivity";
    private static final int delay = 5000; // milliseconds
    private static final String DISTANCE = "distance";
    private static final String LOCATION_ARRAY = "locationArray";
    private static final String STEP_ARRAY = "stepArray";
    private static final String TOTAL_STEPS = "totalSteps";
    onNewLocationListener mLocationListener;
    onNewStepCounterData mNewStepCounterData;
    // Model for drawing Record Workout
    private static ArrayList<LatLng> locationsToDraw = new ArrayList<>();
    private double distance;

    // Data for drawing Details
    private static ArrayList<Integer> steps = new ArrayList<>();
    private int totalSteps;
    // To execute action every 5 seconds
    Handler handler = new Handler();
    Runnable runnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_workout);
        totalSteps = 0;
        // test value
        steps.add(13);
        totalSteps = 13;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        distance = savedInstanceState.getDouble(DISTANCE);
        locationsToDraw = savedInstanceState.getParcelableArrayList(LOCATION_ARRAY);
        steps = savedInstanceState.getIntegerArrayList(STEP_ARRAY);
        totalSteps = savedInstanceState.getInt(TOTAL_STEPS);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // TODO: make timer resume if service is running
        IntentFilter intentFilterStep = new IntentFilter(BROADCAST_STEP_COUNTER);
        IntentFilter intentFilterLocation = new IntentFilter(BROADCAST_LOCATION_CHANGE);
        IntentFilter intentFilterNewWorkout = new IntentFilter(BROADCAST_NEW_WORKOUT);
        IntentFilter intentFilterStopWorkout = new IntentFilter(BROADCAST_STOP_WORKOUT);

        registerReceiver(receiver, intentFilterStep);
        registerReceiver(receiver, intentFilterLocation);
        registerReceiver(receiver, intentFilterNewWorkout);
        registerReceiver(receiver, intentFilterStopWorkout);

        handler.postDelayed(new Runnable()
        {
            public void run()
            {
                // execute ever 5 seconds
                // test data
                steps.add(steps.get(steps.size() - 1) + 1);
                totalSteps += steps.get(steps.size() - 1);
                try {
                    mNewStepCounterData.onNewStepData(steps, delay, totalSteps);
                } catch (NullPointerException e) {
                    Log.w(TAG, "no fragments listening to step updates");
                }

                runnable = this;

                handler.postDelayed(runnable, delay);
            }
        }, delay);
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause()
    {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putDouble(DISTANCE, distance);
        outState.putParcelableArrayList(LOCATION_ARRAY, locationsToDraw);
        outState.putIntegerArrayList(STEP_ARRAY, steps);
        outState.putInt(TOTAL_STEPS, totalSteps);

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(receiver);
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
                        LatLng lastLocation = locationsToDraw.get(locationsToDraw.size() - 1);
                        newDistance = LocationUtils.distanceBetween(lastLocation, newLocation);
                    }
                    distance += newDistance;
                    locationsToDraw.add(newLocation);
                    try {
                        mLocationListener.onNewLocation(locationsToDraw, distance);
                    } catch (NullPointerException e) {
                        Log.w(TAG, "no fragments listening to location updates");
                    }
                    break;
                case BROADCAST_STEP_COUNTER:
                    Toast.makeText(RecordWorkoutActivity.this, "Received step update", Toast.LENGTH_LONG).show();
                    break;
                case BROADCAST_NEW_WORKOUT:
                    locationsToDraw.clear();
                    distance = 0;
                    Log.i(TAG, "locationsToDraw cleared");
                    break;
                case BROADCAST_STOP_WORKOUT:
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(workoutDbHelper.DATE, 2018);
                    contentValues.put(workoutDbHelper.DISTANCE, 15);
                    contentValues.put(workoutDbHelper.DURATION, 150);
                    contentValues.put(workoutDbHelper.STEPS, 999);
                    Uri uri = getContentResolver().insert(MyContentProvider.URI, contentValues);
                    Toast.makeText(RecordWorkoutActivity.this, uri.toString(), Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };


    interface onNewLocationListener
    {
        void onNewLocation(ArrayList<LatLng> locationsToDraw, double distance);
    }

    interface onNewStepCounterData
    {
        void onNewStepData(List<Integer> steps, int updateInterval, int totalSteps);
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
            mLocationListener = (onNewLocationListener) fragment;

            // Wait 1 second for map to load, then update map with existing data
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    mLocationListener.onNewLocation(locationsToDraw,distance);
                }
            }, 1000);   //1 seconds

            Log.e(TAG, "Fragment attached and listening for new locations ");
        }
        if (fragment instanceof onNewStepCounterData) {
            mNewStepCounterData = (onNewStepCounterData) fragment;
            Log.e(TAG, "Fragment attached and listening for steps ");
        }
    }

}
