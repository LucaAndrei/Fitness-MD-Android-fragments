package com.master.aluca.fitnessmd.common.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.datatypes.Device;
import com.master.aluca.fitnessmd.common.datatypes.User;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by andrei on 3/14/2017.
 */

public class UsersDB extends SQLiteOpenHelper {
    private static final String LOG_TAG  = "Fitness_UsersDB";

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FitnessMD_DB";
    public static final String TABLE_NAME_USERS= "users";
    public static final String TABLE_NAME_USERS_DATA= "users_data";

    private ArrayList<IDataRefreshCallback> mCallbackList = new ArrayList<>();

    // columns
    private static final String ID = "docID";
    private static final String EMAIL = "_id";
    private static final String PASSWORD = "password";
    private static final String NAME = "name";
    private static final String WEIGHT = "weight";
    private static final String WEIGHT_GOAL = "weightGoal";
    private static final String HEIGHT = "height";
    private static final String YEAR_OF_BIRTH = "yearOfBirth";
    private static final String GENDER = "gender";
    private static final String ALWAYS_ENABLE_BT = "alwaysEnableBT";
    private static final String SAVED_DEVICE_NAME = "savedDeviceName";
    private static final String SAVED_DEVICE_ADDRESS = "savedDeviceAddress";
    private static final String HAS_PROFILE_PICTURE = "hasProfilePicture";
    private static final String PROFILE_PICTURE_URI = "profilePictureURI";
    private static final String REGISTRATION_COMPLETE = "registrationComplete";
    private static final String HAS_DEVICE_CONNECTED = "hasDeviceConnected";
    private static final String IS_ONLINE = "isOnline";

    private static final String DATABASE_CREATE_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS " +TABLE_NAME_USERS+ "("
                    + "_id String primary key, "// int		primary key, auto increment
                    + "docID String, "
                    + "password String not null, "
                    + "name String not null, "
                    + "weight real not null, "
                    + "weightGoal real not null, "
                    + "height Integer not null, "
                    + "yearOfBirth Integer not null, "
                    + "gender String not null, "
                    + "alwaysEnableBT Integer not null, "
                    + "savedDeviceName String, "
                    + "savedDeviceAddress String, "
                    + "hasProfilePicture Integer not null, "
                    + "profilePictureURI String, "
                    + "registrationComplete Integer not null, "
                    + "hasDeviceConnected Integer not null, "
                    + "isOnline Integer not null)";

    private static final String USER_DATA_DOC_ID = "docID";
    private static final String USER_DATA_EMAIL = "_id";
    private static final String USER_DATA_BEST_STEPS_DAY = "best_steps_day";
    private static final String USER_DATA_BEST_STEPS_VALUE = "best_steps_value";
    private static final String USER_DATA_BEST_WEIGHT_DAY = "best_weight_day";
    private static final String USER_DATA_BEST_WEIGHT_VALUE = "best_weight_value";


    private static final String DATABASE_CREATE_USERS_DATA_TABLE =
            "CREATE TABLE IF NOT EXISTS " +TABLE_NAME_USERS_DATA+ "("
                    + "_id String primary key, "// int		primary key, auto increment
                    + USER_DATA_DOC_ID + " String, "
                    + USER_DATA_BEST_STEPS_DAY + " long, "
                    + USER_DATA_BEST_STEPS_VALUE  + " int, "
                    + USER_DATA_BEST_WEIGHT_DAY  + " long, "
                    + USER_DATA_BEST_WEIGHT_VALUE  + " float)";





    private static final String DATABASE_DROP_USERS_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_USERS;
    private static final String DATABASE_DROP_USERS_DATA_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_USERS_DATA;

    // Context, System
    private final Context mContext;
    private SQLiteDatabase mDb;
    private static UsersDB sInstance;
    private static String connectedUserEmail = "";
    private User connectedUser;


    // Constructor
    private UsersDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(LOG_TAG, "UsersDB constructor");
        this.mContext = context;
    }

    public static synchronized UsersDB getInstance(Context context) {
        Log.d(LOG_TAG, "UsersDB getInstance");
        if (sInstance == null) {
            sInstance = new UsersDB(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "UsersDB onCreate");
        db.execSQL(DATABASE_CREATE_USERS_TABLE);
        db.execSQL(DATABASE_CREATE_USERS_DATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        Log.d(LOG_TAG, "UsersDB onUpgrade");
        db.execSQL(DATABASE_DROP_USERS_TABLE);
        db.execSQL(DATABASE_DROP_USERS_DATA_TABLE);
        onCreate(db);
    }

    public boolean addUser(String name, String email, String hashedPassword) {
        Log.d(LOG_TAG, "addUser: " + email + " pass : " + hashedPassword + " >> name : " + name);
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(hashedPassword) || TextUtils.isEmpty(name)) {
            return false;
        }
        Cursor cursor = getWritableDatabase().query(TABLE_NAME_USERS, null, "_id" + "=?",
                new String[]{email},
                null, null, null);

        if (cursor != null) {
            int count = cursor.getCount();
            Log.d(LOG_TAG,"count : " + count);
            cursor.close();
            if (count > 0) return false;
        }

        ContentValues values = new ContentValues();
        values.put(EMAIL, email);
        values.put(PASSWORD, hashedPassword);
        values.put(NAME, name);
        values.put(WEIGHT,  Constants.WEIGHT_DEFAULT_VALUE);
        values.put(WEIGHT_GOAL,  Constants.WEIGHT_DEFAULT_VALUE);
        values.put(HEIGHT, Constants.HEIGHT_DEFAULT_VALUE);
        values.put(YEAR_OF_BIRTH, Constants.YOB_DEFAULT_VALUE);
        values.put(GENDER, "Male");
        values.put(ALWAYS_ENABLE_BT, 0);
        values.put(SAVED_DEVICE_NAME, (String) null);
        values.put(SAVED_DEVICE_ADDRESS, (String) null);
        values.put(HAS_PROFILE_PICTURE, 0);
        values.put(PROFILE_PICTURE_URI, (String) null);
        values.put(REGISTRATION_COMPLETE, 0);
        values.put(HAS_DEVICE_CONNECTED, 0);
        values.put(IS_ONLINE, 0);

        long ret = getWritableDatabase().insert(TABLE_NAME_USERS, null, values);
        if (ret > 0) {
            ContentValues user_data_values = new ContentValues();
            user_data_values.put(USER_DATA_EMAIL, email);
            getWritableDatabase().insert(TABLE_NAME_USERS_DATA, null, user_data_values);
        }

        Log.d(LOG_TAG, "ret : " + ret);

        return ret > 0;
    }

    public boolean updateWeight(float newWeight) {
        Log.d(LOG_TAG, "updateWeight newWeight: " + newWeight);
        if (newWeight < 35.0 || TextUtils.isEmpty(connectedUserEmail)) {
            Log.d(LOG_TAG,"problems with params");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(WEIGHT, newWeight);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(WEIGHT) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + WEIGHT +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        dispatchCallback(Constants.WEIGHT_CHANGED_CALLBACK);

        return ret == 1;
    }

    public boolean updateWeightGoal(double weightGoal) {
        Log.d(LOG_TAG, "updateWeightGoal weightGoal: " + weightGoal);
        if (weightGoal < 35.0 || TextUtils.isEmpty(connectedUserEmail)) {
            Log.d(LOG_TAG,"problems with params");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(WEIGHT_GOAL, weightGoal);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(WEIGHT_GOAL) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + WEIGHT_GOAL +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        dispatchCallback(Constants.WEIGHT_GOAL_CHANGED_CALLBACK);

        return ret == 1;
    }


    public boolean updateHeight(int newHeight) {
        Log.d(LOG_TAG, "updateHeight newHeight: " + newHeight);
        if (newHeight < Constants.HEIGHT_MIN_VALUE || TextUtils.isEmpty(connectedUserEmail) || newHeight > Constants.HEIGHT_MAX_VALUE) {
            Log.d(LOG_TAG,"problems with params");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(HEIGHT, newHeight);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(HEIGHT) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + HEIGHT +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        dispatchCallback(Constants.HEIGHT_CHANGED_CALLBACK);

        return ret == 1;
    }
    public boolean updateGender(String newGender) {
        Log.d(LOG_TAG, "updateGender newGender: " + newGender);
        if (TextUtils.isEmpty(newGender) || TextUtils.isEmpty(connectedUserEmail)) {
            Log.d(LOG_TAG,"problems with params");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(GENDER, newGender);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(GENDER) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + GENDER +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        dispatchCallback(Constants.GENDER_CHANGED_CALLBACK);

        return ret == 1;
    }
    public boolean updateYearOfBirth(int yearOfBirth) {
        Log.d(LOG_TAG, "updateYearOfBirth yearOfBirth: " + yearOfBirth);
        if (yearOfBirth < Constants.YOB_MIN_VALUE || yearOfBirth > Constants.YOB_MAX_VALUE
                || TextUtils.isEmpty(connectedUserEmail)) {
            Log.d(LOG_TAG,"problems with params");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(YEAR_OF_BIRTH, yearOfBirth);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(YEAR_OF_BIRTH) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + YEAR_OF_BIRTH +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        dispatchCallback(Constants.YOB_CHANGED_CALLBACK);

        return ret == 1;
    }

    public boolean updateAlwaysEnableBT(boolean newAlwaysEnableBT) {
        Log.d(LOG_TAG, "updateHeight newAlwaysEnableBT: " + newAlwaysEnableBT);
        if (TextUtils.isEmpty(connectedUserEmail)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(ALWAYS_ENABLE_BT, newAlwaysEnableBT ? 1 : 0);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(ALWAYS_ENABLE_BT) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + ALWAYS_ENABLE_BT +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        return ret == 1;
    }

    public boolean updateProfilePictureURI(String newProfilePictureURI) {
        Log.d(LOG_TAG, "updateHeight newProfilePictureURI: " + newProfilePictureURI);
        if (TextUtils.isEmpty(connectedUserEmail) || TextUtils.isEmpty(newProfilePictureURI)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(HAS_PROFILE_PICTURE, TextUtils.isEmpty(newProfilePictureURI) ? 0 : 1);
        values.put(PROFILE_PICTURE_URI, newProfilePictureURI);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(PROFILE_PICTURE_URI) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + PROFILE_PICTURE_URI +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        return ret == 1;
    }



    public boolean saveDevice(String deviceName, String deviceAddress) {
        Log.d(LOG_TAG, "saveDevice deviceName: " + deviceName + " >>> deviceAddress : " + deviceAddress);
        if (TextUtils.isEmpty(connectedUserEmail) || TextUtils.isEmpty(deviceName) || TextUtils.isEmpty(deviceAddress)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(SAVED_DEVICE_NAME, deviceName);
        values.put(SAVED_DEVICE_ADDRESS, deviceAddress);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(SAVED_DEVICE_NAME) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + SAVED_DEVICE_NAME +
                    " String;");
        }
        if (cursor.getColumnIndex(SAVED_DEVICE_ADDRESS) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + SAVED_DEVICE_ADDRESS +
                    " String;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();

        return ret == 1;
    }

    public boolean updateDeviceConnected(boolean isConnected) {
        Log.d(LOG_TAG, "updateDeviceConnected isConnected: " + isConnected);
        if (TextUtils.isEmpty(connectedUserEmail)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(HAS_DEVICE_CONNECTED, isConnected ? 1 : 0);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(HAS_DEVICE_CONNECTED) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + HAS_DEVICE_CONNECTED +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        return ret == 1;
    }

    public Device getPairedDevice() {
        Device ret = null;
        Cursor c = getReadableDatabase().query(TABLE_NAME_USERS, null, EMAIL + "=?",
                new String[]{connectedUserEmail}, null, null, null);

        if (c == null) {
            return null;
        }
        if (c.moveToFirst()) {
            int columnIndexName = c.getColumnIndex(SAVED_DEVICE_NAME);
            int columnIndexAddress = c.getColumnIndex(SAVED_DEVICE_ADDRESS);

            if (columnIndexName != -1 && columnIndexAddress != -1) {
                ret = new Device(c.getString(columnIndexName), c.getString(columnIndexAddress));
            }
        }
        c.close();

        return ret;
    }

    public boolean setUserConnected(String email, boolean isConnected) {
        Log.d(LOG_TAG, "setUserConnected email: " + email);
        if (TextUtils.isEmpty(email)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(IS_ONLINE, isConnected ? 1 : 0);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(IS_ONLINE) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + IS_ONLINE +
                    " Integer not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{email});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");
        if (isConnected) {
            connectedUserEmail = email;
        } else {
            connectedUserEmail = "";
        }

        cursor.close();

        return ret == 1;
    }

    public boolean isEmailRegistered(String email) {
        Log.d(LOG_TAG, "isEmailRegistered: " + email);
        String ret = null;
        Cursor c = getReadableDatabase().query(TABLE_NAME_USERS, null, EMAIL + "=?",
                new String[]{email}, null, null, null);

        if (c == null) {
            Log.d(LOG_TAG, "isEmailRegistered: cursor is null");
            return false;
        }
        if (c.moveToFirst()) {
            int columnIndex = c.getColumnIndex(EMAIL);
            Log.d(LOG_TAG, "columnIndex : " + columnIndex);
            do {
                ret = c.getString(columnIndex);
                Log.d(LOG_TAG, "isEmailRegistered returned: " + ret);
            } while (c.moveToNext());
        } else{
            Log.d(LOG_TAG, "isEmailRegistered: possible too many elements returned");
        }
        c.close();

        return (ret != null && ret != "");
    }

    public boolean isPasswordCorrect(String email, String password) {
        Log.d(LOG_TAG, "isPasswordCorrect: " + password);
        String ret = null;
        Cursor c = getReadableDatabase().query(TABLE_NAME_USERS, null, EMAIL + "=?",
                new String[]{email}, null, null, null);

        if (c == null) {
            Log.d(LOG_TAG, "isPasswordCorrect: cursor is null");
            return false;
        }
        if (c.getCount() == 1 && c.moveToFirst()) {
            int columnIndex = c.getColumnIndex(PASSWORD);
            if (columnIndex != -1) {
                ret = c.getString(columnIndex);
                Log.d(LOG_TAG, "password From db : " + ret);

                if (ret.equalsIgnoreCase(password)) {
                    Log.d(LOG_TAG, "passwordCorrect");
                    c.close();
                    return true;
                } else {
                    Log.d(LOG_TAG, "password not correct ret : " + ret );
                }
            }
        } else {
            Log.d(LOG_TAG, "isPasswordCorrect: possible too many elements returned");
        }
        c.close();
        return false;
    }

    public User getConnectedUser() {
        User ret = null;
        Cursor c = getReadableDatabase().query(TABLE_NAME_USERS, null, IS_ONLINE + "=1",
                null, null, null, null);

        if (c == null) {
            return null;
        }
        Log.d(LOG_TAG, "getConnectedUser : " + c.getCount());
        if (c.getCount() == 1 && c.moveToFirst()) {
            int columnIndexEmail = c.getColumnIndex(EMAIL);
            int columnIndexDocumentID = c.getColumnIndex(ID);
            int columnIndexPassword = c.getColumnIndex(PASSWORD);
            int columnIndexName = c.getColumnIndex(NAME);
            int columnIndexWeight = c.getColumnIndex(WEIGHT);
            int columnIndexWeightGoal = c.getColumnIndex(WEIGHT_GOAL);
            int columnIndexHeight = c.getColumnIndex(HEIGHT);
            int columnIndexYOB = c.getColumnIndex(YEAR_OF_BIRTH);
            int columnIndexGender = c.getColumnIndex(GENDER);
            int columnIndexAlwaysEnableBT = c.getColumnIndex(ALWAYS_ENABLE_BT);
            int columnIndexSavedDeviceName = c.getColumnIndex(SAVED_DEVICE_NAME);
            int columnIndexSavedDeviceAddress = c.getColumnIndex(SAVED_DEVICE_ADDRESS);
            int columnIndexHasProfilePicture = c.getColumnIndex(HAS_PROFILE_PICTURE);
            int columnIndexProfilePictureURI = c.getColumnIndex(PROFILE_PICTURE_URI);
            int columnIndexRegistrationComplete = c.getColumnIndex(REGISTRATION_COMPLETE);
            int columnIndexHasDeviceConnected = c.getColumnIndex(HAS_DEVICE_CONNECTED);
            int columnIndexIsOnline = c.getColumnIndex(IS_ONLINE);

            if (columnIndexEmail != -1 && columnIndexDocumentID != -1 && columnIndexPassword != -1 && columnIndexName != -1
                    && columnIndexWeight != -1 && columnIndexWeightGoal != -1 && columnIndexHeight != -1
                    && columnIndexYOB != -1 && columnIndexGender != -1 && columnIndexAlwaysEnableBT != -1
                    && columnIndexSavedDeviceName != -1 && columnIndexSavedDeviceAddress != -1
                    && columnIndexHasProfilePicture != -1 && columnIndexProfilePictureURI != -1
                    && columnIndexRegistrationComplete != -1 && columnIndexHasDeviceConnected!= -1
                    && columnIndexIsOnline != -1) {
                ret = new User(c.getString(columnIndexEmail), c.getString(columnIndexDocumentID),
                        c.getString(columnIndexPassword), c.getString(columnIndexName),
                        c.getString(columnIndexGender), c.getString(columnIndexSavedDeviceName),
                        c.getString(columnIndexSavedDeviceAddress), c.getString(columnIndexProfilePictureURI),
                        c.getFloat(columnIndexWeight), c.getFloat(columnIndexWeightGoal),
                        c.getInt(columnIndexHeight), c.getInt(columnIndexYOB),
                        c.getInt(columnIndexAlwaysEnableBT), c.getInt(columnIndexHasProfilePicture),
                        c.getInt(columnIndexHasDeviceConnected),
                        c.getInt(columnIndexRegistrationComplete), c.getInt(columnIndexIsOnline)
                        );

                connectedUserEmail = c.getString(columnIndexEmail);
                c.close();
                return ret;
            } else {
                Log.d(LOG_TAG, "getConnectedUser : un index e egal cu -1");
            }
        } else {
            Log.d(LOG_TAG, "getConnectedUser : posibil sa fi returnat mai multi useri");
        }
        c.close();

        return ret;
    }

    public void registerCallback(IDataRefreshCallback callback) {
        mCallbackList.add(callback);
    }
    private void dispatchCallback(String changedProperty) {

        Log.d(LOG_TAG, "dispatchCallback mCallbackList.size : " + mCallbackList.size());
        for(IDataRefreshCallback callback : mCallbackList) {
            if (callback != null) {
                callback.onDataChanged(changedProperty);
            }
        }
    }


    public boolean hasUserCompletedRegistration(String email) {
        boolean ret = false;
        Cursor c = getReadableDatabase().query(TABLE_NAME_USERS, null, EMAIL + "=?",
                new String[]{email}, null, null, null);

        if (c == null) {
            return ret;
        }
        Log.d(LOG_TAG, "getConnectedUser : " + c.getCount());
        if (c.getCount() == 1 && c.moveToFirst()) {
            int columnIndexRegistrationComplete = c.getColumnIndex(REGISTRATION_COMPLETE);

            if (columnIndexRegistrationComplete != -1 ) {
                ret = c.getInt(columnIndexRegistrationComplete) == 1 ? true : false;
                c.close();
                return ret;
            } else {
                Log.d(LOG_TAG, "hasUserCompletedRegistration : index e egal cu -1");
            }
        } else {
            Log.d(LOG_TAG, "hasUserCompletedRegistration : posibil sa fi returnat mai multi useri");
        }
        c.close();

        return ret;
    }

    public String getNameByEmail(String email) {
        String ret = "";
        Cursor c = getReadableDatabase().query(TABLE_NAME_USERS, null, EMAIL + "=?",
                new String[]{email}, null, null, null);

        if (c == null) {
            return ret;
        }
        Log.d(LOG_TAG, "getNameByEmail : " + c.getCount());
        if (c.getCount() == 1 && c.moveToFirst()) {
            int columnIndexName = c.getColumnIndex(NAME);

            if (columnIndexName != -1 ) {
                ret = c.getString(columnIndexName);
                c.close();
                return ret;
            } else {
                Log.d(LOG_TAG, "getNameByEmail : index e egal cu -1");
            }
        } else {
            Log.d(LOG_TAG, "getNameByEmail : posibil sa fi returnat mai multi useri");
        }
        c.close();

        return ret;
    }

    public boolean setUserCompletedRegistration(String email) {
        Log.d(LOG_TAG, "setUserCompletedRegistration email: " + email);
        if (TextUtils.isEmpty(email)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(REGISTRATION_COMPLETE, 1);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(REGISTRATION_COMPLETE) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + REGISTRATION_COMPLETE +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{email});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        return ret == 1;
    }

    public boolean updateUserID(String email, String documentID) {
        Log.d(LOG_TAG, "updateUserID email: " + email + " docId : " + documentID);
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(documentID)) {
            Log.d(LOG_TAG,"problems with params");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(ID, documentID);



        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(ID) < 0) {
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + ID +
                    " String;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{email});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");
        if (ret == 1){
            ContentValues user_data_values = new ContentValues();
            user_data_values.put(USER_DATA_DOC_ID, documentID);
            getWritableDatabase().update(TABLE_NAME_USERS_DATA, user_data_values, EMAIL + " = ?",
                    new String[]{email});
        }

        cursor.close();

        return ret == 1;
    }

    public HashMap<Long,Integer> getBestSteps() {
        HashMap<Long, Integer> ret = null;
        Cursor c = getReadableDatabase().query(TABLE_NAME_USERS_DATA, null, EMAIL + "=?",
                new String[]{connectedUserEmail}, null, null, null);

        if (c == null) {
            Log.d(LOG_TAG, "getBestSteps cursor is null");
            return null;
        }
        Log.d(LOG_TAG, "getBestSteps : " + c.getCount());
        if (c.getCount() == 1 && c.moveToFirst()) {

            int columnIndexDay = c.getColumnIndex(USER_DATA_BEST_STEPS_DAY);
            int columnIndexValue = c.getColumnIndex(USER_DATA_BEST_STEPS_VALUE);

            if (columnIndexDay != -1 && columnIndexValue != -1 ) {
                ret = new HashMap<>();
                ret.put(c.getLong(columnIndexDay), c.getInt(columnIndexValue));
                return ret;
            } else {
                Log.d(LOG_TAG, "getBestSteps : index e egal cu -1");
            }
        } else {
            Log.d(LOG_TAG, "getBestSteps : posibil sa fi returnat mai multi useri");
        }
        c.close();

        return ret;
    }

    public boolean updateBestSteps(Long day, Integer value) {
        Log.d(LOG_TAG, "updateBestSteps steps " + value);
        if (TextUtils.isEmpty(connectedUserEmail) || day < 0 || value < 0) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(USER_DATA_BEST_STEPS_DAY, day.longValue());
        values.put(USER_DATA_BEST_STEPS_VALUE, value.intValue());

        int ret = getWritableDatabase().update(TABLE_NAME_USERS_DATA, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");
        return ret == 1;
    }
}
