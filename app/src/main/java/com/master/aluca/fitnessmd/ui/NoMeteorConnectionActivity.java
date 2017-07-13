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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.util.NetworkUtil;
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;
import com.master.aluca.fitnessmd.ui.auth.LoginActivity;

public class NoMeteorConnectionActivity extends Activity {

    public static final String LOG_TAG = "Fitness_NoNetActivity";
    private Button _btnFinish;
    private UsersDB mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_no_meteor_conn);

        mDB = UsersDB.getInstance(getApplicationContext());

        _btnFinish = (Button) findViewById(R.id.btn_finish);
        _btnFinish.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

}