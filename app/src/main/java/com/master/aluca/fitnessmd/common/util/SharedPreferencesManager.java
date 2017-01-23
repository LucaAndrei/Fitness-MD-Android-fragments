package com.master.aluca.fitnessmd.common.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.widget.EditText;

import com.master.aluca.fitnessmd.common.Constants;


/**
 * Created by aluca on 11/8/16.
 */
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
    private IDataRefreshCallback mCallback;
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

    public void registerCallback(IDataRefreshCallback callback) {
        mCallback = callback;
    }

    private SharedPreferencesManager(Context context) {
        Log.d(LOG_TAG, "SharedPreferencesManager");
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        mUserName = sharedPreferences.getString(Constants.SHARED_PREFS_NAME_KEY, null);
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
        mWeightLastMeasurementDay = sharedPreferences.getLong(Constants.SHARED_PREFS_WEIGHT_LAST_MSRMNT, -1);

        mSavedDeviceName = sharedPreferences.getString(Constants.SAVED_DEVICE_NAME_KEY, null);
        mSavedDeviceAddress = sharedPreferences.getString(Constants.SAVED_DEVICE_ADDRESS_KEY, null);


        mStepsForCurrentDay = sharedPreferences.getInt(Constants.SHARED_PREFS_CURR_DAY_STEPS, 0);
        mChronometerBase = sharedPreferences.getLong(Constants.CHRONOMETER_SHARED_PREFS, SystemClock.elapsedRealtime());
        mChronometerRunning = sharedPreferences.getBoolean(Constants.CHRONOMETER_RUNNING_SHARED_PREFS, false);

        mStartOfCurrentDay = sharedPreferences.getLong(Constants.START_OF_CURRENT_DAY, System.currentTimeMillis());

        mServerLoginToken = sharedPreferences.getString(Constants.SERVER_LOGIN_TOKEN, null);

        mSWStartTime = sharedPreferences.getLong(Constants.SHARED_PREFS_SW_START_TIME, 0);
        mSWAccumulatedTime = sharedPreferences.getLong(Constants.SHARED_PREFS_SW_ACCUM_TIME, 0);
        mSWState = sharedPreferences.getInt(Constants.SHARED_PREFS_SW_STATE, Constants.STOPWATCH_RESET);
    }

    public String getUserName() {
        return mUserName;
    }

    public String getUserNameByEmail(String email) {
        return sharedPreferences.getString(email, null);
    }
    public void setUserName(String email, String name) {
        sharedPreferencesEditor.putString(email, name);
        sharedPreferencesEditor.commit();
        mUserName = name;
    }


    public float getWeight() {
        return mWeight;
    }
    public void setWeight(float weight) {
        sharedPreferencesEditor.putFloat(Constants.SHARED_PREFS_WEIGHT_KEY, weight);
        sharedPreferencesEditor.commit();
        mWeight = weight;
        if (mCallback != null)
            mCallback.onDataChanged(Constants.SHARED_PREFS_WEIGHT_KEY);
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

    public int getHeight() {
        return mHeight;
    }
    public void setHeight(int height) {
        sharedPreferencesEditor.putInt(Constants.SHARED_PREFS_HEIGHT_KEY, height);
        sharedPreferencesEditor.commit();
        mHeight = height;
        if (mCallback != null)
            mCallback.onDataChanged(Constants.SHARED_PREFS_HEIGHT_KEY);
    }

    public String getGender() {
        return mGender;
    }
    public void setGender(String gender) {
        sharedPreferencesEditor.putString(Constants.SHARED_PREFS_GENDER_KEY, gender);
        sharedPreferencesEditor.commit();
        mGender = gender;
        if (mCallback != null)
            mCallback.onDataChanged(Constants.SHARED_PREFS_GENDER_KEY);
    }

    public int getYearOfBirth() {
        return mYearOfBirth;
    }
    public void setYearOfBirth(int yearOfBirth) {
        sharedPreferencesEditor.putInt(Constants.SHARED_PREFS_YOB_KEY, yearOfBirth);
        sharedPreferencesEditor.commit();
        mYearOfBirth = yearOfBirth;
        if (mCallback != null)
            mCallback.onDataChanged(Constants.SHARED_PREFS_YOB_KEY);
    }

    public void saveDevice(String deviceName, String deviceAddress) {
        Log.d(LOG_TAG,"deviceName : " + deviceName + " >> " + deviceAddress);
        sharedPreferencesEditor.putString(Constants.SAVED_DEVICE_NAME_KEY, deviceName);
        sharedPreferencesEditor.putString(Constants.SAVED_DEVICE_ADDRESS_KEY, deviceAddress);
        sharedPreferencesEditor.commit();
    }

    public String getSavedDeviceAddress() {
        return mSavedDeviceAddress;
    }

    public String getSavedDeviceName() {
        return mSavedDeviceName;
    }

    public boolean getIsUserLoggedIn() {
        return mIsLoggedIn;
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
        sharedPreferencesEditor.putString(Constants.SERVER_LOGIN_TOKEN, mServerLoginToken);
        sharedPreferencesEditor.commit();
        mServerLoginToken = loginToken;
    }


    public int getStepsForCurrentDay() {
        return mStepsForCurrentDay;
    }

    public void setStepsForCurrentDay(int steps, boolean isUpdate) {
        /*if (isUpdate) {
            mStepsForCurrentDay += steps;
        } else {
            mStepsForCurrentDay = 0;
        }*/

        mStepsForCurrentDay = steps;

        sharedPreferencesEditor.putInt(Constants.SHARED_PREFS_CURR_DAY_STEPS, mStepsForCurrentDay);
        sharedPreferencesEditor.commit();
    }

    public void setChronometerBase(long chronometerBase) {
        sharedPreferencesEditor.putLong(Constants.CHRONOMETER_SHARED_PREFS, chronometerBase);
        sharedPreferencesEditor.commit();
        mChronometerBase = chronometerBase;
    }
    public long getChronometerBase() {
        return mChronometerBase;
    }

    public void setChronometerRunning(boolean chronometerRunning) {
        sharedPreferencesEditor.putBoolean(Constants.CHRONOMETER_RUNNING_SHARED_PREFS, chronometerRunning);
        sharedPreferencesEditor.commit();
        mChronometerRunning = chronometerRunning;
    }

    public boolean getChronometerRunning() {
        return mChronometerRunning;
    }

    public void resetStartOfCurrentDay(long startOfCurrentDay) {
        sharedPreferencesEditor.putLong(Constants.START_OF_CURRENT_DAY, startOfCurrentDay);
        sharedPreferencesEditor.commit();
        mStartOfCurrentDay = startOfCurrentDay;
    }

    public long getStartOfCurrentDay() {
        return mStartOfCurrentDay;
    }


    public void saveSWStartTime(long mStartTime) {
        sharedPreferencesEditor.putLong(Constants.SHARED_PREFS_SW_START_TIME, mStartTime);
        sharedPreferencesEditor.commit();
        mSWStartTime = mStartTime;
    }

    public void saveSWAccumTime(long mAccumulatedTime) {
        sharedPreferencesEditor.putLong(Constants.SHARED_PREFS_SW_ACCUM_TIME, mAccumulatedTime);
        sharedPreferencesEditor.commit();
        mSWAccumulatedTime = mAccumulatedTime;
    }

    public long getSWStartTime() {
        return mSWStartTime;
    }



    public long getSWAccumTime() {
        return mSWAccumulatedTime;
    }

    public void saveSWState(int mState) {
        sharedPreferencesEditor.putInt(Constants.SHARED_PREFS_SW_STATE, mState);
        sharedPreferencesEditor.commit();
        mSWState = mState;
    }

    public int getSWState() {
        return mSWState;
    }
}