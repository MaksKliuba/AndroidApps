package com.maks_kliuba.robotcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothService
{
    static final String TAG = "bluetoothRuby";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inputStream = null;
    private ArrayList<BluetoothDevice> devices;
    private Thread threadReceive;
    private boolean isConnecting = false;

    boolean isConnected = false;

    MainActivity activity;
    ScramButton button;

    BluetoothService(MainActivity activity, ScramButton button)
    {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.activity = activity;
        this.button = button;
    }

    public boolean checkBTState()
    {
        if(isConnecting)
            return false;

        if(btAdapter == null)
        {
            infoPrint("Fatal Error - Bluetooth not supported");

            return false;
        }
        else
        {
            if (btAdapter.isEnabled())
            {
                Log.d(TAG, "...Bluetooth enabled...");

                return true;
            }
            else
            {
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                return false;
            }
        }
    }

    public ArrayList<BluetoothDevice> getDevices()
    {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        devices = new ArrayList<>();

        devices.addAll(pairedDevices);

        return devices;
    }

    public void attemptToConnect(String address, boolean openInputStream)
    {
        if(isConnected)
        {
            disconnect();
        }

        isConnecting = true;

        if(btAdapter.isEnabled() && address != null)
        {
            Log.d(TAG, "...Attempt to connect...");

            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            try
            {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (IOException e)
            {
                Log.d(TAG, "Fatal Error - Socket create failed: " + e.getMessage() + ".");
            }

            btAdapter.cancelDiscovery();

            Log.d(TAG, "...Connecting...");
            try
            {
                btSocket.connect();
                Log.d(TAG, "...Successful connection attempt...");
            }
            catch (IOException e)
            {
                try
                {
                    btSocket.close();
                }
                catch (IOException e2)
                {
                    Log.d(TAG, "Fatal Error - Closing socket failed" + e2.getMessage() + ".");
                }
                catch (NullPointerException e3)
                {
                    Log.d(TAG, "Fatal Error - Closing socket failed" + e3.getMessage() + ".");
                }
            }

            try
            {
                if(btSocket != null && btSocket.isConnected())
                {
                    Log.d(TAG, "...Creating a Stream...");

                    outStream = btSocket.getOutputStream();
                    Log.d(TAG, "...Output Stream...");

                    if(openInputStream) {
                        inputStream = btSocket.getInputStream();
                        Log.d(TAG, "...Input Stream...");
                        receiveData();
                    }

                    isConnected = true;
                    button.on();

                    if(openInputStream)
                        sendData("updateData()");
                    }
                else
                    throw new IOException();
            }
            catch (IOException e)
            {
                Log.d(TAG, "...NULL...");
                device = null;
                btSocket = null;
                outStream = null;
                inputStream = null;
            }
        }

        isConnecting = false;
    }

    void sendData(String command)
    {
        if(command == "" || command == null)
            return;

        //Toast.makeText(activity.getApplicationContext(), command, Toast.LENGTH_SHORT).show();

        if(isConnected)
        {
            Log.d(TAG, "sending data: " + command);

            command += "\n";

            byte[] msgBuffer = command.getBytes();

            try
            {
                outStream.write(msgBuffer);
            }
            catch (IOException e)
            {
                infoPrint("Error - Failed to send data");
                disconnect();
            }
        }
    }

    private void receiveData()
    {
        threadReceive = new Thread(new Runnable() {
            @Override
            public void run(){

                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

                while (true)
                {
                    if(!isConnected)
                        break;

                    try
                    {
                        String line;
                        if ((line = r.readLine()) != null)
                        {
                            Log.d(TAG, "receive: " + line);
                            activity.analyzeReceivedData(line);
                        }
                    }
                    catch (IOException e)
                    {
                        Log.d(TAG, "Error in receiveData: " + e.getMessage() + ".");

                        if(isConnected)
                            disconnect();
                    }
                }
            }
        });
        threadReceive.start();
    }

    void disconnect()
    {
        Log.d(TAG, "...Disconnecting...");

        if (outStream != null)
        {
            try
            {
                outStream.flush();
            }
            catch (IOException e)
            {
                Log.d(TAG, "Fatal Error - Failed to flush output stream: " + e.getMessage() + ".");
            }

            //outStream = null;
        }

        if (inputStream != null)
        {
            try
            {
                threadReceive.interrupt();
                inputStream.close();
            }
            catch (IOException e)
            {
                Log.d(TAG, "Fatal Error - Failed to close input stream: " + e.getMessage() + ".");
            }

            //inputStream = null;
        }

        if (btSocket != null)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e2)
            {
                Log.d(TAG, "Fatal Error - Failed to close socket." + e2.getMessage() + ".");
            }

            //btSocket = null;
        }

        isConnected = false;

        button.off();
    }

    void infoPrint(String message)
    {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, message);
    }
}
