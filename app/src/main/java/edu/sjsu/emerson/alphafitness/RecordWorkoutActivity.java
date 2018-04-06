package edu.sjsu.emerson.alphafitness;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class RecordWorkoutActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener
{
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;
    private Button mStartWorkoutButton;
    WorkoutAidl remoteService;
    RemoteConnection remoteConnection = null;

    class RemoteConnection implements ServiceConnection
    {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service)
        {
            remoteService = WorkoutAidl.Stub.asInterface(service);
            Toast.makeText(RecordWorkoutActivity.this, "Remote service connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            remoteService = null;
            Toast.makeText(RecordWorkoutActivity.this, "Remote service disconnected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_workout);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mStartWorkoutButton = this.findViewById(R.id.button_start);

        remoteConnection = new RemoteConnection();
        Intent intent = new Intent();
        intent.setClassName("edu.sjsu.emerson.alphafitness", WorkoutTrackerService.class.getName());
        if(!bindService(intent, remoteConnection, BIND_AUTO_CREATE)) {
            Toast.makeText(RecordWorkoutActivity.this, "Fail to bind the remote service.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // should this be after onDestroy
        unbindService(remoteConnection);
        remoteConnection = null;
    }


    @Override
    public boolean onMyLocationButtonClick()
    {
        Toast.makeText(RecordWorkoutActivity.this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
        onMyLocationButtonClick();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    public void toggleWorkoutState(View view)
    {
        try {
            remoteService.toggleStatus();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
