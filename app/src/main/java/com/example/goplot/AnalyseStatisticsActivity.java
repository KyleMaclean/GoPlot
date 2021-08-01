package com.example.goplot;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goplot.databinding.ActivityAnalyseStatisticsBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AnalyseStatisticsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting ViewModel and DataBinding
        AnalyseStatisticsViewModel analyseStatisticsViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(AnalyseStatisticsViewModel.class);
        com.example.goplot.databinding.ActivityAnalyseStatisticsBinding activityAnalyseStatisticsBinding = ActivityAnalyseStatisticsBinding.inflate(getLayoutInflater());
        View rootView = activityAnalyseStatisticsBinding.getRoot();
        setContentView(rootView);
        activityAnalyseStatisticsBinding.setLifecycleOwner(this);
        activityAnalyseStatisticsBinding.setViewmodel(analyseStatisticsViewModel);

        // observing the fastest Routes in a RecyclerView
        RecyclerView recyclerViewFastest = findViewById(R.id.recyclerViewFastest);
        RouteListAdapter fastestRouteListAdapter = new RouteListAdapter(new RouteListAdapter.RouteDiff());
        recyclerViewFastest.setAdapter(fastestRouteListAdapter);
        recyclerViewFastest.setLayoutManager(new LinearLayoutManager(this));
        analyseStatisticsViewModel.getTop3RoutesOrderedByPace().observe(this, fastestRouteListAdapter::submitList);

        // observing the longest Routes in a RecyclerView
        RecyclerView recyclerViewLongest = findViewById(R.id.recyclerViewLongest);
        RouteListAdapter longestRouteListAdapter = new RouteListAdapter(new RouteListAdapter.RouteDiff());
        recyclerViewLongest.setAdapter(longestRouteListAdapter);
        recyclerViewLongest.setLayoutManager(new LinearLayoutManager(this));
        analyseStatisticsViewModel.getTop3RoutesOrderedByDistance().observe(this, longestRouteListAdapter::submitList);

        // implementing an Observer to receive the LiveData Routes for the chart
        final Observer<List<Route>> statisticsRoutesObserver = routes -> {
            BarChart barChart = findViewById(R.id.chart);

            // retrieve current day of week to not display data from last week after the current day
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            // to store the total distance recorded in Routes for each day of the week
            float[] dailyDistance = new float[]{0, 0, 0, 0, 0, 0, 0};

            for (Route route : routes) {
                calendar.setTime(route.getStartTime());
                int routeDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                // only consider routes from this week (list is ordered by date descending)
                if (routeDayOfWeek > currentDayOfWeek)
                    break;
                // summing all distance for that day answers: "how far have I run so far today / on x day this week?"
                dailyDistance[routeDayOfWeek - 1] += (float) route.getKilometres();
            }

            // map from dailyDistance to start the week on Monday, not Sunday, in the chart
            BarEntry[] barEntries = new BarEntry[]{
                    new BarEntry(0, dailyDistance[1]),
                    new BarEntry(1, dailyDistance[2]),
                    new BarEntry(2, dailyDistance[3]),
                    new BarEntry(3, dailyDistance[4]),
                    new BarEntry(4, dailyDistance[5]),
                    new BarEntry(5, dailyDistance[6]),
                    new BarEntry(6, dailyDistance[0])
            };

            // setting and styling chart
            BarDataSet barDataSet = new BarDataSet(Arrays.asList(barEntries), "");
            MyValueFormatter myValueFormatter = new MyValueFormatter();
            barDataSet.setColor(getResources().getColor(R.color.teal_700));
            barDataSet.setValueFormatter(myValueFormatter);
            BarData barData = new BarData(barDataSet);
            barChart.setData(barData);
            barChart.getDescription().setEnabled(false);
            barChart.getAxisRight().setEnabled(false);
            barChart.getAxisLeft().setDrawGridLines(false);
            barChart.getLegend().setEnabled(false);
            barChart.setTouchEnabled(false);
            barChart.setDoubleTapToZoomEnabled(false);
            barChart.getXAxis().setEnabled(true);
            barChart.getXAxis().setValueFormatter(myValueFormatter);
            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
            barChart.getXAxis().setDrawGridLines(false);
            barChart.getXAxis().setLabelCount(barEntries.length);
            barChart.getAxisLeft().setTextColor(getResources().getColor(R.color.teal_700));
            barChart.getXAxis().setTextColor(getResources().getColor(R.color.teal_700));
            barChart.getAxisLeft().setEnabled(true);
            barChart.getAxisLeft().setValueFormatter(myValueFormatter);

            barChart.invalidate();
        };

        // set the Routes which this Observer observes from the ViewModel
        analyseStatisticsViewModel.getAllRoutesOrderedByStartTime().observe(this, statisticsRoutesObserver);
    }

    // we need to extend this class to define formatting for the axes
    static class MyValueFormatter extends ValueFormatter {

        final String[] DAYS = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        // no labels above each bar required
        @Override
        public String getBarLabel(BarEntry barEntry) {
            return "";
        }

        @SuppressLint("DefaultLocale")
        @Override
        public String getAxisLabel(float value, AxisBase axis) {

            if (axis instanceof YAxis) {
                return String.format("%.1f km", value);
            }
            if (axis instanceof XAxis) {
                return DAYS[(int) value];
            }
            return super.getAxisLabel(value, axis);
        }
    }


}