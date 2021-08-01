package com.example.goplot;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ListRoutesViewModel extends AndroidViewModel {

    final RouteDao routeDao;

    public ListRoutesViewModel(@NonNull Application application) {
        super(application);
        GoPlotRoomDatabase goPlotRoomDatabase = GoPlotRoomDatabase.getDatabase(application);
        routeDao = goPlotRoomDatabase.routeDao();
    }

    // provides the Route objects for the RecyclerView to observe
    LiveData<List<Route>> getAllRoutesOrderedByEndTime() {
        return routeDao.getAllRoutesOrderedByStartTime();
    }
}