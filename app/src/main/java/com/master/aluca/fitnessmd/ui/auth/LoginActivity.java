/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.util.NetworkUtil;
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;
import com.master.aluca.fitnessmd.ui.MainActivity;
import com.master.aluca.fitnessmd.ui.NoInternetActivity;

import java.util.concurrent.atomic.AtomicBoolean;


public class LoginActivity extends Activity{

    public static final String LOG_TAG = "Fitness_LoginActivity";

    EditText _emailText, _passwordText;
    Button _loginButton;
    TextView _signupLink;

    private static final int REQUEST_SIGNUP = 0;

    private static boolean animPlayed = false;

    private WebserverManager mWebserverManager;

    private UsersDB mDB;

    ProgressDialog progressDialog = null;

    private static Handler mActivityHandler = null;
    private AtomicBoolean isReceiverRegistered = new AtomicBoolean(false);

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        if (!NetworkUtil.isConnectedToInternet(getApplicationContext())) {
            Log.d(LOG_TAG, "NO INTERNET CONNECTION");

            // should comment out these lines until production otherwise i will have to keep internet enabled all the time
            // in order to enter the application.
            // this screen should look like the one from the playstore when you have no internet
            // a return/back button to exit the app, or retry
            Intent intentMainActiv = new Intent(getApplicationContext(), NoInternetActivity.class);
            startActivity(intentMainActiv);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(mReceiver, intentFilter);
            isReceiverRegistered.set(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
        if (isReceiverRegistered.get()) {
            isReceiverRegistered.set(false);
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(LOG_TAG, "onCreate");

        if (!NetworkUtil.isConnectedToInternet(getApplicationContext())) {
            Log.d(LOG_TAG, "NO INTERNET CONNECTION");

            // should comment out these lines until production otherwise i will have to keep internet enabled all the time
            // in order to enter the application.
            // this screen should look like the one from the playstore when you have no internet
            // a return/back button to exit the app, or retry
            Intent intentMainActiv = new Intent(getApplicationContext(), NoInternetActivity.class);
            startActivity(intentMainActiv);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else {
            mDB = UsersDB.getInstance(getApplicationContext());
            if (mDB.getConnectedUser() != null) {
                Log.d(LOG_TAG, "mDB isLoggedIn : true");
                Intent intentMainActiv = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intentMainActiv);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            } else {
                _emailText = (EditText) findViewById(R.id.input_email_login);
                _passwordText = (EditText) findViewById(R.id.input_password_login);
                _loginButton = (Button) findViewById(R.id.btn_login);
                _signupLink = (TextView) findViewById(R.id.link_signup);

                progressDialog = new ProgressDialog(LoginActivity.this);


                setListeners();
                if (!animPlayed) {
                    startAnimations();
                    animPlayed = true;
                }
                mWebserverManager =  WebserverManager.getInstance(this);
                mActivityHandler = new ActivityHandler();
                mWebserverManager.registerCallback(mActivityHandler);
                /*if (!progressDialog.isShowing()) {
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Connecting to server...");
                    progressDialog.show();
                }*/
            }
        }
    }

    public class ActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.LOGIN_RESULT_INTENT:
                    Log.d(LOG_TAG, "msg LOGIN_RESULT_INTENT");
                    Log.d(LOG_TAG, "msg.what : " + msg.what);
                    Log.d(LOG_TAG, "msg.arg1 : " + msg.arg1);
                    Log.d(LOG_TAG, "msg.arg2 : " + msg.arg2);
                    if (msg.obj != null) {
                        Log.d(LOG_TAG, "msg.obj : " + String.valueOf(msg.obj.toString()));
                    }
                    if (msg.arg1 > 0) {
                        /*if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }*/
                        _loginButton.setEnabled(false);
                        _loginButton.setBackgroundColor(Color.LTGRAY);

                        Log.d(LOG_TAG, "Login sucess");

                        Intent intentMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intentMainActivity);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    } else if (msg.arg2 > 0) {
                        Log.d(LOG_TAG, "Login error >> NO INTERNET CONNECTION");
                        _loginButton.setEnabled(true);
                        _loginButton.setBackgroundColor(Color.parseColor("#52B3D9"));
                        Intent intentMainActiv = new Intent(getApplicationContext(), NoInternetActivity.class);
                        startActivity(intentMainActiv);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    } else {
                        _loginButton.setEnabled(true);
                        _loginButton.setBackgroundColor(Color.parseColor("#52B3D9"));
                        Log.d(LOG_TAG, "Login error");
                        /*if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }*/
                        if (msg.obj!= null) {
                            Toast.makeText(getBaseContext(), String.valueOf(msg.obj.toString()), Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
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
                        Intent intentMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intentMainActivity);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        /*if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }*/
                        _loginButton.setEnabled(false);
                        _loginButton.setBackgroundColor(Color.LTGRAY);
                        if (msg.obj!= null) {
                            Toast.makeText(getBaseContext(), String.valueOf(msg.obj.toString()), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d(LOG_TAG, "signup and login failure");
                        _loginButton.setEnabled(true);
                        _loginButton.setBackgroundColor(Color.parseColor("#52B3D9"));
                        Log.d(LOG_TAG, "Login after signup error");
                        /*if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }*/
                        if (msg.obj!= null) {
                            Toast.makeText(getBaseContext(), String.valueOf(msg.obj.toString()), Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                case Constants.METEOR_CLIENT_STATE: {
                    Log.d(LOG_TAG, "msg METEOR_CLIENT_STATE");
                    Log.d(LOG_TAG, "msg.what : " + msg.what);
                    Log.d(LOG_TAG, "msg.arg1 : " + msg.arg1);
                    Log.d(LOG_TAG, "msg.arg2 : " + msg.arg2);
                    if (msg.obj != null) {
                        Log.d(LOG_TAG, "msg.obj : " + String.valueOf(msg.obj.toString()));
                    }
                    if (msg.arg1 > 0) {
                        // should sign in automatically
                        Log.d(LOG_TAG, "should log in automatically");
                        Intent intentMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intentMainActivity);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

                    } else {
                        // should display login form
                        Log.d(LOG_TAG, "should NOT log in automatically");
                    }
                    /*if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }*/
                }
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }


    private void startAnimations() {
        Log.d(LOG_TAG, "StartAnimations");
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        LinearLayout l = (LinearLayout) findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        ImageView iv = (ImageView) findViewById(R.id.logo);
        iv.clearAnimation();
        iv.startAnimation(anim);


        anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        anim.setStartOffset(1000);

        _emailText.clearAnimation();
        _emailText.startAnimation(anim);

        _passwordText.clearAnimation();
        _passwordText.startAnimation(anim);

        _loginButton.clearAnimation();
        _loginButton.startAnimation(anim);

        _signupLink.clearAnimation();
        _signupLink.startAnimation(anim);
    }

    private void setListeners() {
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "_loginButton onClick");
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Log.d(LOG_TAG,"_signupLink onClick");
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        Log.d(LOG_TAG, "Login");
        /*if (!progressDialog.isShowing()) {
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Logging in...");
            progressDialog.show();
        }*/

        _loginButton.setEnabled(false);
        _loginButton.setBackgroundColor(Color.LTGRAY);
        mWebserverManager.requestLogin(_emailText, _passwordText);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult : " + requestCode);
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                Log.d(LOG_TAG, "onActivityResult RESULT_OK");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                this.finish();
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Log.d(LOG_TAG, "action : " + action);
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d(LOG_TAG, "ConnectivityManager.CONNECTIVITY_ACTION received");
                int status = NetworkUtil.getConnectivityStatusString(context);
                //Log.d(LOG_TAG, "status : " + status);
                if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    Log.d(LOG_TAG, "NETWORK_STATUS_NOT_CONNECTED");
                    Intent intentMainActiv = new Intent(getApplicationContext(), NoInternetActivity.class);
                    startActivity(intentMainActiv);
                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed LoginActivity");
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        if (mWebserverManager != null) {
            mWebserverManager.removeCallback();
        }
        super.onDestroy();
    }


}