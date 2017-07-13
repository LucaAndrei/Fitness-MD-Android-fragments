/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.datatypes.AdviceDetails;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;
import com.master.aluca.fitnessmd.library.FitnessMDMeteor;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryCollection;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryDocument;
import com.master.aluca.fitnessmd.ui.fragments.doctor.AdviceItemAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class AdvicesActivity extends Activity {

    public static final String LOG_TAG = "Fitness_AdvicesActivity";
    ListView advicesList;
    ArrayList<AdviceDetails> advices;
    AdviceItemAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advice);
        Log.d(LOG_TAG, "onCreate");

        advicesList = (ListView) findViewById(R.id.MessageList);
        advices = new ArrayList<>();
        mAdapter = new AdviceItemAdapter(advices,this);
        advicesList.setAdapter(mAdapter);
        advicesList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView a, View v, int position, long id) {
                String s = (String) ((TextView) v.findViewById(R.id.From)).getText();
                Toast.makeText(AdvicesActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.FINISH_ACTIVITY_INTENT);
        intentFilter.addAction(Constants.NEW_ADVICE_INTENT);
        getApplicationContext().registerReceiver(mBroadcastReceiver, intentFilter);

        updateAdvicesList();
    }

    private void updateAdvicesList() {

        InMemoryCollection advicesCollection = FitnessMDMeteor.getInstance().getDatabase().getCollection("advices");
        if (advicesCollection != null) {
            Log.d(LOG_TAG,"updateAdvicesList");
            advices.clear();
            for (String adviceDocID : advicesCollection.getDocumentIds()) {
                InMemoryDocument adviceDoc = advicesCollection.getDocument(adviceDocID);
                Log.d(LOG_TAG,"adviceDoc.getField(AdviceDetails.TIMESTAMP) : " + adviceDoc.getField(AdviceDetails.TIMESTAMP));
                String date = adviceDoc.getField(AdviceDetails.TIMESTAMP).toString();
                String[] datesplit = date.split("=", date.length() - 1);
                AdviceDetails advice = new AdviceDetails(adviceDoc.getField(AdviceDetails.OWNER).toString(),
                        datesplit[1].substring(0,datesplit[1].length() - 1),
                        adviceDoc.getField(AdviceDetails.MESSAGE).toString());
                advices.add(advice);
            }
            mAdapter.notifyDataSetChanged();
        } else {
            Log.d(LOG_TAG,"advices collection is null");
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
                    Intent intentMainActiv = new Intent(getApplicationContext(), NoMeteorConnectionActivity.class);
                    startActivity(intentMainActiv);
                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    WebserverManager mWebserverManager = WebserverManager.getInstance(getApplicationContext());
                    mWebserverManager.destroyMeteor();
                } else {
                    finish();
                }
            } else if (intent.getAction().equalsIgnoreCase(Constants.NEW_ADVICE_INTENT)) {
                updateAdvicesList();
            }
        }
    };


    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

}