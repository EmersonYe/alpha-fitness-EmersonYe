package edu.sjsu.emerson.alphafitness;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

public class WorkoutTrackerService extends Service implements LocationListener
{
    WorkoutAidl.Stub mBinder;
    // use https://stackoverflow.com/questions/1513485/how-do-i-get-the-current-gps-location-programmatically-in-android

    public WorkoutTrackerService()
    {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mBinder = new WorkoutAidl.Stub()
        {

            @Override
            public void toggleStatus() throws RemoteException
            {
                System.out.println("Status has been toggled");
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onLocationChanged(Location location)
    {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {

    }

    @Override
    public void onProviderEnabled(String s)
    {

    }

    @Override
    public void onProviderDisabled(String s)
    {

    }
}
