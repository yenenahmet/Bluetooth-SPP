package com.example.bluetoothspp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Ahmet on 08.09.2017.
 */

public class BluetoothScan {
    private static final String TAG ="BluetoothScan";
    private BluetoothDevice bluetoothDevice;
    private ProgressDialog progressDialog ;
    private Boolean isDevice = false;
    private ArrayList<BluetoothDevice> devicesList = new ArrayList<>();
    private Context context;
    private String SearchMac;

    public  BluetoothScan(Context context,String SearchMac){
        this.context = context;
        this.SearchMac = SearchMac;

    }
    public  boolean BluetoothAdapterControl(){
        progressDialog = BluetoothState.ProgressRun(context,"SEARCH Device...");
        if(BluetoothAdapter.getDefaultAdapter() != null) {
            if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                return false;
            }else{
                return true;
            }
        } else {
            return false;
        }
    }
    public Intent makeBTDiscoverable() {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devicesList.add(btDevice);
                if(devicesList.size() >0){
                    for(BluetoothDevice device : devicesList){
                        Log.e(TAG,device.toString() +"," + device.getName());
                        if(device.getAddress().toString().trim().equals(SearchMac)){
                            bluetoothDevice = device;
                            isDevice =true;
                        }
                    }
                }
                if(isDevice){
                    progressDialog.cancel();
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                }
            }
        }
    };

    public void searchForDevices() {
        Log.e(TAG,"Searching devices...");
        if(BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
            Log.e(TAG, "Cancel discoverying...");
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        }
        context.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        Log.e(TAG, "Starting discovery...");
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    public void StopReceiver(){
        context.unregisterReceiver(mReceiver);
    }
    public ArrayList<BluetoothDevice> getDeviceList(){
        return   devicesList;
    }
    public ProgressDialog getProgressDialog(){
        return progressDialog;
    }
    public boolean isDevice(){
        return isDevice;
    }
    public BluetoothDevice getBluetoothDeviceList(){
        return bluetoothDevice;
    }

}
