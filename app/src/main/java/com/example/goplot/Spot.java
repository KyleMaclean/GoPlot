package com.example.goplot;

import android.content.ContentValues;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// for every Route, there are many Spots which constitute it
@Entity(tableName = "spot_table")
public class Spot {

    @ColumnInfo(name = "latitude")
    private double latitude;
    @ColumnInfo(name = "longitude")
    private double longitude;
    @PrimaryKey(autoGenerate = true)
    private int _id;
    @ColumnInfo(name = "route_id")
    private int routeId;
    @ColumnInfo(name = "marked")
    // allows the user to save a specific location during their Route
    private boolean marked;
    public Spot(int routeId, double latitude, double longitude, boolean marked) {
        this.routeId = routeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.marked = marked;
    }

    // to convert a Spot given to the ContentProvider as ContentValues into a Spot object
    // static because it is a utility method to convert between data formats in any context
    public static Spot fromContentValues(ContentValues values) {

        // all these ContentValues are required or else we cannot construct a legal Spot object
        if (values.containsKey(GoPlotContract.SPOT_ROUTE_ID)
                && values.containsKey(GoPlotContract.SPOT_LATITUDE)
                && values.containsKey(GoPlotContract.SPOT_LONGITUDE)
                && values.containsKey(GoPlotContract.SPOT_MARKED)) {
            int routeId = values.getAsInteger(GoPlotContract.SPOT_ROUTE_ID);
            double latitude = values.getAsDouble(GoPlotContract.SPOT_LATITUDE);
            double longitude = values.getAsDouble(GoPlotContract.SPOT_LONGITUDE);
            boolean marked = values.getAsBoolean(GoPlotContract.SPOT_MARKED);
            Spot spot = new Spot(routeId, latitude, longitude, marked);
            if (values.containsKey(GoPlotContract.SPOT_ID)) {
                int spotId = values.getAsInteger(GoPlotContract.SPOT_ID);
                spot.set_id(spotId);
            }
            return spot;
        }
        return null; // some arguments which the constructor requires were not provided
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
