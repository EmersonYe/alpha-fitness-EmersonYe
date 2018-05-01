package edu.sjsu.emerson.alphafitness;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class RecordWorkoutPortraitFragment extends Fragment implements RecordWorkoutActivity.onNewLocationListener
{
    public static final String BROADCAST_NEW_WORKOUT = "BroadcastNewWorkout";

    private static final String TAG = "PortraitFragment";
    MapView mMapView;
    private GoogleMap googleMap;
    private Polyline mPolyline;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_record_workout_portrait, container, false);
        final Button toggleWorkoutButton = rootView.findViewById(R.id.button_start);
        toggleWorkoutButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getActivity(), WorkoutTrackerService.class);
                if (!isServiceRunning(WorkoutTrackerService.class)) {
                    getActivity().startService(intent);
                    toggleWorkoutButton.setText(R.string.stop_workout);
                    // Notify Activity to clear past locations
                    Intent broadcastIntent = new Intent(BROADCAST_NEW_WORKOUT);
                    getActivity().sendBroadcast(broadcastIntent);
                    Log.i(TAG, "WorkoutTrackerService started");
                } else {
                    getActivity().stopService(intent);
                    toggleWorkoutButton.setText(R.string.start_workout);
                    Log.i(TAG, "WorkoutTrackerService stopped");
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

                //googleMap.addPolyline(new PolylineOptions().addAll(RecordWorkoutActivity.getLocationsToDraw()));
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
    public void onNewLocation(ArrayList<LatLng> locationsToDraw)
    {
        if (mPolyline != null)
            mPolyline.remove();
        mPolyline = googleMap.addPolyline(new PolylineOptions().addAll(locationsToDraw));
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
