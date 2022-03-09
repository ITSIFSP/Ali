/*
* O arquivo original foi modificado e está disponível em:
* https://developer.here.com/documentation/android-sdk-lite/4.9.4.0/dev_guide/topics/
* positioning.html
* */
package com.here.infotransctd.HereExamples;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import static android.content.Context.LOCATION_SERVICE;

// A simple Android based positioning implementation.
public class PlatformPositioningProvider implements LocationListener
{
    public static final int LOCATION_UPDATE_INTERVAL_IN_MS = 100;
    private final Context context;
    private LocationManager locationManager;
    Location myLocation;
    @Nullable
    private PlatformLocationListener platformLocationListener;

    public interface PlatformLocationListener
    {
        void onLocationUpdated(Location location);
    }

    public PlatformPositioningProvider(Context context) {
        this.context = context;
    }

    public Location getLocation()
    {
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)
        {
            LocationManager mLocationManager = (LocationManager)
                    context.getSystemService(LOCATION_SERVICE);
            this.startLocating(location -> {
            });
            myLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            stopLocating();
            return myLocation;
        }
        else
        {
            ActivityCompat.requestPermissions((Activity)context, new String[]
                            {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            },
                    1);

            return null;
        }
    }

    @Override
    public void onLocationChanged(android.location.Location location)
    {
        if (platformLocationListener != null) platformLocationListener.onLocationUpdated(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onProviderDisabled(String provider)
    {
    }

    public void startLocating(PlatformLocationListener locationCallback)
    {
        if (this.platformLocationListener != null)
            throw new RuntimeException("Please stop locating before starting again.");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) return;
        this.platformLocationListener = locationCallback;
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_INTERVAL_IN_MS, 1,this);
         else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_INTERVAL_IN_MS, 1,this);
         else stopLocating();
    }

    public void stopLocating()
    {
        if (locationManager == null) return;
        locationManager.removeUpdates(this);
        platformLocationListener = null;
    }
}