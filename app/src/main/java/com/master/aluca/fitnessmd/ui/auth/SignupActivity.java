package com.master.aluca.fitnessmd.ui.auth;

/**
 * Created by andrei on 10/29/2016.
 */
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.util.SharedPreferencesManager;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;
import com.master.aluca.fitnessmd.ui.MainActivity;

import butterknife.ButterKnife;
import butterknife.Bind;

public class SignupActivity extends Activity {
    private static final String LOG_TAG = "Fitness_Signup";

    @Bind(R.id.input_name) EditText _nameText;
    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_signup) Button _signupButton;
    @Bind(R.id.link_login) TextView _loginLink;

    private WebserverManager mWebserverManager;
    private SharedPreferencesManager sharedPreferencesManager;

    ProgressDialog progressDialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Log.d(LOG_TAG, "onCreate");
        ButterKnife.bind(this);

        sharedPreferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());
        mWebserverManager = WebserverManager.getInstance(this);

        progressDialog = new ProgressDialog(SignupActivity.this);


        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.SIGNUP_RESULT_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

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
                Log.d(LOG_TAG,"_loginLink onClick");
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });


    }

    public void signup() {
        Log.d(LOG_TAG, "Signup");
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();
        _signupButton.setEnabled(false);
        mWebserverManager.requestSignup(_nameText, _emailText, _passwordText);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LOG_TAG, "action : " + action.toString());
            if (action.equals(Constants.SIGNUP_RESULT_INTENT)) {
                if(intent.getBooleanExtra(Constants.SIGNUP_RESULT_BUNDLE_KEY, false)) {
                    _signupButton.setEnabled(false);

                    Log.d(LOG_TAG, "Login after signup sucess");
                    // TODO - do I still need this if I use meteor?
                    // probably yes, because meteor works only with internet connection
                    // i should use the value from sharedPrefs only to open the application
                    // and the value from meteor to make requests to the webserver
                    //sharedPreferencesManager.setLoggedIn(true);
                    String email = _emailText.getText().toString();
                    sharedPreferencesManager.setUserName(email, _nameText.getText().toString());
                    sharedPreferencesManager.setEmail(email);
                    Intent intentMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intentMainActivity);
                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    progressDialog.dismiss();
                } else {
                    String reason = intent.getStringExtra(Constants.SIGNUP_RESULT_EXTRA_BUNDLE_KEY);
                    if( reason!= null) {
                        Toast.makeText(getBaseContext(), reason, Toast.LENGTH_LONG).show();
                    }
                    _signupButton.setEnabled(true);
                    Log.d(LOG_TAG, "Login after signup error");
                    progressDialog.dismiss();
                }
            }
        }

    };

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

}