/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.common.bluetooth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.master.aluca.fitnessmd.common.datatypes.StepsDayReport;
import com.master.aluca.fitnessmd.common.datatypes.WeightDayReport;

import java.util.ArrayList;
import java.util.Date;


public class DBHelper_not_used {

    private static final String LOG_TAG  ="Fitness_Stat_DBHelper";

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FitnessMD_DB";

    public static final String TABLE_NAME_ACCEL_REPORT = "statistics";

    public static final int INDEX_ACCEL_MONTH = 4;
    public static final int INDEX_ACCEL_DAY = 5;
    public static final int INDEX_ACCEL_HOUR = 6;
    public static final int INDEX_ACCEL_DATA1 = 9;

    private static final String DATABASE_CREATE_ACCEL_TABLE =
            "CREATE TABLE " +TABLE_NAME_ACCEL_REPORT+ "("
            + "_id Integer primary key autoincrement, "// int		primary key, auto increment
            + "date Integer not null, "// time in milliseconds
            + "steps Integer not null, "// integer
            + "weight real, "// float
            + "calories Integer, "//
            + "timeActive Integer, "// time in milliseconds
            + "wasPushedToServer integer)";// boolean values. 1 = true, 0 = false
    private static final String DATABASE_DROP_ACCEL_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_ACCEL_REPORT;
    //----------- End of Accel data table parameters




    // Context, System
    private final Context mContext;
    private SQLiteDatabase mDb;
    private DatabaseHelper mDbHelper;
    private WeightDayReport averageWeight;

    // Constructor
    public DBHelper_not_used(Context context) {
        this.mContext = context;
    }


    //----------------------------------------------------------------------------------
    // Public classes
    //----------------------------------------------------------------------------------
    // DB open (Writable)
    public DBHelper_not_used openWritable() throws SQLException {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    // Terminate DB
    public void close() {
        if(mDb != null) {
            mDb.close();
            mDb = null;
        }
        if(mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
        }
    }

    //----------------------------------------------------------------------------------
    // INSERT
    //----------------------------------------------------------------------------------
    public long insertActivityReport(long dayForReport, int steps, float weight,
                                     int calories, long timeActive, int wasPushedToServer) throws SQLiteConstraintException, SQLException {
        if (dayForReport < 1) {
            return ErrorCodes.INVALID_TIME;
        }
        if (weight < 1) {
            return ErrorCodes.INVALID_WEIGHT;
        }

        ContentValues insertValues = new ContentValues();
        insertValues.put("date", dayForReport);
        insertValues.put("steps",steps);
        insertValues.put("weight",weight);
        insertValues.put("calories",calories);
        insertValues.put("timeActive",timeActive);
        insertValues.put("wasPushedToServer",wasPushedToServer);

        Log.d(LOG_TAG, "+ Insert activity report : mDate="+(new Date(dayForReport))+", steps="+steps
                +", weight="+weight+", calories="+calories+", timeActive="+timeActive + ", wasPushedToServer=" + wasPushedToServer);

        synchronized (mDb) {
            if(mDb == null)
                return ErrorCodes.GENERAL_ERROR;
            return mDb.insertOrThrow(TABLE_NAME_ACCEL_REPORT, null, insertValues);
        }
    }

    public ArrayList<StepsDayReport> getLastWeekReport(long dateNow) {
        Log.d(LOG_TAG, "dateNow : " + dateNow);
        Log.d(LOG_TAG, "date : " + (new Date(dateNow)));
        synchronized (mDb) {
            if(mDb == null)
                return null;
            String query = "SELECT date, steps FROM " + TABLE_NAME_ACCEL_REPORT + " WHERE date<"+dateNow+" ORDER BY date DESC LIMIT 7";
            Cursor cursor = mDb.rawQuery(query, null);
            ArrayList<StepsDayReport> stepsDayReports = new ArrayList<>();
            if (cursor.moveToFirst()) {
                System.out.println("getLastWeekReport CURSOR IS MOVING");
                do {
                    StepsDayReport stepsDayReport = new StepsDayReport();
                    stepsDayReport.setDay(cursor.getLong(cursor.getColumnIndex("date")));
                    stepsDayReport.setSteps(cursor.getInt(cursor.getColumnIndex("steps")));
                    Log.d(LOG_TAG, "day : " + stepsDayReport.getDay());
                    Log.d(LOG_TAG,"steps : " + stepsDayReport.getSteps());

                    stepsDayReports.add(stepsDayReport);
                } while (cursor.moveToNext());

            } else {
                System.out.println("getLastWeekReport EROARE LA CURSOR");
                return stepsDayReports;
            }
            return stepsDayReports;
            //return mDb.insertOrThrow(TABLE_NAME_ACCEL_REPORT, null, insertValues);
        }
    }

    public StepsDayReport getBestSteps() {
        int bestSteps = -1;
        long date = System.currentTimeMillis();
        StepsDayReport stepsDayReport = null;
        synchronized (mDb) {
            if(mDb == null)
                return null;
            String query = "SELECT MAX(steps) as bestSteps,date FROM " + TABLE_NAME_ACCEL_REPORT + ";";
            Cursor cursor = mDb.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                Log.d(LOG_TAG, "getBestSteps CURSOR IS MOVING");
                do {

                    bestSteps = cursor.getInt(cursor.getColumnIndex("bestSteps"));
                    date = cursor.getLong(cursor.getColumnIndex("date"));
                    Log.d(LOG_TAG, "bestSteps : " + bestSteps);
                    Log.d(LOG_TAG, "date : " + date);
                    stepsDayReport = new StepsDayReport(bestSteps, date);
                } while (cursor.moveToNext());

            } else {
                System.out.println("getBestSteps EROARE LA CURSOR");
                return null;
            }
            return stepsDayReport;
        }
    }

    public WeightDayReport getBestWeight() {
        float bestWeight = -1;
        long date = System.currentTimeMillis();
        WeightDayReport weightDayReport = null;
        synchronized (mDb) {
            if(mDb == null)
                return null;
            String query = "SELECT MIN(weight) as bestWeight,date FROM " + TABLE_NAME_ACCEL_REPORT + ";";
            Cursor cursor = mDb.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                System.out.println("getBestWeight CURSOR IS MOVING");
                do {

                    bestWeight = cursor.getFloat(cursor.getColumnIndex("bestWeight"));
                    date = cursor.getLong(cursor.getColumnIndex("date"));
                    Log.d(LOG_TAG, "bestWeight : " + bestWeight);
                    Log.d(LOG_TAG, "date : " + date);
                    weightDayReport = new WeightDayReport(bestWeight, date);
                } while (cursor.moveToNext());

            } else {
                System.out.println("getBestWeight EROARE LA CURSOR");
                return null;
            }
            return weightDayReport;
        }
    }

    public StepsDayReport getAverageSteps() {
        int avgSteps = -1;
        long date = System.currentTimeMillis();
        StepsDayReport averageStepsRaport = null;
        synchronized (mDb) {
            if(mDb == null)
                return null;
            String query = "SELECT AVG(steps) as avgSteps FROM " + TABLE_NAME_ACCEL_REPORT + ";";
            Cursor cursor = mDb.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                System.out.println("getAverageSteps CURSOR IS MOVING");
                do {

                    avgSteps = (int)cursor.getFloat(cursor.getColumnIndex("avgSteps"));
                    Log.d(LOG_TAG, "avgSteps : " + avgSteps);
                    Log.d(LOG_TAG, "date : " + date);
                    averageStepsRaport = new StepsDayReport(avgSteps, date);
                } while (cursor.moveToNext());

            } else {
                System.out.println("getAverageSteps EROARE LA CURSOR");
                return null;
            }
            return averageStepsRaport;
        }
    }

    public WeightDayReport getAverageWeight() {
        float avgWeight = -1;
        long date = System.currentTimeMillis();
        WeightDayReport averageWeightRaport = null;
        synchronized (mDb) {
            if(mDb == null)
                return null;
            String query = "SELECT AVG(weight) as avgWeight FROM " + TABLE_NAME_ACCEL_REPORT + ";";
            Cursor cursor = mDb.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                Log.d(LOG_TAG, "getAverageWeight CURSOR IS MOVING");
                do {

                    avgWeight = cursor.getFloat(cursor.getColumnIndex("avgWeight"));
                    Log.d(LOG_TAG, "avgWeight : " + avgWeight);
                    Log.d(LOG_TAG, "date : " + date);
                    averageWeightRaport = new WeightDayReport(avgWeight, date);
                } while (cursor.moveToNext());

            } else {
                System.out.println("getAverageWeight EROARE LA CURSOR");
                return null;
            }
            return averageWeightRaport;
        }
    }



    public StepsDayReport getTotalSteps() {
        int totalSteps = -1;
        long date = System.currentTimeMillis();
        StepsDayReport totalStepsRaport = null;
        synchronized (mDb) {
            if(mDb == null)
                return null;
            String query = "SELECT SUM(steps) as totalSteps FROM " + TABLE_NAME_ACCEL_REPORT + ";";
            Cursor cursor = mDb.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                System.out.println("getTotalSteps CURSOR IS MOVING");
                do {

                    totalSteps = (int)cursor.getFloat(cursor.getColumnIndex("totalSteps"));
                    Log.d(LOG_TAG, "totalSteps : " + totalSteps);
                    Log.d(LOG_TAG, "date : " + date);
                    totalStepsRaport = new StepsDayReport(totalSteps, date);
                } while (cursor.moveToNext());

            } else {
                System.out.println("getTotalSteps EROARE LA CURSOR");
                return null;
            }
            return totalStepsRaport;
        }
    }




    //----------------------------------------------------------------------------------
    // SELECT methods
    //----------------------------------------------------------------------------------

    public Cursor selectReportWithDate(int type, int year, int month, int day, int hour) {
        /*synchronized (mDb) {
            if(mDb == null) return null;

            StringBuilder sb = new StringBuilder();
            sb.append(KEY_ACCEL_TYPE).append("=").append(type);
            sb.append(" AND ").append(KEY_ACCEL_YEAR).append("=").append(year);

            if(month > -1 && month < 12) {
                sb.append(" AND ").append(KEY_ACCEL_MONTH).append("=").append(month);
            }
            if(day > -1 && day < 31) {
                sb.append(" AND ").append(KEY_ACCEL_DAY).append("=").append(day);
            }
            if(hour > -1 && hour < 24) {
                sb.append(" AND ").append(KEY_ACCEL_HOUR).append("=").append(hour);
            }
            return mDb.query(
                    TABLE_NAME_ACCEL_REPORT,		// Table : String
                    null,							// Columns : String[]
                    sb.toString(),		// Selection 	: String
                    null,			// Selection arguments: String[]
                    null,			// Group by 	: String
                    null,			// Having 		: String
                    KEY_ACCEL_ID+" DESC",			// Order by 	: String
                    null );		// Limit		: String
        }*/
        return null;
    }

    //----------------------------------------------------------------------------------
    // Update methods
    //----------------------------------------------------------------------------------
/*
	public int updateFilter(FilterObject filter)
	{
		if(filter.mType < 0 || filter.mCompareType < 0
				|| filter.mOriginalString == null || filter.mOriginalString.length() < 1)
			return -1;

		ContentValues insertValues = new ContentValues();
		insertValues.put(KEY_FILTER_TYPE, filter.mType);
		insertValues.put(KEY_FILTER_ICON_TYPE, filter.mIconType);
		insertValues.put(KEY_FILTER_MATCHING, filter.mCompareType);
		insertValues.put(KEY_FILTER_REPLACE_TYPE, filter.mReplaceType);
		insertValues.put(KEY_FILTER_ORIGINAL, filter.mOriginalString);
		insertValues.put(KEY_FILTER_REPLACE, filter.mReplaceString);
//		insertValues.put(KEY_FILTER_ARG0, 0);		// for future use
//		insertValues.put(KEY_FILTER_ARG1, 0);
//		insertValues.put(KEY_FILTER_ARG2, "");
//		insertValues.put(KEY_FILTER_ARG3, "");

		synchronized (mDb) {
			if(mDb == null)
				return -1;
			return mDb.update( TABLE_NAME_FILTERS,		// table
					insertValues, 		// values
					KEY_FILTER_ID + "='" + filter.mId + "'", // whereClause
					null ); 			// whereArgs
		}
	}
*/

    //----------------------------------------------------------------------------------
    // Delete methods
    //----------------------------------------------------------------------------------

    public void deleteReportWithID(int id) {
        /*if(mDb == null) return;

        synchronized (mDb) {
            int count = mDb.delete(TABLE_NAME_ACCEL_REPORT,
                    KEY_ACCEL_ID + "=" + id, // whereClause
                    null); 			// whereArgs
            Log.d(LOG_TAG, "- Delete record : id="+id+", count="+count);
        }*/
    }

    public void deleteReportWithDate(int type, int year, int month, int day, int hour) {
        /*if(mDb == null) return;

        synchronized (mDb) {
            int count = mDb.delete(TABLE_NAME_ACCEL_REPORT,
                    KEY_ACCEL_TYPE + "=" + type
                            + " AND " + KEY_ACCEL_YEAR + "=" + Integer.toString(year)
                            + " AND " + KEY_ACCEL_MONTH + "=" + Integer.toString(month)
                            + " AND " + KEY_ACCEL_DAY + "=" + Integer.toString(day)
                            + " AND " + KEY_ACCEL_HOUR + "=" + Integer.toString(hour), // whereClause
                    null); 			// whereArgs
        }*/
    }

    //----------------------------------------------------------------------------------
    // Count methods
    //----------------------------------------------------------------------------------
    public int getReportCount() {
        String query = "select count(*) from " + TABLE_NAME_ACCEL_REPORT;
        Cursor c = mDb.rawQuery(query, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count;
    }

    public int getReportCountWithType(int type) {
        //String query = "select count(*) from " + TABLE_NAME_ACCEL_REPORT + " where " + KEY_ACCEL_TYPE + "=" + Integer.toString(type);
        //Cursor c = mDb.rawQuery(query, null);
        //c.moveToFirst();
        //int count = c.getInt(0);
       // c.close();
        return -1;
    }

    public int setActivityReportPushedToServer(long dayForReport) {
        if (dayForReport < 1) {
            return ErrorCodes.INVALID_TIME;
        }

        ContentValues updateValues = new ContentValues();
        updateValues.put("wasPushedToServer", 1);

        Log.d(LOG_TAG, "Update activity report : mDate="+(new Date(dayForReport)));

        synchronized (mDb) {
            if(mDb == null)
                return ErrorCodes.GENERAL_ERROR;
            int numberOfRowsAffected = mDb.update(TABLE_NAME_ACCEL_REPORT, updateValues, "date="+dayForReport, null);
            Log.d(LOG_TAG, "numberOfRowsAffected : " + numberOfRowsAffected);
            return numberOfRowsAffected;
        }
    }

    public int eraseAllData() {
        Log.d(LOG_TAG, "eraseAllData");
        synchronized (mDb) {
            if(mDb == null)
                return ErrorCodes.GENERAL_ERROR;
            int numberOfRowsAffected = mDb.delete(TABLE_NAME_ACCEL_REPORT, "1", null);
            Log.d(LOG_TAG, "numberOfRowsAffected : " + numberOfRowsAffected);
            return numberOfRowsAffected;
        }
    }


    //----------------------------------------------------------------------------------
    // SQLiteOpenHelper
    //----------------------------------------------------------------------------------
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        // Constructor
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Will be called one time at first access
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_ACCEL_TABLE);
        }

        // Will be called when the version is increased
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO: Keep previous data
            db.execSQL(DATABASE_DROP_ACCEL_TABLE);

            db.execSQL(DATABASE_CREATE_ACCEL_TABLE);
        }

    }	// End of class DatabaseHelper


    public static class ErrorCodes {
        public static final int GENERAL_ERROR = -1;
        public static final int INVALID_TIME = -2;
        public static final int INVALID_WEIGHT = -3;
    }

}
