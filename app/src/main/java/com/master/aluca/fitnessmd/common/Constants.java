/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.common;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

public class Constants {

    private static final String LOG_TAG = "Fitness_Constants";

    // 24 hours * 60 minutes * 60 seconds * 1000 millis
    public static final long DAY = 24 * 60 * 60 * 1000;

    // 30 minutes * 60 seconds * 1000 millis
    public static final long HALF_HOUR = 30 * 60 * 1000;

    // FOR TESTING PURPOSES
    // 30 seconds * 1000 millis
    public static final long HALF_MINUTE = 30 * 1000;

    public static final String SHARED_PREFERENCES = "FitnessMDPref";

    public static final String LOCALHOST_IP_ADDRESS = "cepirvgtfy.localtunnel.me";
    public static final String LOCALHOST_NODEJS_PORT = "3000";
    public static final String NODEJS_LOGIN_ROUTE = "/auth/login";
    public static final String NODEJS_SIGNUP_ROUTE = "/auth/signup";
    public static final String NODEJS_PUT_PEDOMETER_ROUTE = "/pedometer/putdata";
    public static final String NODEJS_GET_WEIGHT_ROUTE = "/weight/getdata";

    public static final String SHARED_PREFS_NAME_KEY = "SHARED_PREFS_NAME";
    public static final String SHARED_PREFS_PAIR_DEVICE_KEY = "SHARED_PREFS_PAIR_DEVICE";


    public static final String SHARED_PREFS_CURR_DAY_STEPS = "SHARED_PREFS_CURR_DAY_STEPS";

    public static final String SHARED_PREFS_EMAIL_KEY = "SHARED_PREFS_EMAIL";
    public static final String SHARED_PREFS_PASSWORD_KEY = "SHARED_PREFS_PASSWORD";

    public static final CharSequence[] GENDERS = {"Male", "Female"};

    public static final int HEIGHT_MIN_VALUE = 100;
    public static final int HEIGHT_MAX_VALUE = 300;
    public static final int HEIGHT_DEFAULT_VALUE = 165;

    public static final int WEIGHT_KG_MIN_VALUE = 35;
    public static final int WEIGHT_KG_MAX_VALUE = 500;
    public static final int WEIGHT_G_MIN_VALUE = 0;
    public static final int WEIGHT_G_MAX_VALUE = 9;
    public static final float WEIGHT_DEFAULT_VALUE = 55.0f;

    public static final int YOB_MIN_VALUE = 1920;
    public static final int YOB_MAX_VALUE = 2016;
    public static final int YOB_DEFAULT_VALUE = 1980;


    public static final int REQUEST_ENABLE_BT = 1;

    public static final int MESSAGE_BT_STATE_INITIALIZED = 1;
    public static final int MESSAGE_BT_STATE_CONNECTING = 3;
    public static final int MESSAGE_BT_STATE_CONNECTED = 4;
    public static final int MESSAGE_BT_STATE_ERROR = 10;

    public static final int MESSAGE_READ_ACCEL_DATA = 201;
    public static final int MESSAGE_READ_ACCEL_REPORT = 211;

    public static final String CONNECTED_DEVICE_NAME_BUNDLE_KEY = "connected_device_name_key";
    public static final String CONNECTED_DEVICE_ADDRESS_BUNDLE_KEY = "connected_device_address_key";

    public static final String SAVED_DEVICE_ADDRESS_KEY = "saved_device_address";
    public static final String SAVED_DEVICE_NAME_KEY = "saved_device_name";
    public static final String IS_USER_LOGGED_IN = "is_user_logged_in";
    public static final String STEP_INCREMENT_INTENT = "step_increment_intent";
    public static final String END_OF_DAY = "end_of_day_intent";
    public static final String STEP_INCREMENT_BUNDLE_KEY = "step_increment_bundle_key";
    public static final int GET_GALLERY_IMAGE = 3;
    public static final int TAKE_PHOTO = 4;
    public static final String END_OF_DAY_BUNDLE_KEY = "end_of_day_bundle_key";


    public static final String FINISH_ACTIVITY_INTENT = "finish_activity_intent";
    public static final String FINISH_ACTIVITY_BUNDLE_KEY = "finish_activity_bundle_key";

    public static final String GENDER_CHANGED_CALLBACK = "GENDER_CHANGED_INTENT";
    public static final String HEIGHT_CHANGED_CALLBACK = "HEIGHT_CHANGED_INTENT";
    public static final String WEIGHT_CHANGED_CALLBACK = "WEIGHT_CHANGED_INTENT";
    public static final String WEIGHT_GOAL_CHANGED_CALLBACK = "WEIGHT_GOAL_CHANGED_INTENT";
    public static final String YOB_CHANGED_CALLBACK = "YOB_CHANGED_INTENT";
    public static final String CONNECTED_DEVICE_DETAILS_INTENT = "CONNECTED_DEVICE_DETAILS";
    public static final String CHRONOMETER_SHARED_PREFS = "CHRONOMETER_SHARED_PREFS";
    public static final String CHRONOMETER_RUNNING_SHARED_PREFS = "CHRONOMETER_RUNNING";
    public static final String DEVICE_CONNECTION_LOST = "DEVICE_CONNECTION_LOST";
    public static final String START_OF_CURRENT_DAY = "start_of_current_day";
    public static final String SERVER_LOGIN_TOKEN = "server_login_token";
    public static final String PROFILE_IMAGE_CHANGED_CALLBACK = "PROFILE_IMAGE_CHANGED_INTENT";



    public static final String LOGIN_RESULT_BUNDLE_KEY = "login_intent_bundle_key";
    public static final String LOGIN_RESULT_EXTRA_BUNDLE_KEY = "login_intent_extra_bundle_key";


    public static final String SIGNUP_RESULT_BUNDLE_KEY = "signup_intent_bundle_key";
    public static final String SIGNUP_RESULT_EXTRA_BUNDLE_KEY = "signup_intent_extra_bundle_key";

    public static final String ADVICES_SUBSCRIPTION_READY_INTENT = "advice_subscription_ready";
    public static final String ADVICES_SUBSCRIPTION_READY_BUNDLE_KEY = "advice_subscription_bundle_key";

    public static final String METEOR_CONNECTED_BUNDLE_KEY = "meteor_client_connected_bundle_key";

    public static final String SHARED_PREFS_SW_START_TIME = "shared_prefs_sw_start_time";
    public static final String SHARED_PREFS_SW_ACCUM_TIME = "shared_prefs_sw_accum_time";
    public static final String SHARED_PREFS_SW_STATE = "shared_prefs_sw_state";
    public static final String SHARED_PREFS_SW_CLOCK_BASE = "shared_prefs_sw_clock_base";
    public static final String SHARED_PREFS_SW_CLOCK_ELAPSED = "shared_prefs_sw_clock_elapsed";
    public static final String SHARED_PREFS_SW_CLOCK_RUNNING = "shared_prefs_sw_clock_running";


    public static final int STOPWATCH_RESET = 0;
    public static final int STOPWATCH_RUNNING = 1;
    public static final int STOPWATCH_STOPPED = 2;

    private static final String TWO_DIGITS = "%02d";
    private static final String ONE_DIGIT = "%01d";
    public static final int LOGIN_AUTOMATICALLY = 111;
    public static final int LOGIN_RESULT_INTENT = 222;
    public static final int SIGNUP_RESULT_INTENT = 333;
    public static final int METEOR_CLIENT_STATE = 444;



    /*
     *
     *
     *  Bluetooth constants
     *  Prefix 10x
     *
     */
    public static final int CONNECTION_STATE = 101;
    public static final int READ = 102;
    public static final int NOT_CONNECTED = 103;           // we're doing nothing
    public static final int CONNECTING = 104;     // now initiating an outgoing connection
    public static final int CONNECTED = 105;      // now connected to a remote device
    public static final int NO_INTERNET_CONNECTION = 106;
    public static final String NEW_CHALLENGE_INTENT = "new_challenge_intent";
    public static final String NEW_ADVICE_INTENT = "new_advice_intent";


    public static void displayToastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        Log.d(LOG_TAG, "setNumberPickerTextColor");
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException e){
                    Log.d(LOG_TAG, "NoSuchFieldException");
                }
                catch(IllegalAccessException e){
                    Log.d(LOG_TAG, "IllegalAccessException");
                }
                catch(IllegalArgumentException e){
                    Log.d(LOG_TAG, "IllegalArgumentException");
                }
            }
        }
        return false;
    }

    public static long getStartOfCurrentDay() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDayInMillis = calendar.getTimeInMillis();
        Log.d(LOG_TAG, "getStartOfCurrentDay : " + startOfDayInMillis);
        return startOfDayInMillis;
    }
    public static long getStartOfNextDay() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDayInMillis = calendar.getTimeInMillis();
        Log.d(LOG_TAG, "getStartOfCurrentDay : " + startOfDayInMillis);
        return startOfDayInMillis;
    }

    public static String getHashedPassword(String password) {
        MessageDigest md = null;
        String generatedPassword = password;

        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            //String pass = BCrypt.hashpw(rawPass, BCrypt.gensalt());
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< digest.length ;i++)
            {
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
            Log.d(LOG_TAG, "generatedPassword : " + generatedPassword);
            return generatedPassword;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
