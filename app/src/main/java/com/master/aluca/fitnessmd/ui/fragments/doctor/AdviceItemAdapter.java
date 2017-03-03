/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.ui.fragments.doctor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.datatypes.MessageDetails;

import java.util.ArrayList;


public class AdviceItemAdapter extends BaseAdapter {

    private ArrayList<MessageDetails> _data;
    Context _c;

    public AdviceItemAdapter(ArrayList<MessageDetails> data, Context c){
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
            v = vi.inflate(R.layout.advice_item_message, null);
        }

        ImageView image = (ImageView) v.findViewById(R.id.icon);
        TextView fromView = (TextView)v.findViewById(R.id.From);
        TextView subView = (TextView)v.findViewById(R.id.subject);
        TextView descView = (TextView)v.findViewById(R.id.description);
        TextView timeView = (TextView)v.findViewById(R.id.time);

        MessageDetails msg = _data.get(position);
        image.setImageResource(msg.getIcon());
        fromView.setText(msg.getName());
        subView.setText("Subject: "+msg.getSub());
        descView.setText(msg.getDesc());
        timeView.setText(msg.getTime());

        return v;
    }
}