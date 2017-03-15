/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.common.bluetooth;

import android.content.Context;
import android.util.Log;

import com.master.aluca.fitnessmd.common.datatypes.Device;
import com.master.aluca.fitnessmd.common.util.UsersDB;


// Holds information about a saved device
public class ConnectionInfo {

    // Instance
    private static ConnectionInfo mInstance = null;

    private Context mContext;

    // Target device's MAC address
    private String mDeviceAddress = null;
    // Name of the connected device
    private String mDeviceName = null;
    private boolean deviceConnected;
    private Device mDevice;



    private ConnectionInfo(Context c) {
        mContext = c;
        mDevice = UsersDB.getInstance(mContext).getPairedDevice();
        if (mDevice != null) {

            mDeviceAddress = mDevice.getAddress();
            mDeviceName = mDevice.getName();
            Log.d("Fitness_ConnInfo","deviceAddress : " + mDeviceAddress + " >>> deviceName : " + mDeviceName);
        } else {

            Log.d("Fitness_ConnInfo","mDevice is null");
        }
    }

    /**
     * Single pattern
     */
    public synchronized static ConnectionInfo getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ConnectionInfo(context);
        }
        return mInstance;
    }

    /**
     * Get saved device name
     *
     * @return String        device name
     */
    public String getSavedDeviceName() {
        return mDeviceName;
    }

    /**
     * Get saved device address
     *
     * @return String        device address
     */
    public String getSavedDeviceAddress() {
        return mDeviceAddress;
    }

    public void saveDevice(String deviceName, String deviceAddress) {
        mDeviceName = deviceName;
        mDeviceAddress = deviceAddress;
        UsersDB.getInstance(mContext).saveDevice("qwe@qwe.qwe",deviceName, deviceAddress);

    }

    public boolean getDeviceConnected() {
        return deviceConnected;
    }

    public void setDeviceConnected(boolean deviceConnected) {
        this.deviceConnected = deviceConnected;
    }
}