/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.ui.fragments.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.datatypes.StepsDayReport;
import com.master.aluca.fitnessmd.common.datatypes.User;
import com.master.aluca.fitnessmd.common.datatypes.WeightDayReport;
import com.master.aluca.fitnessmd.common.util.IDataRefreshCallback;
import com.master.aluca.fitnessmd.common.util.IStatsChanged;
import com.master.aluca.fitnessmd.common.util.ProfilePhotoUtils;
import com.master.aluca.fitnessmd.common.util.UsersDB;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;
import com.master.aluca.fitnessmd.service.FitnessMDService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String LOG_TAG = "Fitness_ProfileFragment";

    private Activity mActivity;
    private UsersDB mDB;
    static TextView tvName, tvGender, tvAge, tvHeight;
    TextView tvPersonalBestSteps, tvPersonalBestWeight;
    TextView tvPersonalBestStepsDate, tvPersonalBestWeightDate;
    TextView tvAverageSteps, tvAverageWeight;
    ImageView imageViewProfile;

    private static Bitmap image = null;
    private static Bitmap rotateImage = null;


    private WebserverManager webserverManager;



    int personalBestSteps, personalBestWeight;
    int averageSteps, averageWeight;
    private Bitmap profilePicture;

    private Dialog mDialog;

    private FitnessMDService mService;
    SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");



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
        if (webserverManager != null) {
            webserverManager.registerStatsCallback(mStatsChangedCallback);
        }

    }

    private IStatsChanged mStatsChangedCallback = new IStatsChanged() {
        @Override
        public void onTotalStepsChanged(int totalSteps) {
        }

        @Override
        public void onMaxStepsChanged(long dayMillis, int maxSteps) {
            Log.d(LOG_TAG,"IStatsChanged onTotalStepsChanged onMaxStepsChanged : " + maxSteps);
            tvPersonalBestSteps.setText(String.valueOf(maxSteps) + " steps");
            String day = dateFormat.format(new Date(dayMillis));
            tvPersonalBestStepsDate.setText(day);
        }

        @Override
        public void onAverageStepsChanged(int averageSteps) {
            Log.d(LOG_TAG,"IStatsChanged onAverageStepsChanged averageSteps : " + averageSteps);
            tvAverageSteps.setText(String.valueOf(averageSteps) + " steps");
        }

        @Override
        public void onLast7DaysStats(HashMap<Long, Integer> last7DaysStats) {
        }


    };

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
        return inflater.inflate(R.layout.layout_tab_profile, container, false);
    }


    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        View view = getView();
        if (view == null) {
            return;
        }

        Log.d(LOG_TAG,"ProfileFragment");
        mDB = UsersDB.getInstance(getActivity());
        mDB.registerCallback(mCallback);
        webserverManager = WebserverManager.getInstance(getActivity());

        setup(view);
    }


    /*
        When the user sets Gender, Year of birth or Height from the Settings menu
        the Profile tab does not get updated unless this callback is called.
     */
    private IDataRefreshCallback mCallback = new IDataRefreshCallback() {
        @Override
        public void onDataChanged(String changedDataKey) {
            Log.d(LOG_TAG,"onDataChanged : " + changedDataKey);
            User connectedUser = mDB.getConnectedUser();
            switch(changedDataKey) {
                case Constants.GENDER_CHANGED_CALLBACK:
                    updateGenderTextView(connectedUser.getGender());
                    break;
                case Constants.YOB_CHANGED_CALLBACK:
                    updateAgeTextView(connectedUser.getYearOfBirth());
                    break;
                case Constants.HEIGHT_CHANGED_CALLBACK:
                    updateHeightTextView(connectedUser.getHeight());
                    break;
            }
        }
    };

    private void updateHeightTextView(int height) {
        Log.d(LOG_TAG, "height : " + height);
        tvHeight.setText(height + " cm");
    }

    private void updateAgeTextView(int yob) {
        Log.d(LOG_TAG, "yob : " + yob);
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        Log.d(LOG_TAG, "currentYear : " + currentYear);
        int age = currentYear - yob;
        Log.d(LOG_TAG, "age : " + age);
        tvAge.setText(age + " yrs");
    }

    private void updateGenderTextView(String gender) {
        if (gender != null) {
            Log.d(LOG_TAG, "gender : " + gender.toString());
            tvGender.setText(gender);
        } else {
            tvGender.setText("Gender Not set");
        }
    }

    public void setup(View view) {
        tvName =  (TextView) view.findViewById(R.id.tvName);
        tvGender =  (TextView) view.findViewById(R.id.tvGender);
        tvAge =  (TextView) view.findViewById(R.id.tvAge);
        tvHeight =  (TextView) view.findViewById(R.id.tvHeight);

        tvPersonalBestSteps =  (TextView) view.findViewById(R.id.tvPersonalBestSteps);
        tvPersonalBestWeight =  (TextView) view.findViewById(R.id.tvPersonalBestWeight);

        tvPersonalBestStepsDate = (TextView) view.findViewById(R.id.tvPersonalBestStepsDate);
        tvPersonalBestWeightDate = (TextView) view.findViewById(R.id.tvPersonalBestWeightDate);

        tvAverageSteps =  (TextView) view.findViewById(R.id.tvAverageSteps);
        tvAverageWeight =  (TextView) view.findViewById(R.id.tvAverageWeight);

        imageViewProfile = (ImageView) view.findViewById(R.id.imageViewProfile);
        imageViewProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "imageView on click");
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
                builder.setTitle("Profile picture");
                builder.setPositiveButton("Choose photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOG_TAG, "Choose from gallery");
                        imageViewProfile.setImageBitmap(null);
                        if (image != null) {
                            image.recycle();
                        }
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                        getActivity().startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.GET_GALLERY_IMAGE);
                    }
                }).setNegativeButton("Take photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOG_TAG, "Take photo click");
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        getActivity().startActivityForResult(cameraIntent, Constants.TAKE_PHOTO);
                    }
                });
                AlertDialog levelDialog = builder.create();
                levelDialog.show();
            }
        });

        initUserData();
        syncData();
    }



    private void initUserData() {
        Log.d(LOG_TAG,"initUserData");

        User connectedUser = mDB.getConnectedUser();
        String name = connectedUser.getName();
        if (name != null) {
            Log.d(LOG_TAG, "name : " + name.toString());
            tvName.setText(name);
        } else {
            tvName.setText("Name Not set");
        }

        updateGenderTextView(connectedUser.getGender());

        updateAgeTextView(connectedUser.getYearOfBirth());

        updateHeightTextView(connectedUser.getHeight());

        updateProfilePicture(connectedUser.getProfilePictureURI());
    }

    private void updateProfilePicture(String profilePictureUri) {
        Log.d(LOG_TAG, "updateProfilePicture");
        Log.d(LOG_TAG, "profilePictureUri : " + profilePictureUri);
        if (profilePictureUri != null) {
            Bitmap profilePhoto = ProfilePhotoUtils.getProfilePicFromGallery(getActivity().getContentResolver(), Uri.parse(profilePictureUri));
            setProfilePicture(profilePhoto);
        } else {
            setDefaultProfilePicture();
        }
    }

    /*
        Whenever this tab is selected, the UI must update with the latest achievements.
     */
    public void syncData() {

        String day;
        HashMap<Long, Integer> maxSteps = mDB.getBestSteps();
        if (maxSteps != null) {
            Log.d(LOG_TAG, "maxSteps.length " + maxSteps.size());
            if (maxSteps.size() == 0) {
                updateBestSteps(Constants.getStartOfCurrentDay(), 0);
            } else if (maxSteps.size() > 1) {
                Log.d(LOG_TAG,"error max steps");
            } else {
                for (Map.Entry<Long,Integer> entry : maxSteps.entrySet()) {
                    updateBestSteps(entry.getKey(), entry.getValue());
                }
            }
        } else {
            updateBestSteps(Constants.getStartOfCurrentDay(), 0);
        }



        WeightDayReport weightBestReport = webserverManager.getBestWeight();
        float weight = weightBestReport.getWeight();
        tvPersonalBestWeight.setText(String.valueOf(weight) + " kg");

        day = dateFormat.format(new Date(weightBestReport.getDay()));
        Log.d(LOG_TAG,"day : " + day);
        tvPersonalBestWeightDate.setText(day);

        WeightDayReport averageWeightRaport = webserverManager.getAverageWeight();
        float averageWeight = averageWeightRaport.getWeight();
        tvAverageWeight.setText(String.valueOf(averageWeight) + " kg");

    }

    public void setProfilePicture(Bitmap profilePicture) {
        imageViewProfile.setImageBitmap(profilePicture);
    }

    public void setDefaultProfilePicture() {
        imageViewProfile.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.user));
        //imageViewProfile.setImageResource(mContext.getResources().getDrawable(R.drawable.user));
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        Log.d(LOG_TAG, "setUserVisibleHint() isVisibleToUser : " + isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            // If we are becoming invisible, then...
            HashMap<Long, Integer> maxSteps = mDB.getBestSteps();
            if (maxSteps != null) {
                Log.d(LOG_TAG, "maxSteps.length " + maxSteps.size());
                if (maxSteps.size() == 0) {
                    updateBestSteps(Constants.getStartOfCurrentDay(), 0);
                } else if (maxSteps.size() > 1) {
                    Log.d(LOG_TAG, "error max steps");
                } else {
                    for (Map.Entry<Long, Integer> entry : maxSteps.entrySet()) {
                        updateBestSteps(entry.getKey(), entry.getValue());
                    }
                }
            } else {
                updateBestSteps(Constants.getStartOfCurrentDay(), 0);
            }

            if (!isVisibleToUser) {
                Log.d(LOG_TAG, "Not visible anymore.");
                // TODO stop audio playback
            }
        }
    }

    private void updateBestSteps(long dayLong, int steps) {
        Log.d(LOG_TAG, "updateBestSteps day : " + dayLong + " >> steps : " + steps);
        tvPersonalBestSteps.setText(String.valueOf(steps) + " steps");
        String day = dateFormat.format(new Date(dayLong));
        tvPersonalBestStepsDate.setText(day);

    }
}
