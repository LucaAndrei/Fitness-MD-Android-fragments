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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NetworkUtil {

    private static final String LOG_TAG = "Fitness_NetworkUtil";

    public static final int NETWORK_STATUS_NOT_CONNECTED=0;
    public static final int NETWORK_STATUS_WIFI=1;
    public static final int NETWORK_STATUS_MOBILE=2;

    public static int getConnectivityStatusString(Context context) {
        //Log.d(LOG_TAG, "getConnectivityStatusString");
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return NETWORK_STATUS_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return NETWORK_STATUS_MOBILE;
        }
        return NETWORK_STATUS_NOT_CONNECTED;
    }

    public static void setMobileDataEnabled(Context context, boolean enabled) {
        Log.d(LOG_TAG, "setMobileDataEnabled : " + enabled);
        try {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class conmanClass = Class.forName(connectivityManager.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(connectivityManager);
            final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {

        }
    }

    public static boolean isConnectedToInternet(Context context) {
        int networkType = getConnectivityStatusString(context);
        if(networkType == NETWORK_STATUS_MOBILE || networkType == NETWORK_STATUS_WIFI) {
            return true;
        }
        return false;
    }


}
