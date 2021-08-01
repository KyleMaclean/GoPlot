package com.example.goplot;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.goplot.databinding.ActivityRecordRouteBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class RecordRouteActivity extends FragmentActivity implements OnMapReadyCallback {

    private RecordRouteViewModel recordRouteViewModel;

    // the new location which the Service gives us in the callback is sent via a lambda to the ViewModel to add to its list
    final RecordRouteServiceCallback callback = newLocation -> recordRouteViewModel.addLocation(newLocation);
    // reference to the Service's Binder for programmatic control
    private RecordRouteService.MyBinder myBinder;
    // defined in declaration because it is used to start and stop the Service
    private Intent recordRouteServiceIntent;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // saving a reference to the Service's binder and registering our callback with it
            myBinder = (RecordRouteService.MyBinder) service;
            myBinder.registerCallback(callback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myBinder = null;
        }
    };

    // called when user taps button to start recording: bind service and set the Intent to start recording
    private void startBindService() {
        recordRouteServiceIntent = new Intent(getApplicationContext(), RecordRouteService.class);
        recordRouteServiceIntent.setAction(RecordRouteService.START_RECORDING);
        startService(recordRouteServiceIntent);
        bindService(recordRouteServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // called when user taps button to stop recording: unbind from service and set the Intent to stop recording
    private void stopUnbindService() {
        recordRouteServiceIntent = new Intent(getApplicationContext(), RecordRouteService.class);
        unbindService(serviceConnection);
        recordRouteServiceIntent.setAction(RecordRouteService.STOP_RECORDING);
        startService(recordRouteServiceIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // connect ViewModel and set up data binding
        recordRouteViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(RecordRouteViewModel.class);
        com.example.goplot.databinding.ActivityRecordRouteBinding activityRecordRouteBinding = ActivityRecordRouteBinding.inflate(getLayoutInflater());
        View rootView = activityRecordRouteBinding.getRoot();
        setContentView(rootView);
        activityRecordRouteBinding.setLifecycleOwner(this);
        activityRecordRouteBinding.setViewmodel(recordRouteViewModel);

        // obtain SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // we cannot use data binding for this method because the Activity must start & bind to the Service
    // also, after stopping the Route, a new Activity is started, which should be performed by the Activity, not the ViewModel
    public void onClickStartStopButton(View view) {
        if (!recordRouteViewModel.isStarted()) {
            recordRouteViewModel.startRoute();
            startBindService();
        } else {
            if (recordRouteServiceIntent != null) {
                stopUnbindService();
                recordRouteViewModel.stopRoute();
                Intent intent = new Intent(RecordRouteActivity.this, ListRoutesActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // save map into the view model & turn on the My Location layer
        recordRouteViewModel.init(googleMap);
        // get the initial position of the device on which to centre the camera before recording
        // we have to do this permission check but we expect the permissions to already have been granted from the check in MainActivity
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // demonstrating best practice with the FusedLocationProviderClient in that we listen for a successful task to return the location
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Location firstLocation = task.getResult();
                if (firstLocation != null) {
                    double latitude = firstLocation.getLatitude();
                    double longitude = firstLocation.getLongitude();
                    // the first location is only used to set the camera for the first time
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), RecordRouteViewModel.DEFAULT_ZOOM));
                }
            }
        });
    }

}