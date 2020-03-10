package com.maks_kliuba.robotcontroller;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class PushButton extends ImgButton
{
    String command;

    PushButton(MainActivity activity, ImageView imageView, int image, int imagePressed, String command)
    {
        super(activity, imageView, image, imagePressed);
        this.command = command;

        this.setOnTouchListener();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchListener()
    {
        imageView.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mainActivity.analyzeON(PushButton.this);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        mainActivity.analyzeOFF(PushButton.this);
                        break;
                }

                return true;
            }
        });
    }

    @Override
    void updateButton()
    {
        if(state)
            imageView.setImageResource(imageOn);
        else
            imageView.setImageResource(imageOff);
    }
}
