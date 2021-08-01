package com.example.goplot;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

// not used at all in this app; only for external Clients to use
public class GoPlotContentProvider extends ContentProvider {

    private static final UriMatcher uriMatcher;
    // 2 cases for CONTENT_TYPE_MULTIPLE
    private static final int ROUTE_DIR_CODE = 10;
    private static final int SPOT_DIR_CODE = 11;
    // 2 cases for CONTENT_TYPE_SINGLE
    private static final int ROUTE_ITEM_CODE = 20;
    private static final int SPOT_ITEM_CODE = 21;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 2 cases for CONTENT_TYPE_MULTIPLE
        uriMatcher.addURI(GoPlotContract.AUTHORITY, GoPlotContract.ROUTE_URI.getPath(), ROUTE_DIR_CODE);
        uriMatcher.addURI(GoPlotContract.AUTHORITY, GoPlotContract.SPOT_URI.getPath(), SPOT_DIR_CODE);
        // 2 cases for CONTENT_TYPE_SINGLE
        uriMatcher.addURI(GoPlotContract.AUTHORITY, GoPlotContract.ROUTE_URI.getPath() + "#", ROUTE_ITEM_CODE);
        uriMatcher.addURI(GoPlotContract.AUTHORITY, GoPlotContract.SPOT_URI.getPath() + "#", SPOT_ITEM_CODE);
    }

    private RouteDao routeDao;
    private SpotDao spotDao;

    // called in switch statements which do not match any of the cases
    void unrecognisedUriException(Uri uri) {
        throw new IllegalArgumentException("Did not recognise uri: " + uri.toString());
    }

    // by passing the item URI and an ID, just that item is deleted
    // by passing the directory URI, all items are deleted
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deletions = 0;
        if (getContext() != null) {
            switch (uriMatcher.match(uri)) {
                case ROUTE_ITEM_CODE:
                    deletions = routeDao.delete(ContentUris.parseId(uri));
                    break;
                case SPOT_ITEM_CODE:
                    deletions = spotDao.delete(ContentUris.parseId(uri));
                    break;
                case SPOT_DIR_CODE:
                    deletions = spotDao.deleteAll();
                    break;
                case ROUTE_DIR_CODE:
                    deletions = routeDao.deleteAll();
                    break;
                default:
                    unrecognisedUriException(uri);
            }
        }
        if (deletions != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return deletions;
    }

    @Override
    public String getType(Uri uri) {
        String type = null;
        switch (uriMatcher.match(uri)) {
            case ROUTE_DIR_CODE:
            case SPOT_DIR_CODE:
                type = GoPlotContract.CONTENT_TYPE_MULTIPLE;
                break;
            case ROUTE_ITEM_CODE:
            case SPOT_ITEM_CODE:
                type = GoPlotContract.CONTENT_TYPE_SINGLE;
                break;
            default:
                unrecognisedUriException(uri);
        }
        return type;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = 0;
        if (getContext() != null) {
            switch (uriMatcher.match(uri)) {
                // two cases for inserting single items into their 'directories'
                case ROUTE_DIR_CODE:
                    Route route = Route.fromContentValues(values);
                    if (route == null) {
                        Log.d("mdp", "Route object is null. Please provide all required fields for its constructor as ContentValues.");
                    } else {
                        id = routeDao.insert(Route.fromContentValues(values));
                    }
                    break;
                case SPOT_DIR_CODE:
                    Spot spot = Spot.fromContentValues(values);
                    if (spot == null) {
                        Log.d("mdp", "Spot object is null. Please provide all required fields for its constructor as ContentValues.");
                    } else {
                        id = spotDao.insert(Spot.fromContentValues(values));
                    }
                    break;
                case ROUTE_ITEM_CODE:
                case SPOT_ITEM_CODE:
                    Log.d("mdp", "Cannot insert over another item, perhaps you meant to update?");
                    break;
                default:
                    unrecognisedUriException(uri);
            }
        }
        if (id != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, id);
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        GoPlotRoomDatabase goPlotRoomDatabase = GoPlotRoomDatabase.getDatabase(getContext());
        routeDao = goPlotRoomDatabase.routeDao();
        spotDao = goPlotRoomDatabase.spotDao();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            // 2 cases for CONTENT_TYPE_MULTIPLE
            case ROUTE_DIR_CODE:
                cursor = routeDao.getAllRoutesAsCursor();
                break;
            case SPOT_DIR_CODE:
                cursor = spotDao.getAllSpotsAsCursor();
                break;
            // 2 cases for CONTENT_TYPE_SINGLE
            case ROUTE_ITEM_CODE:
                int routeId = Integer.parseInt(uri.getLastPathSegment());
                cursor = routeDao.getRouteByIdAsCursor(routeId);
                break;
            case SPOT_ITEM_CODE:
                int spotId = Integer.parseInt(uri.getLastPathSegment());
                cursor = spotDao.getSpotByIdAsCursor(spotId);
                break;
        }
        if (getContext() != null && cursor != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int id, updates = 0;
        if (getContext() != null) {
            switch (uriMatcher.match(uri)) {
                case ROUTE_ITEM_CODE:
                    id = Integer.parseInt(uri.getLastPathSegment());
                    Route route = routeDao.getRouteById(id);
                    if (route != null) {
                        if (values.containsKey(GoPlotContract.ROUTE_METRES))
                            route.setMetres(values.getAsDouble(GoPlotContract.ROUTE_METRES));
                        if (values.containsKey(GoPlotContract.ROUTE_START_TIME))
                            route.setStartTime(DateConverter.toDate(values.getAsLong(GoPlotContract.ROUTE_START_TIME)));
                        if (values.containsKey(GoPlotContract.ROUTE_END_TIME))
                            route.setEndTime(DateConverter.toDate(values.getAsLong(GoPlotContract.ROUTE_END_TIME)));
                        if (values.containsKey(GoPlotContract.ROUTE_IMAGE_URI))
                            route.setImageUri(UriConverter.toUri(values.getAsString(GoPlotContract.ROUTE_IMAGE_URI)));
                        if (values.containsKey(GoPlotContract.ROUTE_RATING))
                            route.setRating(values.getAsInteger(GoPlotContract.ROUTE_RATING));
                        if (values.containsKey(GoPlotContract.ROUTE_DESCRIPTION))
                            route.setDescription(values.getAsString(GoPlotContract.ROUTE_DESCRIPTION));
                        updates = routeDao.update(route);
                    } else {
                        Log.d("mdp", "Route object is null. Please check the ID");
                    }
                    break;
                case SPOT_ITEM_CODE:
                    id = Integer.parseInt(uri.getLastPathSegment());
                    Spot spot = spotDao.getSpotById(id);
                    if (spot != null) {
                        if (values.containsKey(GoPlotContract.SPOT_ROUTE_ID))
                            spot.setRouteId(values.getAsInteger(GoPlotContract.SPOT_ROUTE_ID));
                        if (values.containsKey(GoPlotContract.SPOT_LATITUDE))
                            spot.setLatitude(values.getAsDouble(GoPlotContract.SPOT_LATITUDE));
                        if (values.containsKey(GoPlotContract.SPOT_LONGITUDE))
                            spot.setLongitude(values.getAsDouble(GoPlotContract.SPOT_LONGITUDE));
                        if (values.containsKey(GoPlotContract.SPOT_MARKED))
                            spot.setMarked(values.getAsBoolean(GoPlotContract.SPOT_MARKED));
                        updates = spotDao.update(spot);
                    } else {
                        Log.d("mdp", "Spot object is null. Please check the ID");
                    }
                    break;
                case ROUTE_DIR_CODE:
                case SPOT_DIR_CODE:
                    Log.d("mdp", "Cannot update a directory with a new item, perhaps you meant to insert?");
                    break;
                default:
                    unrecognisedUriException(uri);
            }
        }
        if (updates != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return updates;
    }
}