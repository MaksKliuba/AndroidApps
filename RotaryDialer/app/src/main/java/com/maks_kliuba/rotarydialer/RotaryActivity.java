package com.maks_kliuba.rotarydialer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class RotaryActivity extends MainActivity
{

    ImgButton buttonStar;
    ImgButton buttonPlus;
    ImgButton buttonSharp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotary);

        setResources(this);
    }

    @Override
    public void setResources(Activity activity) {
        super.setResources(activity);

        styleButton = new ImgButton(activity, (ImageView) findViewById(R.id.style_button), ImgButton.Type.ACTION,"toNumberActivity");

        buttonStar = new ImgButton(activity, (ImageView) findViewById(R.id.button_star), ImgButton.Type.NUMBER,"*");
        buttonPlus = new ImgButton(activity, (ImageView) findViewById(R.id.button_plus), ImgButton.Type.NUMBER,"+");
        buttonSharp = new ImgButton(activity, (ImageView) findViewById(R.id.button_sharp), ImgButton.Type.NUMBER,"#");

        RotaryDialerView rotaryDialerView = (RotaryDialerView) findViewById(R.id.rotary_dialer);
        rotaryDialerView.addDialListener(new RotaryDialerView.DialListener() {
            public void onDial(int number) {
                String phoneNumber = display.getText().toString();
                phoneNumber += String.valueOf(number);
                displaySetText(phoneNumber);
            }
        }, this);
    }
}
