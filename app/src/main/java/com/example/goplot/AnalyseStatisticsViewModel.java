package com.example.goplot;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class AnalyseStatisticsViewModel extends AndroidViewModel {

    private final RouteDao routeDao;

    public AnalyseStatisticsViewModel(@NonNull Application application) {
        super(application);
        GoPlotRoomDatabase goPlotRoomDatabase = GoPlotRoomDatabase.getDatabase(application);
        routeDao = goPlotRoomDatabase.routeDao();
    }

    // to observe the fastest Routes in a RecyclerView
    public LiveData<List<Route>> getTop3RoutesOrderedByPace() {
        return routeDao.getTop3RoutesOrderedByPace();
    }

    // to observe the longest Routes in another RecyclerView
    public LiveData<List<Route>> getTop3RoutesOrderedByDistance() {
        return routeDao.getTop3RoutesOrderedByDistance();
    }

    // to populate the graph with this week's Routes
    public LiveData<List<Route>> getAllRoutesOrderedByStartTime() {
        return routeDao.getAllRoutesOrderedByStartTime();
    }
}
