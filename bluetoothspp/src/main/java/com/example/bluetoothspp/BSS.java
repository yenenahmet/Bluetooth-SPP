package com.example.bluetoothspp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;


/**
 * Created by Ahmet on 22.09.2017.
 */
public class BSS {
    private ProgressDialog progressDialog = null;
    private BSSListener bssListener;
    private String SendMessage ="" , TAG="BSS";
    private BluetoothClientService bluetoothClientService =null;
    private BluetoothServerService bluetoothServerService= null;
    private boolean isClient;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothState.MESSAGE_WRITE:
                    SendMessage =msg.obj.toString();
                    Log.e(TAG +"Write ",SendMessage);
                    break;
                case BluetoothState.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    readMessage = ClearData(readMessage);
                    SendMessage = ClearData(SendMessage);
                    if(readMessage != null && !SendMessage.equals(readMessage) && readMessage != "") {
                        bssListener.onMessage(readMessage);
                    }
                    break;
                case BluetoothState.MESSAGE_DEVICE_NAME:
                    String DeviceName= null;
                    try{
                        DeviceName = ((BluetoothDevice)msg.obj).getName();
                    }catch (Exception ex){
                        Log.e(TAG +"Device Name:",ex.toString());
                        DeviceName = "";
                    }
                    Log.e(TAG+"Device Name:",DeviceName);
                    bssListener.onStateChange("Device Name :"+DeviceName,4);
                    break;
                case BluetoothState.MESSAGE_TOAST:
                    if(progressDialog.isShowing() && progressDialog != null){
                        progressDialog.cancel();
                    }
                    bssListener.onError(msg.obj.toString());
                    break;
                case BluetoothState.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        case 0:
                            bssListener.onStateChange("Stop",0);
                            Log.e(TAG+" Change : ","Stop");
                            break;
                        case 1:
                            bssListener.onStateChange("Start",1);
                            Log.e(TAG +" Change","Start");
                            break;
                        case 2:
                            bssListener.onStateChange("Connecting",2);
                            Log.e(TAG +" Change","Connecting...");
                            break;
                        case 3:
                            bssListener.onStateChange("Connected",3);
                            if(progressDialog !=null) progressDialog.cancel();
                            Log.e(TAG +" Change","Connected.");
                            break;
                    }
                    break;
            }
        }
    };
    public BSS(@NonNull BluetoothDevice device,@NonNull Context context,boolean AutoStart,boolean AutoProgress,boolean SecureConnect,boolean isDevice){
        bluetoothClientService = new BluetoothClientService(device,mHandler,SecureConnect,isDevice);
        if(AutoStart){
            ConnectStart();
        }
        if(AutoProgress){
            progressDialog = BluetoothState.ProgressRun(context,"Connecting to Device ...");
        }
        // Client
        isClient = true;
    }
    public BSS(@NonNull BluetoothDevice device,boolean SecureConnect,boolean isDevice){
        bluetoothClientService = new BluetoothClientService(device,mHandler,SecureConnect,isDevice);
        isClient = true;
        // Client
    }
    public BSS(boolean AutoStart,boolean SecureAccept,boolean isDevice){
        // Server
        bluetoothServerService = new BluetoothServerService(mHandler,SecureAccept,isDevice);
        isClient =false;
        if(AutoStart){
            ConnectStart();
        }
    }
    public void ConnectStart(){
        if(isClient){
            if(bluetoothClientService.getState() ==0 ){
                bluetoothClientService.Connect();
                Log.e(TAG,"Client Run");
            }else{
                bssListener.onError("Connected on Device");
            }
        }else{
            if(bluetoothServerService.getState() ==0){
                bluetoothServerService.Start();
                Log.e(TAG,"Server Run");
            }else{
                bssListener.onError("[Server]Connected on Device");
            }
        }
    }
    public void SendMessage(String Message){
        if(Message == null) return;
        if(isClient){
            bluetoothClientService.SendMessage(Message);
        }else{
            bluetoothServerService.SendMessage(Message);
        }
    }
    public void StopService(){
         if(isClient){
             bluetoothClientService.stop();
         }else {
             bluetoothServerService.Stop();
         }
    }
    public interface BSSListener{
        void onStateChange(String Status, int StatusCode);
        void onError(String Error);
        void onMessage(String Message);
    }
    public void setBSSListener(BSSListener bssListener){
        this.bssListener = bssListener;
    }

    public void Restart(){
        StopService();
        try{
            Thread.sleep(99);
            ConnectStart();
        }catch (InterruptedException ex){
            Log.e(TAG +"Restart",ex.toString());
        }
    }
    private String ClearData(String Data){
        String Clear = Data.replace("\r","");
        return Clear.replace("\n","");
    }
}
