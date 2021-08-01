package com.example.goplot;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SpotDao {

    // return the ID of the inserted object
    @Insert
    long insert(Spot spot);

    // both delete methods return the number of Spots deleted
    @Query("DELETE FROM spot_table")
    int deleteAll();
    @Query("DELETE FROM spot_table WHERE _id = :spotId")
    int delete(long spotId);

    // returns the number of Spots updated
    @Update
    int update(Spot spot);

    // used when saving a Route to efficiently insert all its constituent Spots
    @Insert
    void insertAll(List<Spot> spots);

    // for displaying the map of all Spots for a particular Route
    @Query("SELECT * FROM spot_table WHERE route_id = :routeId")
    LiveData<List<Spot>> getSpotsForRouteIdAsLiveDataList(int routeId);

    // these methods which return Cursor are not used in this app - they are just for the ContentProvider
    @Query("SELECT * FROM spot_table")
    Cursor getAllSpotsAsCursor();

    @Query("SELECT * FROM spot_table WHERE _id = :spotId")
    Cursor getSpotByIdAsCursor(int spotId);

    // used by the ContentProvider to update Spot fields
    @Query("SELECT * FROM spot_table WHERE _id = :spotId")
    Spot getSpotById(int spotId);

    // for displaying the map of all marked Spots from all Routes
    @Query("SELECT * FROM spot_table WHERE marked = 1")
    LiveData<List<Spot>> getMarkedSpots();

}
