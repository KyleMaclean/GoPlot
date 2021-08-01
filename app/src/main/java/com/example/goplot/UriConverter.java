package com.example.goplot;

import android.net.Uri;

import androidx.room.TypeConverter;

// a wrapper around the Uri.parse(...) and uri.toString(...) methods
class UriConverter {

    @TypeConverter
    public static Uri toUri(String uriString) {
        if (uriString == null) return null;
        return Uri.parse(uriString);
    }

    @TypeConverter
    public static String fromUri(Uri uri) {
        if (uri == null) return null;
        return uri.toString();
    }
}
