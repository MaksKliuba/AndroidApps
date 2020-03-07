package com.maks_kliuba.robotcontroller;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    ArrayList<BluetoothDevice> devices;
    BluetoothService bluetoothService;
    final int MENU_DISCONNECTED = 1;
    final int MENU_CONNECTED = 2;

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    ImageView settingsButton;

    ScramButton bluetoothButton;
    ScramButton ledButton;
    ScramButton irButton;

    Slider soundSlider;
    Slider speedSlider;

    PushButton arrowF;
    PushButton arrowB;
    PushButton arrowL;
    PushButton arrowR;
    PushButton arrowTL;
    PushButton arrowTR;
    PushButton buttonStop;

    String address, deviceName;
    Handler h = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        try {
            settingsButton = (ImageView) findViewById(R.id.settingsButton);
            bluetoothButton = new ScramButton(this, (ImageView) findViewById(R.id.bluetoothButton), R.drawable.bluetooth_button3_off, R.drawable.bluetooth_button_on, "", "");
            ledButton = new ScramButton(this, (ImageView) findViewById(R.id.ledButton), R.drawable.led_button_off, R.drawable.led_button_on, "lOn", "lOff");
            irButton = new ScramButton(this, (ImageView) findViewById(R.id.irButton), R.drawable.ir_button_off, R.drawable.ir_button_on, "irOn", "irOff");

            soundSlider = new Slider(this, (SeekBar) findViewById(R.id.soundSlider), 0);
            speedSlider = new Slider(this, (SeekBar) findViewById(R.id.speedSlider), 1);

            arrowF = new PushButton(this, (ImageView) findViewById(R.id.arrowF), R.drawable.arrow, R.drawable.arrow_pressed, "F");
            arrowB = new PushButton(this, (ImageView) findViewById(R.id.arrowB), R.drawable.arrow, R.drawable.arrow_pressed,"B");
            arrowL = new PushButton(this, (ImageView) findViewById(R.id.arrowL), R.drawable.arrow, R.drawable.arrow_pressed, "L");
            arrowR = new PushButton(this, (ImageView) findViewById(R.id.arrowR), R.drawable.arrow, R.drawable.arrow_pressed, "R");
            arrowTL = new PushButton(this, (ImageView) findViewById(R.id.arrowTL), R.drawable.arrow_turn, R.drawable.arrow_turn_pressed, "TL");
            arrowTR = new PushButton(this, (ImageView) findViewById(R.id.arrowTR), R.drawable.arrow_turn, R.drawable.arrow_turn_pressed, "TR");
            buttonStop = new PushButton(this, (ImageView) findViewById(R.id.buttonStop), R.drawable.button_stop_2, R.drawable.button_stop_pressed, "S");

            registerForContextMenu(bluetoothButton.imageView);
            registerForContextMenu(settingsButton);

            bluetoothService = new BluetoothService(this, bluetoothButton);
        }
        catch (NullPointerException e)
        {
            Log.d(bluetoothService.TAG, "LOL: " + e.getMessage() + ".");
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(bluetoothService.isConnected)
        {
            analyzeON(buttonStop);
            analyzeOFF(buttonStop);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(bluetoothService.isConnected)
            bluetoothService.disconnect();
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void analyzeSwiches(ScramButton scramButton)
    {
        if(scramButton != bluetoothButton)
        {
            scramButton.switchButton();

            bluetoothService.sendData(scramButton.command);
        }
        else if(scramButton == bluetoothButton)
        {
            if(bluetoothService.checkBTState()) {
                createConnectionMenu();
            }
        }
    }

    private void createConnectionMenu()
    {
        PopupMenu connectionMenu = new PopupMenu(MainActivity.this, bluetoothButton.imageView);

        devices = bluetoothService.getDevices();

        for(int i = 0; i < devices.size(); i++)
        {
            connectionMenu.getMenu().add(0, i, 0, devices.get(i).getName() + ": " + devices.get(i).getAddress());
        }

        connectionMenu.show();

        connectionMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                address = devices.get(menuItem.getItemId()).getAddress();
                deviceName = devices.get(menuItem.getItemId()).getName();

                h = new Handler();

                final Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run(){

                        bluetoothService.attemptToConnect(address, true);

                        progressDialog.dismiss();

                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                if(bluetoothService.isConnected) {
                                    bluetoothService.infoPrint("Connected to " + deviceName);
                                }
                                else
                                    bluetoothService.infoPrint("Unable to connect device");
                            }
                        });
                    }
                });
                thread.start();

                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Connecting...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setButton(Dialog.BUTTON_POSITIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        thread.interrupt();
                        bluetoothService.disconnect();
                    }
                });
                progressDialog.show();

                return false;
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        if(v == bluetoothButton.imageView)
        {
            if(bluetoothService.isConnected)
            {
                menu.add(0, MENU_DISCONNECTED, 0,"Disconnect from device");
            }
            else
            {
                menu.add(0, MENU_CONNECTED, 0,"Connect to device");
            }
        }
        else if(v == settingsButton)
        {
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                menu.add(0, 3, 0,"PORTRAIT");
            }
            else
            {
                menu.add(0, 4, 0,"LANDSCAPE");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case MENU_DISCONNECTED:
                bluetoothService.disconnect();
                break;
            case MENU_CONNECTED:
                analyzeSwiches(bluetoothButton);
                break;
            case 3:
                bluetoothService.disconnect();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 4:
                bluetoothService.disconnect();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }

        return super.onContextItemSelected(item);
    }

    public void analyzeSlider(String command)
    {
        bluetoothService.sendData(command);
    }

    public void analyzeON(PushButton pushButton)
    {
        String command = "";

        if(pushButton == buttonStop)
        {
            command = buttonStop.command;
            buttonStop.on();
            arrowF.off();
            arrowB.off();
            arrowL.off();
            arrowR.off();
            arrowTL.off();
            arrowTR.off();
        }

        if(pushButton == arrowF && !arrowB.state && !arrowTL.state && !arrowTR.state)
        {
            if(arrowL.state)
                command = arrowF.command + arrowL.command;
            else if(arrowR.state)
                command = arrowF.command + arrowR.command;
            else
                command = arrowF.command;

            arrowF.on();
        }
        else if(pushButton == arrowB && !arrowF.state && !arrowTL.state && !arrowTR.state)
        {
            if(arrowL.state)
                command = arrowB.command + arrowL.command;
            else if(arrowR.state)
                command = arrowB.command + arrowR.command;
            else
                command = arrowB.command;

            arrowB.on();
        }

        if(pushButton == arrowL && !arrowTL.state && !arrowTR.state && !arrowR.state)
        {
            if(arrowF.state)
                command = arrowF.command + arrowL.command;
            else if(arrowB.state)
                command = arrowB.command + arrowL.command;
            else
                command = arrowL.command;

            arrowL.on();
        }
        else if(pushButton == arrowR && !arrowTL.state && !arrowTR.state && !arrowL.state)
        {
            if(arrowF.state)
                command = arrowF.command + arrowR.command;
            else if(arrowB.state)
                command = arrowB.command + arrowR.command;
            else
                command = arrowR.command;

            arrowR.on();
        }

        if(pushButton == arrowTL && !arrowF.state && !arrowB.state && !arrowL.state && !arrowR.state && !arrowTR.state) {
            command = arrowTL.command;
            arrowTL.on();
        }
        else if(pushButton == arrowTR && !arrowF.state && !arrowB.state && !arrowL.state && !arrowR.state && !arrowTL.state) {
            command = arrowTR.command;
            arrowTR.on();
        }

        bluetoothService.sendData(command);
    }

    public void analyzeOFF(PushButton pushButton)
    {
        String command = "";

        if(pushButton.state)
            command = buttonStop.command;

        pushButton.off();

        if(pushButton == buttonStop)
            return;

        if(pushButton == arrowF || pushButton == arrowB)
        {
            if(arrowL.state)
                command = arrowL.command;
            else if(arrowR.state)
                command = arrowR.command;
        }

        if(pushButton == arrowL || pushButton == arrowR)
        {
            if(arrowF.state)
                command = arrowF.command;
            else if(arrowB.state)
                command = arrowB.command;
        }

        bluetoothService.sendData(command);
    }

    void analyzeVoiceData(String command)
    {
        if(command.contains("light") && command.contains("on")) {
            ledButton.on();
        }
        else if(command.contains("light") && command.contains("off")) {
            ledButton.off();
        }

        if(command.contains("infrared") && command.contains("on")) {
            irButton.on();
        }
        else if(command.contains("infrared") && command.contains("off")) {
            irButton.off();
        }

        bluetoothService.sendData(command);
    }

    public void voiceButtonClick(View view)
    {
        startSpeak();
    }

    public void startSpeak()
    {
        Intent intent =  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening...");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK)
        {
            ArrayList commandList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String command = "";

            for (int i = 0; i < commandList.size(); i++) {
                command += commandList.get(i) + " ";
            }

            analyzeVoiceData(command.trim());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void analyzeReceivedData(String command)
    {
        if(command.contains("lOn"))
            ledButton.on();
        else if(command.contains("lOff"))
            ledButton.off();

        if(command.contains("irOn"))
            irButton.on();
        else if(command.contains("irOff"))
            irButton.off();

        if (command.startsWith(soundSlider.number + ":"))
            soundSlider.seekBar.setProgress(Integer.parseInt(command.substring(2)));

        if (command.startsWith(speedSlider.number + ":"))
            speedSlider.seekBar.setProgress(Integer.parseInt(command.substring(2)));
    }

    public void settingsButtonClick(View view)
    {
        PopupMenu settingsMenu = new PopupMenu(this, view);

        settingsMenu.getMenu().add("                INFO");
        settingsMenu.getMenu().add("Forward -> F");
        settingsMenu.getMenu().add("Back -> B");
        settingsMenu.getMenu().add("Left -> L");
        settingsMenu.getMenu().add("Right -> R");
        settingsMenu.getMenu().add("Turn Left -> TL");
        settingsMenu.getMenu().add("Turn Right -> TR");
        settingsMenu.getMenu().add("Forward Left -> FL");
        settingsMenu.getMenu().add("Forward Right -> FR");
        settingsMenu.getMenu().add("Back Left -> BL");
        settingsMenu.getMenu().add("Back Right -> BR");
        settingsMenu.getMenu().add("Stop -> S");

        settingsMenu.getMenu().add("Led On -> lOn");
        settingsMenu.getMenu().add("Led Off -> lOff");
        settingsMenu.getMenu().add("IR-sensor On -> irOn");
        settingsMenu.getMenu().add("IR-sensor Off -> irOff");

        settingsMenu.getMenu().add("Sound -> 0:value");
        settingsMenu.getMenu().add("Speed -> 1:value");

        settingsMenu.show();
    }
}
