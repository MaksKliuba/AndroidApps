package com.maks_kliuba.robotcontroller;

import android.view.View;
import android.widget.SeekBar;

public class Slider
{
    static MainActivity activity;
    SeekBar seekBar;
    int number;

    Slider(MainActivity activity, SeekBar seekBar, int number)
    {
        this.activity = activity;
        this.seekBar = seekBar;
        this.number = number;

        this.setOnSeekBarChangeListener();
    }

    private void setOnSeekBarChangeListener()
    {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //activity.analyzeSlider(number + ":" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                activity.analyzeSlider(number + ":" + seekBar.getProgress());
            }
        });
    }
}
