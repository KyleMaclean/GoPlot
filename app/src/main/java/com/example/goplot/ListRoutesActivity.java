package com.example.goplot;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ListRoutesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_routes);

        // initialise the RecyclerView & ViewModel
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        RouteListAdapter routeListAdapter = new RouteListAdapter(new RouteListAdapter.RouteDiff());
        recyclerView.setAdapter(routeListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ListRoutesViewModel listRoutesViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(ListRoutesViewModel.class);

        // observe the Routes in most recent order from the ViewModel
        listRoutesViewModel.getAllRoutesOrderedByEndTime().observe(this, routeListAdapter::submitList);
    }
}