package com.master.aluca.fitnessmd.common.bluetooth;

/**
 * Created by aluca on 11/7/16.
 */
public class BluetoothState {
    public static final int NOT_CONNECTED = 0;           // we're doing nothing
    public static final int CONNECTING = 2;     // now initiating an outgoing connection
    public static final int CONNECTED = 3;      // now connected to a remote device
}
