package com.master.aluca.fitnessmd.common.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.master.aluca.fitnessmd.common.Constants;


/**
 * Created by andrei on 3/14/2017.
 */

public class UsersDB extends SQLiteOpenHelper {
    private static final String LOG_TAG  = "Fitness_UsersDB";

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FitnessMD_DB";
    public static final String TABLE_NAME_USERS= "users";

    private static final String DATABASE_CREATE_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS " +TABLE_NAME_USERS+ "("
                    + "_id String primary key, "// int		primary key, auto increment
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
                    + "profilePictureURI String)";// boolean values. 1 = true, 0 = false
    private static final String DATABASE_DROP_USERS_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_USERS;

    // Context, System
    private final Context mContext;
    private SQLiteDatabase mDb;
    private static UsersDB sInstance;


    // Constructor
    private UsersDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public static synchronized UsersDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new UsersDB(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(DATABASE_DROP_USERS_TABLE);
        onCreate(db);
    }

    public boolean addUser(String email, String password, String name) {
        Log.d(LOG_TAG, "addUser: " + email + " pass : " + password + " >> name : " + name);
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name)) {
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
        values.put("_id", email);
        values.put("password", password);
        values.put("name", name);
        values.put("weight",  Constants.WEIGHT_DEFAULT_VALUE);
        values.put("weightGoal",  Constants.WEIGHT_DEFAULT_VALUE);
        values.put("height", Constants.HEIGHT_DEFAULT_VALUE);
        values.put("yearOfBirth", Constants.YOB_DEFAULT_VALUE);
        values.put("gender", "Male");
        values.put("alwaysEnableBT", 0);
        values.put("savedDeviceName", (String) null);
        values.put("savedDeviceAddress", (String) null);
        values.put("hasProfilePicture", 0);
        values.put("profilePictureURI", (String) null);

        long ret = getWritableDatabase().insert(TABLE_NAME_USERS, null, values);
        Log.d(LOG_TAG,"ret : " + ret);

        return ret > 0;
    }


   /* public boolean addNewDevice(String macAddress) {
        if (DEBUG) Log.d(TAG, "Add new device: " + macAddress);
        if (TextUtils.isEmpty(macAddress)) {
            return false;
        }
        Cursor cursor = getWritableDatabase().query(PROFILES_TABLE, null, MAC_ADDRESS + "=?",
                new String[]{macAddress},
                null, null, null);

        if (cursor != null) {
            int count = cursor.getCount();
            cursor.close();
            if (count > 0) return false;
        }

        ContentValues values = new ContentValues();
        values.put(MAC_ADDRESS, macAddress);
        values.put(MEDIA, ENABLED);
        values.put(NETWORK, ENABLED);
        values.put(HANDS_FREE, ENABLED);
        values.put(PAIR_INDEX, mMaxIndex + 1);
        values.put(APPLE_DEVICE, DEFAULT_VALUE);
        long ret = getWritableDatabase().insert(PROFILES_TABLE, null, values);

        return ret > 0;
    }


    public boolean updateDevice(String macAddress, int profileId, int value) {
        boolean isProfileSupported = Utils.getUtils().isProfileAvailable(profileId, Utils.getUtils()
                .getDeviceWithMac(macAddress));
        if (!isProfileSupported && value != NEVER_CONNECTED) {
            value = NEVER_CONNECTED;
        }
        if (DEBUG) {
            Log.d(TAG, "Update: " + macAddress + " on: " +
                    Utils.getUtils().getProfileString(profileId) +
                    " with: " + Utils.getUtils().getProfileStateString(value));
        }
        if (TextUtils.isEmpty(macAddress)) {
            return false;
        }
        ContentValues values = new ContentValues();

        switch (profileId) {
            case BluetoothProfile.A2DP_SINK:
            case BluetoothProfile.AVRCP_CONTROLLER:
                values.put(MEDIA, value);
                break;
            case BluetoothProfile.HF_DEVICE:
                values.put(HANDS_FREE, value);
                break;
            case BluetoothProfile.PAN:
                values.put(NETWORK, value);
                break;
            default:
                Log.e(TAG, "Unknown profile: " + Utils.getUtils().getProfileString(profileId));
                break;
        }

        int ret = getWritableDatabase().update(PROFILES_TABLE, values, MAC_ADDRESS + " = ?",
                new String[]{macAddress});
        if (DEBUG) Log.d(TAG, "Updated: " + ret + " rows");

        return ret == 1;
    }


    public boolean removeDevice(String macAddress) {
        if (DEBUG) Log.d(TAG, "Remove device: " + macAddress);
        if (TextUtils.isEmpty(macAddress)) return false;

        int ret = getWritableDatabase().delete(PROFILES_TABLE, MAC_ADDRESS + "=?",
                new String[]{macAddress});

        return ret == 1;
    }


    public int getProfileValue(int profileId, String macAddress) {
        if (!isDevicePaired(macAddress)) {
            return -1;
        }

        int ret = -1;
        Cursor c = getReadableDatabase().query(PROFILES_TABLE, null, MAC_ADDRESS + "=?",
                new String[]{macAddress}, null, null, null);

        if (c == null) {
            return -1;
        }
        if (c.moveToFirst()) {
            String column = "";
            switch (profileId) {
                case BluetoothProfile.A2DP_SINK:
                case BluetoothProfile.AVRCP_CONTROLLER:
                    column = MEDIA;
                    break;
                case BluetoothProfile.HF_DEVICE:
                    column = HANDS_FREE;
                    break;
                case BluetoothProfile.PAN:
                    column = NETWORK;
                    break;
                default:
                    Log.e(TAG, "Unknown profile: " +
                            Utils.getUtils().getProfileString(profileId));
                    break;
            }
            int columnIndex = c.getColumnIndex(column);
            if (columnIndex != -1) {
                ret = c.getInt(columnIndex);
            }
        }
        c.close();

        return ret;
    }


    public boolean isProfileOnValue(int profileId, String macAddress, int checkValue) {
        if (DEBUG) {
            Log.d(TAG, macAddress + " has profile " +
                    Utils.getUtils().getProfileString(profileId) + " on " +
                    Utils.getUtils().getProfileStateString(checkValue) + "?");
        }
        boolean ret = checkValue == getProfileValue(profileId, macAddress);

        if (DEBUG) {
            Log.d(TAG, macAddress + " is " + (ret ? "enabled" : "disabled")
                    + " for profile " + Utils.getUtils().getProfileString(profileId));
        }
        return ret;
    }


    private boolean isDevicePaired(String macAddress) {
        boolean ret = false;
        String query = "SELECT COUNT(*) FROM " + PROFILES_TABLE +
                " WHERE " + MAC_ADDRESS + " = \"" + macAddress + "\"";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                ret = true;
            }
            cursor.close();
        }

        return ret;
    }


    public boolean updateDeviceManufacturer(String macAddress, boolean isAppleDevice) {
        if (DEBUG) {
            String manufacturer = isAppleDevice ? "Apple" : "not Apple";
            Log.d(TAG, "Update manufacturer for: " + macAddress +
                    " with: " + manufacturer + " manufacturer");
        }
        if (TextUtils.isEmpty(macAddress)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(APPLE_DEVICE, isAppleDevice ? ENABLED : DISABLED);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + PROFILES_TABLE, null);
        // see if the column is there
        int checkColumnExistence = cursor.getColumnIndex(APPLE_DEVICE);
        if (checkColumnExistence < 0) {
            // apple_device column not there - add it
            getWritableDatabase().execSQL("ALTER TABLE " + PROFILES_TABLE + " ADD " + APPLE_DEVICE +
                    " integer;");
        }

        int ret = getWritableDatabase().update(PROFILES_TABLE, values, MAC_ADDRESS + " = ?",
                new String[]{macAddress});
        if (DEBUG) Log.d(TAG, "Updated: " + ret + " rows");

        cursor.close();

        return ret == 1;
    }


    public int isAppleDevice(String macAddress) {
        if (DEBUG) Log.d(TAG, "Check if vendor for: " + macAddress + " is Apple");

        int ret = DEFAULT_VALUE;
        Cursor c = getReadableDatabase().query(PROFILES_TABLE, null, MAC_ADDRESS + "=?",
                new String[]{macAddress}, null, null, null);

        if (c == null) {
            return ret;
        }
        if (c.moveToFirst()) {
            String column = APPLE_DEVICE;

            int columnIndex = c.getColumnIndex(column);
            if (columnIndex != -1) {
                ret = c.getInt(columnIndex);
                if (DEBUG) Log.d(TAG, "isAppleDevice returned: " + ret);
            }
        }
        c.close();

        return ret;
    }*/
}
