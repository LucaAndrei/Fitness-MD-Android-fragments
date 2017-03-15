/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.ui.fragments.doctor;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.datatypes.User;
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.service.FitnessMDService;
import com.master.aluca.fitnessmd.ui.AdvicesActivity;
import com.master.aluca.fitnessmd.ui.DietActivity;

import java.util.Calendar;
import java.util.Locale;

public class DoctorFragment extends Fragment {

    private static final String LOG_TAG = "Fitness_DoctorFragment";

    private Activity mActivity;
    private TextView tvDrHeightUM, tvDrAgeUM, tvDrWeightUM;
    private FitnessMDService mService;
    private int mHeight, mAge;
    private String mGender;
    //LinearLayout btnSetWeightAsGoal, btnToFitnessMD;
    float mWeight, mWeightGoal;
    private double mIdealBodyWeight;
    private double mWaterRequired;

    private Dialog mDialog;

    View listRowBMI;
    TextView listRowBMITitle, listRowBMISubtitle;
    ImageView listRowBMIIcon, listRowBMIInfo;

    View listRowBMR;
    TextView listRowBMRTitle, listRowBMRSubtitle;
    ImageView listRowBMRIcon, listRowBMRInfo;

    View listRowWater;
    TextView listRowWaterTitle, listRowWaterSubtitle;
    ImageView listRowWaterIcon, listRowWaterInfo;

    View listRowIdealBodyWeight;
    TextView listRowIdealBodyWeightTitle, listRowIdealBodyWeightSubtitle;
    ImageView listRowIdealBodyWeightIcon, listRowIdealBodyWeightInfo;
    LinearLayout listRowIdealBodyWeightSetAsGoal;

    View listRowCalorieNeeds;
    TextView listRowCalorieNeedsTitle, listRowCalorieNeedsSubtitle;
    ImageView listRowCalorieNeedsIcon, listRowCalorieNeedsInfo;

    View listRowDiet;
    TextView listRowDietTitle, listRowDietSubtitle;
    ImageView listRowDietIcon, listRowDietInfo;

    View listRowAdvice;
    TextView listRowAdviceTitle, listRowAdviceSubtitle;
    ImageView listRowAdviceIcon, listRowAdviceInfo;

    private UsersDB mDB;
    private User connectedUser = null;


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
        return inflater.inflate(R.layout.layout_tab_doctor, container, false);
    }


    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        View view = getView();
        if (view == null) {
            return;
        }

        Log.d(LOG_TAG,"DoctorFragment");
        mDB = UsersDB.getInstance(mActivity.getApplicationContext());
        connectedUser = mDB.getConnectedUser();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.GENDER_CHANGED_INTENT);
        intentFilter.addAction(Constants.HEIGHT_CHANGED_INTENT);
        intentFilter.addAction(Constants.WEIGHT_CHANGED_INTENT);
        intentFilter.addAction(Constants.YOB_CHANGED_INTENT);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);

        setup(view);
    }




    private void setListeners(View view) {

        /*btnToFitnessMD.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "btnToFitnessMD onClick");
                Intent intent = new Intent(mContext, Diet.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });*/
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive : " + intent.getAction());
            if (intent.getAction().equalsIgnoreCase(Constants.GENDER_CHANGED_INTENT)) {
                mGender = intent.getStringExtra(Constants.GENDER_CHANGED_INTENT_BUNDLE_KEY);
                updateBMR();
                updateIdealBodyWeight();
            } else if (intent.getAction().equalsIgnoreCase(Constants.HEIGHT_CHANGED_INTENT)) {
                mHeight = intent.getIntExtra(Constants.HEIGHT_CHANGED_INTENT, -1);
                if (mHeight != -1) {
                    updateBMI();
                    updateBMR();
                    updateIdealBodyWeight();
                } else {
                    Log.d(LOG_TAG, "onReceive : " + intent.getAction() + " ERROR");
                }
            } else if (intent.getAction().equalsIgnoreCase(Constants.WEIGHT_CHANGED_INTENT)) {
                mWeight = intent.getFloatExtra(Constants.HEIGHT_CHANGED_INTENT, 0.0f);
                if (mWeight != 0.0f) {
                    updateBMI();
                    updateBMR();
                    updateWaterRequired();
                } else {
                    Log.d(LOG_TAG, "onReceive : " + intent.getAction() + " ERROR");
                }
            } else if (intent.getAction().equalsIgnoreCase(Constants.YOB_CHANGED_INTENT)) {
                int yob = intent.getIntExtra(Constants.YOB_CHANGED_INTENT, -1);
                if (yob != -1) {
                    Calendar calendar = Calendar.getInstance();
                    int currentYear = calendar.get(Calendar.YEAR);
                    Log.d(LOG_TAG, "currentYear : " + currentYear);
                    mAge = currentYear - yob;
                    updateBMR();
                } else {
                    Log.d(LOG_TAG, "onReceive : " + intent.getAction() + " ERROR");
                }
            }
        }
    };

    private void updateBMR() {
        Log.d(LOG_TAG, "updateBMR : " + mGender);
        double BMR = 0;
        if(mGender.equalsIgnoreCase("Male")) {
            BMR = 66.47 + (13.75*mWeight) + (5*mHeight) - (6.75 * mAge);
            Log.d(LOG_TAG, "updateBMR Male : " + BMR);
        } else if (mGender.equalsIgnoreCase("Female")) {
            BMR = 665.09 + (9.56 * mWeight) + (1.84 * mHeight) - (4.67 * mAge);
            Log.d(LOG_TAG, "updateBMR Female : " + BMR);
        } else {
            Log.d(LOG_TAG, "updateBMR ERROR");
        }
        BMR = Math.round(BMR * 10d) / 10d;
        Log.d(LOG_TAG, "updateBMR BMR : " + BMR);

        listRowBMRSubtitle.setText(String.valueOf(BMR) + " cal/day");
    }


    // formula : weight / height ^ 2
    private void updateBMI() {
        Log.d(LOG_TAG, "updateBMI");
        float mHeightMeters = (float)mHeight / 100;
        double mBMI =  (mWeight / (mHeightMeters * mHeightMeters));
        mBMI = Math.round(mBMI * 10d) / 10d;
        Log.d(LOG_TAG, "mHeight : " + mHeight + " >>> mHeightMeters : " + mHeightMeters + " >>> mWeight : " + mWeight + " >>> mBMI : " + mBMI);

        listRowBMISubtitle.setText(String.valueOf(mBMI));


        float normalBMIAverage = 21.75f;
        mWeightGoal = (mHeightMeters * mHeightMeters) * normalBMIAverage;
        mWeightGoal = Math.round(mWeightGoal * 10f) / 10f;
        Log.d(LOG_TAG, "mWeightGoal : " + mWeightGoal);
        //tvAverageWeight.setText(mWeightGoal + " kg");
    }

    private void updateIdealBodyWeight() {
        Log.d(LOG_TAG, "updateIdealBodyWeight");
        mIdealBodyWeight = 0.0;
        if(mGender.equalsIgnoreCase("Male")) {
            mIdealBodyWeight = 0.9 * mHeight - 88;
            Log.d(LOG_TAG, "updateIdealBodyWeight Male : " + mIdealBodyWeight);
        } else if (mGender.equalsIgnoreCase("Female")) {
            mIdealBodyWeight = 0.9 * mHeight - 92;
            Log.d(LOG_TAG, "updateIdealBodyWeight Female : " + mIdealBodyWeight);
        } else {
            Log.d(LOG_TAG, "updateIdealBodyWeight ERROR");
        }
        mIdealBodyWeight = Math.round(mIdealBodyWeight * 100d) / 100d;
        Log.d(LOG_TAG, "updateIdealBodyWeight BMR : " + mIdealBodyWeight);
        listRowIdealBodyWeightSubtitle.setText(String.valueOf(mIdealBodyWeight) + " kg");
    }



    private void updateWaterRequired() {
        Log.d(LOG_TAG, "updateWaterRequired");
        mWaterRequired = mWeight / 30;
        mWaterRequired = Math.round(mWaterRequired * 10d) / 10d;
        listRowWaterSubtitle.setText(String.valueOf(mWaterRequired) + " litres");
    }

    public void setup(View view) {
        tvDrHeightUM = (TextView) view.findViewById(R.id.tvDrHeightUM);
        tvDrAgeUM = (TextView) view.findViewById(R.id.tvDrAgeUM);
        tvDrWeightUM = (TextView) view.findViewById(R.id.tvDrWeightUM);
        //btnSetWeightAsGoal = (LinearLayout) view.findViewById(R.id.btnSetWeightAsGoal);
        //btnToFitnessMD = (LinearLayout) view.findViewById(R.id.btnToFitnessMD);

        initUserData();
        setupListRows(view);
        setListeners(view);
    }


    private void initUserData() {
        Log.d(LOG_TAG, "initUserData");

        mGender = connectedUser.getGender();

        mHeight = connectedUser.getHeight();
        Log.d(LOG_TAG, "mHeight : " + mHeight);
        tvDrHeightUM.setText(mHeight + " cm");

        mWeight = connectedUser.getWeight();
        Log.d(LOG_TAG, "mWeight : " + mWeight);
        tvDrWeightUM.setText(mWeight + " kg");

        int yob = connectedUser.getYearOfBirth();
        Log.d(LOG_TAG, "yob : " + yob);
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        Log.d(LOG_TAG, "currentYear : " + currentYear);
        mAge = currentYear - yob;
        Log.d(LOG_TAG, "age : " + mAge);
        tvDrAgeUM.setText(mAge + " yrs");
    }


    private void setupListRows(View view) {
        listRowBMI = view.findViewById(R.id.listRowBMI);
        listRowBMITitle = (TextView) listRowBMI.findViewById(R.id.listRowTitle);
        listRowBMISubtitle = (TextView) listRowBMI.findViewById(R.id.listRowSubtitle);
        listRowBMIInfo = (ImageView) listRowBMI.findViewById(R.id.listRowInfo);
        listRowBMIIcon = (ImageView) listRowBMI.findViewById(R.id.listRowIcon);

        listRowBMITitle.setText("Body Mass Index");
        updateBMI();
        listRowBMIIcon.setImageResource(R.drawable.bmi);
        listRowBMI.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "listRowBMI onClick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View theView = inflater.inflate(R.layout.dialog_bmi_info, null, false);
                builder.setView(theView);
                AlertDialog levelDialog = builder.create();
                levelDialog.show();
            }
        });



        listRowBMR = view.findViewById(R.id.listRowBMR);
        listRowBMRTitle = (TextView) listRowBMR.findViewById(R.id.listRowTitle);
        listRowBMRSubtitle = (TextView) listRowBMR.findViewById(R.id.listRowSubtitle);
        listRowBMRInfo = (ImageView) listRowBMR.findViewById(R.id.listRowInfo);
        listRowBMRIcon = (ImageView) listRowBMR.findViewById(R.id.listRowIcon);

        listRowBMRTitle.setText("Basal metabolic rate");
        updateBMR();
        listRowBMRIcon.setImageResource(R.drawable.bmr);
        listRowBMR.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "listRowBMR onClick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View theView = inflater.inflate(R.layout.dialog_bmr_info, null, false);
                builder.setView(theView);
                AlertDialog levelDialog = builder.create();
                levelDialog.show();

            }
        });



        listRowWater = view.findViewById(R.id.listRowWater);
        listRowWaterTitle = (TextView) listRowWater.findViewById(R.id.listRowTitle);
        listRowWaterSubtitle = (TextView) listRowWater.findViewById(R.id.listRowSubtitle);
        listRowWaterInfo = (ImageView) listRowWater.findViewById(R.id.listRowInfo);
        listRowWaterIcon = (ImageView) listRowWater.findViewById(R.id.listRowIcon);

        listRowWaterTitle.setText("Water required");
        updateWaterRequired();

        listRowWaterIcon.setImageResource(R.drawable.water);
        listRowWater.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "listRowWater onClick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View theView = inflater.inflate(R.layout.dialog_water_required_info, null, false);
                TextView waterRequiredInfoLitres = (TextView) theView.findViewById(R.id.waterRequiredInfoLitres);
                waterRequiredInfoLitres.setText(String.valueOf(mWaterRequired));
                builder.setView(theView);
                AlertDialog levelDialog = builder.create();
                levelDialog.show();
            }
        });



        listRowIdealBodyWeight = view.findViewById(R.id.listRowIdealBodyWeight);
        listRowIdealBodyWeightTitle = (TextView) listRowIdealBodyWeight.findViewById(R.id.listRowTitle);
        listRowIdealBodyWeightSubtitle = (TextView) listRowIdealBodyWeight.findViewById(R.id.listRowSubtitle);
        listRowIdealBodyWeightInfo = (ImageView) listRowIdealBodyWeight.findViewById(R.id.listRowInfo);
        listRowIdealBodyWeightIcon = (ImageView) listRowIdealBodyWeight.findViewById(R.id.listRowIcon);


        listRowIdealBodyWeightTitle.setText("Ideal Body Weight");
        updateIdealBodyWeight();
        listRowIdealBodyWeightIcon.setImageResource(R.drawable.idealbody);
        listRowIdealBodyWeight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "listRowIdealBodyWeight onClick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View theView = inflater.inflate(R.layout.dialog_ideal_body_weight_info, null, false);
                TextView idealBodyWeightInfoKg = (TextView) theView.findViewById(R.id.idealBodyWeightInfoKg);
                idealBodyWeightInfoKg.setText(String.valueOf(mIdealBodyWeight));

                builder.setView(theView);
                final AlertDialog levelDialog = builder.create();

                listRowIdealBodyWeightSetAsGoal = (LinearLayout) theView.findViewById(R.id.listRowIdealBodyWeightSetAsGoal);
                Log.d(LOG_TAG, "ideal : " + listRowIdealBodyWeightSetAsGoal);
                listRowIdealBodyWeightSetAsGoal.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(LOG_TAG, "idealBodyWeightSetAsGoal onClick");
                        Toast.makeText(getActivity(), "Set as goal", Toast.LENGTH_LONG).show();
                        //sharedPreferencesManager.setWeightGoal(mWeightGoal);
                        Intent intent = new Intent(Constants.WEIGHT_GOAL_INTENT);
                        intent.putExtra(Constants.WEIGHT_GOAL_BUNDLE_KEY, mWeightGoal);
                        getActivity().sendBroadcast(intent);
                        levelDialog.dismiss();
                    }
                });
                levelDialog.show();
            }
        });



        listRowCalorieNeeds = view.findViewById(R.id.listRowCalorieNeeds);
        listRowCalorieNeedsTitle = (TextView) listRowCalorieNeeds.findViewById(R.id.listRowTitle);
        listRowCalorieNeedsSubtitle = (TextView) listRowCalorieNeeds.findViewById(R.id.listRowSubtitle);
        listRowCalorieNeedsInfo = (ImageView) listRowCalorieNeeds.findViewById(R.id.listRowInfo);
        listRowCalorieNeedsIcon = (ImageView) listRowCalorieNeeds.findViewById(R.id.listRowIcon);

        listRowCalorieNeedsTitle.setText("Calorie Needs");
        listRowCalorieNeedsSubtitle.setText(String.valueOf(mWeight * 35));
        listRowCalorieNeedsIcon.setImageResource(R.drawable.calories);
        listRowCalorieNeeds.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "listRowCalorieNeeds onClick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View theView = inflater.inflate(R.layout.dialog_calories_needed_info, null, false);

                TextView sedentaryValue = (TextView) theView.findViewById(R.id.tvSedentaryValue);
                double sedentaryValueCal = mWeight * 31;
                sedentaryValueCal = Math.round(sedentaryValueCal * 100d) / 100d;
                sedentaryValue.setText(String.valueOf(sedentaryValueCal) + " Cal");

                TextView lightlyActiveValue = (TextView) theView.findViewById(R.id.tvLightActiveValue);
                double lightlyActiveCal = mWeight * 35;
                lightlyActiveCal = Math.round(lightlyActiveCal * 100d) / 100d;
                lightlyActiveValue.setText(String.valueOf(lightlyActiveCal) + " Cal");

                TextView moderatelyActiveValue = (TextView) theView.findViewById(R.id.tvModActiveValue);
                double moderatelyActiveCal = mWeight * 40;
                moderatelyActiveCal = Math.round(moderatelyActiveCal * 100d) / 100d;
                moderatelyActiveValue.setText(String.valueOf(moderatelyActiveCal) + " Cal");

                TextView veryActiveValue = (TextView) theView.findViewById(R.id.tvVeryActiveValue);
                double veryActiveCal = mWeight * 45;
                veryActiveCal = Math.round(veryActiveCal * 100d) / 100d;
                veryActiveValue.setText(String.valueOf(veryActiveCal) + " Cal");

                builder.setView(theView);
                AlertDialog levelDialog = builder.create();
                levelDialog.show();
            }
        });



        listRowDiet = view.findViewById(R.id.listRowDiet);
        listRowDietTitle = (TextView) listRowDiet.findViewById(R.id.listRowTitle);
        listRowDietSubtitle = (TextView) listRowDiet.findViewById(R.id.listRowSubtitle);
        listRowDietInfo = (ImageView) listRowDiet.findViewById(R.id.listRowInfo);
        listRowDietIcon = (ImageView) listRowDiet.findViewById(R.id.listRowIcon);

        listRowDietTitle.setText("Diet intake");
        listRowDietSubtitle.setText(Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()));
        listRowDietIcon.setImageResource(R.drawable.diet);
        listRowDiet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "listRowDiet onClick");
                Intent intent = new Intent(getActivity(), DietActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
            }
        });

        listRowAdvice = view.findViewById(R.id.listRowAdvice);
        listRowAdviceTitle = (TextView) listRowAdvice.findViewById(R.id.listRowTitle);
        listRowAdviceSubtitle = (TextView) listRowAdvice.findViewById(R.id.listRowSubtitle);
        listRowAdviceInfo = (ImageView) listRowAdvice.findViewById(R.id.listRowInfo);
        listRowAdviceIcon = (ImageView) listRowAdvice.findViewById(R.id.listRowIcon);

        listRowAdviceTitle.setText("Doctor's advice");
        listRowAdviceSubtitle.setText("");
        listRowAdviceIcon.setImageResource(R.drawable.advice);
        listRowAdvice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "listRowAdvice onClick");
                Intent intent = new Intent(getActivity(), AdvicesActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }
}
