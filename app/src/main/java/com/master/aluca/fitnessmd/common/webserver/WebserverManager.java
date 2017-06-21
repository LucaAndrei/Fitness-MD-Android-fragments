/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.common.webserver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.datatypes.AdviceDetails;
import com.master.aluca.fitnessmd.common.datatypes.ChallengeDetails;
import com.master.aluca.fitnessmd.common.datatypes.StepsDayReport;
import com.master.aluca.fitnessmd.common.datatypes.User;
import com.master.aluca.fitnessmd.common.datatypes.WeightDayReport;
import com.master.aluca.fitnessmd.common.util.IStatsChanged;
import com.master.aluca.fitnessmd.common.util.NetworkUtil;
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.library.FitnessMDMeteor;
import com.master.aluca.fitnessmd.library.MeteorCallback;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryCollection;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryDocument;
import com.master.aluca.fitnessmd.library.listeners.ResultListener;
import com.master.aluca.fitnessmd.library.listeners.SubscribeListener;
import com.master.aluca.fitnessmd.library.listeners.UnsubscribeListener;
import com.master.aluca.fitnessmd.ui.AdvicesActivity;
import com.master.aluca.fitnessmd.ui.auth.AuthenticationLogic;
import com.master.aluca.fitnessmd.ui.challenges.ChallengesActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private ArrayList<IStatsChanged> statsCallbackList = new ArrayList<>();


    private UsersDB mDB;
    private static String userLoggingInEmail;
    private boolean successfullySubscribedToChallenges = false;
    private boolean successfullySubscribedToAdvices = false;


    LinkedHashMap<Long, Integer> savedStats = new LinkedHashMap<>();


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
        //FitnessMDMeteor.createInstance(context, "wss://fitnessmd.now.sh/websocket");
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
        if (NetworkUtil.isConnectedToInternet(mContext)) {
            if (!mAuthLogicInstance.isInputValid(null, emailText, passwordText)) {
                Log.d(LOG_TAG, "Login failed");
                dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, false, "Input is not valid");
            } else {
                final String email = emailText.getText().toString();
                String password = passwordText.getText().toString();

                boolean isEmailRegistered = mDB.isEmailRegistered(email);
                if (isEmailRegistered) {
                    String hashedPassword = Constants.getHashedPassword(password);
                    boolean isPasswordCorrect = mDB.isPasswordCorrect(email, hashedPassword);
                    if (isPasswordCorrect) {

                        Log.d(LOG_TAG, "try to connect to web server");
                        if (FitnessMDMeteor.getInstance().isConnected()) {
                            Log.d(LOG_TAG, "FitnessMDMeteor.getInstance().isConnected()");
                            if (!FitnessMDMeteor.getInstance().isLoggedIn()) {
                                doLogin(email, password);
                            } else {
                                mDB.setUserConnected(email, true);
                                dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, true, "Log in successful, already logged in on server");
                            }
                        } else {
                            Log.d(LOG_TAG, "FitnessMDMeteor.getInstance().isConnected() false");
                            dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, false, "Server is not up and running");
                            //mDB.setUserConnected(email, true);
                            //dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, true, "Log in successful, problem with server");
                        }

                    } else {
                        dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, false, "Password not correct");
                    }
                } else {
                    dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, false, "Email not registered");
                }
            }
        } else {
            Log.d(LOG_TAG, "Login successfully, no internet, no login to server");
            dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, 0, Constants.NO_INTERNET_CONNECTION, "No Internet connection");
            //mDB.setUserConnected(email, true);
            //dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, true, "Login successfully, no internet, no login to server");
        }
    }

    public void requestSignup(final EditText nameText, final EditText emailText, final EditText passwordText) {
        Log.d(LOG_TAG, "requestSignup");
        if (NetworkUtil.isConnectedToInternet(mContext)) {
            if (!mAuthLogicInstance.isInputValid(nameText, emailText, passwordText)) {
                Log.d(LOG_TAG, "input not valid");
                dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, false, "Input is not valid");
            } else {
                if (FitnessMDMeteor.getInstance().isConnected()) {
                    String name = nameText.getText().toString();
                    String email = emailText.getText().toString();
                    String hashedPassword = Constants.getHashedPassword(passwordText.getText().toString());

                    boolean isEmailRegistered = mDB.isEmailRegistered(email);
                    if (!isEmailRegistered) {
                        boolean addSuccess = mDB.addUser(
                                name,
                                email,
                                hashedPassword);
                        if (addSuccess) {
                            Log.d(LOG_TAG, "addSuccess : " + addSuccess);
                            // connect only if the client has internet connection and the webserver is up and running
                            Log.d(LOG_TAG, "try to connect to web server");

                            Log.d(LOG_TAG, "Meteor client connected");
                            doSignup(name, email, hashedPassword);
                        } else {
                            dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, false, "Could not add user");
                        }
                    } else {
                        dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, false, "Email already registered");
                    }
                } else {
                    dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, false, "Server is not up and running");
                    //mDB.setUserConnected(emailText.getText().toString(), true);
                    //dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, true, "Signup successfully, problem with server");
                }
            }
        } else {
            dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, 0, Constants.NO_INTERNET_CONNECTION, "No Internet connection");
            //mDB.setUserConnected(emailText.getText().toString(), true);
            //dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, true, "Signup successfully, no internet, not registered on server");
        }
    }

    private void doLogin(final String email, String normalPassword) {
        Log.d(LOG_TAG, "doLogin : " + normalPassword);
        userLoggingInEmail = email;
        FitnessMDMeteor.getInstance().loginWithEmail(email, normalPassword,
                new ResultListener() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(LOG_TAG, "Meteor Login SUCCESS : " + s);
                        mDB.setUserConnected(email, true);
                        dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, true, "Login successfully + server success");
                    }

                    @Override
                    public void onError(String error, String reason, String details) {
                        Log.d(LOG_TAG, "Meteor Login ERROR");
                        dispatchMessageToHandlers(Constants.LOGIN_RESULT_INTENT, false, reason);
                    }
                });
    }

    private void doSignup(String name, final String email, String hashedPassword) {
        Log.d(LOG_TAG, "doSignup");
        userLoggingInEmail = email;
        FitnessMDMeteor.getInstance().registerAndLogin(name,
                email, hashedPassword, new ResultListener() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(LOG_TAG, "Meteor Login after Signup SUCCESS");
                        mDB.setUserConnected(email, true);
                        dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, true, "Signup successfully");
                    }

                    @Override
                    public void onError(String error, String reason, String details) {
                        Log.d(LOG_TAG, "Meteor Login after signup ERROR");
                        Log.d(LOG_TAG, "error : " + error + " >>> reason : " + reason + " >>> details : " + details);
                        dispatchMessageToHandlers(Constants.SIGNUP_RESULT_INTENT, false, reason);
                    }
                });
    }

    public void requestLogout() {
        Log.d(LOG_TAG, "requestLogout");
        final User connectedUser = mDB.getConnectedUser();
        if (connectedUser != null) {
            if (NetworkUtil.isConnectedToInternet(mContext)) {
                Log.d(LOG_TAG,"try to connect to web server");
                if (FitnessMDMeteor.getInstance().isConnected()) {
                    Log.d(LOG_TAG, "Meteor client connected");
                    if (FitnessMDMeteor.getInstance().isLoggedIn()) {
                        Log.d(LOG_TAG, "Meteor user already logged in. Logging out ...");
                        FitnessMDMeteor.getInstance().logout(new ResultListener() {
                            @Override
                            public void onSuccess(String result) {
                                Log.d(LOG_TAG, "requestLogout onSuccess");
                                if (mDB.setUserConnected(connectedUser.getEmail(), false)) {
                                    Intent intentFinishActivity = new Intent(Constants.FINISH_ACTIVITY_INTENT);
                                    intentFinishActivity.putExtra(Constants.FINISH_ACTIVITY_BUNDLE_KEY, false);
                                    mContext.sendBroadcast(intentFinishActivity);
                                }
                            }

                            @Override
                            public void onError(String error, String reason, String details) {
                                Log.d(LOG_TAG, "requestLogout onError");
                                Toast.makeText(mContext, reason, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Log.d(LOG_TAG, "Meteor user NOT logged in");
                        // somehow the user is still connected in the app although it's not connected to the server
                        // force finish the activity and disconnect user
                        // ??????????
                        if (mDB.setUserConnected(connectedUser.getEmail(), false)) {
                            Intent intentFinishActivity = new Intent(Constants.FINISH_ACTIVITY_INTENT);
                            intentFinishActivity.putExtra(Constants.FINISH_ACTIVITY_BUNDLE_KEY, false);
                            mContext.sendBroadcast(intentFinishActivity);
                        }
                    }
                } else {
                    Log.d(LOG_TAG, "Meteor not connected. problem with server");
                    // the server is down
                    // force finish the activity and disconnect user
                    // ??????????
                    if (mDB.setUserConnected(connectedUser.getEmail(), false)) {
                        Intent intentFinishActivity = new Intent(Constants.FINISH_ACTIVITY_INTENT);
                        intentFinishActivity.putExtra(Constants.FINISH_ACTIVITY_BUNDLE_KEY, false);
                        mContext.sendBroadcast(intentFinishActivity);
                    }
                }
            } else {
                Log.d(LOG_TAG, "no internet. cannot logout.");
                // no internet but still the user can click the logout button ?!?!?!?
                // force finish the activity and disconnect user
                // ??????????
                if (mDB.setUserConnected(connectedUser.getEmail(), false)) {
                    Intent intentFinishActivity = new Intent(Constants.FINISH_ACTIVITY_INTENT);
                    intentFinishActivity.putExtra(Constants.FINISH_ACTIVITY_BUNDLE_KEY, false);
                    mContext.sendBroadcast(intentFinishActivity);
                }
            }
        } else {
            Log.d(LOG_TAG, "user not even logged in. how did this call got here?");
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
        oRet = true;
        Log.d(LOG_TAG, "put pedometer data success");
        Date date = new Date(day);
        Log.d(LOG_TAG, "date : " + date);
        return oRet;
    }



    @Override
    public void onConnect(boolean shouldSignIn) {
        Log.d(LOG_TAG, "Meteor DDP onConnect");
        isMeteorClientConnected.set(true);
        dispatchMessageToHandlers(Constants.METEOR_CLIENT_STATE, shouldSignIn);
        //subscribeToStats();
    }

    private void dispatchMessageToHandlers(int what, boolean b) {
        dispatchMessageToHandlers(what, b ? 1 : 0, 0, null);
    }

    private void dispatchMessageToHandlers(int what, boolean b, String reason) {
        dispatchMessageToHandlers(what, b ? 1 : 0, 0, reason);
    }

    private void dispatchMessageToHandlers(int what, int arg1, int arg2, Object obj) {

        Log.d(LOG_TAG, "dispatchMessageToHandlers handlerList.size : " + handlerList.size());
        if (handlerList.size() > 0) {
            for(Handler handler : handlerList) {
                if (handler != null) {
                    handler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
                }
            }
        }
    }

    @Override
    public void onDisconnect() {
        Log.d(LOG_TAG, "Meteor DDP onDisconnect");
        dispatchMessageToHandlers(Constants.METEOR_CLIENT_STATE, false);
        Intent intentFinishActivity = new Intent(Constants.FINISH_ACTIVITY_INTENT);
        intentFinishActivity.putExtra(Constants.FINISH_ACTIVITY_BUNDLE_KEY, true);
        mContext.sendBroadcast(intentFinishActivity);
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
        if (collectionName.equalsIgnoreCase("users")) {
            String[] userDocIds = FitnessMDMeteor.getInstance().getDatabase().getCollection(collectionName).getDocumentIds();
            Log.d(LOG_TAG,"userdocIds.length : " + userDocIds.length);
            for (int i = 0; i < userDocIds.length; i++) {
                InMemoryDocument userdoc = FitnessMDMeteor.getInstance().getDatabase().getCollection(collectionName).getDocument(userDocIds[i]);
                Log.d(LOG_TAG,"field emails : " + userdoc.getField("emails").toString());
                Gson gson = new Gson();
                String json  = gson.toJson(userdoc.getField("emails"));
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(json);
                JsonArray obj = element.getAsJsonArray();
                for(int t = 0; t < obj.size() ; t++) {
                    JsonObject elem = obj.get(t).getAsJsonObject();
                    if (elem.has("address") && elem.get("address").getAsString().equalsIgnoreCase(userLoggingInEmail)) {
                        mDB.updateUserID(userLoggingInEmail, documentID);
                    }
                }
            }
        } else if (collectionName.equalsIgnoreCase("challenges")) {
            Log.d(LOG_TAG,"successfullySubscribedToChallenges : " + successfullySubscribedToChallenges);
            if (successfullySubscribedToChallenges) {
                InMemoryDocument challengeDoc = FitnessMDMeteor.getInstance().getDatabase().getCollection(collectionName)
                        .getDocument(documentID);
                Intent sendChallenge = new Intent(Constants.NEW_CHALLENGE_INTENT);
                mContext.sendBroadcast(sendChallenge);
                Intent intent = new Intent(mContext, ChallengesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
                mBuilder.setSmallIcon(R.drawable.icon_sm);
                mBuilder.setContentTitle("New " + challengeDoc .getField(ChallengeDetails.TYPE).toString() + " challenge !");
                mBuilder.setContentText("Difficulty : " + challengeDoc .getField(ChallengeDetails.DIFFICULTY));
                mBuilder.setSubText("Description : " + challengeDoc .getField(ChallengeDetails.TEXT));
                mBuilder.addAction(R.drawable.icon_sm, "Take it", pIntent);
                mBuilder.setContentIntent(pIntent);
                mBuilder.setAutoCancel(true);
                NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(0, mBuilder.build());
            }
        } else if (collectionName.equalsIgnoreCase("advices")) {
            Log.d(LOG_TAG,"successfullySubscribedToAdvices : " + successfullySubscribedToAdvices);
            if (successfullySubscribedToAdvices) {
                InMemoryDocument adviceDoc = FitnessMDMeteor.getInstance().getDatabase().getCollection(collectionName)
                        .getDocument(documentID);
                Intent sendAdvice = new Intent(Constants.NEW_ADVICE_INTENT);
                mContext.sendBroadcast(sendAdvice);
                Intent intent = new Intent(mContext, AdvicesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
                mBuilder.setSmallIcon(R.drawable.icon_sm);
                mBuilder.setContentTitle("Advice from " + adviceDoc.getField(AdviceDetails.OWNER).toString() + " !");
                mBuilder.setContentText(adviceDoc.getField(AdviceDetails.MESSAGE).toString());
                mBuilder.setSubText(adviceDoc.getField(AdviceDetails.TIMESTAMP).toString());
                mBuilder.setContentIntent(pIntent);
                mBuilder.setAutoCancel(true);
                NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(0, mBuilder.build());
            }
        }
    }

    @Override
    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        Log.d(LOG_TAG, "Meteor DDP onDataChanged Data changed in <"
                + collectionName + "> in document <" + documentID + "> \n"
                + "    Updated: " + updatedValuesJson
                + "\n    Removed: " + removedValuesJson);

        if (collectionName.equalsIgnoreCase("users")) {
            parseStatisticsData();
        }

    }


    private synchronized void parseStatisticsData() {
        if (mDB.getConnectedUser() != null) {
            InMemoryDocument userdoc =
                    FitnessMDMeteor.getInstance().getDatabase().getCollection("users").getDocument(
                            mDB.getConnectedUser().getDocId());
            if (userdoc != null) {
                Gson gson = new Gson();
                String json  = gson.toJson(userdoc.getField("pedometerData"));
                if (json != null) {
                    JsonParser parser = new JsonParser();
                    JsonElement element = parser.parse(json);
                    JsonArray arr = element.getAsJsonArray();

                    LinkedHashMap<Long, Integer> map = new LinkedHashMap<>();
                    LinkedHashMap<Long, Integer> stats = new LinkedHashMap<>();
                    long startOfCurrentDay = Constants.getStartOfCurrentDay();
                    int totalSteps = 0;
                    long[] daysAgo = new long[7];

                    for (int k = 7; k >=1 ; k--) {
                        daysAgo[k-1] = startOfCurrentDay - (k*24*60*60*1000);
                    }
                    // data is separated by hour index. concatenate data per day
                    //Log.d(LOG_TAG,"map.size : " + map.size() + " entrySet.size : " + map.entrySet().size());
                    for(int t = 0; t < arr.size() ; t++) {
                        JsonObject elem = arr.get(t).getAsJsonObject();
                        JsonElement dayElement = elem.get("day");
                        if (dayElement != null) {
                            Integer stepsForDay = map.get(dayElement.getAsLong());
                            //Log.d(LOG_TAG,"day : " + dayElement.getAsLong() + " steps : " + elem.get("steps").getAsInt() + " stepsForDay : " + stepsForDay);
                            if (stepsForDay != null) {
                                int steps = map.get(elem.get("day").getAsLong());
                                steps += elem.get("steps").getAsInt();
                                map.put(elem.get("day").getAsLong(), steps);
                            } else {
                                map.put(elem.get("day").getAsLong(), elem.get("steps").getAsInt());
                            }
                        }
                        totalSteps += elem.get("steps").getAsInt();
                    }
                    int maxSteps = -1;
                    long dayForMaxSteps = -1;
                    AtomicBoolean valueInStatsChanged = new AtomicBoolean(false);
                    Log.d(LOG_TAG,"savedStats.entrySet.size : " + savedStats.entrySet().size());
                    for (Map.Entry<Long,Integer> entry : map.entrySet()) {

                        for (int l = 0; l < daysAgo.length; l++) {
                            //Log.d(LOG_TAG,entry.getKey() + " : " + entry.getValue() + " daysAgo : " + daysAgo[l]);
                            if (entry.getKey().longValue() == daysAgo[l]) {
                                if(savedStats.entrySet().size() == 0) {
                                    valueInStatsChanged.set(true);
                                }
                                if (savedStats.containsKey(entry.getKey())) {
                                    Log.d(LOG_TAG,entry.getKey() + " : " + entry.getValue() + " savedStats : " + savedStats.get(entry.getKey().longValue()));
                                    if (savedStats.get(entry.getKey().longValue()) != entry.getValue().longValue()) {
                                        Log.d(LOG_TAG,"values not equal : " + entry.getValue() + " savedStats : " + savedStats.get(entry.getKey().longValue()));
                                        valueInStatsChanged.set(true);
                                    }
                                } else {
                                    savedStats.put(entry.getKey(), entry.getValue());
                                }
                                stats.put(entry.getKey(), entry.getValue());
                            }
                        }
                        if (entry.getValue() > maxSteps) {
                            maxSteps = entry.getValue();
                            dayForMaxSteps = entry.getKey();
                            mDB.updateBestSteps(entry.getKey(), entry.getValue());
                        }
                    }

                    for (IStatsChanged callback : statsCallbackList) {
                        callback.onTotalStepsChanged(totalSteps);
                        callback.onAverageStepsChanged(totalSteps / (arr.size() / 8));
                        callback.onMaxStepsChanged(dayForMaxSteps, maxSteps);
                        if (valueInStatsChanged.get()) {
                            Log.d(LOG_TAG,"stats values CHANGED");
                            callback.onLast7DaysStats(stats);
                        } else {
                            Log.d(LOG_TAG,"stats values not changed");
                        }
                    }
                }

            }

        }
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
        savedStats.clear();
    }

    public void subscribeToStats() {
        if (mDB.getConnectedUser() != null) {
            Log.d(LOG_TAG, "subscribeToStats : " + mDB.getConnectedUser().getDocId());
            Map<String, String> insertMessage = new HashMap<String, String>();
            insertMessage.put("_id", mDB.getConnectedUser().getDocId());


            FitnessMDMeteor.getInstance().subscribe("userDataByUserId", new Object[]{insertMessage}, new SubscribeListener() {
                @Override
                public void onSuccess() {
                    Log.d(LOG_TAG, "onSuccess stats");
                    parseStatisticsData();
                }

                @Override
                public void onError(String error, String reason, String details) {
                    Log.d(LOG_TAG, "onError subscriptionId : " + error + " >>> " + reason + " >>> " + details);
                }
            });
        }
    }

    public void subscribeToChallenges() {
        Log.d(LOG_TAG, "subscribeToChallenges ");
        if (mDB.getConnectedUser() != null) {
            Log.d(LOG_TAG, "subscribeToChallenges : " + mDB.getConnectedUser().getDocId());
            Map<String, String> insertMessage = new HashMap<String, String>();
            insertMessage.put("_id", mDB.getConnectedUser().getDocId());


            FitnessMDMeteor.getInstance().subscribe("challenges_all", new Object[]{insertMessage}, new SubscribeListener() {
                @Override
                public void onSuccess() {
                    Log.d(LOG_TAG, "onSuccess challenges");
                    successfullySubscribedToChallenges = true;
                }

                @Override
                public void onError(String error, String reason, String details) {
                    Log.d(LOG_TAG, "onError subscriptionId : " + error + " >>> " + reason + " >>> " + details);
                }
            });
        }
    }

    public void subscribeToAdvices() {
        Log.d(LOG_TAG, "subscribeToAdvices ");
        if (mDB.getConnectedUser() != null) {
            Log.d(LOG_TAG, "subscribeToAdvices : " + mDB.getConnectedUser().getDocId());
            Map<String, String> insertMessage = new HashMap<String, String>();
            insertMessage.put("_id", mDB.getConnectedUser().getDocId());


            FitnessMDMeteor.getInstance().subscribe("advices_all", new Object[]{insertMessage}, new SubscribeListener() {
                @Override
                public void onSuccess() {
                    Log.d(LOG_TAG, "onSuccess advices");
                    successfullySubscribedToAdvices = true;
                }

                @Override
                public void onError(String error, String reason, String details) {
                    Log.d(LOG_TAG, "onError subscriptionId : " + error + " >>> " + reason + " >>> " + details);
                }
            });
        }
    }

    public InMemoryCollection getAdvices() {
        String[] collectionNames = FitnessMDMeteor.getInstance().getDatabase().getCollectionNames();
        Log.d(LOG_TAG, "collectionNames.length : " + collectionNames.length);
        for (int i = 0; i < collectionNames.length; i++) {
            Log.d(LOG_TAG, "collectionNames[" + i + "] : " + collectionNames[i]);
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

    public void registerStatsCallback(IStatsChanged callback) {
        statsCallbackList.add(callback);
    }

    public void removeStatsCallback() {
        //handlerList.remove(0);
    }

    public void sendStepsToServer(long startOfCurrentDay, int totalSteps, int hourIndex) {
        if (FitnessMDMeteor.getInstance().isConnected()) {
            if (mDB.getConnectedUser() != null) {
                FitnessMDMeteor.getInstance().sendStepsToServer(mDB.getConnectedUser().getDocId(),
                        startOfCurrentDay, totalSteps, hourIndex,
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
            } else {
                Log.d(LOG_TAG, "sendStepsToServer connectedUser is null");
            }
        } else {
            Log.d(LOG_TAG, "Meteor not connected ERROR -- send steps to server");
        }
    }
    public void registerToChallenge(String challengeID, boolean isRegistering) {
        if (FitnessMDMeteor.getInstance().isConnected()) {
            if (mDB.getConnectedUser() != null) {
                FitnessMDMeteor.getInstance().registerToChallenge(mDB.getConnectedUser().getDocId(),
                        challengeID, isRegistering,
                        new ResultListener() {
                            @Override
                            public void onSuccess(String s) {
                                Log.d(LOG_TAG, "registerToChallenge -- SUCCESS");

                            }

                            @Override
                            public void onError(String error, String reason, String details) {
                                Log.d(LOG_TAG, "registerToChallenge -- ERROR");
                            }
                        });
            } else {
                Log.d(LOG_TAG, "registerToChallenge connectedUser is null");
            }
        } else {
            Log.d(LOG_TAG, "Meteor not connected ERROR -- registerToChallenge");
        }
    }
}
