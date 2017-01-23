package com.master.aluca.fitnessmd.ui.fragments.stats;

/**
 * Created by aluca on 11/4/16.
 */
import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by philipp on 02/06/16.
 */
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
