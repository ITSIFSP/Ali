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
/*
* O arquivo original foi modificado e está disponível em:
* https://github.com/heremaps/here-sdk-examples/blob/master/examples/latest/lite/android/SearchLite/
* app/src/main/java/com/here/search/SearchExample.java
* */
import android.content.Context;
import android.text.Editable;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.annotation.NonNull;
import com.here.infotransctd.R;
import com.here.sdk.core.Anchor2D;
import com.here.sdk.core.CustomMetadataValue;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.Metadata;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapviewlite.MapImage;
import com.here.sdk.mapviewlite.MapImageFactory;
import com.here.sdk.mapviewlite.MapMarker;
import com.here.sdk.mapviewlite.MapMarkerImageStyle;
import com.here.sdk.mapviewlite.MapViewLite;
import com.here.sdk.search.Place;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchError;
import com.here.sdk.search.SearchOptions;
import com.here.sdk.search.Suggestion;
import com.here.sdk.search.TextQuery;
import java.util.List;

public class SearchExample
{
    private final Context context;
    private final MapViewLite mapView;
    private final SearchEngine searchEngine;
    private short flagOrigin = 0;
    private GeoCoordinates origin, destination;
    private final Metadata metadataOrigin = new Metadata();
    private final RoutingExample routingExample;
    private final String meanOfTransport;
    private final SearchOptions searchOptions = new SearchOptions();
    private final String[] suggestions = new String[5];

    public SearchExample(Context context, MapViewLite mapView, String meanOfTransport)
    {
        this.context = context;
        this.mapView = mapView;
        this.meanOfTransport = meanOfTransport;
        routingExample = new RoutingExample(context, mapView, this);
        searchOptions.languageCode = LanguageCode.PT_BR;
        searchOptions.maxItems = 5;
        try
        {
            searchEngine = new SearchEngine();
        }
        catch (InstantiationErrorException e)
        {
            throw new RuntimeException("Initialization of SearchEngine failed: " + e.error.name());
        }
    }

    void pickMapMarker(final Point2D point2D)
    {
        mapView.pickMapItems(point2D, 2, pickMapItemsResult ->
        {
            if (pickMapItemsResult == null) return;
            MapMarker topmostMapMarker = pickMapItemsResult.getTopmostMarker();
            if (topmostMapMarker == null) return;
            Metadata metadata = topmostMapMarker.getMetadata();
            if (metadata != null)
            {
                CustomMetadataValue customMetadataValue =
                        metadata.getCustomValue("key_search_result");
                if (customMetadataValue != null)
                {
                    SearchExample.SearchResultMetadata searchResultMetadata =
                            (SearchExample.SearchResultMetadata) customMetadataValue;
                    RoutingExample.showDialog(searchResultMetadata.searchResult.getTitle(),
                            searchResultMetadata.searchResult.getAddress().addressText, context);
                }
            }
        });
    }

    public void searchInViewport(String queryStringOrigin, String queryStringDestination)
    {
        searchEngine.search(new TextQuery(queryStringOrigin, mapView.getCamera().getBoundingBox()),
                new SearchOptions(LanguageCode.PT_BR, 1), (searchError, list) ->
        {
            if(list != null)
            {
                if (searchError != null)
                {
                    if(flagOrigin == 0)
                    {
                        flagOrigin = 1;
                        searchInViewport(queryStringDestination, queryStringOrigin);
                    }
                    else
                    {
                        if(flagOrigin == 2)
                            RoutingExample.showDialog("Não foi possível calcular a rota",
                                    "Por favor, confira o destino inserido " +
                                            "e tente novamente", context);
                        else RoutingExample.showDialog("Não foi possível calcular a rota",
                                "Por favor, confira os locais inseridos e " +
                                        "tente novamente", context);
                        flagOrigin = 0;
                    }
                }
                else
                {
                    switch(flagOrigin)
                    {
                        case 0:
                            origin = list.get(0).getGeoCoordinates();
                            metadataOrigin.setCustomValue("key_search_result",
                                    new SearchResultMetadata(list.get(0)));
                            flagOrigin = 2;
                            searchInViewport(queryStringDestination, queryStringOrigin);
                            return;
                        case 1:
                            RoutingExample.showDialog("Não foi possível calcular a rota",
                                    "Por favor, confira a origem inserida e " +
                                            "tente novamente",
                                    context);
                            flagOrigin = 0;
                            return;
                        default:
                            destination = list.get(0).getGeoCoordinates();
                            Metadata metadataDestination = new Metadata();
                            metadataDestination.setCustomValue("key_search_result",
                                    new SearchResultMetadata(list.get(0)));
                            routingExample.addRoute(meanOfTransport, origin, destination,
                                    metadataOrigin, metadataDestination);
                            flagOrigin = 0;
                    }
                }
            }
        });
    }

    static class SearchResultMetadata implements CustomMetadataValue
    {
        public final Place searchResult;
        public SearchResultMetadata(Place searchResult) {
            this.searchResult = searchResult;
        }

        @NonNull
        @Override
        public String getTag() {
            return "SearchResult Metadata";
        }
    }

    public void autoSuggestExample(Editable s, AutoCompleteTextView autoCompleteTextView)
    {
        searchEngine.suggest(new TextQuery(s.toString(),
                        mapView.getCamera().getBoundingBox()),
                searchOptions, (searchError, list) ->
                        autoCompleteTextView.setAdapter(getSuggestions(searchError, list)));
    }

    private ArrayAdapter<String> getSuggestions(SearchError searchError, List<Suggestion> list)
    {
        if (searchError != null || list == null) return null;
        int i = 0;
        String addressText;
        Place place;
        for (Suggestion autoSuggestResult : list)
        {
            addressText = "";
            place = autoSuggestResult.getPlace();
            if (place != null) addressText = place.getAddress().addressText;
            if(!addressText.equals("")) suggestions[i] = addressText;
            i += 1;
        }
        return new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, suggestions);
    }

    void addPoiMapMarker(GeoCoordinates geoCoordinates, Metadata metadata,
                              List<MapMarker> mapMarkerList, Context context,
                              MapViewLite mapView)
    {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), R.drawable.poi);
        MapMarker mapMarker = new MapMarker(geoCoordinates);
        MapMarkerImageStyle mapMarkerImageStyle = new MapMarkerImageStyle();
        mapMarkerImageStyle.setAnchorPoint(new Anchor2D(0.5F, 1));
        mapMarker.addImage(mapImage, mapMarkerImageStyle);
        mapMarker.setMetadata(metadata);
        mapView.getMapScene().addMapMarker(mapMarker);
        mapMarkerList.add(mapMarker);
    }
}