package com.example.goplot;

import androidx.room.TypeConverter;

import java.util.Date;

// used as a @TypeConverter for Route objects
class DateConverter {

    @TypeConverter
    public static Date toDate(Long dateLong) {
        return new Date(dateLong);
    }

    @TypeConverter
    public static Long fromDate(Date date) {
        return date.getTime();
    }
}
