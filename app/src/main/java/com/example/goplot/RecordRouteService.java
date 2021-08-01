package com.example.goplot;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class RecordRouteService extends Service {

    static final int LOCATION_REQUEST_INTERVAL = 4000;
    static final int LOW_BATTERY_LOCATION_REQUEST_INTERVAL = 10000;
    static final int LOCATION_SERVICE_ID = 33;
    static final String START_RECORDING = "start recording";
    static final String STOP_RECORDING = "stop recording";
    final IBinder myBinder = new MyBinder();
    final RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<>();
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                doCallbacks(locationResult.getLastLocation());
            }
        }
    };

    // this LocationRequest object is manipulated by this BroadcastReceiver implementation in the case of low battery
    LocationRequest locationRequest;
    // we use a dynamic BroadcastReceiver to receive Intent.ACTION_BATTERY_LOW which is contextually registered
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // this equality check will always succeed in our case because we only add the Intent.ACTION_BATTERY_LOW filter
            // we still perform the check to prevent misuse if other actions are added to the intent filter
            if (action.equals(Intent.ACTION_BATTERY_LOW)) {
                Log.d("mdp", "low battery: decreasing location request frequency");
                locationRequest.setInterval(LOW_BATTERY_LOCATION_REQUEST_INTERVAL);
                locationRequest.setFastestInterval(LOW_BATTERY_LOCATION_REQUEST_INTERVAL / 2);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // registering the implemented BroadcastReceiver for Intent.ACTION_BATTERY_LOW actions
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        registerReceiver(broadcastReceiver, intentFilter); // contextually register
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // lifecycle awareness: when the Service is destroyed, there is no need to listen to low battery broadcasts any more
        unregisterReceiver(broadcastReceiver); // contextually unregister
    }

    @Nullable
    @Override
    // provide the Binder to the Activity so that it can control the Service according to user interaction
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    // observer pattern for best practice scalability in which multiple observers can receive the new location updates
    public void doCallbacks(Location newLocation) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            remoteCallbackList.getBroadcastItem(i).callback.update(newLocation);
        }
        remoteCallbackList.finishBroadcast();
    }

    // builds notification and starts the Service in the foreground to handle location updates
    private void startLocationService() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // PendingIntent of the "Cancel Recording" button which calls this Service's onStartCommand with the STOP_RECORDING action
        PendingIntent cancelPendingIntent = PendingIntent.getService(getApplicationContext(), 0,
                new Intent(this, RecordRouteService.class).setAction(STOP_RECORDING), 0);
        // to be set as the ContentIntent so launch the Activity when the user taps the notification
        // note that this is achieved with the Activity having the property android:launchMode="singleTop" in the manifest
        PendingIntent recordRouteActivityPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(this, RecordRouteActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GoPlot")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentText("Recording Route")
                .setContentIntent(recordRouteActivityPendingIntent)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(0, "Cancel Recording", cancelPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null
                    && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId,
                        "RecordRouteService",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("RecordRouteService channel");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        // the default initialisation values of the LocationRequest object for when the battery is not low
        createLocationRequest();

        // required permission check but we already checked them in MainActivity when the user tapped the Record Route button
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        // start in foreground so that the Route recording is never unduly interrupted
        startForeground(LOCATION_SERVICE_ID, builder.build());
    }

    void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_INTERVAL / 2);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // called with the appropriate Intent action in onStartCommand by the Activity when the user stops recording a Route
    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        // send a null location to indicate that the Service has stopped
        doCallbacks(null);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(START_RECORDING)) {
                    startLocationService();
                } else if (action.equals(STOP_RECORDING)) {
                    // two cases in which this action is used:
                    // 1. when the user cancels the recording from the notification
                    // 2. when the user stops the recording from the Activity
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public class MyBinder extends Binder implements IInterface {

        RecordRouteServiceCallback callback;

        @Override
        public IBinder asBinder() {
            return this;
        }

        public void registerCallback(RecordRouteServiceCallback callback) {
            this.callback = callback;
            // maintain the list of callbacks in the outer class (Service)
            // register a callback in the inner class (Binder)
            remoteCallbackList.register(MyBinder.this);
        }
    }
}
