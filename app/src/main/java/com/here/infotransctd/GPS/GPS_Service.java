package com.here.infotransctd.GPS;

import static java.lang.Math.abs;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GPS_Service extends Service
{
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String meansOfTransport, initDate;
    private ArrayList<LocationData> listOfLocation = new ArrayList<>();
    private double speedSum;
    private final DatabaseReference myRefLat = FirebaseDatabase.getInstance()
            .getReference("locations");
    private CountDownTimer inactiveCounter, activeCounter;
    private short state = 0;
    private final SimpleDateFormat simpleDateFormat = new
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ITALY);

    @RequiresApi(api = Build.VERSION_CODES.O)
    public GPS_Service()
    {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        meansOfTransport = (String) intent.getExtras().get("meansOfTransport");
        return START_STICKY;
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        newNode();
        if (locationManager != null) locationManager.removeUpdates(locationListener);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate()
    {
        inactiveCounter = new CountDownTimer(120000, 1)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
            }

            @Override
            public void onFinish()
            {
                finishCounter();
            }
        };

        inactiveCounter.cancel();

        activeCounter = new CountDownTimer(300000, 1)
        {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish()
            {
                finishCounter();
            }
        };

        activeCounter.cancel();

        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                LocationData lastLocation = new LocationData(location.getLatitude(),
                        location.getLongitude(),
                        simpleDateFormat.format(new Date()));
                if(state == 0)
                {
                    listOfLocation = new ArrayList<>();
                    listOfLocation.add(new
                            LocationData(lastLocation.getLatitude(), lastLocation.getLongitude(),
                        simpleDateFormat.format(new Date())));
                    state = 1;
                }

                else if(abs(lastLocation.getLatitude() - listOfLocation.get(listOfLocation.size()
                        - 1).getLatitude()) > 0.0005 || abs(lastLocation.getLongitude() -
                        listOfLocation.get(listOfLocation.size() - 1).getLongitude()) > 0.0005)
                {
                    listOfLocation.add(lastLocation);
                    speedSum += location.getSpeed() * 3.6;
                    if(state == 1)
                    {
                        initDate = simpleDateFormat.format(new Date());
                        listOfLocation.remove(0);
                        speedSum = 0.0;
                        activeCounter.start();
                        state = 2;
                    }
                    else inactiveCounter.cancel();
                    inactiveCounter.start();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider)
            {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        };
        if (locationManager != null) locationManager.removeUpdates(locationListener);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                3000, 0, locationListener);
    }

    private void finishCounter()
    {
        state = 0;
        activeCounter.cancel();
        inactiveCounter.cancel();
        newNode();
        listOfLocation = new ArrayList<>();
    }

    private void newNode()
    {
        if (listOfLocation.size() > 1)
        {
            LocationData lastLocation = listOfLocation.get(listOfLocation.size() - 1);
            myRefLat.push().setValue(new RouteDate(initDate,
                    simpleDateFormat.format(new Date()),
                    listOfLocation, speedSum / listOfLocation.size(), meansOfTransport,
                    cityName(lastLocation.getLatitude(), lastLocation.getLongitude())));
        }
    }

    private String cityName(double lat, double lon)
    {
        String cityName = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try
        {
            addresses = geocoder.getFromLocation(lat, lon, 10);
            if (addresses.size() > 0)
            {
                for (Address address : addresses)
                {
                    if (address.getLocality() != null && address.getLocality().length() > 0)
                    {
                        cityName = address.getLocality();
                        break;
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return cityName;
    }
}
