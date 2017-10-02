package com.example.bluetoothspp;

import android.app.ProgressDialog;
import android.content.Context;

import java.util.UUID;

/**
 * Created by Ahmet on 14.09.2017.
 */

public class BluetoothState {
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       	// we're doing nothing
    public static final int STATE_LISTEN = 1;     	// now listening for incoming connections
    public static final int STATE_CONNECTING = 2; 	// now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  	// now connected to a remote device
    public static final int STATE_NULL = -1;  	 	// now service is null

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;


    public static final boolean DEVICE_ANDROID = true;
    public static final boolean DEVICE_OTHER = false;

    //UUID
    public static final UUID uuid_HC05 = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final UUID UUID_SECURE_Android = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final UUID UUID_INSECURE_Android = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    public static ProgressDialog ProgressRun (Context context, String veri){
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(veri);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }
}
