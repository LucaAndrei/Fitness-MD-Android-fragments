/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.ui.fragments.scale;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.ArcProgress;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.util.SharedPreferencesManager;
import com.master.aluca.fitnessmd.service.FitnessMDService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScaleFragment extends Fragment {

    private static final String LOG_TAG = "Fitness_ScaleFragment";

    private Activity mActivity;
    private SharedPreferencesManager sharedPreferencesManager;
    static TextView tvDate, tvLastMeasurement;
    private ArcProgress mArcProgressScale;

    private Dialog mDialog;

    private FitnessMDService mService;

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
        return inflater.inflate(R.layout.layout_tab_scale, container, false);
    }


    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        View view = getView();
        if (view == null) {
            return;
        }

        Log.d(LOG_TAG,"ScaleFragment");
        sharedPreferencesManager = SharedPreferencesManager.getInstance(getActivity());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.WEIGHT_RECEIVED_INTENT);
        intentFilter.addAction(Constants.WEIGHT_GOAL_INTENT);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);

        setup(view);
    }


    /*
            TODO - this receiver should be unregistered when the application is destroyed
     */
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive : " + intent.getAction());
            if (intent.getAction().equalsIgnoreCase(Constants.WEIGHT_RECEIVED_INTENT)) {
                Log.d(LOG_TAG, "WEIGHT_RECEIVED_INTENT received");
                float weight = intent.getFloatExtra(Constants.WEIGHT_RECEIVED_WEIGHT_BUNDLE_KEY, -1);
                if (weight != -1) {
                    Log.d(LOG_TAG, "weight : " + weight);
                    if (mArcProgressScale != null) {
                        mArcProgressScale.setProgressWeight(weight);
                    }
                } else {
                    Log.d(LOG_TAG, "weight ERROR");
                }

                long lastMeasurementDay = intent.getLongExtra(Constants.WEIGHT_RECEIVED_LAST_MSRMNT_BUNDLE_KEY, -1);
                if (lastMeasurementDay != -1) {
                    Log.d(LOG_TAG, "lastMeasurementDay : " + (new Date(lastMeasurementDay)));
                    SimpleDateFormat s = new SimpleDateFormat("d MMMM yyyy");
                    tvDate.setText(s.format(new Date(lastMeasurementDay)));
                } else {
                    Log.d(LOG_TAG, "lastMeasurementDay ERROR");
                }

            } else if(intent.getAction().equalsIgnoreCase(Constants.WEIGHT_GOAL_INTENT)) {
                Log.d(LOG_TAG, "WEIGHT_GOAL_INTENT received");
                float weightGoal = intent.getFloatExtra(Constants.WEIGHT_GOAL_BUNDLE_KEY, -1);
                if (weightGoal != -1) {
                    Log.d(LOG_TAG, "weightGoal : " + weightGoal);
                    if (mArcProgressScale != null) {
                        mArcProgressScale.setBottomText("Goal: " + weightGoal + " kg");
                    }
                } else {
                    Log.d(LOG_TAG, "weightGoal ERROR");
                }
            }
        }
    };

    public void setup(View view) {
        tvDate = (TextView) view.findViewById(R.id.tvDate);
        tvLastMeasurement = (TextView) view.findViewById(R.id.tvLastMeasurement);
        mArcProgressScale = (ArcProgress) view.findViewById(R.id.arc_progress_scale);
        float weightGoal = sharedPreferencesManager.getWeightGoal();
        mArcProgressScale.setBottomText("Goal: " + weightGoal + " kg");

        float weight = sharedPreferencesManager.getWeight();
        mArcProgressScale.setProgressWeight(weight);

        SimpleDateFormat s = new SimpleDateFormat("d MMMM yyyy");
        tvDate.setText(s.format(new Date(System.currentTimeMillis())));
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }
}
