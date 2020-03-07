package com.maks_kliuba.robotcontroller;

import android.view.View;
import android.widget.ImageView;

public class ScramButton extends ImgButton
{
    String command, commandOn, commandOff;

    ScramButton(MainActivity activity, ImageView imageView, int imageOff, int imageOn, String commandOn, String commandOff)
    {
        super(activity, imageView, imageOff, imageOn);
        this.commandOn = commandOn;
        this.commandOff = commandOff;

        this.setOnClickListener();
    }

    private void setOnClickListener()
    {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.analyzeSwiches(ScramButton.this);
            }
        });
    }

    @Override
    void updateButton()
    {
        if(state) {
            imageView.setImageResource(imageOn);
            command = commandOn;
        }
        else {
            imageView.setImageResource(imageOff);
            command = commandOff;
        }
    }
}
