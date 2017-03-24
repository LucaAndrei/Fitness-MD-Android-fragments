/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.ui.fragments.stats;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Paint;
import android.support.v4.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.datatypes.StepsDayReport;
import com.master.aluca.fitnessmd.common.util.IStatsChanged;
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;
import com.master.aluca.fitnessmd.service.FitnessMDService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class StatisticsFragment extends Fragment {

    private static final String LOG_TAG = "Fitness_Statistics";

    private static StatisticsFragment mInstance = null;
    private com.github.mikephil.charting.charts.BarChart mChart;
    TextView tvStatsAverageSteps, tvStatsTotalSteps;

    private Activity mActivity;
    private TextView tvDateToday;

    private FitnessMDService mService;

    HashMap<Long, Integer> last7DaysStats = new HashMap<>();

    private Dialog mDialog;

    private WebserverManager webserverManager;
    private UsersDB mDB;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(LOG_TAG, "on service connected");
            FitnessMDService.FitnessMD_Binder binder = (FitnessMDService.FitnessMD_Binder) iBinder;
            mService = binder.getService();

            if (mService == null) {
                Log.e(LOG_TAG, "unable to connect to service");
                return;
            } else {
                //webserverManager.subscribeToChallenges();
                //webserverManager.subscribeToStats();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(LOG_TAG, "on service disconnected");
            mService = null;
        }
    };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "onStart()");
        super.onStart();

        Intent intent = new Intent(this.mActivity, FitnessMDService.class);
        if (!FitnessMDService.isServiceRunning()) {
            mActivity.startService(intent);
        }
        if (!mActivity.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)) {
            Log.e(LOG_TAG, "Unable to bind to optical service");
        }

        if (webserverManager != null) {
            webserverManager.registerStatsCallback(mStatsChangedCallback);
            webserverManager.subscribeToStats();
        }
    }

    private IStatsChanged mStatsChangedCallback = new IStatsChanged() {
        @Override
        public void onTotalStepsChanged(int totalSteps) {
            Log.d(LOG_TAG, "IStatsChanged onTotalStepsChanged totalSteps : " + totalSteps);
            tvStatsTotalSteps.setText(String.valueOf(totalSteps));
        }

        @Override
        public void onMaxStepsChanged(long day, int maxSteps) {
        }

        @Override
        public void onAverageStepsChanged(int averageSteps) {
            Log.d(LOG_TAG, "IStatsChanged onAverageStepsChanged averageSteps : " + averageSteps);
            tvStatsAverageSteps.setText(String.valueOf(averageSteps));
        }

        @Override
        public void onLast7DaysStats(HashMap<Long, Integer> last7DaysStats) {
            Log.d(LOG_TAG, "IStatsChanged onLast7DaysStats : " + last7DaysStats.size());
            for (Map.Entry<Long,Integer> entry : last7DaysStats.entrySet()) {
                Log.d(LOG_TAG,"IStatsChanged " + entry.getKey() + " >> " + new Date(entry.getKey()) + " : " + entry.getValue());
            }
            SortedSet<Long> keys = new TreeSet<>(last7DaysStats.keySet());
            int[] values = new int[keys.size()];
            int counter = last7DaysStats.size();
            for (Long key : keys) {
                //Log.d(LOG_TAG,"key : " + key + " value : " + last7DaysStats.get(key));
                values[--counter] = last7DaysStats.get(key);

            }
            setData(7, values);

            Map<Long, Integer> myMap = new TreeMap<Long,Integer>(last7DaysStats);
            for (Map.Entry<Long, Integer> entry : myMap.entrySet()) {
                //Log.d(LOG_TAG, "entryKey : " + entry.getKey() + " : " + entry.getValue());
            }

        }


    };

    public void onStop() {
        Log.d(LOG_TAG, "onStop()");
        super.onStop();

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if (mService != null) {
            mActivity.unbindService(mServiceConnection);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.layout_tab_stats, container, false);
    }


    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        View view = getView();
        if (view == null) {
            return;
        }

        Log.d(LOG_TAG, "onActivityCreated StatisticsFragment");
        mDB = UsersDB.getInstance(getActivity());
        webserverManager = WebserverManager.getInstance(getActivity());

        setup(view);
    }

    public void setup(View view) {
        tvDateToday = (TextView) view.findViewById(R.id.tvDateToday);
        SimpleDateFormat s = new SimpleDateFormat("d MMMM yyyy");
        tvDateToday.setText(s.format(new Date()));

        tvStatsAverageSteps = (TextView) view.findViewById(R.id.tvStatsAverageSteps);
        tvStatsTotalSteps = (TextView) view.findViewById(R.id.tvStatsTotalSteps);

        mChart = (com.github.mikephil.charting.charts.BarChart) view.findViewById(R.id.chart1);
        mChart.getDescription().setEnabled(false);
        mChart.getLegend().setForm(LegendForm.NONE);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setScaleXEnabled(false);
        mChart.setScaleYEnabled(false);


        IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter();

        mChart.setDrawValueAboveBar(false);


        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(xAxisFormatter);
        //xAxis.setLabelRotationAngle(20);
        xAxis.setTextColor(getActivity().getResources().getColor(R.color.tab_menu_background));
        //xAxis.setTextSize(10f);



        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(false);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMaximum(10000);
        rightAxis.setAxisMinimum(0);


        /*
            TODO - set limit based on DoctorTab recommendation
         */
        //setData(7, 10000);
        if (mChart.getData() != null)
            mChart.getData().setHighlightEnabled(false);
    }

    private void setData(int count, int[] values) {

        Log.d(LOG_TAG, "count : " + count + " >>> values.length : " + values.length);
        ArrayList<BarEntry> barValues = new ArrayList<>();
        if (values.length == 0) {
            Paint paint = mChart.getPaint(Chart.PAINT_INFO);
            paint.setTextSize(24);
            mChart.setNoDataText("No results for last 7 days");
            mChart.setNoDataTextColor(getActivity().getResources().getColor(R.color.tab_menu_background));
            mChart.clear();
            mChart.invalidate();
        } else {
            for (int i = 0; i < values.length; i++) {
                Log.d(LOG_TAG,"setData values[" + i + "] : " + values[i]);
            }
            for (int i = 0; i < count; i++) {
                Log.d(LOG_TAG,"values[" + i + "] : " + values[i]);
                barValues.add(new BarEntry(-2-i, values[i]));
            }
            BarDataSet set1 = new BarDataSet(barValues, "");
            set1.setColors(ColorTemplate.MATERIAL_COLORS);
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            data.setValueTextSize(10);
            //data.setBarWidth(0.8f);
            mChart.clear();
            mChart.setData(data);
            mChart.invalidate();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        Log.d(LOG_TAG, "setUserVisibleHint() isVisibleToUser : " + isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            // If we are becoming invisible, then...
            if (!isVisibleToUser) {
                Log.d(LOG_TAG, "Not visible anymore.");
                // TODO stop audio playback
            }
        }
    }
}
