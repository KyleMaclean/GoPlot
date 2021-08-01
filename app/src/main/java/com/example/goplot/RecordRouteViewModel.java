package com.example.goplot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;

public class RecordRouteViewModel extends AndroidViewModel {

    private static final String START_TEXT = "Start";
    private static final String STOP_TEXT = "Stop";
    private final MutableLiveData<String> startStopButtonText = new MutableLiveData<>(START_TEXT);

    public static final int DEFAULT_ZOOM = 15;
    final RouteDao routeDao;
    final SpotDao spotDao;
    // initialisation of the data bound to the UI to show before the user starts recording
    private final MutableLiveData<Double> kilometres = new MutableLiveData<>((double) 0);
    private final MutableLiveData<String> time = new MutableLiveData<>("00:00");
    private final MutableLiveData<Double> pace = new MutableLiveData<>((double) 0);
    private final MutableLiveData<Boolean> placeMarkerButtonEnabled = new MutableLiveData<>(false);
    private boolean started = false;
    public Date startTime;
    // maintain three lists (LatLng, Location, Spot) because all formats are required for their different properties
    ArrayList<Spot> spots;
    ArrayList<LatLng> latLngs;
    ArrayList<Location> locations;
    // to store the Markers that the user places during recording so that if the Route is cancelled, they can be systematically removed
    ArrayList<Marker> markers;
    // this Date reference is assigned a new object every time a new location is received
    // we could use LocalDate.now() but we use the older Date class for lower API compatibility
    Date currentTime;
    private double metres;
    private Polyline polyline;
    // the map displayed in the Activity is maintained and manipulated in the ViewModel
    private GoogleMap map;

    public RecordRouteViewModel(@NonNull Application application) {
        super(application);
        GoPlotRoomDatabase goPlotRoomDatabase = GoPlotRoomDatabase.getDatabase(application);
        routeDao = goPlotRoomDatabase.routeDao();
        spotDao = goPlotRoomDatabase.spotDao();
    }

    @SuppressLint("DefaultLocale")
    @BindingAdapter("android:text")
    // custom adapter to update the UI with the two data in "double" format: distance and pace
    public static void setDouble(TextView textView, LiveData<Double> doubleLiveData) {
        textView.setText(String.format("%05.2f", doubleLiveData.getValue()));
    }

    public LiveData<Double> getPace() {
        return pace;
    }

    public LiveData<Double> getKilometres() {
        return kilometres;
    }

    public LiveData<String> getTime() {
        return time;
    }

    public LiveData<String> getStartStopButtonText() {
        return startStopButtonText;
    }

    public LiveData<Boolean> getPlaceMarkerButtonEnabled() {
        return placeMarkerButtonEnabled;
    }

    public boolean isStarted() {
        return started;
    }

    // all instance fields are initialised in this method so that old values are never used in a new Route
    public void startRoute() {
        polyline = map.addPolyline(new PolylineOptions().clickable(false));
        latLngs = new ArrayList<>();
        locations = new ArrayList<>();
        spots = new ArrayList<>();
        markers = new ArrayList<>();
        metres = 0;
        polyline.setPoints(latLngs);
        startTime = new Date();
        startStopButtonText.setValue(STOP_TEXT);
        placeMarkerButtonEnabled.setValue(true);
        started = true;
    }

    // creates Route and Spot objects and inserts them into the database when the user finishes recording
    public void stopRoute() {
        if (polyline != null) {
            Date endTime = new Date();
            Route route = new Route(metres, startTime, endTime);
            GoPlotRoomDatabase.databaseWriteExecutor.execute(() -> {
                int routeId = (int) routeDao.insert(route);
                for (Spot spot : spots) {
                    spot.setRouteId(routeId);
                }
                spotDao.insertAll(spots);
            });
            resetUI();
        }
    }

    // called in two cases:
    // 1. the user stops recording a Route (it is saved) - the user can return to this Activity and record a new Route
    // 2. the user cancels recording a Route (it is not saved) - the user can record a new Route
    private void resetUI() {
        // the Polyline is cleared from the Map
        polyline.remove();
        // all Markers are cleared from the Map
        for (Marker marker : markers) {
            marker.remove();
        }
        kilometres.setValue((double) 0);
        time.setValue("00:00");
        pace.setValue((double) 0);
        startStopButtonText.setValue(START_TEXT);
        placeMarkerButtonEnabled.setValue(false);
        started = false;
    }

    public void addLocation(Location newLocation) {
        currentTime = new Date();
        double totalSeconds = currentTime.getTime() / 1000d - startTime.getTime() / 1000d;
        int minutes = (int) (totalSeconds / 60);
        int seconds = (int) (totalSeconds % 60);
        @SuppressLint("DefaultLocale") String timeValue = String.format("%02d:%02d", minutes, seconds);
        // update the time value now because it does not depend on the new location
        time.setValue(timeValue);
        // we only expect newLocation to be null when the user cancels recording through the notification button
        if (newLocation == null) {
            Log.d("mdp", "The Location object to be added is null. This indicates that the Service has stopped.");
            // the Route is not saved and the user can easily start recording a new one because the UI metrics are reset
            resetUI();
        } else {
            double latitude = newLocation.getLatitude();
            double longitude = newLocation.getLongitude();
            LatLng newLatLng = new LatLng(latitude, longitude);
            // Spots list is used to add to the Spot table once the Route has finished recording
            this.spots.add(new Spot(-1, latitude, longitude, false));
            // Locations list is used for the .distanceTo() method of the Location class
            this.locations.add(newLocation);
            // LatLngs list is used to move the camera according to each location update
            this.latLngs.add(newLatLng);
            int size = spots.size();
            if (size >= 2) {
                // maintain the distance in metres for more accurate calculations
                double metresDelta = locations.get(size - 1).distanceTo(locations.get(size - 2));
                metres += metresDelta;
                // kilometres are used in the UI
                kilometres.setValue(metres / 1000);
                // pace is a purely derived field
                pace.setValue((totalSeconds / 60d) / (metres / 1000d));
            }
            if (polyline != null) polyline.setPoints(latLngs);
            // when recording, the zoom is further out to see the polyline route in totality
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, DEFAULT_ZOOM));
        }
    }

    public void init(GoogleMap googleMap) {
        map = googleMap;
        // mandatory permissions check but we already checked the location and enabled status in MainActivity
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // we have location access so we turn on the My Location layer and display the location button
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
    }

    // bound to the "Place Marker" button to allow the user to "mark"/"star" specific Spots in their Route
    public void onMark() {
        if (polyline != null) {
            int lastSpot = spots.size() - 1;
            Spot spot = spots.get(lastSpot);
            // set the flag that this Spot is marked to persist this information in the database
            spot.setMarked(true);
            spots.set(lastSpot, spot);
            // add a Marker onto the map which takes up part of the RecordRoute UI
            // maintain the Marker so that it can be removed from the map when the user stops / cancels recording
            markers.add(map.addMarker(new MarkerOptions().position(latLngs.get(latLngs.size() - 1))));
        }
    }

}
