package com.example.goplot;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

// this table stores three objects from these classes as fields and thus needs @TypeConverters
@TypeConverters({DateConverter.class, UriConverter.class})
@Entity(tableName = "route_table")
public class Route {

    @PrimaryKey(autoGenerate = true)
    private int _id;
    @ColumnInfo(name = "metres")
    private double metres;
    @ColumnInfo(name = "start_time")
    private Date startTime;
    @ColumnInfo(name = "end_time")
    private Date endTime;
    @ColumnInfo(name = "image")
    private Uri imageUri;
    @ColumnInfo(name = "rating")
    private int rating;
    @ColumnInfo(name = "description")
    private String description;
    @SuppressWarnings("FieldCanBeLocal")
    // purely derived field, so suppress the suggestion to make it local because we want to query its value
    @ColumnInfo(name = "pace")
    private double pace;

    public Route(double metres, @NonNull Date startTime, @NonNull Date endTime) {
        this.metres = metres;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // used by ContentProvider to convert ContentValues into Route object in all contexts (static)
    // specifically, the insert method uses this to get a correctly-initialised Route object
    public static Route fromContentValues(ContentValues values) {
        // all Routes must provide these three values for the constructor
        // if we do not have these values, we fail to generate a Route
        if (values.containsKey(GoPlotContract.ROUTE_METRES)
                && values.containsKey(GoPlotContract.ROUTE_START_TIME)
                && values.containsKey(GoPlotContract.ROUTE_END_TIME)) {
            double metres = values.getAsDouble(GoPlotContract.ROUTE_METRES);
            Date startTime = DateConverter.toDate(values.getAsLong(GoPlotContract.ROUTE_START_TIME));
            Date endTime = DateConverter.toDate(values.getAsLong(GoPlotContract.ROUTE_END_TIME));
            Route route = new Route(metres, startTime, endTime);
            // do not check values.containsKey(GoPlotContract.ROUTE_ID)) because it is captured in the URI segment
            // checks all optional fields and sets them on the Route object if present
            if (values.containsKey(GoPlotContract.ROUTE_IMAGE_URI)) {
                Uri imageUri = UriConverter.toUri(values.getAsString(GoPlotContract.ROUTE_IMAGE_URI));
                route.setImageUri(imageUri);
            }
            if (values.containsKey(GoPlotContract.ROUTE_RATING)) {
                int rating = values.getAsInteger(GoPlotContract.ROUTE_RATING);
                route.setRating(rating);
            }
            if (values.containsKey(GoPlotContract.ROUTE_DESCRIPTION)) {
                String description = values.getAsString(GoPlotContract.ROUTE_DESCRIPTION);
                route.setDescription(description);
            }
            return route;
        }
        return null; // some arguments which the constructor requires were not provided
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public double getMetres() {
        return this.metres;
    }

    public void setMetres(double mDistance) {
        this.metres = mDistance;
    }

    public double getKilometres() {
        return this.metres / 1000;
    }

    double getSeconds() {
        return this.endTime.getTime() / 1000d - this.startTime.getTime() / 1000d;
    }

    double getPace() {
        return (getSeconds() / 60) / getKilometres();
    }

    void setPace(double pace) {
        this.pace = pace;
    }

    // used in the ViewHolder to display a human-friendly summary String of this Route's properties
    public String getRoute() {
        @SuppressLint("DefaultLocale") String paceTwoDecimalPlaces = String.format("%.2f", getPace());
        return getStartDayDate() + ": Distance = " + getKilometresString() + " km, Pace = " + paceTwoDecimalPlaces + " min/km";
    }

    // used in the ViewModel for displaying a single Route's details
    @SuppressLint("DefaultLocale")
    public String getKilometresString() {
        return String.format("%.2f", metres / 1000);
    }

    @SuppressLint("DefaultLocale")
    public String getStartDayDate() {
        return String.format("%ta, %te %th", this.startTime, this.startTime, this.startTime);
    }

    public String getStartDayDateTime() {
        return getDayDateTime(this.startTime);
    }

    public String getEndDayDateTime() {
        return getDayDateTime(this.endTime);
    }

    @SuppressLint("DefaultLocale")
    private String getDayDateTime(Date date) {
        return String.format("%tA %tr (%tF)", date, date, date);
    }

}

