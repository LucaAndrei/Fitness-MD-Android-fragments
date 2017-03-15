/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.service;

import android.app.ProgressDialog;
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
import com.master.aluca.fitnessmd.common.bluetooth.ConnectionInfo;
import com.master.aluca.fitnessmd.common.datatypes.StepsDayReport;
import com.master.aluca.fitnessmd.common.datatypes.WeightDayReport;
import com.master.aluca.fitnessmd.common.util.IStepNotifier;
import com.master.aluca.fitnessmd.common.util.NetworkUtil;
import com.master.aluca.fitnessmd.common.util.SharedPreferencesManager;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;
import com.master.aluca.fitnessmd.receivers.AlarmReceiver;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class FitnessMDService extends Service {
    /*

        Model baza de date
        id    data(long millis)      steps(int)       weight(float)      wasPushedToServer(boolean)

        Cand se termina ziua -> save steps, weight in baza de date locala;
                                ca si data foloseste inceputul zilei (asa cum e folosita in Alarm);
                                wasPushedToServer - by default sa fie pe false
                             -> inregistreaza un receiver care sa asculte atunci cand se activeaza/dezactiveaza conexiunea internet
                             -> daca conexiunea la internet -> este activa extrage din baza de date toate datele care au wasPushedToServer == false
                                                               fa push la datele astea pe server
                                                               update in baza de date si pune wasPushedToServer pe true
                                                            -> nu este activa salveaza un flag in serviciu care sa indice daca sunt/nu sunt date
                                                               la care ar trebui facut push pe server;
                                                               cand se primeste intentul ca s-a schimbat conexiunea la internet, verifica flag-ul :
                                                               daca trebuie facut push la date, fa push.

        Daca utilizatorul a apasat sync now -> daca conexiunea la internet -> este activa extrage din baza de date toate datele care au wasPushedToServer == false
                                                               fa push la datele astea pe server
                                                               update in baza de date si pune wasPushedToServer pe true
                                            -> daca conexiunea la internet nu este activa -> notifica utilizatorul si cere-i sa activezi conexiunea la internet
                                               cand se primeste intentul ca s-a schimbat conexiunea la internet, verifica flag-ul :
                                                               daca trebuie facut push la date, fa push.




     */

    public static final String LOG_TAG = "Fitness_Service";
    private BluetoothAdapter mBluetoothAdapter = null;
    // Context, System
    private Context mContext = null;
    private static Handler mActivityHandler = null;
    private ServiceHandler mServiceHandler = new ServiceHandler();
    private final IBinder mBinder = new FitnessMD_Binder();
    private static boolean sRunning = false;

    private ConnectionInfo mConnectionInfo;


    private BluetoothManager mBtManager;

    private ArrayList<StepsDayReport> mNotPushedToServerData = new ArrayList<>();


    AlarmReceiver alarm;

    private IStepNotifier mCallback;

    private static AtomicBoolean isConnectedToWifi = new AtomicBoolean(false);
    private static AtomicBoolean isConnectedToNetworkData = new AtomicBoolean(false);

    private int calories;
    private long timeActive;
    private long timeServiceStarted;

    private WebserverManager mWebserverManager;
    private SharedPreferencesManager sharedPreferencesManager;

    ProgressDialog progressDialog = null;

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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Constants.END_OF_DAY);
        intentFilter.addAction(Constants.CONNECTED_DEVICE_DETAILS_INTENT);
        intentFilter.addAction(Constants.DEVICE_CONNECTION_LOST);

        registerReceiver(mReceiver, intentFilter);
        sharedPreferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());

        alarm = new AlarmReceiver();

        Log.d(LOG_TAG, "# Service : initialize ---");

        // Get connection info instance
        mConnectionInfo = ConnectionInfo.getInstance(mContext);

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

        alarm.setAlarm(this);
        sharedPreferencesManager.resetStartOfCurrentDay(Constants.getStartOfCurrentDay());



        // If service returns START_STICKY, android restarts service automatically after forced close.
        // At this time, onStartCommand() method in service must handle null intent.
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "# Service - onBind()");
        timeServiceStarted = System.currentTimeMillis();
        mWebserverManager = WebserverManager.getInstance(this);
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
        Log.d(LOG_TAG, "mConnectionInfo.getSavedDeviceAddress() " + mConnectionInfo.getSavedDeviceAddress());
        Log.d(LOG_TAG, "mConnectionInfo.getSavedDeviceName() " + mConnectionInfo.getSavedDeviceName());
        if(mConnectionInfo.getSavedDeviceAddress() != null && mConnectionInfo.getSavedDeviceName() != null) {
            connectDevice(mConnectionInfo.getSavedDeviceAddress());
        } else {
            Constants.displayToastMessage(mContext, "You need to pair with the device.");
        }

        // Get content manager
        // TODO:
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
        // Save activity report to DB
        //TODO - mContentManager.saveCurrentActivityReport();

        // Stop the bluetooth session
        mBluetoothAdapter = null;
        if (mBtManager != null)
            mBtManager.closeConnection();
        mBtManager = null;

        sRunning = false;
    }

    public long getTimeActive() {
        Log.d(LOG_TAG,"getTimeActive");

        Date timeNow = new Date(System.currentTimeMillis());
        Date dateTimeServiceStarted = new Date(timeServiceStarted);
        long millis = timeNow.getTime() - dateTimeServiceStarted.getTime();
        int Hours = (int) (millis/(1000 * 60 * 60));
        int Mins = (int) (millis/(1000*60)) % 60;
        String diff = Hours + ":" + Mins;
        Log.d(LOG_TAG,"getTimeActive diff : " + diff);
        return millis;
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

                    mCallback.onStepIncrement(numberOfSteps);

                    Intent intent1 = new Intent(Constants.STEP_INCREMENT_INTENT);
                    intent1.putExtra(Constants.STEP_INCREMENT_BUNDLE_KEY, numberOfSteps);
                    mContext.sendBroadcast(intent1);
                    break;
            }    // End of switch(msg.what)

            super.handleMessage(msg);
        }
    }

    public void registerCallback(IStepNotifier callback) {
        mCallback = callback;
    }

    public boolean isConnectedToWifi() {
        return isConnectedToWifi.get();
    }

    public boolean isConnectedToNetworkData() {
        return isConnectedToNetworkData.get();
    }

    public synchronized static boolean hasInternet() {
        return isConnectedToWifi.get() || isConnectedToNetworkData.get();
    }


    public static boolean isServiceRunning() {
        return sRunning;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LOG_TAG, "action : " + action);

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF) {
                    Log.d(LOG_TAG, "Service - Bluetooth turned off");
                } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                    Log.d(LOG_TAG, "Service - Bluetooth turned on");
                    //for testing only. this should be moved where MESSAGE_READ_ACCEL_REPORT is sent
                    //Intent intent1 = new Intent(Constants.STEP_INCREMENT_INTENT);
                    //intent1.putExtra(Constants.STEP_INCREMENT_BUNDLE_KEY, 17);
                    //mContext.sendBroadcast(intent1);
                }
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d(LOG_TAG, "ConnectivityManager.CONNECTIVITY_ACTION received");
                int status = NetworkUtil.getConnectivityStatusString(context);
                Log.d(LOG_TAG, "status : " + status);
                if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    Log.d(LOG_TAG, "NETWORK_STATUS_NOT_CONNECTED");
                    isConnectedToNetworkData.set(false);
                    isConnectedToWifi.set(false);
                } else if (status == NetworkUtil.NETWORK_STATUS_MOBILE) {
                    Log.d(LOG_TAG, "NETWORK_STATUS_MOBILE");
                    isConnectedToWifi.set(false);
                    isConnectedToNetworkData.set(true);
                    pushDataToServer(false);
                } else if (status == NetworkUtil.NETWORK_STATUS_WIFI) {
                    Log.d(LOG_TAG, "NETWORK_STATUS_WIFI");
                    isConnectedToWifi.set(true);
                    isConnectedToNetworkData.set(false);
                    pushDataToServer(true);

                } else {
                    isConnectedToWifi.set(false);
                    isConnectedToNetworkData.set(false);
                    Log.d(LOG_TAG, "network status unknown");
                }
            } else if (action.equals(Constants.END_OF_DAY)) {
                Log.d(LOG_TAG, "save to db intent received");
                /*Log.d(LOG_TAG, "weight : " + mDB.getAverageWeight().getWeight());
                long dayForReport = intent.getLongExtra(Constants.END_OF_DAY_BUNDLE_KEY, -1);
                int steps = 110;//getSteps();
                float weight = getWeight();
                int calories = 220;//getCalories();
                long timeActive = getTimeActive();
                int wasPushedToServer = 0;
                Log.d(LOG_TAG, "dayForReport : " + (new Date(dayForReport)) + " >>> steps : " + steps + " >>> weight : " + weight
                        + " >>> calories : " + calories + " >>> timeActive : " + (new Date(timeActive)) + " >>> wasPushedToServer : " + wasPushedToServer);
                long error = mDB.insertActivityReport(dayForReport, steps, weight, calories, timeActive, wasPushedToServer);
                sharedPreferencesManager.setStepsForCurrentDay(sharedPreferencesManager.getEmail(), 0);

                if ( error == ErrorCodes.GENERAL_ERROR) {
                    Log.d(LOG_TAG, "inserting to DB failed. GENERAL_ERROR");
                } else if (error == ErrorCodes.INVALID_TIME) {
                    Log.d(LOG_TAG, "inserting to DB failed. INVALID_TIME");
                } else if (error == ErrorCodes.INVALID_WEIGHT) {
                    Log.d(LOG_TAG, "inserting to DB failed. INVALID_WEIGHT");
                }
                if (isConnectedToWifi) {
                    Log.d(LOG_TAG, "user connected to WIFI");
                    pushDataToServer(false);
                } else if (isConnectedToNetworkData) {
                    Log.d(LOG_TAG, "user connected to NetworkData");
                    pushDataToServer(true);
                } else {
                    Log.d(LOG_TAG, "user not connected to internet");
                    mNotPushedToServerData.add(new StepsDayReport(steps, dayForReport, timeActive));
                }*/

                //NetworkUtil.setMobileDataEnabled(context, true);
            } else if (action.equals(Constants.CONNECTED_DEVICE_DETAILS_INTENT)) {
                Log.d(LOG_TAG, "Service - CONNECTED_DEVICE_DETAILS: ");

                Bundle bundle = intent.getExtras();
                String deviceAddress = bundle.getString(Constants.CONNECTED_DEVICE_ADDRESS_BUNDLE_KEY);
                String deviceName = bundle.getString(Constants.CONNECTED_DEVICE_NAME_BUNDLE_KEY);

                if(deviceName != null && deviceAddress != null) {
                    // Remember device's address and name
                    mConnectionInfo.saveDevice(deviceName, deviceAddress);
                    mConnectionInfo.setDeviceConnected(true);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + deviceName, Toast.LENGTH_SHORT).show();
                }
            } else if (action.equals(Constants.DEVICE_CONNECTION_LOST)) {
                mConnectionInfo.setDeviceConnected(false);
            }
        }
    };

    /**
     * Get connected device name
     */
    public String getDeviceName() {
        return mConnectionInfo.getSavedDeviceName();
    }

    public boolean isDeviceConnected() {
        return mConnectionInfo.getDeviceConnected();
    }

    private void pushDataToServer(boolean isWifi) {
        Log.d(LOG_TAG, "pushDataToServer isWifi : " + isWifi);
        if(mNotPushedToServerData.size() > 0) {
            Iterator<StepsDayReport> iterator = mNotPushedToServerData.iterator();
            while(iterator.hasNext()) {
                StepsDayReport dayReport = iterator.next();
                if (WebserverManager.getInstance(this).sendPedometerData(dayReport.getDay(), dayReport.getSteps(), dayReport.getTimeActive())) {
                    /*if (mDB.setActivityReportPushedToServer(dayReport.getDay()) != 1){
                        if(isWifi) {
                            Log.d(LOG_TAG, "NETWORK_STATUS_WIFI setActivityReportPushedToServer : " + (new Date(dayReport.getDay())) + " ERROR");
                        } else {
                            Log.d(LOG_TAG, "NETWORK_STATUS_MOBILE setActivityReportPushedToServer : " + (new Date(dayReport.getDay())) + " ERROR");
                        }
                    } else {
                        if (isWifi) {
                            Log.d(LOG_TAG, "NETWORK_STATUS_WIFI setActivityReportPushedToServer : " + (new Date(dayReport.getDay())) + " OK");
                        } else {
                            Log.d(LOG_TAG, "NETWORK_STATUS_MOBILE setActivityReportPushedToServer : " + (new Date(dayReport.getDay())) + " OK");
                        }
                    }*/
                    iterator.remove();
                } else {
                    Log.d(LOG_TAG, "could not push to web server");
                }
            }
        }
    }

    public void eraseAllData() {
        Log.d(LOG_TAG, "eraseAllData");
        /*int numberOfRowsAffected = mDB.eraseAllData();
        if (numberOfRowsAffected != ErrorCodes.GENERAL_ERROR) {
            Log.d(LOG_TAG, "eraseAllData SUCCESS : " + numberOfRowsAffected);
        } else {
            Log.d(LOG_TAG, "eraseAllData ERROR : " + numberOfRowsAffected);
        }*/
    }

    public void sendStepsToServer(long startOfCurrentDay, int totalSteps, int hourIndex) {
//        if (mWebserverManager == null)
//            mWebserverManager = WebserverManager.getInstance(this);
        WebserverManager.getInstance(this).sendStepsToServer(startOfCurrentDay, totalSteps, hourIndex);
    }
}