/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.ui.auth;

import android.util.Log;
import android.widget.EditText;

public class AuthenticationLogic {
    private static final String LOG_TAG="Fitness_AuthLogic";
    private static AuthenticationLogic mInstance;


    public static synchronized AuthenticationLogic getInstance() {
        if(mInstance == null) {
            mInstance = new AuthenticationLogic();
        }
        return mInstance;
    }

    private AuthenticationLogic() {

    }


        /*
            In order to connect to a localhost you need to do the following :
                1. If OS == Windows
                    Open cmd
                    Type 'ipconfig'
                    Find out your IPv4 address --- Should be something like : 192.168.1.4
                    Connect with the server request to this address
                2. If OS == Ubuntu
                    Open terminal
                    Type 'ifconfig'
                    .... same
         */



    public boolean isInputValid(EditText name, EditText email, EditText password) {

        boolean isInputValid = true;
        String emailInputText = email.getText().toString();
        String passwordInputText = password.getText().toString();
        Log.d(LOG_TAG, "isInputValid emailInputText : " + emailInputText + " >>> passwordInputText : " + passwordInputText);

        if(name == null) {
            Log.d(LOG_TAG,"isInputValid LOGIN");
        } else {
            String nameInputText = name.getText().toString();
            if (nameInputText.isEmpty()) {
                name.setError("Enter a valid name");
                Log.d(LOG_TAG,"name error");
                isInputValid = false;
            } else {
                name.setError(null);
            }
            Log.d(LOG_TAG, "isInputValid nameInputText : " + nameInputText);
        }

        if (emailInputText.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailInputText).matches()) {
            email.setError("enter a valid email address");
            Log.d(LOG_TAG,"email error");
            isInputValid = false;
        } else {
            email.setError(null);
        }

        if (passwordInputText.isEmpty() || passwordInputText.length() < 4 || passwordInputText.length() > 10) {
            password.setError("between 4 and 10 alphanumeric characters");
            Log.d(LOG_TAG, "password error");
            isInputValid = false;
        } else {
            password.setError(null);
        }
        Log.d(LOG_TAG,"isInputValid : " + isInputValid);
        return isInputValid;
    }
}
