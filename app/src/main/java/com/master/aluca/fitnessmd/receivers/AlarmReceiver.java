package com.master.aluca.fitnessmd.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;


import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.util.SharedPreferencesManager;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by aluca on 11/14/16.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "Fitness_AlarmReceiver";
    private SharedPreferencesManager sharedPreferencesManager;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(LOG_TAG, "onReceive");
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();

        // Put here YOUR code.
        Calendar calendar = Calendar.getInstance();
        long currentTimeInMillis = System.currentTimeMillis();
        Log.d(LOG_TAG,"currentTimeInMillis : " + currentTimeInMillis);
        Date currentTimeDate = new Date(currentTimeInMillis);
        Log.d(LOG_TAG,"currentTimeDate : " + currentTimeDate);

        long startOfDayInMillis = Constants.getStartOfCurrentDay();
        Date startOfDayDate = new Date(startOfDayInMillis);
        Log.d(LOG_TAG, "startOfDayInMillis : " + startOfDayInMillis);
        Log.d(LOG_TAG, "startOfDayDate : " + startOfDayDate);

        // Add one day's time to the beginning of the day.
        // 24 hours * 60 minutes * 60 seconds * 1000 milliseconds = 1 day
        if(sharedPreferencesManager == null)
            sharedPreferencesManager = SharedPreferencesManager.getInstance(context);
        long endOfDayInMillis = sharedPreferencesManager.getStartOfCurrentDay() + Constants.DAY;
        Date endOfDayDate = new Date(endOfDayInMillis );
        Log.d(LOG_TAG, "endOfDayInMillis : " + endOfDayInMillis);
        Log.d(LOG_TAG, "endOfDayDate : " + endOfDayDate);

        if (currentTimeInMillis > endOfDayInMillis) {
            Log.d(LOG_TAG, "Save number of steps to database");
            Toast.makeText(context, "Saving to Database", Toast.LENGTH_LONG).show();
            Intent endOfDayIntent = new Intent(Constants.END_OF_DAY);
            endOfDayIntent.putExtra(Constants.END_OF_DAY_BUNDLE_KEY, startOfDayInMillis);
            context.sendBroadcast(endOfDayIntent);
            sharedPreferencesManager.resetStartOfCurrentDay(Constants.getStartOfCurrentDay());
        } else {
            Log.d(LOG_TAG, "Day not ended");
        }

        wakeLock.release();
    }

    public void setAlarm(Context context) {
        Log.d(LOG_TAG, "setAlarm");
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.HALF_HOUR, pendingIntent);

        sharedPreferencesManager = SharedPreferencesManager.getInstance(context);
    }

    public void cancelAlarm(Context context)
    {
        Log.d(LOG_TAG, "cancelAlarm");
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}