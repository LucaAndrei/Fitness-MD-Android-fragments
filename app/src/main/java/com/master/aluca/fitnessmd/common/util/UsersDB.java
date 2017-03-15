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


/**
 * Created by andrei on 3/14/2017.
 */

public class UsersDB extends SQLiteOpenHelper {
    private static final String LOG_TAG  = "Fitness_UsersDB";

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FitnessMD_DB";
    public static final String TABLE_NAME_USERS= "users";

    private IDataRefreshCallback mCallback;

    // columns
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
    private static final String IS_ONLINE = "isOnline";

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
                    + "profilePictureURI String, "
                    + "registrationComplete Integer not null, "
                    + "isOnline Integer not null)";
    private static final String DATABASE_DROP_USERS_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_USERS;

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        Log.d(LOG_TAG, "UsersDB onUpgrade");
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
        values.put(EMAIL, email);
        values.put(PASSWORD, password);
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
        values.put(IS_ONLINE, 0);

        long ret = getWritableDatabase().insert(TABLE_NAME_USERS, null, values);
        Log.d(LOG_TAG,"ret : " + ret);

        return ret > 0;
    }

    public boolean updateWeight(float newWeight) {
        Log.d(LOG_TAG, "updateWeight newWeight: " + newWeight);
        if (newWeight < 55.0f || TextUtils.isEmpty(connectedUserEmail)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(WEIGHT, newWeight);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(WEIGHT) < 0) {
            // apple_device column not there - add it
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + WEIGHT +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        if (mCallback != null)
            mCallback.onDataChanged(Constants.SHARED_PREFS_WEIGHT_KEY);

        return ret == 1;
    }
    public boolean updateHeight(int newHeight) {
        Log.d(LOG_TAG, "updateHeight newHeight: " + newHeight);
        if (newHeight < 55.0f || TextUtils.isEmpty(connectedUserEmail)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(HEIGHT, newHeight);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(HEIGHT) < 0) {
            // apple_device column not there - add it
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + HEIGHT +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        if (mCallback != null)
            mCallback.onDataChanged(Constants.SHARED_PREFS_WEIGHT_KEY);

        return ret == 1;
    }
    public boolean updateGender(String newGender) {
        Log.d(LOG_TAG, "updateGender newGender: " + newGender);
        if (TextUtils.isEmpty(newGender) || TextUtils.isEmpty(connectedUserEmail)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(GENDER, newGender);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(GENDER) < 0) {
            // apple_device column not there - add it
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + GENDER +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        if (mCallback != null)
            mCallback.onDataChanged(Constants.SHARED_PREFS_WEIGHT_KEY);

        return ret == 1;
    }
    public boolean updateYearOfBirth(int yearOfBirth) {
        Log.d(LOG_TAG, "updateHeight yearOfBirth: " + yearOfBirth);
        if (yearOfBirth < Constants.HEIGHT_MIN_VALUE || yearOfBirth > Constants.HEIGHT_MAX_VALUE
                || TextUtils.isEmpty(connectedUserEmail)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(YEAR_OF_BIRTH, yearOfBirth);

        // cursor for all data
        Cursor cursor = getWritableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME_USERS, null);
        // see if the column is there
        if (cursor.getColumnIndex(YEAR_OF_BIRTH) < 0) {
            // apple_device column not there - add it
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + YEAR_OF_BIRTH +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        if (mCallback != null)
            mCallback.onDataChanged(Constants.SHARED_PREFS_WEIGHT_KEY);

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
            // apple_device column not there - add it
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
            // apple_device column not there - add it
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + PROFILE_PICTURE_URI +
                    " real not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{connectedUserEmail});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

        cursor.close();
        return ret == 1;
    }



    public boolean saveDevice(String email, String deviceName, String deviceAddress) {
        Log.d(LOG_TAG, "saveDevice deviceName: " + deviceName + " >>> deviceAddress : " + deviceAddress);
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(deviceName) || TextUtils.isEmpty(deviceAddress)) {
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
                new String[]{email});
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
            // apple_device column not there - add it
            getWritableDatabase().execSQL("ALTER TABLE " + TABLE_NAME_USERS + " ADD " + IS_ONLINE +
                    " Integer not null;");
        }

        int ret = getWritableDatabase().update(TABLE_NAME_USERS, values, EMAIL + " = ?",
                new String[]{email});
        Log.d(LOG_TAG, "Updated: " + ret + " rows");

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
            if (columnIndex != -1) {
                ret = c.getString(columnIndex);
                Log.d(LOG_TAG, "isEmailRegistered returned: " + ret);
            }
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
                if (ret.equalsIgnoreCase(password)) {
                    c.close();
                    connectedUserEmail = email;
                    return true;
                }
            }
        } else {
            Log.d(LOG_TAG, "isPasswordCorrect: possible too many elements returned");
        }
        c.close();
        return false;
    }

    public boolean getIsUserLoggedIn() {
        Log.d(LOG_TAG, "getIsUserLoggedIn");
        String ret = null;
        Cursor c = getReadableDatabase().query(TABLE_NAME_USERS, null, IS_ONLINE + "=1",
                null, null, null, null);

        if (c == null) {
            return false;
        }
        Log.d(LOG_TAG, "getIsUserLoggedIn : " + c.getCount());
        if (c.getCount() == 1 && c.moveToFirst()) {
            int columnIndex = c.getColumnIndex(IS_ONLINE);
            if (columnIndex != -1) {
                int columnIndexEmail = c.getColumnIndex(EMAIL);
                ret = c.getString(columnIndexEmail);
                Log.d(LOG_TAG, "getIsUserLoggedIn email : " + ret);
                if (ret != null && ret != "") {
                    c.close();
                    return true;
                }
            }
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
            int columnIndexIsOnline = c.getColumnIndex(IS_ONLINE);

            if (columnIndexEmail != -1 && columnIndexPassword != -1 && columnIndexName != -1
                    && columnIndexWeight != -1 && columnIndexWeightGoal != -1 && columnIndexHeight != -1
                    && columnIndexYOB != -1 && columnIndexGender != -1 && columnIndexAlwaysEnableBT != -1
                    && columnIndexSavedDeviceName != -1 && columnIndexSavedDeviceAddress != -1
                    && columnIndexHasProfilePicture != -1 && columnIndexProfilePictureURI != -1
                    && columnIndexRegistrationComplete != -1 && columnIndexIsOnline != -1) {
                ret = new User(c.getString(columnIndexEmail), c.getString(columnIndexPassword), c.getString(columnIndexName),
                        c.getString(columnIndexGender), c.getString(columnIndexSavedDeviceName),
                        c.getString(columnIndexSavedDeviceAddress), c.getString(columnIndexProfilePictureURI),
                        c.getFloat(columnIndexWeight), c.getFloat(columnIndexWeightGoal),
                        c.getInt(columnIndexHeight), c.getInt(columnIndexYOB),
                        c.getInt(columnIndexAlwaysEnableBT), c.getInt(columnIndexHasProfilePicture),
                        c.getInt(columnIndexRegistrationComplete), c.getInt(columnIndexIsOnline)
                        );
                c.close();
                connectedUserEmail = c.getString(columnIndexEmail);
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


    public void registerCallback(IDataRefreshCallback callback) {
        mCallback = callback;
    }
}
