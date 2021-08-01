package com.example.goplot;

import android.location.Location;

interface RecordRouteServiceCallback {
    // used to broadcast the new location to the list of IBinder objects maintained by the Service
    // in our case, the Activity registers its callback to receive new locations, which are processed by the ViewModel
    void update(Location newLocation);
}
