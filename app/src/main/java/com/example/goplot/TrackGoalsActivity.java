package com.example.goplot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.View;

import com.example.goplot.databinding.ActivityTrackGoalsBinding;

public class TrackGoalsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect the ViewModel and set up Data Binding
        TrackGoalsViewModel trackGoalsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(TrackGoalsViewModel.class);
        com.example.goplot.databinding.ActivityTrackGoalsBinding activityTrackGoalsBinding = ActivityTrackGoalsBinding.inflate(getLayoutInflater());
        View rootView = activityTrackGoalsBinding.getRoot();
        setContentView(rootView);
        activityTrackGoalsBinding.setLifecycleOwner(this);
        activityTrackGoalsBinding.setViewmodel(trackGoalsViewModel);
    }
}