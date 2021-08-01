package com.example.goplot;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Route.class, Spot.class}, version = 1, exportSchema = false)
public abstract class GoPlotRoomDatabase extends RoomDatabase {

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static final RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // pre-populate here if necessary
        }
    };
    private static volatile GoPlotRoomDatabase INSTANCE;

    public static GoPlotRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GoPlotRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            GoPlotRoomDatabase.class, "goplot_database")
                            .addCallback(roomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract RouteDao routeDao();

    public abstract SpotDao spotDao();
}
