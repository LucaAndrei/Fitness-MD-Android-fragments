/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.master.aluca.fitnessmd.common.Constants;

public class SharedPreferencesManager {

    private static final String LOG_TAG = "Fitness_SharedPrefsMgr";

    private static SharedPreferencesManager mInstance = null;
    private Context mContext;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    private String mEmail;

    private int mStepsForCurrentDay;
    private long mStartOfCurrentDay;

    private String mServerLoginToken;

    long mSWStartTime, mSWAccumulatedTime;
    int mSWState;





    public static SharedPreferencesManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPreferencesManager(context);
        }
        return mInstance;
    }

    private SharedPreferencesManager(Context context) {
        Log.d(LOG_TAG, "SharedPreferencesManager");
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        mEmail = sharedPreferences.getString(Constants.SHARED_PREFS_EMAIL_KEY, null);

        mStepsForCurrentDay = sharedPreferences.getInt(Constants.SHARED_PREFS_CURR_DAY_STEPS+mEmail, 0);

        mStartOfCurrentDay = sharedPreferences.getLong(Constants.START_OF_CURRENT_DAY, System.currentTimeMillis());

        mServerLoginToken = sharedPreferences.getString(Constants.SERVER_LOGIN_TOKEN, null);

        mSWStartTime = sharedPreferences.getLong(Constants.SHARED_PREFS_SW_START_TIME+mEmail, System.currentTimeMillis());
        mSWAccumulatedTime = sharedPreferences.getLong(Constants.SHARED_PREFS_SW_ACCUM_TIME+mEmail, System.currentTimeMillis());
        mSWState = sharedPreferences.getInt(Constants.SHARED_PREFS_SW_STATE + mEmail, Constants.STOPWATCH_RESET);
    }

    public String getEmail() {
        return mEmail;
    }

    public String getServerLoginToken() {
        return mServerLoginToken;
    }

    public void saveServerLoginToken(String loginToken) {
        sharedPreferencesEditor.putString(Constants.SERVER_LOGIN_TOKEN, loginToken);
        sharedPreferencesEditor.commit();
        mServerLoginToken = loginToken;
    }


    public int getStepsForCurrentDay() {
        return mStepsForCurrentDay;
    }

    public void setStepsForCurrentDay(String key, int steps) {
        /*if (isUpdate) {
            mStepsForCurrentDay += steps;
        } else {
            mStepsForCurrentDay = 0;
        }*/
        sharedPreferencesEditor.putInt(key, steps);
        sharedPreferencesEditor.commit();
        mStepsForCurrentDay = steps;
    }
    public void resetStartOfCurrentDay(long startOfCurrentDay) {
        sharedPreferencesEditor.putLong(Constants.START_OF_CURRENT_DAY, startOfCurrentDay);
        sharedPreferencesEditor.commit();
        mStartOfCurrentDay = startOfCurrentDay;
    }

    public long getStartOfCurrentDay() {
        return mStartOfCurrentDay;
    }


    public void saveSWStartTime(String key, long mStartTime) {
        sharedPreferencesEditor.putLong(key, mStartTime);
        sharedPreferencesEditor.commit();
        mSWStartTime = mStartTime;
    }

    public void saveSWAccumTime(String key, long mAccumulatedTime) {
        sharedPreferencesEditor.putLong(key, mAccumulatedTime);
        sharedPreferencesEditor.commit();
        mSWAccumulatedTime = mAccumulatedTime;
    }

    public long getSWStartTime() {
        return mSWStartTime;
    }



    public long getSWAccumTime() {
        return mSWAccumulatedTime;
    }

    public void saveSWState(String key, int mState) {
        sharedPreferencesEditor.putInt(key, mState);
        sharedPreferencesEditor.commit();
        mSWState = mState;
    }

    public int getSWState() {
        return mSWState;
    }
}