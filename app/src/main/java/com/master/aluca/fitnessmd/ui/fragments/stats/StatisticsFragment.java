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
import com.master.aluca.fitnessmd.service.FitnessMDService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class StatisticsFragment extends Fragment {

    private static final String LOG_TAG = "Fitness_Statistics";

    private static StatisticsFragment mInstance = null;
    private com.github.mikephil.charting.charts.BarChart mChart;
    TextView tvStatsAverageSteps, tvStatsTotalSteps;

    private Activity mActivity;
    private TextView tvDateToday;

    private FitnessMDService mService;

    private Dialog mDialog;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(LOG_TAG, "on service connected");
            FitnessMDService.FitnessMD_Binder binder = (FitnessMDService.FitnessMD_Binder) iBinder;
            mService = binder.getService();

            if (mService == null) {
                Log.e(LOG_TAG, "unable to connect to service");
                return;
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
    }

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

        Log.d(LOG_TAG, "StatisticsFragment");

        setup(view);
    }

    public void setup(View view) {
        tvDateToday = (TextView) view.findViewById(R.id.tvDateToday);
        SimpleDateFormat s = new SimpleDateFormat("d MMMM yyyy");
        tvDateToday.setText(s.format(new Date()));

        tvStatsAverageSteps = (TextView) view.findViewById(R.id.tvStatsAverageSteps);
        tvStatsTotalSteps = (TextView) view.findViewById(R.id.tvStatsTotalSteps);

        setStatsAverageSteps();
        setStatsTotalSteps();

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
        setData(7, 10000);
        if (mChart.getData() != null)
            mChart.getData().setHighlightEnabled(false);
    }

    private void setData(int count, float range) {

        /*ArrayList<StepsDayReport> stepsDayReports = mDB.getLastWeekReport(System.currentTimeMillis());
        Log.d(LOG_TAG,"stepsDayReports.size : " + stepsDayReports.size());*/


        Log.d(LOG_TAG, "count : " + count + " >>> range : " + range);
        ArrayList<BarEntry> barValues = new ArrayList<>();
        /*for (int i = 1; i < count + 1; i++) {
            float mult = (range + 1);
            int val = (int) (Math.random() * mult);
            Log.d(LOG_TAG, "mult : " + mult);
            Log.d(LOG_TAG, "val : " + val);
            barValues.add(new BarEntry(-1-i, val));
        }*/

        String dateFormat = "EEE,MMM d";
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        Calendar cal = Calendar.getInstance();
        /*if (stepsDayReports.size() == 0) {
            Log.d(LOG_TAG, "NO DATA CHART TEXT");
            Paint paint = mChart.getPaint(Chart.PAINT_INFO);
            paint.setTextSize(24);
            mChart.setNoDataText("No results for last 7 days");
            mChart.setNoDataTextColor(getActivity().getResources().getColor(R.color.tab_menu_background));
            mChart.clear();
            mChart.invalidate();
        } else {*/
            for (int i = 0; i < count; i++) {
                //barValues.add(new BarEntry(-2-i, stepsDayReports.get(i).getSteps()));
                Random r = new Random();
                int Low = 3000;
                int High = 10000;
                int Result = r.nextInt(High-Low) + Low;
                barValues.add(new BarEntry(-2-i, Result));
                //barValues.add(new BarEntry(s.format(new Date(stepsDayReports.get(i).getDay())),stepsDayReports.get(i).getSteps()));
                //Log.d(LOG_TAG,"stepsDayReports.size : " + stepsDayReports.size());
                //Log.d(LOG_TAG,"stepsDayReports.get(i).getSteps() : " + stepsDayReports.get(i).getSteps());
                //String yesterday = getCalculatedDate((int) (value + 1));
                //Log.d(LOG_TAG, (int)value + " day(s) ago was : " + yesterday);
                Log.d(LOG_TAG, "Result : " + Result);

                //cal.add(Calendar.DAY_OF_YEAR, days);
                //return s.format(new Date(cal.getTimeInMillis()));
            }
            Log.d(LOG_TAG, "create mchart data");
            BarDataSet set1 = new BarDataSet(barValues, "");
            set1.setColors(ColorTemplate.MATERIAL_COLORS);
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            data.setValueTextSize(10);
            //data.setBarWidth(0.8f);
            mChart.setData(data);
       // }
    }

    private void setStatsAverageSteps() {
        /*StepsDayReport averageStepsRaport = mDB.getAverageSteps();
        int averageSteps = averageStepsRaport.getSteps();
        tvStatsAverageSteps.setText(String.valueOf(averageSteps));*/
    }
    private void setStatsTotalSteps() {
        /*StepsDayReport averageStepsRaport = mDB.getTotalSteps();
        int averageSteps = averageStepsRaport.getSteps();
        tvStatsTotalSteps.setText(String.valueOf(averageSteps));*/
    }
}
