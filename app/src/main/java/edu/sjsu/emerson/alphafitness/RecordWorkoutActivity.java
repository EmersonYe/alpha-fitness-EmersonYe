package edu.sjsu.emerson.alphafitness;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Objects;

import static edu.sjsu.emerson.alphafitness.WorkoutTrackerService.BROADCAST_LOCATION_CHANGE;
import static edu.sjsu.emerson.alphafitness.WorkoutTrackerService.BROADCAST_STEP_COUNTER;
import static edu.sjsu.emerson.alphafitness.WorkoutTrackerService.LATITUDE;
import static edu.sjsu.emerson.alphafitness.WorkoutTrackerService.LONGITUDE;

public class RecordWorkoutActivity extends AppCompatActivity
{
    private static ArrayList<LatLng> locationsToDraw = new ArrayList<>();
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

        locationsToDraw.add(ADELAIDE);
        locationsToDraw.add(DARWIN);
        locationsToDraw.add(MELBOURNE);
        locationsToDraw.add(PERTH);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (Objects.equals(intent.getAction(), BROADCAST_STEP_COUNTER)) {
                Toast.makeText(RecordWorkoutActivity.this, "Received step update", Toast.LENGTH_LONG).show();
            }
            if (Objects.equals(intent.getAction(), BROADCAST_LOCATION_CHANGE)) {
                double latitude = intent.getDoubleExtra(LATITUDE, 0);
                double longitude = intent.getDoubleExtra(LONGITUDE, 0);
                LatLng newLocation = new LatLng(latitude,longitude);
                locationsToDraw.add(newLocation);
            }
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter intentFilterStep = new IntentFilter(BROADCAST_STEP_COUNTER);
        IntentFilter intentFilterLocation = new IntentFilter(BROADCAST_LOCATION_CHANGE);

        registerReceiver(receiver, intentFilterStep);
        registerReceiver(receiver, intentFilterLocation);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public static ArrayList<LatLng> getLocationsToDraw()
    {
        return locationsToDraw;
    }
}
