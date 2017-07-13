/**
 * ******************************************************
 * <p/>
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 * <p/>
 * *******************************************************
 */

package com.master.aluca.fitnessmd.ui.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.util.NetworkUtil;
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;
import com.master.aluca.fitnessmd.ui.MainActivity;
import com.master.aluca.fitnessmd.ui.NoMeteorConnectionActivity;

import java.util.concurrent.atomic.AtomicBoolean;


public class SignupActivity extends Activity {
    private static final String LOG_TAG = "Fitness_Signup";

    EditText _nameText, _emailText, _passwordText;
    Button _signupButton;
    TextView _loginLink;

    private WebserverManager mWebserverManager;
    private static Handler mActivityHandler = null;

    ProgressDialog progressDialog = null;
    private UsersDB mDB;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Log.d(LOG_TAG, "onCreate");
            mDB = UsersDB.getInstance(getApplicationContext());
            if (mDB.getConnectedUser() != null) {
                Log.d(LOG_TAG, "mDB isLoggedIn : true");
                Intent intentMainActiv = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intentMainActiv);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            } else {
                _nameText = (EditText) findViewById(R.id.input_name);
                _emailText = (EditText) findViewById(R.id.input_email);
                _passwordText = (EditText) findViewById(R.id.input_password);
                _signupButton = (Button) findViewById(R.id.btn_signup);
                _loginLink = (TextView) findViewById(R.id.link_login);

                progressDialog = new ProgressDialog(SignupActivity.this);

                _signupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(LOG_TAG, "_signupButton onClick");
                        signup();
                    }
                });

                _loginLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(LOG_TAG, "_loginLink onClick");
                        // Finish the registration screen and return to the Login activity
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }
                });

                mWebserverManager = WebserverManager.getInstance(this);
                mActivityHandler = new ActivityHandler();
                mWebserverManager.registerCallback(mActivityHandler);
            }
        
    }

    public void signup() {
        Log.d(LOG_TAG, "Signup");
        /*progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();*/
        _signupButton.setEnabled(false);
        _signupButton.setBackgroundColor(Color.LTGRAY);
        mWebserverManager.requestSignup(_nameText, _emailText, _passwordText);
    }

    public class ActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.SIGNUP_RESULT_INTENT:
                    Log.d(LOG_TAG, "msg SIGNUP_RESULT_INTENT");
                    Log.d(LOG_TAG, "msg.what : " + msg.what);
                    Log.d(LOG_TAG, "msg.arg1 : " + msg.arg1);
                    Log.d(LOG_TAG, "msg.arg2 : " + msg.arg2);
                    if (msg.obj != null) {
                        Log.d(LOG_TAG, "msg.obj : " + String.valueOf(msg.obj.toString()));
                    }
                    if (msg.arg1 > 0) {
                        Log.d(LOG_TAG, "signup and login success");

                        _signupButton.setEnabled(false);
                        _signupButton.setBackgroundColor(Color.LTGRAY);
                        Intent intentMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intentMainActivity);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        /*if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }*/
                        if (msg.obj != null) {
                            Toast.makeText(getBaseContext(), String.valueOf(msg.obj.toString()), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d(LOG_TAG, "signup and login failure");
                        _signupButton.setEnabled(true);
                        _signupButton.setBackgroundColor(Color.parseColor("#52B3D9"));
                        Log.d(LOG_TAG, "Login after signup error");
                        /*if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }*/
                        if (msg.obj != null) {
                            Toast.makeText(getBaseContext(), String.valueOf(msg.obj.toString()), Toast.LENGTH_LONG).show();
                        }
                    }
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        mWebserverManager.removeCallback();
        super.onDestroy();
    }

}