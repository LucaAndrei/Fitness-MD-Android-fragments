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


import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.ArcProgress;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.datatypes.User;
import com.master.aluca.fitnessmd.common.util.IDataRefreshCallback;
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.service.FitnessMDService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScaleFragment extends Fragment {

    private static final String LOG_TAG = "Fitness_ScaleFragment";

    private Activity mActivity;
    private UsersDB mDB;
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
        mDB = UsersDB.getInstance(getActivity());
        mDB.registerCallback(mCallback);


        setup(view);
    }

    private IDataRefreshCallback mCallback = new IDataRefreshCallback() {
        @Override
        public void onDataChanged(String changedDataKey) {
            Log.d(LOG_TAG,"onDataChanged : " + changedDataKey);
            User connectedUser = mDB.getConnectedUser();
            switch(changedDataKey) {
                case Constants.WEIGHT_CHANGED_CALLBACK:
                    Log.d(LOG_TAG, "weight : " + connectedUser.getWeight());
                    if (mArcProgressScale != null) {
                        mArcProgressScale.setProgressWeight(connectedUser.getWeight());
                    }
                    // long lastMeasurementDay = intent.getLongExtra(Constants.WEIGHT_RECEIVED_LAST_MSRMNT_BUNDLE_KEY, -1);
                    // if (lastMeasurementDay != -1) {
                    //     Log.d(LOG_TAG, "lastMeasurementDay : " + (new Date(lastMeasurementDay)));
                    //     SimpleDateFormat s = new SimpleDateFormat("d MMMM yyyy");
                    //     tvDate.setText(s.format(new Date(lastMeasurementDay)));
                    // } else {
                    //     Log.d(LOG_TAG, "lastMeasurementDay ERROR");
                    // }
                    break;
                case Constants.WEIGHT_GOAL_CHANGED_CALLBACK :
                    Log.d(LOG_TAG, "weightGoal : " + connectedUser.getWeightGoal());
                    if (mArcProgressScale != null) {
                        mArcProgressScale.setBottomText("Goal: " + connectedUser.getWeightGoal() + " kg");
                    }
                    break;
            }
        }
    };

    public void setup(View view) {
        User connectedUser = mDB.getConnectedUser();
        tvDate = (TextView) view.findViewById(R.id.tvDate);
        tvLastMeasurement = (TextView) view.findViewById(R.id.tvLastMeasurement);
        mArcProgressScale = (ArcProgress) view.findViewById(R.id.arc_progress_scale);
        float weightGoal = connectedUser.getWeightGoal();
        mArcProgressScale.setBottomText("Goal: " + weightGoal + " kg");

        float weight = connectedUser.getWeight();
        mArcProgressScale.setProgressWeight(weight);

        SimpleDateFormat s = new SimpleDateFormat("d MMMM yyyy");
        tvDate.setText(s.format(new Date(System.currentTimeMillis())));
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }
}
