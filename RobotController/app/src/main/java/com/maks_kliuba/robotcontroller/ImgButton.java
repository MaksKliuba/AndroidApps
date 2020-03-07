package com.maks_kliuba.robotcontroller;

import android.view.View;
import android.widget.ImageView;

public abstract class ImgButton
{
    MainActivity mainActivity;
    ImageView imageView;
    int imageOn, imageOff;
    boolean state;

    ImgButton(MainActivity mainActivity, ImageView imageView, int imageOff, int imageOn)
    {
        this.mainActivity = mainActivity;
        this.imageView = imageView;
        this.imageOn = imageOn;
        this.imageOff = imageOff;
    }

    abstract void updateButton();


    void switchButton()
    {
        state = !state;

        updateButton();
    }

    void on()
    {
        state = true;

        updateButton();
    }

    void off()
    {
        state = false;

        updateButton();
    }
}
