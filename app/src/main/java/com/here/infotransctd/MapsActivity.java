package com.here.infotransctd;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.here.infotransctd.GPS.GPS_Service;
import com.here.infotransctd.HereExamples.*;
import com.here.infotransctd.HereExamples.PermissionsRequestor.ResultListener;
import com.here.infotransctd.Interdictions.Interdiction;
import com.here.infotransctd.Interdictions.InterdictionsList;
import com.here.infotransctd.Interdictions.Local_Interdiction;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.mapviewlite.Camera;
import com.here.sdk.mapviewlite.MapStyle;
import com.here.sdk.mapviewlite.MapViewLite;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity
{
    private PermissionsRequestor permissionsRequestor;
    private MapViewLite mapView;
    public AutoCompleteTextView txtOrigin, txtDestination;
    private Location mLocation;
    private SearchExample searchExample;
    private final Context context = this;
    private RoutingExample routingExample;
    private PlatformPositioningProvider platformPositioningProvider;
    private Camera camera;
    private List<Interdiction> interdictions;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ImageButton btnList = findViewById(R.id.btnList);
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        permissionsRequestor = new PermissionsRequestor(this);
        permissionsRequestor.request(new ResultListener()
        {
            @Override
            public void permissionsGranted()
            {
                mapView.getMapScene().loadScene(MapStyle.NORMAL_DAY, errorCode -> {
                    if (errorCode == null)
                    {
                        camera = mapView.getCamera();
                        platformPositioningProvider = new PlatformPositioningProvider(context);
                        mLocation = platformPositioningProvider.getLocation();
                        if (mLocation == null)
                        {
                            mLocation = new Location("");
                            mLocation.setLatitude(-21.132017);
                            mLocation.setLongitude(-48.971795);
                        }
                        camera.setTarget(new GeoCoordinates(mLocation.getLatitude(),
                                mLocation.getLongitude()));
                        camera.setZoomLevel(12);
                    }
                });
            }

            @Override
            public void permissionsDenied()
            {
            }
        });
        searchExample = new SearchExample(context, mapView, getIntent().getExtras()
                .getString("meansOfTransport"));
        routingExample = new RoutingExample(context, mapView, searchExample);
        platformPositioningProvider = new PlatformPositioningProvider(context);
        FirebaseDatabase.getInstance().getReference("interdictions")
                .addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                btnList.setEnabled(false);
                interdictions = new ArrayList<>();
                routingExample.cleanInterdictions();
                try
                {
                    for (DataSnapshot keyNode : dataSnapshot.getChildren())
                    {
                        Interdiction interdiction = new Interdiction();
                        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                                Locale.ITALY);
                        String beginDate = Objects.requireNonNull(
                                keyNode.child("beginDate").getValue()).toString();
                        String endDate = Objects
                                .requireNonNull(keyNode.child(
                                        "endDate").getValue()).toString();
                        Date dateBegin = new Date(Objects
                                .requireNonNull(df.parse(beginDate)).getTime());
                        Date dateEnd = new Date(Objects
                                .requireNonNull(df.parse(endDate)).getTime());
                        String formatedDate = df.format(Calendar.getInstance().getTime());
                        Date dateToday =
                                new Date(Objects.requireNonNull(df.parse(formatedDate)).getTime());
                        if (Objects.requireNonNull(keyNode.child(
                                "status").getValue()).toString().equals("true"))
                            if ((dateToday.after(dateBegin) && dateToday.before(dateEnd)) ||
                                    formatedDate.equals(dateBegin.toString()) ||
                                    formatedDate.equals(dateEnd.toString()))
                        {
                            interdiction.setBeginDate(beginDate);
                            interdiction.setEndDate(endDate);
                            interdiction.setOrganization(Objects.requireNonNull(
                                    keyNode.child("organization").getValue()).toString());
                            interdiction.setDescription(Objects.requireNonNull(
                                    keyNode.child("description").getValue()).toString());
                            interdiction.setOrigin(new Local_Interdiction(
                                    Objects.requireNonNull(keyNode.child("origin").child(
                                            "street").getValue()).toString(),
                                    Objects.requireNonNull(keyNode.child("origin").child(
                                            "lat").getValue()).toString(),
                                    Objects.requireNonNull(keyNode.child("origin").child(
                                            "lng").getValue()).toString()));
                            interdiction.setDestination(new Local_Interdiction(
                                    Objects.requireNonNull(keyNode.child(
                                    "destination").child("street").getValue()).toString(),
                                    Objects.requireNonNull(keyNode.child("destination").child(
                                            "lat").getValue()).toString(),
                                    Objects.requireNonNull(keyNode.child("destination").child(
                                            "lng").getValue()).toString()));
                            interdictions.add(interdiction);
                            routingExample.addInterdiction(
                                    new GeoCoordinates(
                                            Double.parseDouble(interdiction.getOrigin().getLat()),
                                            Double.parseDouble(interdiction.getOrigin().getLng())),
                                    new GeoCoordinates(
                                            Double.parseDouble(
                                                    interdiction.getDestination().getLat()),
                                            Double.parseDouble(
                                                    interdiction.getDestination().getLng())));
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                btnList.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                RoutingExample.showDialog("Não foi possível acessar as interdições",
                        "Por favor, verifique a conexão com a internet e " +
                                "se o dispositivo usa a data e hora fornecidas pela rede",
                        context);
            }
        });

        findViewById(R.id.btnSearch).setOnClickListener(v -> searchExample.searchInViewport(
                txtOrigin.getText().toString(), txtDestination.getText().toString()));

        btnList.setOnClickListener(v -> {
            try
            {
                Intent i = new Intent(getApplicationContext(), InterdictionsList.class);
                i.putExtra("interdictions", (Serializable) interdictions);
                startActivity(i);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        txtOrigin = findViewById(R.id.txtOrigin);
        txtOrigin.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                searchExample.autoSuggestExample(s, txtOrigin);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }
        });
        txtDestination = findViewById(R.id.txtDestination);
        txtDestination.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                searchExample.autoSuggestExample(s, txtDestination);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsRequestor.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
        stopService(i);
        finish();
        mapView.onDestroy();
        platformPositioningProvider.stopLocating();
    }
}