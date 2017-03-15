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
import android.os.SystemClock;
import android.util.Log;

import com.master.aluca.fitnessmd.common.Constants;

public class SharedPreferencesManager {

    private static final String LOG_TAG = "Fitness_SharedPrefsMgr";

    private static SharedPreferencesManager mInstance = null;
    private Context mContext;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    private float mWeight;
    private int mHeight;
    private String mGender, mUserName, mSubscription;
    private int mYearOfBirth;

    private boolean mHasProfilePicture;
    private String mProfilePictureURI;

    private boolean mAlwaysEnableBT;

    private boolean mIsLoggedIn;

    private String mSavedDeviceName;
    private String mSavedDeviceAddress;
    private String mEmail;
    private long mWeightLastMeasurementDay;
    private float mWeightGoal;

    private int mStepsForCurrentDay;
    private long mChronometerBase;
    private boolean mChronometerRunning;
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


        mWeight = sharedPreferences.getFloat(Constants.SHARED_PREFS_WEIGHT_KEY, Constants.WEIGHT_DEFAULT_VALUE);
        mHeight = sharedPreferences.getInt(Constants.SHARED_PREFS_HEIGHT_KEY, Constants.HEIGHT_DEFAULT_VALUE);
        mGender = sharedPreferences.getString(Constants.SHARED_PREFS_GENDER_KEY, "Male");
        mYearOfBirth = sharedPreferences.getInt(Constants.SHARED_PREFS_YOB_KEY, Constants.YOB_DEFAULT_VALUE);
        mSubscription = sharedPreferences.getString(Constants.SHARED_PREFS_SUBSC_KEY, null);
        mWeightGoal = sharedPreferences.getFloat(Constants.SHARED_PREFS_WEIGHT_GOAL_KEY, Constants.WEIGHT_DEFAULT_VALUE);

        mHasProfilePicture = sharedPreferences.getBoolean(Constants.SHARED_PREFS_HAS_PROFILE_PIC, false);
        mProfilePictureURI = sharedPreferences.getString(Constants.SHARED_PREFS_PROFILE_PIC_URI, null);
        mIsLoggedIn = sharedPreferences.getBoolean(Constants.IS_USER_LOGGED_IN, false);

        mAlwaysEnableBT = sharedPreferences.getBoolean(Constants.SHARED_PREFS_ALWAYS_ENABLE, false);

        mEmail = sharedPreferences.getString(Constants.SHARED_PREFS_EMAIL_KEY, null);
        mUserName = sharedPreferences.getString(Constants.SHARED_PREFS_NAME_KEY+mEmail, null);
        mWeightLastMeasurementDay = sharedPreferences.getLong(Constants.SHARED_PREFS_WEIGHT_LAST_MSRMNT, -1);

        mSavedDeviceName = sharedPreferences.getString(Constants.SAVED_DEVICE_NAME_KEY, null);
        mSavedDeviceAddress = sharedPreferences.getString(Constants.SAVED_DEVICE_ADDRESS_KEY, null);


        mStepsForCurrentDay = sharedPreferences.getInt(Constants.SHARED_PREFS_CURR_DAY_STEPS+mEmail, 0);
        mChronometerBase = sharedPreferences.getLong(Constants.CHRONOMETER_SHARED_PREFS, SystemClock.elapsedRealtime());
        mChronometerRunning = sharedPreferences.getBoolean(Constants.CHRONOMETER_RUNNING_SHARED_PREFS, false);

        mStartOfCurrentDay = sharedPreferences.getLong(Constants.START_OF_CURRENT_DAY, System.currentTimeMillis());

        mServerLoginToken = sharedPreferences.getString(Constants.SERVER_LOGIN_TOKEN, null);

        mSWStartTime = sharedPreferences.getLong(Constants.SHARED_PREFS_SW_START_TIME+mEmail, System.currentTimeMillis());
        mSWAccumulatedTime = sharedPreferences.getLong(Constants.SHARED_PREFS_SW_ACCUM_TIME+mEmail, System.currentTimeMillis());
        mSWState = sharedPreferences.getInt(Constants.SHARED_PREFS_SW_STATE + mEmail, Constants.STOPWATCH_RESET);
    }

    public float getWeightGoal() {
        return mWeightGoal;
    }

    public void setWeightGoal(float weightGoal) {
        sharedPreferencesEditor.putFloat(Constants.SHARED_PREFS_WEIGHT_GOAL_KEY, weightGoal);
        sharedPreferencesEditor.commit();
        mWeightGoal = weightGoal;
        Log.d(LOG_TAG, "setWeightGoal : " + weightGoal);
    }

    public long getWeightLastMeasurement() {
        return mWeightLastMeasurementDay;
    }

    public void setWeightLastMeasurement(long weightLastMeasurementDay) {
        sharedPreferencesEditor.putLong(Constants.SHARED_PREFS_WEIGHT_LAST_MSRMNT, weightLastMeasurementDay);
        sharedPreferencesEditor.commit();
        mWeightLastMeasurementDay = weightLastMeasurementDay;
    }


    public void setLoggedIn(boolean loggedIn) {
        sharedPreferencesEditor.putBoolean(Constants.IS_USER_LOGGED_IN, loggedIn);
        sharedPreferencesEditor.commit();
        mIsLoggedIn = loggedIn;
    }

    public boolean getHasProfilePicture() {
        return mHasProfilePicture;
    }
    public void setHasProfilePicture(boolean hasProfilePicture) {
        sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFS_HAS_PROFILE_PIC, hasProfilePicture);
        sharedPreferencesEditor.commit();
        mHasProfilePicture = hasProfilePicture;
    }

    public String getProfilePictureURI() {
        return mProfilePictureURI;
    }
    public void setProfilePictureURI(String uri) {
        sharedPreferencesEditor.putString(Constants.SHARED_PREFS_PROFILE_PIC_URI, uri);
        sharedPreferencesEditor.commit();
        mProfilePictureURI = uri;
    }


    public boolean getAlwaysEnableBT() {
        return mAlwaysEnableBT;
    }

    public void setAlwaysEnableBT(boolean alwaysEnableBT) {
        sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFS_ALWAYS_ENABLE, alwaysEnableBT);
        sharedPreferencesEditor.commit();
        mAlwaysEnableBT = alwaysEnableBT;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        sharedPreferencesEditor.putString(Constants.SHARED_PREFS_EMAIL_KEY, email);
        sharedPreferencesEditor.commit();
        this.mEmail = email;
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

    public String getEmail(String email) {
        return sharedPreferences.getString(Constants.SHARED_PREFS_EMAIL_KEY + email, null);
    }

    public String getPassword(String email) {
        return sharedPreferences.getString(Constants.SHARED_PREFS_PASSWORD_KEY + email, null);
    }

    public void addEmail(String email) {
        sharedPreferencesEditor.putString(Constants.SHARED_PREFS_EMAIL_KEY + email, email);
        sharedPreferencesEditor.commit();
    }

    public void addPassword(String email, String password) {
        sharedPreferencesEditor.putString(Constants.SHARED_PREFS_PASSWORD_KEY+email, password);
        sharedPreferencesEditor.commit();
    }
}