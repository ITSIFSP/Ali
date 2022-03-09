package com.here.infotransctd;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.splashscreen.SplashScreen;
import com.google.firebase.FirebaseApp;
import com.here.infotransctd.GPS.GPS_Service;

public class MainActivity extends AppCompatActivity
{
    private RadioGroup rgVehicles;
    private RadioButton car, motorcycle, truck, onFoot;
    private Context context;

    private void runtime_permissions()
    {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1)
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                runtime_permissions();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        try
        {
            setContentView(R.layout.activity_main);
            context = this;
            FirebaseApp.initializeApp(this);
            if(getSupportActionBar() != null) getSupportActionBar().setElevation(0);
            rgVehicles = findViewById(R.id.rgVehicles);
            car = findViewById(R.id.car);
            motorcycle = findViewById(R.id.motorcycle);
            truck = findViewById(R.id.truck);
            onFoot = findViewById(R.id.onFoot);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        runtime_permissions();
        rgVehicles.setOnCheckedChangeListener((group, checkedId) -> {
            try
            {
                if ((ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) &&
                        (ActivityCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED)) return;
                    Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("meansOfTransport", defineVehicle(checkedId));
                    i.putExtras(bundle);
                    startService(i);
                    Intent maps = new Intent(getApplicationContext(), MapsActivity.class);
                    maps.putExtras(bundle);
                    startActivity(maps);
                    car.setEnabled(false);
                    motorcycle.setEnabled(false);
                    truck.setEnabled(false);
                    onFoot.setEnabled(false);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (isServiceRunning())
        {
            car.setEnabled(false);
            truck.setEnabled(false);
            motorcycle.setEnabled(false);
            onFoot.setEnabled(false);
        }
    }

    private String defineVehicle(int checkedId)
    {
        //Defines the vehicle
        if (checkedId == R.id.car) return "Car";
        else if (checkedId == R.id.motorcycle) return "Motorcycle";
        else if (checkedId == R.id.truck) return "Truck";
        else return "OnFoot";
    }

    private boolean isServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE))
            if (GPS_Service.class.getName().equals(service.service.getClassName())) return true;
        return false;
    }
}
