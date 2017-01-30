package com.master.aluca.fitnessmd.ui.fragments.pedometer;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.TextView;
import android.widget.Toast;


import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.ArcProgress;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.util.SharedPreferencesManager;
import com.master.aluca.fitnessmd.service.FitnessMDService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by aluca on 11/3/16.
 */
public class PedometerFragment extends Fragment {

    private static final String LOG_TAG = "Fitness_PedometerFrag";

    private Activity mActivity;
    private SharedPreferencesManager sharedPreferencesManager;
    static TextView tvKCal, tvKm, tvDateToday;
    private FitnessMDService mService;
    private ArcProgress mArcProgress;
    private int stepsForCurrentDay;
    private int totalSteps = 0;
    Chronometer timeElapsed;
    int hours,minutes,seconds;
    double kilometers;
    double caloriesBurned = 0.0d;

    Button startTimer;

    private Dialog mDialog;

    private static final String TWO_DIGITS = "%02d";
    private static final String ONE_DIGIT = "%01d";
    private static final String NEG_TWO_DIGITS = "-%02d";
    private static final String NEG_ONE_DIGIT = "-%01d";

    View pedometerView;

    int mState = Constants.STOPWATCH_STOPPED;


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(LOG_TAG, "on service connected");
            FitnessMDService.FitnessMD_Binder binder = (FitnessMDService.FitnessMD_Binder) iBinder;
            mService = binder.getService();

            if (mService == null) {
                Log.e(LOG_TAG, "unable to connect to service");
                return;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(LOG_TAG, "on service disconnected");
            mService = null;
        }
    };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "onStart()");
        super.onStart();

        Intent intent = new Intent(this.mActivity, FitnessMDService.class);
        if (!FitnessMDService.isServiceRunning()) {
            mActivity.startService(intent);
        }
        if (!mActivity.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)) {
            Log.e(LOG_TAG, "Unable to bind to optical service");
        }
    }

    public void onStop() {
        Log.d(LOG_TAG, "onStop()");
        super.onStop();

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if (mService != null) {
            mActivity.unbindService(mServiceConnection);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);



        return inflater.inflate(R.layout.layout_tab_pedometer, container, false);
    }


    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        View view = getView();
        if (view == null) {
            return;
        }

        Log.d(LOG_TAG,"PedometerFragment");
        sharedPreferencesManager = SharedPreferencesManager.getInstance(getActivity());

        totalSteps = sharedPreferencesManager.getStepsForCurrentDay();

        Log.d(LOG_TAG, "constructor totalSteps : " + totalSteps);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.STEP_INCREMENT_INTENT);
        intentFilter.addAction(Constants.CONNECTED_DEVICE_DETAILS_INTENT);
        intentFilter.addAction(Constants.DEVICE_CONNECTION_LOST);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
        pedometerView = view;
        setup(view);
    }


    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive : " + intent.getAction());
            if (intent.getAction() == Constants.STEP_INCREMENT_INTENT) {
                Log.d(LOG_TAG, "STEP_INCREMENT_INTENT received");
                int steps = intent.getIntExtra(Constants.STEP_INCREMENT_BUNDLE_KEY,-1);
                totalSteps += steps;
                Log.d(LOG_TAG, "totalSteps : " + totalSteps);
                mArcProgress.setProgress(totalSteps);
                setKm();
                setKCal();
            } else if (intent.getAction().equals(Constants.CONNECTED_DEVICE_DETAILS_INTENT)) {
                Log.d(LOG_TAG, "CONNECTED_DEVICE_DETAILS_INTENT received");
                //startTimer();

                //TODO  - cand iesi din aplicatie si intri iar, timerul ramane setat pe 0 si nu mai porneste.

                /*if(!sharedPreferencesManager.getChronometerRunning()) {
                    sharedPreferencesManager.setChronometerBase(SystemClock.elapsedRealtime());
                }
                timeElapsed.setBase(sharedPreferencesManager.getChronometerBase());
                timeElapsed.start();
                sharedPreferencesManager.setChronometerRunning(true);*/
                Log.d(LOG_TAG, "startTimer : " + mState);
                if(mState == Constants.STOPWATCH_STOPPED || mState == Constants.STOPWATCH_RESET) {
                    doStart();
                }
            } else if (intent.getAction().equals(Constants.DEVICE_CONNECTION_LOST)) {
                Log.d(LOG_TAG, "DEVICE_CONNECTION_LOST received");
                Toast.makeText(getActivity().getApplicationContext(),"Device connection lost",Toast.LENGTH_LONG).show();
                sharedPreferencesManager.setChronometerBase(SystemClock.elapsedRealtime());
                sharedPreferencesManager.setChronometerRunning(false);
                if(mState == Constants.STOPWATCH_RUNNING) {
                    long curTime = SystemClock.elapsedRealtime();
                    mAccumulatedTime += (curTime - mStartTime);
                    doStop();
                }
                //timeElapsed.stop();
            }
        }
    };

    // Used for calculating the time from the start taking into account the pause times
    long mStartTime = 0;
    long mAccumulatedTime = 0;

    public void setup(final View view) {
        tvKCal = (TextView) view.findViewById(R.id.tvKCal);
        tvKm = (TextView) view.findViewById(R.id.tvKm);
        tvDateToday = (TextView) view.findViewById(R.id.tvDateToday);
        SimpleDateFormat s = new SimpleDateFormat("d MMMM yyyy");
        tvDateToday.setText(s.format(new Date()));
        mArcProgress = (ArcProgress) view.findViewById(R.id.arc_progress_pedometer);
        mArcProgress.setProgress(sharedPreferencesManager.getStepsForCurrentDay());

        timeElapsed = (Chronometer) view.findViewById(R.id.chronometer);
        /*timeElapsed.setOnChronometerTickListener(new OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                Log.d(LOG_TAG,"onChronoTick");
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                hours = (int) (time / 3600000);
                minutes = (int) (time - hours * 3600000) / 60000;
                seconds = (int) (time - hours * 3600000 - minutes * 60000) / 1000;
                String hh = hours < 10 ? "0" + hours : hours + "";
                String mm = minutes < 10 ? "0" + minutes : minutes + "";
                String ss = seconds < 10 ? "0" + seconds : seconds + "";
                chronometer.setText(hh + ":" + mm + ":" + ss);
            }
        });*/

        startTimer = (Button) view.findViewById(R.id.startTimer);
        startTimer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "startTimer onClick() : " + mState);
                switch (mState) {
                    case Constants.STOPWATCH_RUNNING:
                        long curTime = SystemClock.elapsedRealtime();
                        mAccumulatedTime += (curTime - mStartTime);
                        doStop();
                        break;
                    case Constants.STOPWATCH_RESET:
                    case Constants.STOPWATCH_STOPPED:
                        // do reset
                        doStart();
                        break;
                    default:
                        Log.d(LOG_TAG, "Illegal state " + mState
                                + " while pressing the left stopwatch button");
                        break;
                }
            }
        });
        setKm();
        setKCal();

    }

    private void doStop() {
        Log.d(LOG_TAG, "doStop()");
        stopUpdateThread();
        startTimer.setText("START");
        mState = Constants.STOPWATCH_STOPPED;
    }
    private void doStart() {
        Log.d(LOG_TAG, "doStart()");
        mStartTime = SystemClock.elapsedRealtime();
        startUpdateThread();
        startTimer.setText("STOP");
        mState = Constants.STOPWATCH_RUNNING;
    }

    private void startUpdateThread() {
        Log.d(LOG_TAG, "startUpdateThread()");
        pedometerView.post(mTimeUpdateThread);
    }

    private void stopUpdateThread() {
        Log.d(LOG_TAG, "stopUpdateThread()");
        pedometerView.removeCallbacks(mTimeUpdateThread);
    }

    Runnable mTimeUpdateThread = new Runnable() {
        @Override
        public void run() {
            long curTime = SystemClock.elapsedRealtime();
            long totalTime = mAccumulatedTime + (curTime - mStartTime);
            setTime(totalTime);
            pedometerView.postDelayed(mTimeUpdateThread, 10);
        }
    };
    private String mHours, mMinutes, mSeconds, mHunderdths;

    public void setTime(long time) {
        String format = null;
        long seconds, minutes, hours;
        seconds = time / 1000;
        minutes = seconds / 60;
        seconds = seconds - minutes * 60;
        hours = minutes / 60;
        minutes = minutes - hours * 60;

        if (hours > 99) {
            hours = 0;
        }
        if (hours >= 10) {
            format = TWO_DIGITS;
            mHours = String.format(format, hours);
        } else if (hours >= 0) {
            format = ONE_DIGIT;
            mHours = String.format(format, hours);
        }/* else {
            mHours = 0;
        }*/

        if (minutes >= 10 || hours > 0) {
            format = TWO_DIGITS;
            mMinutes = String.format(format, minutes);
        } else {
            format = ONE_DIGIT;
            mMinutes = String.format(format, minutes);
        }

        mSeconds = String.format(TWO_DIGITS, seconds);
        timeElapsed.setText(mHours + ":" + mMinutes + ":" + mSeconds);
//        Log.d(LOG_TAG,"mHours : " + mHours);
//        Log.d(LOG_TAG, "mMinutes : " + mMinutes);
//        Log.d(LOG_TAG, "mSeconds : " + mSeconds);
//        Log.d(LOG_TAG,"timeElapsed : " + timeElapsed.getText());
    }

    public void setKm() {
        kilometers = (double)totalSteps / 1320;
        Log.d(LOG_TAG, "kilometers : " + kilometers);
        kilometers = Math.round(kilometers * 10d) / 10d;
        tvKm.setText("" + kilometers);
    }

    /*
            For 0% grade:

    CB = [0.0215 x KPH3 - 0.1765 x KPH2 + 0.8710 x KPH + 1.4577] x WKG x T


http://www.shapesense.com/fitness-exercise/calculators/walking-calorie-burn-calculator.shtml
     */
    public void setKCal() {
        double timeActiveInHours = (double)((hours * 60) + minutes) / 60;
        double kilometersPerHour = kilometers / timeActiveInHours;
        kilometersPerHour = Math.round(kilometersPerHour * 10d) / 10d;
        if (kilometersPerHour > 1.0) {
            double kph3 = 0.0215 * (Math.pow(kilometersPerHour,3));
            double kph2 = 0.1765 * (Math.pow(kilometersPerHour,2));
            double kph = 0.8710 * kilometersPerHour;
            timeActiveInHours = Math.round(timeActiveInHours * 10d) / 10d;

            kph3 = Math.round(kph3 * 10d) / 10d;
            kph2 = Math.round(kph2 * 10d) / 10d;
            kph = Math.round(kph * 10d) / 10d;
            caloriesBurned = (kph3 - kph2 + kph + 1.4577) * sharedPreferencesManager.getWeight() * timeActiveInHours;
        }
        tvKCal.setText("" + (int)caloriesBurned);
    }

    /*

    Activity Multiplier (Both HB + KA Method use same activity multiplier)
    Little or No Exercise, Desk Job                     1.2 x BMR
    Light Exercise, Sports 1 to 3 Times Per Week        1.375 x BMR
    Moderate Exercise, Sports 3 to 5 Times Per Week     1.55 x BMR
    Heavy Exercise, Sports 6 to 7 Times Per Week        1.725 x BMR


     */

    public int getStepsForCurrentDay() {
        return totalSteps;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume()");
        readFromSharedPref();
        pedometerView.postInvalidate();
        Log.d(LOG_TAG, "onResume() mState : " + mState);
        startTimer.setText(mState == Constants.STOPWATCH_RUNNING ? "STOP" : "START");
        setTime(mAccumulatedTime);
        if (mState == Constants.STOPWATCH_RUNNING) {
            startUpdateThread();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause()");
        if (mState == Constants.STOPWATCH_RUNNING) {
            stopUpdateThread();
        }
        // The stopwatch must keep running even if the user closes the app so save stopwatch state
        // in shared prefs
        writeToSharedPref();

        super.onPause();
    }

    private void writeToSharedPref() {
        Log.d(LOG_TAG, "writeToSharedPref()");
        String email = sharedPreferencesManager.getEmail();
        sharedPreferencesManager.saveSWStartTime(Constants.SHARED_PREFS_SW_START_TIME+email, mStartTime);
        sharedPreferencesManager.saveSWAccumTime(Constants.SHARED_PREFS_SW_ACCUM_TIME+email, mAccumulatedTime);
        sharedPreferencesManager.saveSWState(Constants.SHARED_PREFS_SW_STATE+email, mState);
        sharedPreferencesManager.setStepsForCurrentDay(Constants.SHARED_PREFS_CURR_DAY_STEPS+email, getStepsForCurrentDay());
        Log.d(LOG_TAG, "writeToSharedPref() getStepsForCurrentDay() : " + getStepsForCurrentDay());
    }

    private void readFromSharedPref() {
        Log.d(LOG_TAG, "readFromSharedPref()");

        mStartTime = sharedPreferencesManager.getSWStartTime();
        mAccumulatedTime = sharedPreferencesManager.getSWAccumTime();
        mState = sharedPreferencesManager.getSWState();
        totalSteps = sharedPreferencesManager.getStepsForCurrentDay();
        mArcProgress.setProgress(totalSteps);
        Log.d(LOG_TAG, "mStartTime : " + mStartTime);
        Log.d(LOG_TAG, "mAccumulatedTime : " + mAccumulatedTime);
        Log.d(LOG_TAG, "mState : " + mState);
        Log.d(LOG_TAG, "totalSteps : " + totalSteps);
    }
}