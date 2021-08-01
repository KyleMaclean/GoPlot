package com.example.goplot;

import android.net.Uri;

public class GoPlotContract {

    public static final String AUTHORITY = "uk.ac.nott.cs.psykjm.goplot";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final Uri ROUTE_URI = Uri.parse("content://" + AUTHORITY + "/route/");
    public static final Uri SPOT_URI = Uri.parse("content://" + AUTHORITY + "/spot/");

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/goplot.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/goplot.data.text";

    public static final String ROUTE_ID = "_id";
    public static final String ROUTE_METRES = "metres";
    public static final String ROUTE_START_TIME = "start_time";
    public static final String ROUTE_END_TIME = "end_time";
    public static final String ROUTE_IMAGE_URI = "image";
    public static final String ROUTE_RATING = "rating";
    public static final String ROUTE_DESCRIPTION = "description";

    public static final String SPOT_ID = "_id";
    public static final String SPOT_ROUTE_ID = "route_id";
    public static final String SPOT_LATITUDE = "latitude";
    public static final String SPOT_LONGITUDE = "longitude";
    public static final String SPOT_MARKED = "marked";

}
