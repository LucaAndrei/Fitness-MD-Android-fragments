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

public class NoInternetActivity extends Activity {

    public static final String LOG_TAG = "Fitness_NoNetActivity";
    private Button _btnRetry;
    private UsersDB mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_nointernet);

        mDB = UsersDB.getInstance(getApplicationContext());

        _btnRetry = (Button) findViewById(R.id.btn_retry);
        _btnRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtil.isConnectedToInternet(getApplicationContext())) {
                    Log.d(LOG_TAG, "NO INTERNET CONNECTION");
                    Toast.makeText(getBaseContext(), "No Internet", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), "Has internet conn", Toast.LENGTH_LONG).show();
                    if (mDB.getConnectedUser() != null) {
                        Log.d(LOG_TAG, "mDB isLoggedIn : true");
                        Intent intentMainActiv = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intentMainActiv);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    } else {
                        Intent intentMainActiv = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intentMainActiv);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }

                }
            }
        });

    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

}