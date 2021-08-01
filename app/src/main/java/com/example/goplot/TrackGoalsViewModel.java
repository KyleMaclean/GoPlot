package com.example.goplot;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TrackGoalsViewModel extends AndroidViewModel {

    private static final String GOAL_FILE_KEY = "com.example.goplot.GOAL_FILE_KEY";
    private static final String WEEKLY_GOAL_KEY = "com.example.goplot.WEEKLY_GOAL_KEY";
    private static final String MONTHLY_GOAL_KEY = "com.example.goplot.MONTHLY_GOAL_KEY";
    private static final int DEFAULT_WEEKLY_GOAL = 20;
    private static final int DEFAULT_MONTHLY_GOAL = 100;
    private final RouteDao routeDao;
    private final MutableLiveData<Integer> progressThisWeek = new MutableLiveData<>();
    private final MutableLiveData<Integer> progressThisMonth = new MutableLiveData<>();
    private double distanceThisWeek = 0;
    private double distanceThisMonth = 0;
    public final TrackGoalsViewModelObservable trackGoalsViewModelObservable;
    private String weeklyGoalAsString, monthlyGoalAsString;
    final SharedPreferences sharedPreferences;

    public TrackGoalsViewModel(@NonNull Application application) {
        super(application);
        GoPlotRoomDatabase goPlotRoomDatabase = GoPlotRoomDatabase.getDatabase(application);
        routeDao = goPlotRoomDatabase.routeDao();
        // initialise our singleton Observable to handle two-way binding of adjustable goals
        trackGoalsViewModelObservable = new TrackGoalsViewModelObservable();

        // for reading and writing the user's goals
        sharedPreferences = getApplication().getSharedPreferences(GOAL_FILE_KEY, Context.MODE_PRIVATE);

        // initialising the goals which will be two-way bound
        int weeklyGoalAsInt = sharedPreferences.getInt(WEEKLY_GOAL_KEY, DEFAULT_WEEKLY_GOAL);
        weeklyGoalAsString = Integer.toString(weeklyGoalAsInt);
        int monthlyGoalAsInt = sharedPreferences.getInt(MONTHLY_GOAL_KEY, DEFAULT_MONTHLY_GOAL);
        monthlyGoalAsString = Integer.toString(monthlyGoalAsInt);

        // for use in calculations about which Routes to use in the thread below
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // retrieve the Routes and add their distances to the running totals as applicable
        GoPlotRoomDatabase.databaseWriteExecutor.execute(() -> {
            List<Route> routes = routeDao.getAllRoutesOrderedByStartTimeAsNonLiveList();
            for (Route route : routes) {
                calendar.setTime(route.getStartTime());
                int routeDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int routeDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                // when a Route is from before the current month and before the current week then we stop counting distances
                if (routeDayOfWeek > currentDayOfWeek && routeDayOfMonth > currentDayOfMonth)
                    break;
                // count Routes from this week towards this week's distance
                if (routeDayOfWeek <= currentDayOfWeek)
                    distanceThisWeek += (float) route.getKilometres();
                // count Routes from this month towards this month's distance
                if (routeDayOfMonth <= currentDayOfMonth)
                    distanceThisMonth += (float) route.getKilometres();
            }
            // calculating the proportion of the goals achieved and setting the corresponding LiveData
            progressThisWeek.postValue((int) (distanceThisWeek / weeklyGoalAsInt * 100));
            progressThisMonth.postValue((int) (distanceThisMonth / monthlyGoalAsInt * 100));
        });

    }

    @SuppressLint("DefaultLocale")
    public String getDistanceThisMonth() {
        return String.format("%.1f km", distanceThisMonth);
    }

    // reformat the distances for each period from doubles to Strings to be bound in XML
    @SuppressLint("DefaultLocale")
    public String getDistanceThisWeek() {
        return String.format("%.1f km", distanceThisWeek);
    }

    // the week's and month's progress are bound to the determinate progress bars

    public LiveData<Integer> getProgressThisWeek() {
        return progressThisWeek;
    }

    public LiveData<Integer> getProgressThisMonth() {
        return progressThisMonth;
    }

    public class TrackGoalsViewModelObservable extends BaseObservable {

        @Bindable
        public String getWeeklyGoalAsString() {
            return TrackGoalsViewModel.this.weeklyGoalAsString;
        }

        @Bindable
        public String getMonthlyGoalAsString() {
            return TrackGoalsViewModel.this.monthlyGoalAsString;
        }

        // whenever the user changes a character of their goal, we try to parse it as an Integer and persist the change

        public void setMonthlyGoalAsString(String monthlyGoalAsString) {
            TrackGoalsViewModel.this.monthlyGoalAsString = monthlyGoalAsString;
            notifyPropertyChanged(BR.monthlyGoalAsString);
            int newMonthlyGoal = updateGoal(monthlyGoalAsString, MONTHLY_GOAL_KEY);
            TrackGoalsViewModel.this.progressThisMonth.postValue((int) (distanceThisMonth / newMonthlyGoal * 100));
        }

        public void setWeeklyGoalAsString(String weeklyGoalAsString) {
            TrackGoalsViewModel.this.weeklyGoalAsString = weeklyGoalAsString;
            notifyPropertyChanged(BR.weeklyGoalAsString);
            int newWeeklyGoal = updateGoal(weeklyGoalAsString, WEEKLY_GOAL_KEY);
            TrackGoalsViewModel.this.progressThisWeek.postValue((int) (distanceThisWeek / newWeeklyGoal * 100));
        }

        int updateGoal(String goal, String key) {
            int newGoal = 0;
            try {
                newGoal = Integer.parseInt(goal);
            } catch (NumberFormatException e) {
                Log.d("mdp", "New goal is not a number so it shall be set to 0.");
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(key, newGoal);
            editor.apply();
            return newGoal;
        }
    }
}
