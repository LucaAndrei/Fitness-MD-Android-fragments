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
import com.master.aluca.fitnessmd.models.Challenge;

import java.util.ArrayList;
import java.util.Date;

public class ChallengesActivity extends Activity {

    public static final String LOG_TAG = "Fitness_ChallengesActiv";

    private WebserverManager mWebserverManager;

    ListView challengesList;
    ArrayList<ChallengeDetails> challenges;
    ChallengeItemAdapter mAdapter;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        challengesList = (ListView) findViewById(R.id.ChallengesList);
        challenges = new ArrayList<ChallengeDetails>();

        InMemoryCollection challengesCollection = FitnessMDMeteor.getInstance().getDatabase().getCollection("challenges");
        for (String challengeDocID : challengesCollection.getDocumentIds()) {
            InMemoryDocument challengeDoc = challengesCollection.getDocument(challengeDocID);

            ChallengeDetails challenge = new ChallengeDetails(challengeDoc.getField(Challenge.DIFFICULTY).toString(),
                    challengeDoc.getField(Challenge.TYPE).toString(), challengeDoc.getField(Challenge.TEXT).toString());
            challenge.setChallengeDocID(challengeDocID);
            if (challengeDoc.getField(Challenge.REGISTERED_USERS) != null) {
                Log.d(LOG_TAG, "---------------- : " + challengeDoc.getField(Challenge.DIFFICULTY));
                String regUsers = challengeDoc.getField(Challenge.REGISTERED_USERS).toString();
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

        pDialog = new ProgressDialog(ChallengesActivity.this);

        mWebserverManager =  WebserverManager.getInstance(this);


        mAdapter = new ChallengeItemAdapter(challenges,this);

        challengesList.setAdapter(mAdapter);

        challengesList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView a, View v, int position, long id) {

                String s = (String) ((TextView) v.findViewById(R.id.From)).getText();
                Toast.makeText(ChallengesActivity.this, s, Toast.LENGTH_LONG).show();
            }
        });

    }


    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

}