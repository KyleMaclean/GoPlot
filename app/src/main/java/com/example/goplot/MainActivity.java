package com.example.goplot;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // for efficiency, handle location issues before starting the Activity which requires them
    public void onClickRecordRoute(View v) {
        // firstly, check location permission
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // secondly, since we have permission, check whether location is enabled
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager.isLocationEnabled()) {
                // since location is granted and enabled, start RecordRouteActivity
                Intent intent = new Intent(MainActivity.this, RecordRouteActivity.class);
                startActivity(intent);
            } else {
                // we have permission but location is not enabled, so encourage the user to enable it
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Location not enabled");
                alertDialog.setMessage("Open settings?");
                alertDialog.setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                });
                alertDialog.setNegativeButton("No", (dialog, which) -> dialog.cancel());
                alertDialog.show();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // this is where ActivityCompat.requestPermissions(...) returns to
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // result array is empty if the request was cancelled
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MainActivity.this, RecordRouteActivity.class);
                startActivity(intent);
            }
        }
    }

    public void onClickListRoutes(View v) {
        Intent intent = new Intent(MainActivity.this, ListRoutesActivity.class);
        startActivity(intent);
    }

    public void onClickAnalyseStatistics(View v) {
        Intent intent = new Intent(MainActivity.this, AnalyseStatisticsActivity.class);
        startActivity(intent);
    }

    public void onClickShowMarkers(View v) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    public void onClickTrackGoals(View v) {
        Intent intent = new Intent(MainActivity.this, TrackGoalsActivity.class);
        startActivity(intent);
    }

}