package com.master.aluca.fitnessmd.common.bluetooth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.text.format.Time;
import android.util.Log;


public class ContentManager {

    private static final String LOG_TAG = "Fitness_ContentManager";


    private static ContentManager mContentManager = null;		// Singleton pattern

    private Context mContext;
    private DBHelper mDB = null;

    private ArrayList<ContentObject> mContentList;		// Cache content objects

    // Time parameters
    private static final int REPORT_INTERVAL = 1000;
    private static final int REPORT_SAMPLING_TIME = 50;
    private long mPreviousProcessTime = 0;

    /**
     * Constructor
     */
    private ContentManager(Context context) {
        Log.d(LOG_TAG, "ContentManager");
        mContext = context;

        mContentList = new ArrayList<>();

        //----- Make DB helper
        if(mDB == null) {
            mDB = new DBHelper(mContext).openWritable();
        }
    }

    /**
     * Singleton pattern
     */
    public synchronized static ContentManager getInstance(Context context) {
        Log.d(LOG_TAG, "getInstance");
        if(mContentManager == null)
            mContentManager = new ContentManager(context);

        return mContentManager;
    }

    public synchronized void finalize() {
        Log.d(LOG_TAG, "finalize");
        if(mDB != null) {
            mDB.close();
            mDB = null;
        }
        if(mContentList != null)
            mContentList.clear();
        mContentManager = null;
    }


    /*****************************************************
     *	Public methods
     ******************************************************/

    /**
     * After parsing packets from remote, service calls this method with result object.
     * This method analyze accel raw data and calculate walks, calories.
     * And makes an activity report instance which has analyzed results.
     * @param co		content object which has accel raw data array
     * @return			activity report instance which has analyzed results.
     */
    public synchronized ActivityReport addContentObject(ContentObject co) {
        Log.d(LOG_TAG, "addContentObject");
        if(co == null) {
            return null;
        }

        // Caching contents
        mContentList.add(co);

        // Get current time
        long currentTime = System.currentTimeMillis();
        if(mPreviousProcessTime < 1)
            mPreviousProcessTime = currentTime;

        // Analyze cached contents
        ActivityReport ar = null;
        if(currentTime - mPreviousProcessTime > REPORT_INTERVAL) {
            Log.d(LOG_TAG,"#");
            Log.d(LOG_TAG,"# before analyzer");
            // Analyze accelerometer value and make report
            ar = Analyzer.analyzeAccel(mContentList, REPORT_SAMPLING_TIME, REPORT_INTERVAL);

            mPreviousProcessTime = currentTime;
            mContentList.clear();
        }

        return ar;
    }

}
