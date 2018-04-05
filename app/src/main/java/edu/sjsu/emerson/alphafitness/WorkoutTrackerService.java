package edu.sjsu.emerson.alphafitness;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class WorkoutTrackerService extends Service
{
    WorkoutAidl.Stub mBinder;

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
            public int square(int value) throws RemoteException
            {
                return value * value;
            }

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

}
