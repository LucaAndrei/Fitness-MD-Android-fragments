/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.common.webserver;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.datatypes.StepsDayReport;
import com.master.aluca.fitnessmd.common.datatypes.WeightDayReport;
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.library.FitnessMDMeteor;
import com.master.aluca.fitnessmd.library.MeteorCallback;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryCollection;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryDocument;
import com.master.aluca.fitnessmd.library.listeners.ResultListener;
import com.master.aluca.fitnessmd.library.listeners.SubscribeListener;
import com.master.aluca.fitnessmd.library.listeners.UnsubscribeListener;
import com.master.aluca.fitnessmd.service.FitnessMDService;
import com.master.aluca.fitnessmd.ui.auth.AuthenticationLogic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebserverManager implements MeteorCallback{
    private static final String LOG_TAG = "Fitness_WebserverMgr";
    AuthenticationLogic mAuthLogicInstance;

    private Context mContext;

    private AtomicBoolean isMeteorClientConnected = new AtomicBoolean(false);

    private static WebserverManager sWebserverManager = null;
    private StepsDayReport bestSteps;
    //private Collection advices;

    private ArrayList<Handler> handlerList = new ArrayList<>();


    private UsersDB mDB;



    public static WebserverManager getInstance(Context context) {
        Log.d(LOG_TAG, "WebserverManager getInstance :" + String.valueOf(sWebserverManager));
        if (sWebserverManager == null) {
            sWebserverManager = new WebserverManager(context);
        }
        return sWebserverManager;
    }

    private WebserverManager(Context context) {
        Log.d(LOG_TAG, "private WebserverManager");
        mAuthLogicInstance = AuthenticationLogic.getInstance();
        mContext = context;
        mDB = UsersDB.getInstance(context);

        // create a new instance
        //FitnessMDMeteor.createInstance(context, "ws://192.168.1.4:3000/websocket");
        FitnessMDMeteor.createInstance(context, "ws://128.224.108.202:3000/websocket");
        // register the callback that will handle events and receive messages

        FitnessMDMeteor.getInstance().addCallback(this);

        // establish the connection
        Log.d(LOG_TAG, "FitnessMDMeteor.getInstance().isConnected() : " + FitnessMDMeteor.getInstance().isConnected());
        if (!FitnessMDMeteor.getInstance().isConnected()) {
            Log.d(LOG_TAG, "FitnessMDMeteor connect");
            FitnessMDMeteor.getInstance().connect();
        }
    }

    public boolean isLoggedIn() {
        return FitnessMDMeteor.getInstance().isLoggedIn();
    }

    public void requestLogin(final EditText emailText, final EditText passwordText) {
        Log.d(LOG_TAG, "requestLogin");
        if (!mAuthLogicInstance.isInputValid(null, emailText, passwordText)) {
            Log.d(LOG_TAG, "Login failed");
            dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, false, "Input is not valid");
        } else {
            boolean isEmailRegistered = mDB.isEmailRegistered(emailText.getText().toString());
            if (isEmailRegistered) {
                boolean isPasswordCorrect = mDB.isPasswordCorrect(emailText.getText().toString(), passwordText.getText().toString());
                if (isPasswordCorrect) {
                    if (FitnessMDService.hasInternet()) {
                        Log.d(LOG_TAG, "try to connect to web server");
                        if (FitnessMDMeteor.getInstance().isConnected()) {
                            Log.d(LOG_TAG, "FitnessMDMeteor.getInstance().isConnected()");
                            if (!FitnessMDMeteor.getInstance().isLoggedIn()) {
                                Log.d(LOG_TAG, "FitnessMDMeteor.getInstance().loginWithEmail()");
                                FitnessMDMeteor.getInstance().loginWithEmail(emailText.getText().toString(), passwordText.getText().toString(),
                                        new ResultListener() {
                                            @Override
                                            public void onSuccess(String s) {
                                                Log.d(LOG_TAG, "Meteor Login SUCCESS");
                                                mDB.setUserConnected(emailText.getText().toString(), true);
                                                dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, true, "Login successfully + server success");
                                            }

                                            @Override
                                            public void onError(String error, String reason, String details) {
                                                Log.d(LOG_TAG, "Meteor Login ERROR");
                                                dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, false, reason);
                                            }
                                        });
                            } else {
                                mDB.setUserConnected(emailText.getText().toString(), true);
                                dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, true, "Log in successful, already logged in on server");
                            }
                        } else {
                            Log.d(LOG_TAG, "FitnessMDMeteor.getInstance().isConnected() false");
                            mDB.setUserConnected(emailText.getText().toString(), true);
                            dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, true, "Log in successful, problem with server");
                        }
                    } else {
                            mDB.setUserConnected(emailText.getText().toString(), true);
                            dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, true, "Login successfully, no internet, no login to server");
                    }
                } else {
                    dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, false, "Password not correct");
                }
            } else {
                dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, false, "Email not registered");
            }
        }
    }

    public void requestSignup(final EditText nameText, final EditText emailText, final EditText passwordText) {
        Log.d(LOG_TAG, "requestSignup");
        if (!mAuthLogicInstance.isInputValid(nameText, emailText, passwordText)) {
            Log.d(LOG_TAG, "input not valid");
            dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, false, "Input is not valid");
        } else {
            boolean addSuccess = mDB.addUser(
                    emailText.getText().toString(),
                    passwordText.getText().toString(),
                    nameText.getText().toString());

            if (addSuccess) {
                Log.d(LOG_TAG,"addSuccess : " + addSuccess + " >>> hasInternet : " + FitnessMDService.hasInternet());
                if (FitnessMDService.hasInternet()) {
                    Log.d(LOG_TAG,"try to connect to web server");
                    if (FitnessMDMeteor.getInstance().isConnected()) {
                        Log.d(LOG_TAG, "Meteor client connected");
                        FitnessMDMeteor.getInstance().registerAndLogin(nameText.getText().toString(),
                                emailText.getText().toString(), passwordText.getText().toString(), new ResultListener() {
                                    @Override
                                    public void onSuccess(String s) {
                                        Log.d(LOG_TAG, "Meteor Login after Signup SUCCESS");
                                        mDB.setUserConnected(emailText.getText().toString(), true);
                                        dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, true, "Signup successfully");
                                    }

                                    @Override
                                    public void onError(String error, String reason, String details) {
                                        Log.d(LOG_TAG, "Meteor Login after signup ERROR");
                                        Log.d(LOG_TAG, "error : " + error + " >>> reason : " + reason + " >>> details : " + details);

                                        dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, false, reason);
                                    }
                                });
                    } else {
                        mDB.setUserConnected(emailText.getText().toString(), true);
                        dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, true, "Signup successfully, problem with server");
                    }
                } else {
                    mDB.setUserConnected(emailText.getText().toString(), true);
                    dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, true, "Signup successfully, no internet, not registered on server");
                }
            } else {
                dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, false, "Could not add user");
            }
        }
    }


    public boolean sendPedometerData(long day, int steps, long timeActive) {
        int Hours = (int) (timeActive / (1000 * 60 * 60));
        int Mins = (int) (timeActive / (1000 * 60)) % 60;
        String diffTimeActive = Hours + ":" + Mins;
        Log.d(LOG_TAG, "sendPedometerData day: " + (new Date(day)) + " >>> steps : " + steps + " >>> timeActive : " + diffTimeActive);
        boolean oRet = false;
        if (day < 1 || steps < 0)
            return oRet;

       /* params = new ArrayList<>();
        params.add(new BasicNameValuePair("day", ""+day));
        params.add(new BasicNameValuePair("steps", "" + steps));
        params.add(new BasicNameValuePair("timeActive", "" + timeActive));
        JSONObject json = serverRequest.putPedometerData("http://" + Constants.LOCALHOST_IP_ADDRESS
                        + ":"
                        + Constants.LOCALHOST_NODEJS_PORT
                        + Constants.NODEJS_PUT_PEDOMETER_ROUTE,
                        params);
        if(json != null){
            try{
                Log.d(LOG_TAG, "try get string response");
                Log.d(LOG_TAG, "json : " + json.toString());
                String jsonstr = json.getString("message");
                Log.d(LOG_TAG, "jsonstr : " + jsonstr);
                if(json.getBoolean("res")){
                    oRet = true;
                    Log.d(LOG_TAG, "json.getBoolean");
                } else {
                    int resultCode = json.getInt("message");
                    if (resultCode == 400) {
                        Log.d(LOG_TAG, "resultCode 400");
                    } else if (resultCode == 401) {
                        Log.d(LOG_TAG, "resultCode 401");
                    } else if (resultCode == 404) {
                        Log.d(LOG_TAG, "resultCode 404");
                    } else {
                        Log.d(LOG_TAG, "other result code");
                    }
                    Log.d(LOG_TAG, "json get boolean else");
                }

            }catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(LOG_TAG, "json is null");
        }*/
        oRet = true;
        Log.d(LOG_TAG, "put pedometer data success");
        Date date = new Date(day);
        Log.d(LOG_TAG, "date : " + date);
        return oRet;
    }

    public void requestLogout() {
        Log.d(LOG_TAG, "requestLogout");
        if (mDB.getIsUserLoggedIn()) {
            if (mDB.setUserConnected(mDB.getConnectedUser().getEmail(), false)) {
                if (FitnessMDService.hasInternet()) {
                    Log.d(LOG_TAG,"try to connect to web server");
                    if (FitnessMDMeteor.getInstance().isConnected()) {
                        Log.d(LOG_TAG, "Meteor client connected");
                        if (FitnessMDMeteor.getInstance().isLoggedIn()) {
                            Log.d(LOG_TAG, "Meteor user already logged in. Logging out ...");
                            FitnessMDMeteor.getInstance().logout(new ResultListener() {
                                @Override
                                public void onSuccess(String result) {
                                    Log.d(LOG_TAG, "requestLogout onSuccess");
                                    Intent intent = new Intent(Constants.LOGOUT_INTENT);
                                    mContext.sendBroadcast(intent);
                                }

                                @Override
                                public void onError(String error, String reason, String details) {
                                    Log.d(LOG_TAG, "requestLogout onError");
                                    Toast.makeText(mContext, reason, Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            Intent intent = new Intent(Constants.LOGOUT_INTENT);
                            mContext.sendBroadcast(intent);
                            Log.d(LOG_TAG, "Meteor user NOT logged in");
                        }
                    } else {
                        Intent intent = new Intent(Constants.LOGOUT_INTENT);
                        mContext.sendBroadcast(intent);
                        Log.d(LOG_TAG, "Meteor not connected. problem with server");
                    }
                } else {
                    Intent intent = new Intent(Constants.LOGOUT_INTENT);
                    mContext.sendBroadcast(intent);
                    Log.d(LOG_TAG, "no internet. cannot logout from server. only logged out from DB");
                }
            }
        } else {
            Log.d(LOG_TAG, "user not even logged in. how did this call got here?");
        }
    }

    @Override
    public void onConnect(boolean shouldSignIn) {
        Log.d(LOG_TAG, "Meteor DDP onConnect");
        isMeteorClientConnected.set(true);
        dispatchMessageToHandlers(Constants.METEOR_CLIENT_STATE, shouldSignIn);
    }
    private void dispatchMessageToHandlers(int what, boolean b) {
        dispatchMessageToHandlers(what, b ? 1 : 0, 0, null);
    }

    private void dispatchMessageToHandlers(int what, boolean b, String reason) {
        dispatchMessageToHandlers(what, b ? 1 : 0, 0, reason);
    }

    private void dispatchMessageToHandlers(int what, int arg1, int arg2, Object obj) {

        Log.d(LOG_TAG, "dispatchMessageToHandlers handlerList.size : " + handlerList.size());
        for(Handler handler : handlerList) {
            handler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
        }
    }

    @Override
    public void onDisconnect() {
        Log.d(LOG_TAG, "Meteor DDP onDisconnect");
        dispatchMessageToHandlers(Constants.METEOR_CLIENT_STATE, false);
    }

    @Override
    public void onException(Exception e) {
        Log.d(LOG_TAG, "Meteor DDP onException : ");
        e.printStackTrace();

    }

    @Override
    public void onDataAdded(String collectionName, String documentID, String fieldsJson) {
        Log.d(LOG_TAG, "Meteor DDP onDataAdded "
                + "Data added to <" + collectionName + "> in document <" + documentID + ">\n"
                + "    Added: " + fieldsJson);
        String[] userDocIds = FitnessMDMeteor.getInstance().getDatabase().getCollection("users").getDocumentIds();

        for (int i = 0; i < userDocIds.length; i++) {
            InMemoryDocument userdoc = FitnessMDMeteor.getInstance().getDatabase().getCollection("users").getDocument(userDocIds[i]);
            String[] fieldNames = userdoc.getFieldNames();
            for (int j = 0; j < fieldNames.length; j++) {
                Log.d(LOG_TAG, "" + fieldNames[j] + " : " + userdoc.getField(fieldNames[j]));
            }
        }

    }

    @Override
    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        Log.d(LOG_TAG, "Meteor DDP onDataAdded onDataChanged Data changed in <"
                + collectionName + "> in document <" + documentID + "> \n"
                + "    Updated: " + updatedValuesJson
                + "\n    Removed: " + removedValuesJson);
    }

    @Override
    public void onDataRemoved(String collectionName, String documentID) {
        Log.d(LOG_TAG, "Meteor DDP onDataRemoved Data removed from <" + collectionName + "> in document <" + documentID + ">");

    }

    public void destroyMeteor() {
        Log.d(LOG_TAG, "destroyMeteor");
        FitnessMDMeteor.getInstance().unsubscribe("messages", new UnsubscribeListener() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "unsubscribe onSuccess");
            }
        });
        FitnessMDMeteor.destroyInstance();
        sWebserverManager = null;
    }

    public void subscribeToAdvices() {
        Map<String, Object> insertMessage = new HashMap<String, Object>();
        insertMessage.put("interlocutorId", "7cBFoCPdbfmpXJErZ");

        FitnessMDMeteor.getInstance().subscribe("advices", new Object[]{insertMessage}, new SubscribeListener() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "onSuccess subscriptionId");
                String[] collectionNames = FitnessMDMeteor.getInstance().getDatabase().getCollectionNames();
                Log.d(LOG_TAG, "collectionNames.length : " + collectionNames.length);
                for (int i = 0; i < collectionNames.length; i++) {
                    Log.d(LOG_TAG, "collectionNames[" + i + "] : " + collectionNames[i]);
                }

                Intent intent = new Intent(Constants.ADVICES_SUBSCRIPTION_READY_INTENT);
                intent.putExtra(Constants.ADVICES_SUBSCRIPTION_READY_BUNDLE_KEY, true);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            }

            @Override
            public void onError(String error, String reason, String details) {
                Log.d(LOG_TAG, "onError subscriptionId : " + error + " >>> " + reason + " >>> " + details);
            }
        });
    }

    public InMemoryCollection getAdvices() {
        String[] collectionNames = FitnessMDMeteor.getInstance().getDatabase().getCollectionNames();
        for (int i = 0; i < collectionNames.length; i++) {
            if (collectionNames[i].equalsIgnoreCase("advices")) {
                return FitnessMDMeteor.getInstance().getDatabase().getCollection(collectionNames[i]);
            }
        }
        return null;
    }

    public StepsDayReport getBestSteps() {
        bestSteps = new StepsDayReport(1234, 1485461807,3600);
        return bestSteps;
    }

    public WeightDayReport getBestWeight() {
        WeightDayReport bestWeight = new WeightDayReport();
        return bestWeight;
    }

    public StepsDayReport getAverageSteps() {
        StepsDayReport averageSteps = new StepsDayReport(345, 0, 0);
        return averageSteps;
    }

    public WeightDayReport getAverageWeight() {
        WeightDayReport averageWeight = new WeightDayReport();
        return averageWeight;
    }

    public void registerCallback(Handler mActivityHandler) {
        handlerList.add(mActivityHandler);
    }
    public void removeCallback() {
        handlerList.remove(0);
    }

    public void sendStepsToServer(long startOfCurrentDay, int totalSteps, int hourIndex) {
        if (FitnessMDMeteor.getInstance().isConnected()) {
            if (!FitnessMDMeteor.getInstance().isLoggedIn()) {
                FitnessMDMeteor.getInstance().loginWithEmail(mDB.getConnectedUser().getEmail(),
                        mDB.getConnectedUser().getPassword(),
                        new ResultListener() {
                            @Override
                            public void onSuccess(String s) {
                                Log.d(LOG_TAG, "Meteor Login SUCCESS -- send steps to server");

                            }

                            @Override
                            public void onError(String error, String reason, String details) {
                                Log.d(LOG_TAG, "Meteor Login ERROR -- send steps to server");
                            }
                        });
            } else {
                dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, true, "Log in successful");
                FitnessMDMeteor.getInstance().sendStepsToServer(startOfCurrentDay, totalSteps, hourIndex,
                        new ResultListener() {
                            @Override
                            public void onSuccess(String s) {
                                Log.d(LOG_TAG, "Send steps to server -- SUCCESS");

                            }

                            @Override
                            public void onError(String error, String reason, String details) {
                                Log.d(LOG_TAG, "Send steps to server -- ERROR");
                            }
                        });
            }
        } else {
            Log.d(LOG_TAG, "Meteor not connected ERROR -- send steps to server");
        }
    }
}
