/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.bluetooth.BluetoothManager;
import com.master.aluca.fitnessmd.common.datatypes.User;
import com.master.aluca.fitnessmd.common.util.SharedPreferencesManager;
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;

public class FitnessMDService extends Service {

    public static final String LOG_TAG = "Fitness_Service";
    private BluetoothAdapter mBluetoothAdapter = null;
    // Context, System
    private Context mContext = null;
    private static Handler mActivityHandler = null;
    private ServiceHandler mServiceHandler = new ServiceHandler();
    private final IBinder mBinder = new FitnessMD_Binder();
    private static boolean sRunning = false;

    private BluetoothManager mBtManager;

    private SharedPreferencesManager sharedPreferencesManager;
    private UsersDB mDB;

    public class FitnessMD_Binder extends Binder {
        public FitnessMDService getService() {
            return FitnessMDService.this;
        }
    }

    /**
     * **************************************************
     * Overrided methods
     * ****************************************************
     */
    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "# Service - onCreate() starts here");

        mContext = getApplicationContext();
        WebserverManager.getInstance(mContext).subscribeToChallenges();
        WebserverManager.getInstance(mContext).subscribeToAdvices();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Constants.CONNECTED_DEVICE_DETAILS_INTENT);
        intentFilter.addAction(Constants.DEVICE_CONNECTION_LOST);
        intentFilter.addAction("myIntent");

        registerReceiver(mBroadcastReceiver, intentFilter);
        sharedPreferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());
        mDB = UsersDB.getInstance(getApplicationContext());

        Log.d(LOG_TAG, "# Service : initialize ---");

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            // BT is not on, need to turn on manually.
            // Activity will do this.
        } else {
            if (mBtManager == null) {
                initializeBluetoothManager();
            }
        }
    }

    public boolean enableBluetooth() {
        Log.d(LOG_TAG, "Service - enableBluetooth()");
        return mBluetoothAdapter.enable();
    }

    public void initializeBluetoothManager() {
        Log.d(LOG_TAG, "Service - initializeBluetoothManager()");

        // Initialize the BluetoothManager to perform bluetooth connections
        if (mBtManager == null)
            mBtManager = new BluetoothManager(getApplicationContext(), mServiceHandler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        sRunning = true;
        Log.d(LOG_TAG, "# Service - onStartCommand() starts here");

        sharedPreferencesManager.resetStartOfCurrentDay(Constants.getStartOfCurrentDay());

        // If service returns START_STICKY, android restarts service automatically after forced close.
        // At this time, onStartCommand() method in service must handle null intent.
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "# Service - onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "# Service - onUnbind()");
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "# Service - onDestroy()");
        finalizeService();

    }

    /**
     * Check bluetooth is enabled or not.
     */
    public boolean isBluetoothEnabled() {
        if (mBluetoothAdapter == null) {
            Log.e(LOG_TAG, "# Service - cannot find bluetooth adapter. Restart app.");
            return false;
        }
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * Setting up bluetooth connection
     *
     * @param activityHandler
     */
    public void setup(Handler activityHandler) {
        mActivityHandler = activityHandler;

        // Double check BT manager instance
        if(mBtManager == null)
            initializeBluetoothManager();


        // If ConnectionInfo holds previous connection info,
        // try to connect using it.
        Log.d(LOG_TAG, "setup");
        User connectedUser = mDB.getConnectedUser();
        Log.d(LOG_TAG, "connectedUser.getSavedDeviceAddress() " + connectedUser.getSavedDeviceAddress());
        Log.d(LOG_TAG, "connectedUser.getSavedDeviceName() " + connectedUser.getSavedDeviceName());
        if(connectedUser.getSavedDeviceAddress() != null && connectedUser.getSavedDeviceName() != null) {
            connectDevice(connectedUser.getSavedDeviceAddress());
        } else {
            Constants.displayToastMessage(mContext, "You need to pair with the device.");
        }
    }

    /**
     * Initiate a connection to a remote device.
     *
     * @param address Device's MAC address to connect
     */
    public void connectDevice(String address) {
        Log.d(LOG_TAG, "Service - connect to " + address);
        // Get the BluetoothDevice object
        if (mBluetoothAdapter != null) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

            if (device != null && mBtManager != null) {
                mBtManager.pairDevice(device);
            }
        }
    }

    protected void finalizeService() {
        // Stop the bluetooth session
        mBluetoothAdapter = null;
        if (mBtManager != null)
            mBtManager.closeConnection();
        mBtManager = null;

        sRunning = false;
    }

    /**
     * Receives messages from bluetooth manager
     */
    class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                // Bluetooth state changed
                case Constants.CONNECTION_STATE:
                    // Bluetooth state Changed
                    String state = "null";
                    if (msg.arg1 == Constants.NOT_CONNECTED) {
                        state = "NOT_CONNECTED";
                    } else if (msg.arg1 == Constants.CONNECTED) {
                        state = "CONNECTED";
                    } else if (msg.arg1 == Constants.CONNECTING) {
                        state = "CONNECTING";
                    }

                    Log.d(LOG_TAG, "Service - CONNECTION_STATE: " + state);
                    if (mActivityHandler != null) {
                        switch (msg.arg1) {
                            case Constants.NOT_CONNECTED:
                                mActivityHandler.obtainMessage(Constants.MESSAGE_BT_STATE_INITIALIZED).sendToTarget();
                                break;
                            case Constants.CONNECTING:
                                mActivityHandler.obtainMessage(Constants.MESSAGE_BT_STATE_CONNECTING).sendToTarget();
                                break;
                            case Constants.CONNECTED:
                                mActivityHandler.obtainMessage(Constants.MESSAGE_BT_STATE_CONNECTED).sendToTarget();
                                break;
                        }
                    }
                    break;
                // Received packets from remote
                case Constants.READ:
                    Log.d(LOG_TAG, "Service - MESSAGE_READ: ");
                    byte[] buffer = (byte[]) msg.obj;
                    Log.d(LOG_TAG, "nrOfBytes : " + msg.arg1);
                    int numberOfSteps = 0;
                    for (int i = 0; i < buffer.length; i++) {
                        Log.d(LOG_TAG, "buffer[" + i + "] : " + buffer[i]);
                        numberOfSteps += buffer[i];
                    }

                    Intent intent1 = new Intent(Constants.STEP_INCREMENT_INTENT);
                    intent1.putExtra(Constants.STEP_INCREMENT_BUNDLE_KEY, numberOfSteps);
                    mContext.sendBroadcast(intent1);
                    break;
            }    // End of switch(msg.what)

            super.handleMessage(msg);
        }
    }

    public static boolean isServiceRunning() {
        return sRunning;
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LOG_TAG, "action : " + action);

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF) {
                    Log.d(LOG_TAG, "Service - Bluetooth turned off");
                } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                    Log.d(LOG_TAG, "Service - Bluetooth turned on");
                }
            } else if (action.equals(Constants.CONNECTED_DEVICE_DETAILS_INTENT)) {
                Log.d(LOG_TAG, "Service - CONNECTED_DEVICE_DETAILS: ");

                Bundle bundle = intent.getExtras();
                String deviceAddress = bundle.getString(Constants.CONNECTED_DEVICE_ADDRESS_BUNDLE_KEY);
                String deviceName = bundle.getString(Constants.CONNECTED_DEVICE_NAME_BUNDLE_KEY);

                if(deviceName != null && deviceAddress != null) {
                    // Remember device's address and name
                    mDB.saveDevice(deviceName, deviceAddress);
                    mDB.updateDeviceConnected(true);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + deviceName, Toast.LENGTH_SHORT).show();
                }
            } else if (action.equals(Constants.DEVICE_CONNECTION_LOST)) {
                mDB.updateDeviceConnected(false);
            }
        }
    };

    /**
     * Get connected device name
     */
    public String getDeviceName() {
        return mDB.getConnectedUser().getSavedDeviceName();
    }

    public boolean isDeviceConnected() {
        return mDB.getConnectedUser().getHasDeviceConnected() == 1 ? true : false;
    }

    public void sendStepsToServer(long startOfCurrentDay, int totalSteps, int hourIndex) {
        WebserverManager.getInstance(this).sendStepsToServer(startOfCurrentDay, totalSteps, hourIndex);
    }
}