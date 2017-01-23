package com.master.aluca.fitnessmd.common.bluetooth;

/**
 * Created by aluca on 11/8/16.
 */
import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;

/**
 * ContentObject holds accelerometer data at specified time.
 * @author Administrator
 *
 */
public class ContentObject {
    private static final String LOG_TAG = "Fitness_ContentObj";

    public static final int DATA_COUNT = 20;

    private int[] mAccelData = null;
    private int mAccelIndex = 0;
    private int mCacheIndex = 0;


    public ContentObject() {

        mAccelData = new int[DATA_COUNT*3];		// DATA_COUNT * 3 axis
        Arrays.fill(mAccelData, 0x00000000);
        mAccelIndex = 0;
        mCacheIndex = 0;
    }


    /*****************************************************
     *	Public methods
     ******************************************************/

    public void setAccelData(int x_axis, int y_axis, int z_axis) {
        if(mAccelData != null && mAccelIndex > -1 && mAccelIndex < mAccelData.length / 3) {
            mAccelData[mAccelIndex] = x_axis;
            mAccelData[mAccelIndex+1] = y_axis;
            mAccelData[mAccelIndex+2] = z_axis;
            mAccelIndex++;
        }
    }

    public int[] getAccelData() {
        return mAccelData;
    }

    public int getAccelIndex() {
        return mAccelIndex;
    }

    public void setAccelData(int data) {
        Log.d(LOG_TAG, "setAccelData");
        if(mAccelData != null && mAccelIndex > -1 && mAccelIndex < DATA_COUNT) {
            mAccelData[mAccelIndex*3 + mCacheIndex] = data;
            mCacheIndex++;
            if(mCacheIndex == 3) {
                //Logs.d("# Accel = "+mAccelData[mAccelIndex*3 + mCacheIndex - 3]+", "+mAccelData[mAccelIndex*3 + mCacheIndex - 2]+", "+mAccelData[mAccelIndex*3 + mCacheIndex - 1]);
                mAccelIndex++;
                mCacheIndex = 0;
            }
        }
    }

}

