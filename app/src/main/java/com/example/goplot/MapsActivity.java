package com.example.goplot;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

// used in two distinct ways: to display all "marked" Spots and to display a specific Route
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    MapsViewModel mapsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(MapsViewModel.class);
        setContentView(R.layout.activity_maps);
        // obtain  SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        int routeId = getIntent().getIntExtra("routeId", -1);
        if (routeId == -1) {
            // the user tapped "Show Markers" from MainActivity
            mapsViewModel.getMarkedSpots().observe(this, event -> mapsViewModel.placeMarkers(googleMap, event));
        } else {
            // the user tapped "Show Map" from SingleRouteActivity
            mapsViewModel.getSpotsForRoute(routeId).observe(this, event -> mapsViewModel.setupMap(googleMap, event));
        }
    }
}