/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.util.ProfilePhotoUtils;
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;
import com.master.aluca.fitnessmd.library.Meteor;
import com.master.aluca.fitnessmd.service.FitnessMDService;
import com.master.aluca.fitnessmd.ui.fragments.doctor.DoctorFragment;
import com.master.aluca.fitnessmd.ui.fragments.pedometer.PedometerFragment;
import com.master.aluca.fitnessmd.ui.fragments.profile.ProfileFragment;
import com.master.aluca.fitnessmd.ui.fragments.scale.ScaleFragment;
import com.master.aluca.fitnessmd.ui.fragments.settings.SettingsFragment;
import com.master.aluca.fitnessmd.ui.fragments.stats.StatisticsFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "Fitness_MainActivity";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;


    private FitnessMDService mService;
    private String mCurrentFragmentName;
    private FrameLayout mMainLayout;

    private ImageView mImageBT = null;
    private TextView mTextStatus = null;

    private boolean alwaysEnableBT;
    private UsersDB mDB;

    private ActivityHandler mActivityHandler;

    Meteor fitnessMDMeteor;
    private AtomicBoolean wasBTPopupDisplayed = new AtomicBoolean(false);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(6);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Log.d(LOG_TAG, "onCreate");
        loadSharedPrefs();

        mActivityHandler = new ActivityHandler();
        mDB = UsersDB.getInstance(getApplicationContext());
        alwaysEnableBT = mDB.getConnectedUser().getAlwaysEnableBT();

        // Setup views
        mImageBT = (ImageView) findViewById(R.id.status_title);
        mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
        mTextStatus = (TextView) findViewById(R.id.status_text);
        mTextStatus.setText(getResources().getString(R.string.bt_state_init));


        setupTabIcons(viewPager);

        mImageBT = (ImageView) findViewById(R.id.status_title);
        mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
        mTextStatus = (TextView) findViewById(R.id.status_text);
        mTextStatus.setText(getResources().getString(R.string.bt_state_init));


        /*fitnessMDMeteor = FitnessMDMeteor.createInstance(MainActivity.this, "ws://128.224.108.234:3000/websocket");
        fitnessMDMeteor.connect();*/
    }

    private void setupTabIcons(ViewPager viewPager) {
        tabLayout.getTabAt(0).setIcon(R.drawable.tab_selector_pedometer);
        tabLayout.getTabAt(1).setIcon(R.drawable.tab_selector_doctor);
        tabLayout.getTabAt(2).setIcon(R.drawable.tab_selector_profile);
        tabLayout.getTabAt(3).setIcon(R.drawable.tab_selector_scale);
        tabLayout.getTabAt(4).setIcon(R.drawable.tab_selector_stats);
        tabLayout.getTabAt(5).setIcon(R.drawable.tab_selector_settings);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new PedometerFragment(), "Pedometer");
        adapter.addFrag(new DoctorFragment(), "Doctor");
        adapter.addFrag(new ProfileFragment(), "Profile");
        adapter.addFrag(new ScaleFragment(), "Scale");
        adapter.addFrag(new StatisticsFragment(), "Statistics");
        adapter.addFrag(new SettingsFragment(), "Settings");

        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(LOG_TAG, "getItem : " + position);
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            // return null to display only the icon
            return null;
        }
    }


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

            // Activity couldn't work with mService until connections are made
            // So initialize parameters and settings here, not while running onCreate()
            Log.d(LOG_TAG, "# Activity - initialize()");

            mService.setup(mActivityHandler);
            // If BT is not on, request that it be enabled.
            // RetroWatchService.setupBT() will then be called during onActivityResult
            Log.d(LOG_TAG, "wasBTPopupDisplayed.get() : " + wasBTPopupDisplayed.get());
            if (!mService.isBluetoothEnabled() && !alwaysEnableBT && !wasBTPopupDisplayed.get()) {
                Log.d(LOG_TAG, "displayPopup : " + wasBTPopupDisplayed.get());
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                LayoutInflater adbInflater = LayoutInflater.from(MainActivity.this);
                View dialogView = adbInflater.inflate(R.layout.layout_enable_bt, null);

                final CheckBox alwaysEnable = (CheckBox) dialogView.findViewById(R.id.alwaysEnableBluetooth);
                builder.setView(dialogView);
                builder.setMessage(Html.fromHtml("FitnessMD wants to turn on Bluetooth."));
                builder.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        wasBTPopupDisplayed.set(true);
                    }
                });

                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean checkBoxResult = false;

                        if (alwaysEnable.isChecked()) {
                            checkBoxResult = true;
                        }
                        mDB.updateAlwaysEnableBT(checkBoxResult);
                        mService.enableBluetooth();

                        return;
                    }
                });

                builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Some functions will not work unless you turn on bluetooth", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });

                AlertDialog levelDialog = builder.create();
                levelDialog.show();

                //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            } else if (alwaysEnableBT) {
                mService.enableBluetooth();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(LOG_TAG, "on service disconnected");
            mService = null;
        }
    };


    /**
     * **************************************************
     * Handler, Callback, Sub-classes
     * ****************************************************
     */

    public class ActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // BT state messages
                case Constants.MESSAGE_BT_STATE_INITIALIZED:
                    mTextStatus.setText(getResources().getString(R.string.bt_title) + ": " +
                            getResources().getString(R.string.bt_state_init));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
                    break;
                case Constants.MESSAGE_BT_STATE_CONNECTING:
                    mTextStatus.setText(getResources().getString(R.string.bt_title) + ": " +
                            getResources().getString(R.string.bt_state_connect));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_away));
                    break;
                case Constants.MESSAGE_BT_STATE_CONNECTED:
                    if(mService != null) {
                        String deviceName = mService.getDeviceName();
                        if(deviceName != null) {
                            mTextStatus.setText(getResources().getString(R.string.bt_title) + ": " +
                                    getResources().getString(R.string.bt_state_connected) + " " + deviceName);
                            mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_online));
                        }
                    }
                    break;
                case Constants.MESSAGE_BT_STATE_ERROR:
                    mTextStatus.setText(getResources().getString(R.string.bt_state_error));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
                    break;

                // BT Command status
                /*case Constants.MESSAGE_CMD_ERROR_NOT_CONNECTED:
                    mTextStatus.setText(getResources().getString(R.string.bt_cmd_sending_error));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
                    break;

                ////////////////////////////////////////////
                // Contents changed
                ////////////////////////////////////////////
                case Constants.MESSAGE_READ_ACCEL_REPORT:
                    ActivityReport ar = (ActivityReport)msg.obj;
                    if(ar != null) {
                        TimelineFragment frg = (TimelineFragment) mSectionsPagerAdapter.getItem(LLFragmentAdapter.FRAGMENT_POS_TIMELINE);
                        frg.showActivityReport(ar);
                    }
                    break;

                case Constants.MESSAGE_READ_ACCEL_DATA:
                    ContentObject co = (ContentObject)msg.obj;
                    if(co != null) {
                        GraphFragment frg = (GraphFragment) mSectionsPagerAdapter.getItem(LLFragmentAdapter.FRAGMENT_POS_GRAPH);
                        frg.drawAccelData(co.mAccelData);
                    }
                    break;*/
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }    // End of class ActivityHandler

    /*****************************************************
     *	Public classes
     ******************************************************/

    /**
     * Receives result from external activity
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult " + resultCode);
        if (resultCode != 0) {
            switch (requestCode) {
                case Constants.REQUEST_ENABLE_BT:
                    // When the request to enable Bluetooth returns
                    if (resultCode == Activity.RESULT_OK) {
                        // Bluetooth is now enabled, so set up a BT session
                        mService.initializeBluetoothManager();
                    } else {
                        // User did not enable Bluetooth or an error occured
                        Log.e(LOG_TAG, "BT is not enabled");
                        Toast.makeText(this, "Bluetooth was not enabled by user", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.TAKE_PHOTO:
                    Log.d(LOG_TAG, "TAKE_PHOTO");
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    String url = ProfilePhotoUtils.insertImage(getContentResolver(), photo, "profile_pic", "desc");
                    Uri uri = Uri.parse(url);
                    photo = ProfilePhotoUtils.rotatePhoto(getContentResolver(), uri);
                    //mTabMenu.setProfilePicture("Profile", photo);
                    mDB.updateProfilePictureURI(uri.toString());
                    break;
                case Constants.GET_GALLERY_IMAGE:
                    Log.d(LOG_TAG, "GET_GALLERY_IMAGE");
                    //Log.d(LOG_TAG, "data extras : " + data.getExtras().get("data"));
                    Uri mImageUri = data.getData();
                    Log.d(LOG_TAG, "Uri : " + data.getData());
                    Bitmap image = ProfilePhotoUtils.getProfilePicFromGallery(getContentResolver(), mImageUri);
                    //mTabMenu.setProfilePicture("Profile",image);
                    mDB.updateProfilePictureURI(mImageUri.toString());

            }    // End of switch(requestCode)
        } else Log.d(LOG_TAG,"resultCode is 0 ");

    }



    @Override
    public void onStart() {
        Log.d(LOG_TAG, "onStart()");
        super.onStart();

        Intent intent = new Intent(this, FitnessMDService.class);
        if (!FitnessMDService.isServiceRunning()) {
            startService(intent);
        }
        if (!bindService(intent, mServiceConnection, BIND_AUTO_CREATE)) {
            Log.e(LOG_TAG, "Unable to bind to fitnessMD service");
        }

    }

    @Override
    public void onStop() {
        Log.d(LOG_TAG, "onStop()");
        super.onStop();
        if (mService != null) {
            unbindService(mServiceConnection);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume()");

        //each fragment handles its resume for now. The code to resume the connection should be here.
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        //sharedPreferencesManager.setStepsForCurrentDay(mTabMenu.getStepsForCurrentDay(), false);

        WebserverManager mWebserverManager = WebserverManager.getInstance(this);
        mWebserverManager.destroyMeteor();

        super.onDestroy();

    }

    public void loadSharedPrefs() {

        // Define default return values. These should not display, but are needed
        final String STRING_ERROR = "error!";
        final Integer INT_ERROR = -1;
        // ...
        final Set<String> SET_ERROR = new HashSet<>(1);

        // Add an item to the set
        SET_ERROR.add("Set Error!");

        // Loop through the Shared Prefs
        Log.i(LOG_TAG, "-----------------------------------");
        Log.i(LOG_TAG, "-------------------------------------");

        //for (String pref_name: prefs) {

        SharedPreferences preference = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        Map<String, ?> prefMap = preference.getAll();

        Object prefObj;
        Object prefValue = null;

        for (String key : prefMap.keySet()) {

            prefObj = prefMap.get(key);

            if (prefObj instanceof String) prefValue = preference.getString(key, STRING_ERROR);
            if (prefObj instanceof Integer) prefValue = preference.getInt(key, INT_ERROR);
            // ...
            if (prefObj instanceof Set) prefValue = preference.getStringSet(key, SET_ERROR);

            Log.i(LOG_TAG,String.format("Shared Preference : %s - %s - %s", Constants.SHARED_PREFERENCES, key, String.valueOf(prefValue)));

        }

        Log.i(LOG_TAG, "-------------------------------------");

        //}

        Log.i(LOG_TAG, "------------------------------------");

    }
}
