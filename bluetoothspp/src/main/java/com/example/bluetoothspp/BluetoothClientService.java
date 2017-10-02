package com.example.bluetoothspp;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Ahmet on 18.09.2017.
 */

public class BluetoothClientService {
    private BluetoothDevice device;
    private Connect connect;
    private Connected connected;
    private int State =0;
    private Handler mHandler;
    private boolean  SecureConnect,isDevice;
    private String TAG ="BluetoothClientService";
    public  BluetoothClientService(BluetoothDevice bluetoothDevice,Handler mHandler,boolean SecureConnect,boolean isDevice){
        this.device = bluetoothDevice;
        this.mHandler= mHandler;
        this.SecureConnect = SecureConnect;
        this.isDevice = isDevice;
    }
    public synchronized int getState(){
        return this.State;
    }
    private synchronized void setState(int state) {
        this.State = state;
        this.mHandler.obtainMessage(1, state, -1).sendToTarget();
    }
    public synchronized void Connect() {
        if (this.connect != null) {
            this.connect.SocketClose();
            this.connect = null;
        }
        if (this.connected != null) {
            this.connected.SocketClose();
            this.connected = null;
        }
        setState(1);
        this.connect = new Connect();
        this.connect.start();
}
    public synchronized void stop() {
        if (this.connect != null) {
            this.connect.SocketClose();
            this.connect = null;
        }
        if (this.connected != null) {
            this.connected.SocketClose();
            this.connected = null;
        }
        setState(0);
    }

    public void SendMessage(String command)  {
        Connected r;
        synchronized (this){
            if(State ==0){
                mHandler.obtainMessage(BluetoothState.MESSAGE_TOAST,"Send Message Error , Not Connect").sendToTarget();
                return;
            }
            r = connected;
        }
        r.Write(command);
    }
    private class Connect extends  Thread{
        private BluetoothSocket mmSocket = null;
        public  Connect()  {
            try{
                BluetoothSocket socket = null;
                if(SecureConnect){
                    if(isDevice){
                        socket = device.createRfcommSocketToServiceRecord(BluetoothState.UUID_SECURE_Android);
                    }else{
                        socket = device.createRfcommSocketToServiceRecord(BluetoothState.uuid_HC05);
                    }
                }else{
                    if(isDevice){
                        socket = device.createInsecureRfcommSocketToServiceRecord(BluetoothState.UUID_INSECURE_Android);
                    }else {
                        socket = device.createInsecureRfcommSocketToServiceRecord(BluetoothState.uuid_HC05);
                    }
                }
                mmSocket = socket;
                setState(2);
            }catch (IOException ex){
                setState(0);
                mHandler.obtainMessage(BluetoothState.MESSAGE_TOAST,"Not Connect. ERROR ").sendToTarget();
                return;
            }
        }
        @Override
        public void run() {
            try {
                mmSocket.connect();
                connected = new Connected(mmSocket);
                connected.start();
            } catch (IOException e) {
                Log.e(TAG,e.toString());
                SocketClose();
                setState(0);
                mHandler.obtainMessage(BluetoothState.MESSAGE_TOAST,"Not Connect  RUN. ERROR").sendToTarget();
                return;
            }
        }
        public void SocketClose(){
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, closeException.toString());
            }

        }
    }
    private class Connected extends Thread {
        private BluetoothSocket socket = null;
        private InputStream in;
        private OutputStream out;
        private StringBuilder builder = new StringBuilder();
        private  byte[] buffer = new byte[512];
        public Connected(BluetoothSocket socket) {
            try{
                this.socket = socket;
                in = socket.getInputStream();
                out = socket.getOutputStream();
                setState(3);
            }catch (IOException ex){
                setState(0);
                mHandler.obtainMessage(BluetoothState.MESSAGE_TOAST,"Not Connected.Input Output . ERROR ").sendToTarget();
                return;
            }

        }
        @Override
        public void run() {
            while (State == BluetoothState.STATE_CONNECTED){
                try{
                    DataCharEdit();
                   // DataStringEdit();
                }catch (IOException ex){
                    SocketClose();
                    Log.e(TAG,ex.toString());
                    mHandler.obtainMessage(BluetoothState.MESSAGE_TOAST,"Not Connected.Input Output  READ . ERROR").sendToTarget();
                    break;
                }
            }
        }
        public void Write(String command)  {
            try{
                out.write((command+"\r").getBytes());
                mHandler.obtainMessage(BluetoothState.MESSAGE_WRITE,-1,-1,command).sendToTarget();
            }catch (IOException ex){
                mHandler.obtainMessage(BluetoothState.MESSAGE_TOAST,"Error Send Message").sendToTarget();
            }
        }
        private void DataCharEdit() throws IOException {// Bluetooth Socket Fast  Read
            char c =(char)in.read();
            if(c=='\n'){
                mHandler.obtainMessage(2,-1,-1,builder.toString()).sendToTarget();
                builder.setLength(0);
            }else{
                builder.append(c);
            }
        }
        private void DataStringEdit() throws IOException {
            if(in.available() >2){
                int bytes =  in.read(buffer);
                mHandler.obtainMessage(2,bytes,-1,new String(buffer, 0, bytes)).sendToTarget();
            }else{
                SystemClock.sleep(100);
            }
        }
        public void SocketClose() {
            try {
                ResetCommand();
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        private void ResetCommand(){
            try {
                out.write(("\r").getBytes());
                out.flush();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
