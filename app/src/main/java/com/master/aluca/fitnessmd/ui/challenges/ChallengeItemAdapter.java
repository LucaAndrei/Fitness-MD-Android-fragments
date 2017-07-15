/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.ui.challenges;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.datatypes.ChallengeDetails;
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;

import java.util.ArrayList;


public class ChallengeItemAdapter extends BaseAdapter {

    private static final String LOG_TAG = "Fitness_ChItemAdapter";

    private ArrayList<ChallengeDetails> _data;
    Context _c;

    public ChallengeItemAdapter(ArrayList<ChallengeDetails> data, Context c){
        _data = data;
        _c = c;
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return _data.size();
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return _data.get(position);
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater)_c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.challenge_item_adapter, null);
        }

        LinearLayout item = (LinearLayout) v.findViewById(R.id.challenge_item);
        TextView difficulty = (TextView)v.findViewById(R.id.challenge_difficulty);
        TextView type = (TextView)v.findViewById(R.id.challenge_type);
        TextView text = (TextView)v.findViewById(R.id.challenge_text);
        final Button action = (Button)v.findViewById(R.id.challenge_btn);

        final ChallengeDetails msg = _data.get(position);
        difficulty.setText("Difficulty : " + msg.getDifficulty());
        type.setText("Type : " + msg.getType());
        text.setText("Description : " + msg.getText());
        action.setText("Take it");
        if (msg.getRegisteredUsers() == null) {
            Log.d(LOG_TAG,"msg registered users is null");
            action.setText("Take it");
        } else {
            Log.d(LOG_TAG,"msg registered users thisuser : " + UsersDB.getInstance(_c).getConnectedUser().getDocId());
            for(String userId : msg.getRegisteredUsers()) {
                Log.d(LOG_TAG,"msg registered users userId : " + userId);
                if (userId.equalsIgnoreCase(UsersDB.getInstance(_c).getConnectedUser().getDocId())) {
                    action.setText("Give up");
                    break;
                }
            }
        }

        switch(msg.getType()) {
            case "Speed" :
                item.setBackgroundColor(Color.parseColor("#ff8f5e")); // orange
                break;
            case "Strength" :
                item.setBackgroundColor(Color.parseColor("#baa9ba")); // purple
                break;
            case "Endurance" :
                item.setBackgroundColor(Color.parseColor("#d6c1ab")); // brown
                break;
        }
        action.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(action.getText().toString()) {
                    case "Take it" :
                        Log.d(LOG_TAG, "onClick action take it");
                        WebserverManager.getInstance(_c).registerToChallenge(msg.getChallengeDocID(), true);
                        action.setText("Give up");
                        break;
                    case "Give up" :
                        Log.d(LOG_TAG,"onClick action give up");
                        WebserverManager.getInstance(_c).registerToChallenge(msg.getChallengeDocID(), false);
                        action.setText("Take it");
                        break;
                }

            }
        });

        return v;
    }
}