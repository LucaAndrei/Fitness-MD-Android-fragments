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
import android.content.Intent;
import android.graphics.Color;
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

    private static Handler mActivityHandler = null;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(LOG_TAG, "onCreate");
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
                        _loginButton.setEnabled(false);
                        _loginButton.setBackgroundColor(Color.LTGRAY);

                        Log.d(LOG_TAG, "Login sucess");

                        Intent intentMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intentMainActivity);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    } else {
                        _loginButton.setEnabled(true);
                        _loginButton.setBackgroundColor(Color.parseColor("#52B3D9"));
                        Log.d(LOG_TAG, "Login error");

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
                Log.d(LOG_TAG, "_loginButton onClick");
                login();
            }
        });

        _loginButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(LOG_TAG, "_loginButton onLongClick");

                int rowsDeleted = mDB.deleteAAAUser();
                Toast.makeText(getBaseContext(), "On long click. Delete user from db : " + rowsDeleted, Toast.LENGTH_LONG).show();
                return rowsDeleted == 1 ? true : false;
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