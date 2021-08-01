package com.example.goplot;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RouteDao {

    // returns the routeId of the inserted Route if successful
    @Insert
    long insert(Route route);

    // both delete methods return the number of Routes deleted
    @Query("DELETE FROM route_table WHERE _id = :routeId")
    int delete(long routeId);

    @Query("DELETE FROM route_table")
    int deleteAll();

    // returns the number of Routes updated
    @Update
    int update(Route route);

    // used in the ViewModels for analysing statistics, tracking goals and listing Routes
    @Query("SELECT * FROM route_table ORDER BY start_time DESC")
    LiveData<List<Route>> getAllRoutesOrderedByStartTime();

    // used in the ViewModels for analysing statistics, tracking goals and listing Routes
    @Query("SELECT * FROM route_table ORDER BY start_time DESC")
    List<Route> getAllRoutesOrderedByStartTimeAsNonLiveList();

    // get the Route by its ID so SingleRouteViewModel can retrieve its details and bind them to its Activity's Views
    @Query("SELECT * FROM route_table WHERE _id = :routeId")
    Route getRouteById(int routeId);

    // for the two RecyclerViews in the AnalyseStatisticsActivity which observe this LiveData from their ViewModels
    @Query("SELECT * FROM route_table ORDER BY pace LIMIT 3")
    LiveData<List<Route>> getTop3RoutesOrderedByPace();

    @Query("SELECT * FROM route_table ORDER BY metres DESC LIMIT 3")
    LiveData<List<Route>> getTop3RoutesOrderedByDistance();

    // these methods which return Cursor are not used in this app - they are just for the ContentProvider
    @Query("SELECT * FROM route_table")
    Cursor getAllRoutesAsCursor();
    @Query("SELECT * FROM route_table WHERE _id = :routeId")
    Cursor getRouteByIdAsCursor(int routeId);
}
