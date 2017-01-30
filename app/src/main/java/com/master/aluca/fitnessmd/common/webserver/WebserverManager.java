package com.master.aluca.fitnessmd.common.webserver;

import android.app.Activity;
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
import com.master.aluca.fitnessmd.common.util.SharedPreferencesManager;
import com.master.aluca.fitnessmd.library.FitnessMDMeteor;
import com.master.aluca.fitnessmd.library.MeteorCallback;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryCollection;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryDocument;
import com.master.aluca.fitnessmd.library.listeners.ResultListener;
import com.master.aluca.fitnessmd.library.listeners.SubscribeListener;
import com.master.aluca.fitnessmd.library.listeners.UnsubscribeListener;
import com.master.aluca.fitnessmd.ui.auth.AuthenticationLogic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
/*
import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.FitnessMDMeteor;
import im.delight.android.ddp.ResultListener;
import im.delight.android.ddp.SubscribeListener;
import im.delight.android.ddp.db.Collection;
import im.delight.android.ddp.db.memory.InMemoryDatabase;
*/
/**
 * Created by aluca on 11/15/16.
 */

public class WebserverManager implements MeteorCallback{
    private static final String LOG_TAG = "Fitness_MeteorWebMgr";
    AuthenticationLogic mAuthLogicInstance;

    private Context mContext;

    private SharedPreferencesManager sharedPreferencesManager;

    private AtomicBoolean isMeteorClientConnected = new AtomicBoolean(false);

    private static WebserverManager sWebserverManager = null;
    private StepsDayReport bestSteps;
    //private Collection advices;

    private ArrayList<Handler> handlerList = new ArrayList<>();



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
        sharedPreferencesManager = SharedPreferencesManager.getInstance(context);
        mContext = context;

        // create a new instance
        FitnessMDMeteor.createInstance(context, "ws://192.168.1.4:3000/websocket");
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

    public void requestLogin(final EditText _emailText, final EditText _passwordText) {
        Log.d(LOG_TAG, "requestLogin");
        if (!mAuthLogicInstance.isInputValid(null, _emailText, _passwordText)) {
            Log.d(LOG_TAG, "Login failed");
            dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, false, "Input is not valid");
        } else {
            String emailFromSharedPrefs = sharedPreferencesManager.getEmail(_emailText.getText().toString());
            String passwordFromSharedPrefs = sharedPreferencesManager.getPassword(_emailText.getText().toString());
            if (emailFromSharedPrefs != null && _emailText.getText().toString().equalsIgnoreCase(emailFromSharedPrefs)) {
                // emails OK
                if (passwordFromSharedPrefs != null && _passwordText.getText().toString().equalsIgnoreCase(passwordFromSharedPrefs)) {
                    // password OK
                    sharedPreferencesManager.setLoggedIn(true);
                    sharedPreferencesManager.setUserName(_emailText.getText().toString(), sharedPreferencesManager.getUserName());
                    sharedPreferencesManager.setEmail(_emailText.getText().toString());
                    dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, true, "Login successfully");

                    // try to login on server

                    if (FitnessMDMeteor.getInstance().isConnected()) { 
                        if (!FitnessMDMeteor.getInstance().isLoggedIn()) {
                            FitnessMDMeteor.getInstance().loginWithEmail(emailFromSharedPrefs, passwordFromSharedPrefs,
                                new ResultListener() {
                                    @Override
                                    public void onSuccess(String s) {
                                        Log.d(LOG_TAG, "Meteor Login SUCCESS");
                                    }

                                    @Override
                                    public void onError(String error, String reason, String details) {
                                        Log.d(LOG_TAG, "Meteor Login ERROR");
                                    }
                                });
                        }
                    }
                } else {
                    dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, false, "Password does not match");
                }
            }  else {
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
            if (sharedPreferencesManager.getEmail(emailText.getText().toString()) != null) {
                // email already registered
                dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, false);
                return;
            } else {
                // try to login to server

                if (FitnessMDMeteor.getInstance().isConnected()) {
                    Log.d(LOG_TAG, "Meteor client connected");
                    FitnessMDMeteor.getInstance().registerAndLogin(nameText.getText().toString(),
                        emailText.getText().toString(), passwordText.getText().toString(), new ResultListener() {
                            @Override
                            public void onSuccess(String s) {
                                Log.d(LOG_TAG, "Meteor Login after Signup SUCCESS");
                                sharedPreferencesManager.addEmail(emailText.getText().toString());
                                sharedPreferencesManager.addPassword(emailText.getText().toString(), passwordText.getText().toString());
                                sharedPreferencesManager.setUserName(emailText.getText().toString(), nameText.getText().toString());
                                sharedPreferencesManager.setEmail(emailText.getText().toString());

                                sharedPreferencesManager.setLoggedIn(true);
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
                    dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, 
                        false, "Server could not be contacted. Do you have internet connection for account validation?");
                }
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


    /*
        This method will provide data from the webserver.
        It wil create a WeightDayReport object
            weight - the received weight from the webserver
            day - the day the weight measurement was taken
      */
    public WeightDayReport getWeightFromServer() {
        // TODO - for testing purposes I set oRet to default weight value
        // in production, initialize with -1 as an error code;
        WeightDayReport weightDayReport = new WeightDayReport();
        boolean sharedPrefsIsLoggedIn = sharedPreferencesManager.getIsUserLoggedIn();
        boolean meteorIsLoggedIn = FitnessMDMeteor.getInstance().isLoggedIn();
        Log.d(LOG_TAG, "getWeightFromServer sharedPrefsIsLoggedIn : " + sharedPrefsIsLoggedIn + " >> meteorIsLoggedIn : " + meteorIsLoggedIn);
        if (meteorIsLoggedIn && sharedPrefsIsLoggedIn) {
            String connectedUserEmail = sharedPreferencesManager.getEmail();
            Log.d(LOG_TAG, "getWeightFromServer email : " + connectedUserEmail);
        } else {
            Log.d(LOG_TAG, "getWeightFromServer not logged in sharedPrefsIsLoggedIn : " + sharedPrefsIsLoggedIn + " >> meteorIsLoggedIn : " + meteorIsLoggedIn);
        }


        /*params = new ArrayList<>();
        params.add(new BasicNameValuePair("email", connectedUserEmail));
        JSONObject json = serverRequest.requestWeight("http://" + Constants.LOCALHOST_IP_ADDRESS
                        + ":"
                        + Constants.LOCALHOST_NODEJS_PORT
                        + Constants.NODEJS_GET_WEIGHT_ROUTE,
                        params);
        if(json != null){
            try{
                Log.d(LOG_TAG, "try get string response");
                Log.d(LOG_TAG, "json : " + json.toString());
                String jsonstr = json.getString("message");
                Log.d(LOG_TAG, "jsonstr : " + jsonstr);
                if(json.getBoolean("res")){
                    oRet = Constants.WEIGHT_DEFAULT_VALUE;
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
        }
        oRet = Constants.WEIGHT_DEFAULT_VALUE;
        Log.d(LOG_TAG, "getWeightFromServer success");*/
        return weightDayReport;
    }


    public void requestLogout() {
        Log.d(LOG_TAG, "requestLogout");
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
                Log.d(LOG_TAG, "Meteor user NOT logged in");
            }
        } else {
            Log.d(LOG_TAG, "Meteor NOT connected");
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
}
