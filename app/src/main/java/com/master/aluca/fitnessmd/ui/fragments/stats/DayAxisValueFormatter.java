/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.ui.fragments.stats;

import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DayAxisValueFormatter implements IAxisValueFormatter {
    private static final String LOG_TAG = "Fitness_Statistics_DayX";

    public DayAxisValueFormatter() {}

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        Log.d(LOG_TAG, "value : " + value);
        String yesterday = getCalculatedDate("EEE,MMM d",(int)(value+1));
        Log.d(LOG_TAG, (int)value + " day(s) ago was : " + yesterday);
        return yesterday;
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }

    public static String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }
}
