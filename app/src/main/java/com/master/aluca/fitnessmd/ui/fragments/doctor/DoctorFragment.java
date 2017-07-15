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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import com.master.aluca.fitnessmd.common.util.IDataRefreshCallback;
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
    private double mIdealBodyWeight;

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


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(LOG_TAG, "on service connected");
            FitnessMDService.FitnessMD_Binder binder = (FitnessMDService.FitnessMD_Binder) iBinder;
            mService = binder.getService();

            if (mService == null) {
                Log.e(LOG_TAG, "unable to connect to service");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(LOG_TAG, "on service disconnected");
            mService = null;
        }
    };

    private IDataRefreshCallback mCallback = new IDataRefreshCallback() {
        @Override
        public void onDataChanged(String changedDataKey) {
            Log.d(LOG_TAG,"onDataChanged : " + changedDataKey);
            User connectedUser = mDB.getConnectedUser();
            switch(changedDataKey) {
                case Constants.GENDER_CHANGED_CALLBACK:
                    updateBMR(connectedUser.getGender(), connectedUser.getWeight(), connectedUser.getHeight(), connectedUser.getYearOfBirth());
                    updateIdealBodyWeight(connectedUser.getGender(), connectedUser.getHeight());
                    break;
                case Constants.WEIGHT_CHANGED_CALLBACK:
                    updateBMI(connectedUser.getWeight(), connectedUser.getHeight());
                    updateBMR(connectedUser.getGender(), connectedUser.getWeight(), connectedUser.getHeight(), connectedUser.getYearOfBirth());
                    updateWaterRequired(connectedUser.getWeight());
                    updateWeight(connectedUser.getWeight());
                    break;
                case Constants.YOB_CHANGED_CALLBACK:
                    Calendar calendar = Calendar.getInstance();
                    int currentYear = calendar.get(Calendar.YEAR);
                    Log.d(LOG_TAG, "currentYear : " + currentYear);
                    updateBMR(connectedUser.getGender(), connectedUser.getWeight(), connectedUser.getHeight(), connectedUser.getYearOfBirth());
                    updateAge(connectedUser.getYearOfBirth());
                    break;
                case Constants.HEIGHT_CHANGED_CALLBACK:
                    updateBMI(connectedUser.getWeight(), connectedUser.getHeight());
                    updateBMR(connectedUser.getGender(), connectedUser.getWeight(), connectedUser.getHeight(), connectedUser.getYearOfBirth());
                    updateIdealBodyWeight(connectedUser.getGender(), connectedUser.getHeight());
                    updateHeight(connectedUser.getHeight());
                    break;
            }
        }
    };

    private void updateWeight(float weight) {
        Log.d(LOG_TAG, "updateWeight weight : " + weight);
        tvDrWeightUM.setText(weight + " kg");
    }

    private void updateAge(int yearOfBirth) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        Log.d(LOG_TAG, "currentYear : " + currentYear);
        Log.d(LOG_TAG, "updateAge age : " + (currentYear - yearOfBirth));
        tvDrAgeUM.setText((currentYear - yearOfBirth) + " yrs");
    }

    private void updateHeight(int height) {
        Log.d(LOG_TAG, "updateHeight height : " + height);
        tvDrHeightUM.setText(height + " cm");

    }

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

        Log.d(LOG_TAG, "DoctorFragment");
        mDB = UsersDB.getInstance(mActivity.getApplicationContext());
        mDB.registerCallback(mCallback);

        setup(view);
    }

    private void updateBMR(String gender, float weight, int height, int yob) {
        double BMR = 0;
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int age = currentYear - yob;
        Log.d(LOG_TAG, "updateBMR : " + gender + " >> weight : " + weight + " >> height : " + height + " >> age : " + age);

        if(gender.equalsIgnoreCase("Male")) {
            BMR = 66.47 + (13.75*weight) + (5*height) - (6.75 * age);
            Log.d(LOG_TAG, "updateBMR Male : " + BMR);
        } else if (gender.equalsIgnoreCase("Female")) {
            BMR = 665.09 + (9.56 * weight) + (1.84 * height) - (4.67 * age);
            Log.d(LOG_TAG, "updateBMR Female : " + BMR);
        } else {
            Log.d(LOG_TAG, "updateBMR ERROR");
        }
        BMR = Math.round(BMR * 10d) / 10d;
        Log.d(LOG_TAG, "updateBMR BMR : " + BMR);

        listRowBMRSubtitle.setText(String.valueOf(BMR) + " cal/day");
    }


    // formula : weight / height ^ 2
    private void updateBMI(float weight, int height) {
        Log.d(LOG_TAG, "updateBMI");
        float mHeightMeters = (float)height / 100;
        double mBMI =  (weight / (mHeightMeters * mHeightMeters));
        mBMI = Math.round(mBMI * 10d) / 10d;
        Log.d(LOG_TAG, "height : " + height + " >>> mHeightMeters : " + mHeightMeters + " >>> weight : " + weight + " >>> mBMI : " + mBMI);

        listRowBMISubtitle.setText(String.valueOf(mBMI));

        float normalBMIAverage = 21.75f;
        float mWeightGoal = (mHeightMeters * mHeightMeters) * normalBMIAverage;
        mWeightGoal = Math.round(mWeightGoal * 10f) / 10f;
        Log.d(LOG_TAG, "mWeightGoal : " + mWeightGoal);
        //tvAverageWeight.setText(mWeightGoal + " kg");
    }

    private void updateIdealBodyWeight(String gender, int height) {
        Log.d(LOG_TAG, "updateIdealBodyWeight gender : " + gender + " >> height : " + height);
        mIdealBodyWeight = 0.0;
        if(gender.equalsIgnoreCase("Male")) {
            mIdealBodyWeight = 0.9 * height - 88;
            Log.d(LOG_TAG, "updateIdealBodyWeight Male : " + mIdealBodyWeight);
        } else if (gender.equalsIgnoreCase("Female")) {
            mIdealBodyWeight = 0.9 * height - 92;
            Log.d(LOG_TAG, "updateIdealBodyWeight Female : " + mIdealBodyWeight);
        } else {
            Log.d(LOG_TAG, "updateIdealBodyWeight ERROR");
        }
        mIdealBodyWeight = Math.round(mIdealBodyWeight * 100d) / 100d;
        Log.d(LOG_TAG, "updateIdealBodyWeight BMR : " + mIdealBodyWeight);
        listRowIdealBodyWeightSubtitle.setText(String.valueOf(mIdealBodyWeight) + " kg");
    }

    private void updateWaterRequired(float weight) {
        Log.d(LOG_TAG, "updateWaterRequired weight : " + weight);
        double mWaterRequired = weight / 30;
        mWaterRequired = Math.round(mWaterRequired * 10d) / 10d;
        listRowWaterSubtitle.setText(String.valueOf(mWaterRequired) + " litres");
    }

    public void setup(View view) {
        tvDrHeightUM = (TextView) view.findViewById(R.id.tvDrHeightUM);
        tvDrAgeUM = (TextView) view.findViewById(R.id.tvDrAgeUM);
        tvDrWeightUM = (TextView) view.findViewById(R.id.tvDrWeightUM);

        User connectedUser = mDB.getConnectedUser();

        initUserData(connectedUser);
        setupListRows(view, connectedUser);
    }

    private void initUserData(User connectedUser) {
        Log.d(LOG_TAG, "initUserData");

        int mHeight = connectedUser.getHeight();
        Log.d(LOG_TAG, "mHeight : " + mHeight);
        tvDrHeightUM.setText(mHeight + " cm");

        float mWeight = connectedUser.getWeight();
        Log.d(LOG_TAG, "mWeight : " + mWeight);
        tvDrWeightUM.setText(mWeight + " kg");

        int yob = connectedUser.getYearOfBirth();
        Log.d(LOG_TAG, "yob : " + yob);
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        Log.d(LOG_TAG, "currentYear : " + currentYear);
        Log.d(LOG_TAG, "age : " + (currentYear - yob));
        tvDrAgeUM.setText((currentYear - yob) + " yrs");
    }


    private void setupListRows(View view, User connectedUser) {
        final float weight = connectedUser.getWeight();
        int height = connectedUser.getHeight();
        String gender = connectedUser.getGender();
        int yearOfBirth = connectedUser.getYearOfBirth();
        final float weightGoal = connectedUser.getWeightGoal();
        double waterRequired = weight / 30;
        waterRequired = Math.round(waterRequired * 10d) / 10d;
        final double finalWaterRequired = waterRequired;


        listRowBMI = view.findViewById(R.id.listRowBMI);
        listRowBMITitle = (TextView) listRowBMI.findViewById(R.id.listRowTitle);
        listRowBMISubtitle = (TextView) listRowBMI.findViewById(R.id.listRowSubtitle);
        listRowBMIInfo = (ImageView) listRowBMI.findViewById(R.id.listRowInfo);
        listRowBMIIcon = (ImageView) listRowBMI.findViewById(R.id.listRowIcon);

        listRowBMITitle.setText("Body Mass Index");
        updateBMI(connectedUser.getWeight(), connectedUser.getHeight());
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
        updateBMR(gender, weight, height, yearOfBirth);
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
        updateWaterRequired(weight);

        listRowWaterIcon.setImageResource(R.drawable.water);
        listRowWater.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "listRowWater onClick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View theView = inflater.inflate(R.layout.dialog_water_required_info, null, false);
                TextView waterRequiredInfoLitres = (TextView) theView.findViewById(R.id.waterRequiredInfoLitres);
                waterRequiredInfoLitres.setText(String.valueOf(finalWaterRequired));
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
        updateIdealBodyWeight(gender, height);
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
                        mDB.updateWeightGoal(mIdealBodyWeight);
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
        listRowCalorieNeedsSubtitle.setText(String.valueOf(weight * 35));
        listRowCalorieNeedsIcon.setImageResource(R.drawable.calories);
        listRowCalorieNeeds.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "listRowCalorieNeeds onClick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View theView = inflater.inflate(R.layout.dialog_calories_needed_info, null, false);

                TextView sedentaryValue = (TextView) theView.findViewById(R.id.tvSedentaryValue);
                double sedentaryValueCal = weight * 31;
                sedentaryValueCal = Math.round(sedentaryValueCal * 100d) / 100d;
                sedentaryValue.setText(String.valueOf(sedentaryValueCal) + " Cal");

                TextView lightlyActiveValue = (TextView) theView.findViewById(R.id.tvLightActiveValue);
                double lightlyActiveCal = weight * 35;
                lightlyActiveCal = Math.round(lightlyActiveCal * 100d) / 100d;
                lightlyActiveValue.setText(String.valueOf(lightlyActiveCal) + " Cal");

                TextView moderatelyActiveValue = (TextView) theView.findViewById(R.id.tvModActiveValue);
                double moderatelyActiveCal = weight * 40;
                moderatelyActiveCal = Math.round(moderatelyActiveCal * 100d) / 100d;
                moderatelyActiveValue.setText(String.valueOf(moderatelyActiveCal) + " Cal");

                TextView veryActiveValue = (TextView) theView.findViewById(R.id.tvVeryActiveValue);
                double veryActiveCal = weight * 45;
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
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        Log.d(LOG_TAG, "setUserVisibleHint() isVisibleToUser : " + isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            // If we are becoming invisible, then...
            if (!isVisibleToUser) {
                Log.d(LOG_TAG, "Not visible anymore.");
                // TODO stop audio playback
            }
        }
    }
}
