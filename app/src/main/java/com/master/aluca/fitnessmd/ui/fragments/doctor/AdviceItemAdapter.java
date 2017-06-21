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
import android.widget.TextView;

import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.datatypes.AdviceDetails;

import java.util.ArrayList;


public class AdviceItemAdapter extends BaseAdapter {

    private ArrayList<AdviceDetails> _data;
    Context _c;

    public AdviceItemAdapter(ArrayList<AdviceDetails> data, Context c){
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

        TextView fromView = (TextView)v.findViewById(R.id.From);
        TextView descView = (TextView)v.findViewById(R.id.description);
        TextView timeView = (TextView)v.findViewById(R.id.time);

        AdviceDetails msg = _data.get(position);
        fromView.setText(msg.getOwnerName());
        descView.setText(msg.getMessage());
        android.text.format.DateFormat df = new android.text.format.DateFormat();

        timeView.setText(df.format("yyyy-MM-dd hh:mm", Long.parseLong(msg.getTimestamp())));

        return v;
    }
}