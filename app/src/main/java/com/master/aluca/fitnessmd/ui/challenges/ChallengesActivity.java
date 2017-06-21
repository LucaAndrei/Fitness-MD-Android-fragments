/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.ui.challenges;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.datatypes.ChallengeDetails;
import com.master.aluca.fitnessmd.common.datatypes.MessageDetails;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;
import com.master.aluca.fitnessmd.library.FitnessMDMeteor;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryCollection;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryDocument;
import com.master.aluca.fitnessmd.ui.NoInternetActivity;

import java.util.ArrayList;
import java.util.Date;

public class ChallengesActivity extends Activity {

    public static final String LOG_TAG = "Fitness_ChallengesActiv";

    private WebserverManager mWebserverManager;

    ListView challengesList;
    ArrayList<ChallengeDetails> challenges;
    ChallengeItemAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        Log.d(LOG_TAG, "onCreate");


        challengesList = (ListView) findViewById(R.id.ChallengesList);
        challenges = new ArrayList<ChallengeDetails>();
        mWebserverManager =  WebserverManager.getInstance(this);
        mAdapter = new ChallengeItemAdapter(challenges,this);
        challengesList.setAdapter(mAdapter);
        challengesList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView a, View v, int position, long id) {

                String s = (String) ((TextView) v.findViewById(R.id.From)).getText();
                Toast.makeText(ChallengesActivity.this, s, Toast.LENGTH_LONG).show();
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.FINISH_ACTIVITY_INTENT);
        intentFilter.addAction(Constants.NEW_CHALLENGE_INTENT);
        getApplicationContext().registerReceiver(mBroadcastReceiver, intentFilter);

        updateChallengesList();

    }

    private void updateChallengesList() {

        InMemoryCollection challengesCollection = FitnessMDMeteor.getInstance().getDatabase().getCollection("challenges");
        if (challengesCollection != null) {
            Log.d(LOG_TAG,"updateChallengesList");
            challenges.clear();
            for (String challengeDocID : challengesCollection.getDocumentIds()) {
                InMemoryDocument challengeDoc = challengesCollection.getDocument(challengeDocID);
                ChallengeDetails challenge = new ChallengeDetails(challengeDoc.getField(ChallengeDetails.DIFFICULTY).toString(),
                        challengeDoc.getField(ChallengeDetails.TYPE).toString(), challengeDoc.getField(ChallengeDetails.TEXT).toString());
                challenge.setChallengeDocID(challengeDocID);
                if (challengeDoc.getField(ChallengeDetails.REGISTERED_USERS) != null) {
                    Log.d(LOG_TAG, "---------------- : " + challengeDoc.getField(ChallengeDetails.DIFFICULTY));
                    String regUsers = challengeDoc.getField(ChallengeDetails.REGISTERED_USERS).toString();
                    regUsers = regUsers.substring(1, regUsers.length() - 1);
                    Log.d(LOG_TAG, "regUsers : " + regUsers);
                    if (regUsers.indexOf(",") != -1) {
                        String[] regUsersArr = regUsers.split(", ");
                        challenge.setRegisteredUsers(regUsersArr);
                        for (String regUserID : regUsersArr) {
                            Log.d(LOG_TAG,"regUserID : " + regUserID);
                        }
                    } else {
                        String[] regUsersArr = new String[]{regUsers};
                        challenge.setRegisteredUsers(regUsersArr);
                    }

                    //challenge.setRegisteredUsers(challengeDoc.getField(Challenge.REGISTERED_USERS));
                }
                challenges.add(challenge);
            }
            mAdapter.notifyDataSetChanged();
        } else {
            Log.d(LOG_TAG,"challenges collection is null");
        }
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive : " + intent.getAction());
            if (intent.getAction() == Constants.FINISH_ACTIVITY_INTENT) {
                Log.d(LOG_TAG, "FINISH_ACTIVITY_INTENT received");
                boolean shouldFinish = intent.getBooleanExtra(Constants.FINISH_ACTIVITY_BUNDLE_KEY,false);
                if (shouldFinish) {
                    Intent intentMainActiv = new Intent(getApplicationContext(), NoInternetActivity.class);
                    startActivity(intentMainActiv);
                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    WebserverManager mWebserverManager = WebserverManager.getInstance(getApplicationContext());
                    mWebserverManager.destroyMeteor();
                } else {
                    finish();
                }
            } else if (intent.getAction().equalsIgnoreCase("new_challenge_intent")) {
                updateChallengesList();
            }
        }
    };


    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

}