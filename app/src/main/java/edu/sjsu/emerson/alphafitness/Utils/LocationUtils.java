package edu.sjsu.emerson.alphafitness.Utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by emersonsjsu on 4/30/18.
 */

public class LocationUtils
{
    public static final String TAG = "LocationUtils";
    public static double distanceBetween(LatLng a, LatLng b) {

        Location locationA = new Location("Point A");
        locationA.setLatitude(a.latitude);
        locationA.setLongitude(a.longitude);
        Location locationB = new Location("Point B");
        locationB.setLatitude(b.latitude);
        locationB.setLongitude(b.longitude);

        return locationA.distanceTo(locationB);
    }
}
