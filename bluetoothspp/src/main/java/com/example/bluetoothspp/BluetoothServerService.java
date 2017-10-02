package com.example.bluetoothspp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by Ahmet on 28.09.2017.
 */

public class BluetoothServerService {
    private static final String NAME_SECURE = "BSSSecure", NAME_INSECURE = "BSSInSecure", TAG ="BluetoothClientService";
    private int State =0;
    private Handler mHandler;
    private boolean  SecureConnect,isDevice;
    private BluetoothAdapter bluetoothAdapter;
    private Connected connected =null;
    private Accept accept = null;
    public BluetoothServerService(Handler mHandler,boolean SecureConnect,boolean isDevice){
        this.isDevice=isDevice;
        this.mHandler = mHandler;
        this.SecureConnect = SecureConnect;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    public synchronized int getState(){
        return this.State;
    }
    private synchronized void setState(int state) {
        this.State = state;
        this.mHandler.obtainMessage(1, state, -1).sendToTarget();
    }

    public synchronized void Start(){
        if(this.connected != null){
            this.connected.SocketClose();
            this.connected =null;
        }
        if(this.accept == null){
            this.accept = new Accept();
            this.accept.start();
        }
        setState(1);
    }
    public synchronized void Stop(){
        if(this.accept !=null){
            this.accept.SocketClose();
            this.accept.CloseServer();
            this.accept =null;
        }
        if(this.connected !=null){
            this.connected.SocketClose();
            this.connected =null;
        }
        setState(0);
    }
    public void SendMessage(String command)  {
        Connected r;
        synchronized (this){
            if(State ==0){
                mHandler.obtainMessage(BluetoothState.MESSAGE_TOAST,"[Server]Send Message Error , Not Connect").sendToTarget();
                return;
            }
            r = connected;
        }
        r.Write(command);
    }
    private class Accept extends Thread {
        private  BluetoothServerSocket mmServerSocket;
        private boolean isRunServer = true;
        public Accept() {
            try {
                this.isRunServer =true;
                BluetoothServerSocket socket = null;
                if (SecureConnect) {
                    if(isDevice){
                        socket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,BluetoothState.UUID_SECURE_Android);
                    }else{
                        socket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,BluetoothState.uuid_HC05);
                    }
                } else {
                    if(isDevice){
                        socket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE,BluetoothState.UUID_INSECURE_Android);
                    }else{
                        socket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE,BluetoothState.uuid_HC05);
                    }
                }
                mmServerSocket = socket;
                setState(2);
            } catch (IOException e) {
                setState(0);
                mHandler.obtainMessage(BluetoothState.MESSAGE_TOAST,"[Server]Not Accept . ERROR ").sendToTarget();
                return;
            }
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;
            while (State != BluetoothState.STATE_CONNECTED && isRunServer){
                try{
                    socket = mmServerSocket.accept();
                    if(socket != null){
                        mHandler.obtainMessage(BluetoothState.MESSAGE_DEVICE_NAME,-1,-1,socket.getRemoteDevice()).sendToTarget();
                        connected = new Connected(socket);
                        connected.start();
                        break;
                    }
                }catch (IOException ex){
                    SocketClose();
                    setState(0);
                    mHandler.obtainMessage(BluetoothState.MESSAGE_TOAST,"[Server]Not Acppet RUN. ERROR").sendToTarget();
                    break;
                }
            }
        }
    public void SocketClose() {
        try {
            mmServerSocket.close();
            mmServerSocket = null;
        } catch (IOException e) {
            Log.e(TAG,e.toString());
        }
    }
    public void CloseServer(){
        this.isRunServer =false;
    }
}
    private class Connected extends Thread {
        private BluetoothSocket socket = null;
        private InputStream in;
        private OutputStream out;
        private StringBuilder builder = new StringBuilder();
        public Connected(BluetoothSocket socket) {
            try{
                this.socket = socket;
                in = socket.getInputStream();
                out = socket.getOutputStream();
                setState(3);
            }catch (IOException ex){
                setState(0);
                mHandler.obtainMessage(BluetoothState.MESSAGE_TOAST,"[Server]Not Connected.Input Output . ERROR ").sendToTarget();
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
                    mHandler.obtainMessage(BluetoothState.MESSAGE_TOAST,"[Server]Not Connected.Input Output  READ . ERROR").sendToTarget();
                    break;
                }
            }
        }
        public void Write(String command)  {
            try{
                out.write((command+"\r").getBytes());
                mHandler.obtainMessage(BluetoothState.MESSAGE_WRITE,-1,-1,command).sendToTarget();
            }catch (IOException ex){
                mHandler.obtainMessage(BluetoothState.MESSAGE_TOAST,"[Server]Error Send Message").sendToTarget();
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
