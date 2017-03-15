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
import android.content.Intent;
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
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;
import com.master.aluca.fitnessmd.ui.MainActivity;


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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(LOG_TAG, "onCreate");
        _emailText = (EditText) findViewById(R.id.input_email_login);
        _passwordText = (EditText) findViewById(R.id.input_password_login);
        _loginButton = (Button) findViewById(R.id.btn_login);
        _signupLink = (TextView) findViewById(R.id.link_signup);

        progressDialog = new ProgressDialog(LoginActivity.this);
        mDB = UsersDB.getInstance(getApplicationContext());


        boolean isLoggedIn = mDB.getIsUserLoggedIn();
        Log.d(LOG_TAG, "mDB isLoggedIn : " + isLoggedIn);

        if (isLoggedIn) {
            Intent intentMainActiv = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intentMainActiv);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else {
            setListeners();
            if (!animPlayed) {
                startAnimations();
                animPlayed = true;
            }
            mWebserverManager =  WebserverManager.getInstance(this);
            mActivityHandler = new ActivityHandler();
            mWebserverManager.registerCallback(mActivityHandler);
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
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        _loginButton.setEnabled(false);

                        Log.d(LOG_TAG, "Login sucess");

                        Intent intentMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intentMainActivity);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    } else {
                        _loginButton.setEnabled(true);
                        Log.d(LOG_TAG, "Login error");
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
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
                Log.d(LOG_TAG,"_loginButton onClick");
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
        if (!progressDialog.isShowing()) {
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Connecting...");
            progressDialog.show();
        }

        _loginButton.setEnabled(false);
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