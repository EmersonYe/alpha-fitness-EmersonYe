package edu.sjsu.emerson.alphafitness;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import edu.sjsu.emerson.alphafitness.database.MyContentProvider;
import edu.sjsu.emerson.alphafitness.database.workoutDbHelper;

public class RecordWorkoutPortraitFragment extends Fragment implements RecordWorkoutActivity.onNewLocationListener
{
    public static final String BROADCAST_NEW_WORKOUT = "BroadcastNewWorkout";
    public static final String BROADCAST_STOP_WORKOUT = "BroadcastStopWorkout";

    private static final String TAG = "PortraitFragment";
    private static final String CHRONOMETER_BASE = "chronometerBase";
    public static final String DURATION_SECONDS = "durationSeconds";
    MapView mMapView;
    private GoogleMap googleMap;
    private Polyline mPolyline;
    private Chronometer mChronometer;
    private TextView mDistanceText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_record_workout_portrait, container, false);
        mChronometer = rootView.findViewById(R.id.chronometer);
        mDistanceText = rootView.findViewById(R.id.text_distance);
        final Button toggleWorkoutButton = rootView.findViewById(R.id.button_start);
        if (isServiceRunning(WorkoutTrackerService.class)) {
            toggleWorkoutButton.setText(R.string.stop_workout);
            mChronometer.setBase(savedInstanceState.getLong(CHRONOMETER_BASE));
            mChronometer.start();
        } else
            toggleWorkoutButton.setText(R.string.start_workout);

        toggleWorkoutButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getActivity(), WorkoutTrackerService.class);
                // Start WorkoutTrackerService, notify Activity to reset locations, start mChronometer
                if (!isServiceRunning(WorkoutTrackerService.class)) {
                    getActivity().startService(intent);
                    // Notify Activity to clear past locations
                    Intent broadcastIntent = new Intent(BROADCAST_NEW_WORKOUT);
                    getActivity().sendBroadcast(broadcastIntent);
                    // Start mChronometer
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                    toggleWorkoutButton.setText(R.string.stop_workout);
                    Log.i(TAG, "Workout started");
                } else {
                    getActivity().stopService(intent);
                    Intent broadcastIntent = new Intent(BROADCAST_STOP_WORKOUT);
                    broadcastIntent.putExtra(DURATION_SECONDS,(int)(SystemClock.elapsedRealtime() - mChronometer.getBase()));
                    getActivity().sendBroadcast(broadcastIntent);
                    // Stop mChronometer
                    mChronometer.stop();
                    toggleWorkoutButton.setText(R.string.start_workout);
                    Log.i(TAG, "Workout stopped");
                }
            }
        });

        final ImageButton profileButton = rootView.findViewById(R.id.profile_button);
        profileButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Fragment profileFragment = new ProfileFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.portraitFrame, profileFragment);
                fragmentTransaction.addToBackStack(null);

                fragmentTransaction.commit();
                /* test read from mysqlite. works! :)
                */
                Cursor c = getActivity().getContentResolver().query(MyContentProvider.URI, null, null, null, "date");
                if (c.moveToFirst()) {
                    do {
                        Log.i(TAG,c.getString(c.getColumnIndex(MyContentProvider._ID))
                                + ", "
                                + c.getString(
                                c.getColumnIndex(workoutDbHelper.DATE))
                                + ", "
                                + c.getString(
                                c.getColumnIndex(workoutDbHelper.DISTANCE))
                                + ", "
                                + c.getString(
                                c.getColumnIndex(workoutDbHelper.DURATION))
                                + ","
                                + c.getString(
                                c.getColumnIndex(workoutDbHelper.CALORIES)));

                    } while (c.moveToNext());
                }
            }
        });

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback()
        {
            @Override
            public void onMapReady(GoogleMap mMap) throws SecurityException
            {
                googleMap = mMap;

                // For showing a move to my location button
                googleMap.setMyLocationEnabled(true);
            }
        });
        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong(CHRONOMETER_BASE, mChronometer.getBase());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onNewLocation(ArrayList<LatLng> locationsToDraw, double distance)
    {
        // center camera on last location
        if (!locationsToDraw.isEmpty())
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(locationsToDraw.get(locationsToDraw.size() - 1)));

        if (mPolyline != null)
            mPolyline.remove();
        mPolyline = googleMap.addPolyline(new PolylineOptions().addAll(locationsToDraw));
        String formattedDistance = String.format("%.2f", distance);
        mDistanceText.setText(formattedDistance);
    }

    private boolean isServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
