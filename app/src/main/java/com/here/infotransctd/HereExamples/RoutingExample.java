package com.here.infotransctd.HereExamples;
/*
 * Copyright (C) 2019-2021 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */
/* O arquivo original foi modificado e está disponível em:
 https://github.com/heremaps/here-sdk-examples/blob/master/examples/latest/lite/android/RoutingLite/
 app/src/main/java/com/here/routing/RoutingExample.java
*/
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.here.infotransctd.R;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.Metadata;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapviewlite.MapImage;
import com.here.sdk.mapviewlite.MapImageFactory;
import com.here.sdk.mapviewlite.MapMarker;
import com.here.sdk.mapviewlite.MapMarkerImageStyle;
import com.here.sdk.mapviewlite.MapPolyline;
import com.here.sdk.mapviewlite.MapPolylineStyle;
import com.here.sdk.mapviewlite.MapViewLite;
import com.here.sdk.mapviewlite.PixelFormat;
import com.here.sdk.routing.CarOptions;
import com.here.sdk.routing.OptimizationMode;
import com.here.sdk.routing.PedestrianOptions;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.ScooterOptions;
import com.here.sdk.routing.TruckOptions;
import com.here.sdk.routing.Waypoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class RoutingExample
{
    private final Context context;
    private final MapViewLite mapView;
    private final List<MapMarker> mapMarkerListUser = new ArrayList<>();
    private final List<MapPolyline> mapPolylinesUser = new ArrayList<>();
    private final List<MapMarker> mapMarkerListInterdictions = new ArrayList<>();
    private final List<MapPolyline> mapPolylinesInterdictions = new ArrayList<>();
    private final RoutingEngine routingEngine;
    private final SearchExample searchExample;

    public RoutingExample(Context context, MapViewLite mapView, SearchExample searchExample)
    {
        this.context = context;
        this.mapView = mapView;
        this.searchExample = searchExample;
        try
        {
            routingEngine = new RoutingEngine();
        }
        catch (InstantiationErrorException e)
        {
            throw new RuntimeException("Initialization of RoutingEngine failed: " + e.error.name());
        }
    }

    void addRoute(String meanOfTransport, GeoCoordinates startGeoCoordinates,
                         GeoCoordinates destinationGeoCoordinates, Metadata metadataOrigin,
                         Metadata metadataDestination)
    {
        for (MapMarker mapMarker : mapMarkerListUser)
            mapView.getMapScene().removeMapMarker(mapMarker);
        mapMarkerListUser.clear();
        for (MapPolyline mapPolyline : mapPolylinesUser)
            mapView.getMapScene().removeMapPolyline(mapPolyline);
        mapPolylinesUser.clear();
        List<Waypoint> waypoints = new ArrayList<>(Arrays.asList(new Waypoint(startGeoCoordinates),
                new Waypoint(destinationGeoCoordinates)));
        switch(meanOfTransport)
        {
            case "Car":
                CarOptions carOptions = new CarOptions();
                carOptions.routeOptions.alternatives = 2;
                routingEngine.calculateRoute(waypoints, carOptions, (routingError, routes) ->
                        routeVerification(routingError, routes, false));
                break;
            case "Motorcycle":
                ScooterOptions motorcycleOptions = new ScooterOptions();
                motorcycleOptions.routeOptions.alternatives = 2;
                routingEngine.calculateRoute(waypoints, motorcycleOptions, (routingError, routes) ->
                        routeVerification(routingError, routes, false));
                break;
            case "Truck":
                TruckOptions truckOptions = new TruckOptions();
                truckOptions.routeOptions.alternatives = 2;
                routingEngine.calculateRoute(waypoints, truckOptions, (routingError, routes) ->
                        routeVerification(routingError, routes, false));
                break;
            default:
                PedestrianOptions pedestrianOptions = new PedestrianOptions();
                pedestrianOptions.routeOptions.alternatives = 2;
                routingEngine.calculateRoute(waypoints, pedestrianOptions, (routingError, routes) ->
                        routeVerification(routingError, routes,false));
                break;
        }
    }

    public void addInterdiction(GeoCoordinates startGeoCoordinates,
                                GeoCoordinates destinationGeoCoordinates)
    {
        addCircleMapMarker(startGeoCoordinates);
        addCircleMapMarker(destinationGeoCoordinates);
        PedestrianOptions pedestrianOptions = new PedestrianOptions();
        pedestrianOptions.routeOptions.optimizationMode = OptimizationMode.SHORTEST;
        routingEngine.calculateRoute(new ArrayList<>(Arrays.asList(new
                        Waypoint(startGeoCoordinates), new Waypoint(destinationGeoCoordinates))),
                pedestrianOptions, (routingError, routes) -> routeVerification(routingError, routes,
                        true));
    }

    private void routeVerification(@Nullable RoutingError routingError, @Nullable List<Route>
            routes, boolean interdiction)
    {
        if (routingError == null && routes != null)
        {
            if(!interdiction)
            {
                String routeDetails;
                showRouteOnMap(routes.get(0), false, true);
                if(routes.size() > 1)
                {
                    showRouteOnMap(routes.get(1), false, false);
                    routeDetails = "Rota azul: \nTempo aproximado: " +
                            formatTime(routes.get(0).getDurationInSeconds()) + "\nDistância: " +
                            formatLength(routes.get(0).getLengthInMeters()) +
                            "\n\nRota roxa: " + "\nTempo aproximado: " +
                            formatTime(routes.get(1).getDurationInSeconds()) + "\nDistância: " +
                            formatLength(routes.get(1).getLengthInMeters());
                }
                else routeDetails = "Tempo aproximado: " + formatTime(routes.get(0).
                        getDurationInSeconds()) + "\nDistância: " +
                        formatLength(routes.get(0).getLengthInMeters());
                showDialog("Informações", routeDetails, context);
                mapView.getGestures().setTapListener(searchExample::pickMapMarker);
            }
            else
            {
                showRouteOnMap(routes.get(0), true,true);
            }
        }
        else showDialog("Não foi possível obter a rota", "Erro: " + routingError,
                context);
    }

    private String formatTime(long sec)
    {
        return String.format(Locale.getDefault(), "%01dh%02d", sec / 3600,
                (sec % 3600) / 60);
    }

    private String formatLength(int meters)
    {
        return String.format(Locale.getDefault(), "%01d,%02dkm", meters / 1000,
                meters % 1000);
    }

    private void showRouteOnMap(Route route, boolean interdiction, boolean blue)
    {
        // Show route as polyline.
        GeoPolyline routeGeoPolyline;
        try
        {
            routeGeoPolyline = new GeoPolyline(route.getPolyline());
        }
        catch (InstantiationErrorException e)
        {
            // It should never happen that the route polyline contains less than two vertices.
            return;
        }
        MapPolylineStyle mapPolylineStyle = new MapPolylineStyle();
        if (!interdiction)
        {
            searchExample.addPoiMapMarker(startGeoCoordinates, metadataOrigin,
                mapMarkerListUser, context, mapView);
            searchExample.addPoiMapMarker(destinationGeoCoordinates,
                metadataDestination, mapMarkerListUser, context, mapView);
            if(blue) mapPolylineStyle.setColor(0x00908AA0, PixelFormat.RGBA_8888);
            else mapPolylineStyle.setColor(0x900080A0, PixelFormat.RGBA_8888);
        }
        else mapPolylineStyle.setColor(0xFF0000FF, PixelFormat.RGBA_8888);
        mapPolylineStyle.setWidthInPixels(10);
        MapPolyline routeMapPolyline = new MapPolyline(routeGeoPolyline, mapPolylineStyle);
        mapView.getMapScene().addMapPolyline(routeMapPolyline);
        if (!interdiction) mapPolylinesUser.add(routeMapPolyline);
        else mapPolylinesInterdictions.add(routeMapPolyline);
    }

    public void cleanInterdictions()
    {
        for (MapMarker mapMarker : mapMarkerListInterdictions)
            mapView.getMapScene().removeMapMarker(mapMarker);
        mapMarkerListInterdictions.clear();
        for (MapPolyline mapPolyline : mapPolylinesInterdictions)
            mapView.getMapScene().removeMapPolyline(mapPolyline);
        mapPolylinesInterdictions.clear();
    }

    private void addCircleMapMarker(GeoCoordinates geoCoordinates)
    {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(),
                R.drawable.red_dot);
        MapMarker mapMarker = new MapMarker(geoCoordinates);
        mapMarker.addImage(mapImage, new MapMarkerImageStyle());
        mapView.getMapScene().addMapMarker(mapMarker);
        mapMarkerListInterdictions.add(mapMarker);
    }

    public static void showDialog(String title, String message, Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, id) -> { });
        builder.show();
    }
}
