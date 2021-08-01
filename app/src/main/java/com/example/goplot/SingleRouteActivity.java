package com.example.goplot;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.goplot.databinding.ActivitySingleRouteBinding;

import java.util.Objects;

public class SingleRouteActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE_REQUEST_CODE = 1;
    private int routeId;
    private SingleRouteViewModel singleRouteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set the ViewModel and DataBinding then pass the RouteId to the ViewModel to update the bound Views
        singleRouteViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(SingleRouteViewModel.class);
        com.example.goplot.databinding.ActivitySingleRouteBinding activitySingleRouteBinding = ActivitySingleRouteBinding.inflate(getLayoutInflater());
        View rootView = activitySingleRouteBinding.getRoot();
        setContentView(rootView);
        activitySingleRouteBinding.setLifecycleOwner(this);
        activitySingleRouteBinding.setViewmodel(singleRouteViewModel);
        routeId = getIntent().getIntExtra("routeId", -1);
        singleRouteViewModel.setRoute(routeId);
    }

    // pass this Route's ID to the generic MapsActivity for its ViewModel to visualise the Route
    public void onClickShowMap(View view) {
        Intent intent = new Intent(SingleRouteActivity.this, MapsActivity.class);
        intent.putExtra("routeId", routeId);
        startActivity(intent);
    }

    // two of these flags require KITKAT; quite cumbersome but necessary to maintain the image
    public void onClickChooseImage(View view) {
        Intent chooseImageIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            chooseImageIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        Objects.requireNonNull(chooseImageIntent).setType("image/*");
        chooseImageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            chooseImageIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }
        // once the image result is ready, it is passed to the ViewModel to persist it
        startActivityForResult(chooseImageIntent, CHOOSE_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // once the user has used the implicit Intent to choose an image, we can save it
        if (requestCode == CHOOSE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri choseImageUri = Objects.requireNonNull(data).getData();
            // another permission which is necessary to ensure we do not lose access to the image
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getContentResolver().takePersistableUriPermission(choseImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            // just save the Uri because we set the flag to allow us to have a persistable Uri
            singleRouteViewModel.setImageUri(choseImageUri);
        }
    }
}