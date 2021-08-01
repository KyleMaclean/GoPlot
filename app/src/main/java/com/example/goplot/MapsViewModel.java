package com.example.goplot;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

// used in two distinct ways: to display all "marked" Spots and to display a specific Route
public class MapsViewModel extends AndroidViewModel {

    private static final float DEFAULT_ZOOM = 14.5f;
    final SpotDao spotDao;
    private GoogleMap map;

    public MapsViewModel(@NonNull Application application) {
        super(application);
        GoPlotRoomDatabase goPlotRoomDatabase = GoPlotRoomDatabase.getDatabase(application);
        spotDao = goPlotRoomDatabase.spotDao();
    }

    // used by: button "Show Map" in SingleRouteActivity
    public LiveData<List<Spot>> getSpotsForRoute(int routeId) {
        return spotDao.getSpotsForRouteIdAsLiveDataList(routeId);
    }

    // used by: button "Show Map" in SingleRouteActivity
    public void setupMap(GoogleMap googleMap, List<Spot> spots) {
        map = googleMap;
        Polyline polyline = map.addPolyline(new PolylineOptions().clickable(false));
        ArrayList<LatLng> latLngs = new ArrayList<>();
        for (Spot spot : spots) {
            LatLng latLng = new LatLng(spot.getLatitude(), spot.getLongitude());
            // places a Marker on all "marked" Spots in the Route
            if (spot.isMarked()) {
                map.addMarker(new MarkerOptions().position(latLng));
            }
            latLngs.add(latLng);
        }
        // draws a polyline between all constituent Spots of a Route
        polyline.setPoints(latLngs);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(0), DEFAULT_ZOOM));
    }

    // used by: button "Show Markers" in MainActivity
    public LiveData<List<Spot>> getMarkedSpots() {
        return spotDao.getMarkedSpots();
    }

    // used by: button "Show Markers" in MainActivity
    public void placeMarkers(GoogleMap googleMap, List<Spot> markedSpots) {
        map = googleMap;
        LatLng latLng = null;
        // the Query only returns "marked" Spots so we just add a Marker on the map for each of them
        for (Spot markedSpot : markedSpots) {
            latLng = new LatLng(markedSpot.getLatitude(), markedSpot.getLongitude());
            map.addMarker(new MarkerOptions().position(latLng));
        }
        if (latLng != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
    }
}
