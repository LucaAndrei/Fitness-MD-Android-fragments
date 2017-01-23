package com.master.aluca.fitnessmd.common.bluetooth;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by aluca on 11/8/16.
 */
public class TransactionReceiver {
    private static final String LOG_TAG = "Fitness_TransReceiver";

    private static final int PARSE_MODE_WAIT_START_BYTE = 1;
    private static final int PARSE_MODE_WAIT_DATA = 3;
    private static final int PARSE_MODE_COMPLETED = 101;

    private ArrayList<ContentObject> mObjectQueue = new ArrayList<ContentObject>();

    private int mParseMode = PARSE_MODE_WAIT_START_BYTE;
    private ContentObject mContentObject = null;

    private static TransactionReceiver mInstance = null;


    public static TransactionReceiver getInstance() {
        if (mInstance == null) {
            mInstance = new TransactionReceiver();
        }
        return mInstance;
    }

    private TransactionReceiver() {
        Log.d(LOG_TAG, "TransactionReceiver");
        reset();
    }

    /**
     * Reset transaction receiver.
     */
    private void reset() {
        Log.d(LOG_TAG, "reset");
        mParseMode = PARSE_MODE_WAIT_START_BYTE;
        mCacheStart = 0x00;
        mCacheEnd = 0x00;
        mCacheData = 0x00;
        mCached = false;
    }


    /**
     * After parsing bytes received, Transaction receiver makes parsed object.
     * This method returns parsed object
     * @return	ContentObject		parsed object
     */
    public ContentObject getObject() {
        Log.d(LOG_TAG, "getObject");
        ContentObject object = null;
        if(mObjectQueue != null && mObjectQueue.size() > 0)
            object = mObjectQueue.remove(0);

        return object;
    }

    // Temporary variables for parsing
    private byte mCacheStart = 0x00;
    private byte mCacheEnd = 0x00;
    private int mCacheData = 0x00;
    private boolean mCached = false;

    /**
     * Caching received stream.
     * And parse byte array to make content object
     * @param buffer        byte array to parse
     * @param count            byte array size
     */
    public ContentObject parseStream(byte[] buffer, int count) {
        mContentObject = null;
        if (buffer != null) {
            Log.d(LOG_TAG, "buffer != null");
            if (buffer.length > 0) {
                Log.d(LOG_TAG, "buffer.length > 0 : " + buffer.length);
                if (count > 0) {
                    Log.d(LOG_TAG, "count > 0 : " + count);
                    Log.d(LOG_TAG, "Transaction.TRANSACTION_START_BYTE : " + Transaction.TRANSACTION_START_BYTE);
                    Log.d(LOG_TAG, "Transaction.TRANSACTION_START_BYTE_2 : " + Transaction.TRANSACTION_START_BYTE_2);
                    Log.d(LOG_TAG, "mCacheStart : " + mCacheStart);
                    for(int i=0; i < buffer.length && i < count; i++) {
                        Log.d(LOG_TAG, "buffer[" + i + "] : " + buffer[i]);
                        switch(mParseMode) {
                            case PARSE_MODE_WAIT_START_BYTE:
                                Log.d(LOG_TAG, "case PARSE_MODE_WAIT_START_BYTE");
                                if (buffer[i] == Transaction.TRANSACTION_START_BYTE_2
                                        && mCacheStart == Transaction.TRANSACTION_START_BYTE) {
                                    Log.d(LOG_TAG, "buffer == TR_start_Byte_2 && cacheStart == tr_start_Byte");
                                    mParseMode = PARSE_MODE_WAIT_DATA;
                                    if (mContentObject == null) {
                                        mContentObject = new ContentObject();
                                    }
                                } else {
                                    Log.d(LOG_TAG, "buffer != TR_start_Byte_2 && cacheStart != tr_start_Byte");
                                    mCacheStart = buffer[i];
                                }
                            break;
                            case PARSE_MODE_WAIT_DATA:
                                Log.d(LOG_TAG, "case PARSE_MODE_WAIT_DATA");
                                Log.d(LOG_TAG, "mContentObject.getAccelIndex() : " + mContentObject.getAccelIndex());

                                // Forced to fill 20 accel data
                                if(mContentObject != null && mContentObject.getAccelIndex() > ContentObject.DATA_COUNT - 1) {
                                    //Logs.d("Read data: TRANSACTION_END_BYTE");
                                    Log.d(LOG_TAG, "set mParseMode = PARSE_MODE_COMPLETED");
                                    mParseMode = PARSE_MODE_COMPLETED;
                                    break;
                                } else {
                                    Log.d(LOG_TAG, "not completed.parse data");
                                }

                                // Remote device(Arduino) uses 2-byte integer.
                                // We must cache 2byte to make single value
                                if(mCached) {
                                    Log.d(LOG_TAG, "cached true");
                                    int tempData = 0x00000000;
                                    int tempData2 = 0x00000000;
                                    boolean isNegative = false;
                                    Log.d(LOG_TAG, "mCacheData : " + mCacheData);
                                    if(mCacheData == 0x0000007f){
                                        Log.d(LOG_TAG, "mCacheData == 0x0000007f");
                                        // Recover first byte (To avoid null byte, 0x00 was converted to 0x7f)
                                        mCacheData = 0x00000000;
                                    } else {
                                        Log.d(LOG_TAG, "mCacheData != 0x0000007f");
                                    }

                                    if( (mCacheData & 0x00000080) == 0x00000080 ){
                                        Log.d(LOG_TAG, "(mCacheData & 0x00000080) == 0x00000080. set isNegative true");
                                        // Check first bit which is 'sign' bit
                                        isNegative = true;
                                    } else {
                                        Log.d(LOG_TAG, "(mCacheData & 0x00000080) != 0x00000080. isNegative remains false");
                                    }

                                    if(buffer[i] == 0x01){
                                        Log.d(LOG_TAG, "buffer[i] == 0x01");
                                        // Recover second byte (To avoid null byte, 0x00 was converted to 0x01)
                                        buffer[i] = 0x00;
                                    } else {
                                        Log.d(LOG_TAG, "buffer[i] != 0x01");
                                    }


                                    tempData2 |= (buffer[i] & 0x000000ff);
                                    Log.d(LOG_TAG, "tempData2 : " + tempData2);
                                    tempData = (((mCacheData << 8) | tempData2) & 0x00007FFF);
                                    Log.d(LOG_TAG, "tempData : " + tempData);

                                    //Logs.d(String.format("%02X ", mCacheData) + String.format("%02X ", tempData2) + String.format("%02X ", tempData));

                                    // negative number uses 2's complement math. Set first 9 bits as 1.
                                    if(isNegative) {
                                        tempData = (tempData | 0xFFFF8000);
                                        Log.d(LOG_TAG, "isNegative : " + isNegative + " >>> tempData : " + tempData);
                                    } else {
                                        Log.d(LOG_TAG, "isNegative : " + false);
                                    }


                                    // Recovered integer value. Remember this value.
                                    if(mContentObject != null) {
                                        mContentObject.setAccelData(tempData);
                                    }
                                    mCacheData = 0x00000000;
                                    mCached = false;
                                } else {
                                    Log.d(LOG_TAG, "cached false : " + mCached);
                                    mCacheData |= (buffer[i] & 0x000000ff);		// Remember first byte
                                    Log.d(LOG_TAG, "mCacheData : " + mCacheData);
                                    mCached = true;
                                }
                                break;
                        }
                        if(mParseMode == PARSE_MODE_COMPLETED) {
                            if(mContentObject != null) {
                                //Logs.d("ContentObject created: time="+mContentObject.mTimeInMilli);
                                mObjectQueue.add(mContentObject);
                            }
                            reset();
                        }
                    }
                } else {
                    Log.d(LOG_TAG, "count < 0");
                }
            } else {
                Log.d(LOG_TAG, "buffer.lenth < 0");
            }
        } else {
            Log.d(LOG_TAG, "buffer is null");
        }
        return mContentObject;
    }

    /**
     * Defines transaction constants
     */
    public class Transaction {
        private static final byte TRANSACTION_START_BYTE = (byte)0xfe;
        private static final byte TRANSACTION_START_BYTE_2 = (byte)0xfd;
        private static final byte TRANSACTION_END_BYTE = (byte)0xfd;
        private static final byte TRANSACTION_END_BYTE_2 = (byte)0xfe;

        public static final int COMMAND_TYPE_NONE = 0x00;
        public static final int COMMAND_TYPE_PING = 0x01;
        public static final int COMMAND_TYPE_ACCEL_DATA = 0x02;
    }
}
