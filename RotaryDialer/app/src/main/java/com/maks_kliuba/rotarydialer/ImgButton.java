package com.maks_kliuba.rotarydialer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImgButton
{
    Activity activity;
    ImageView imageView;
    Type type;
    String code, code2;
    boolean state;
    Handler handel;

    static ArrayList<ImgButton> imgButtons = new ArrayList<>();
    private static long TIME_PRESSED = 750;

    ImgButton(Activity activity, ImageView imageView, Type type, String code)
    {
        this.activity = activity;
        this.imageView = imageView;
        this.type = type;
        this.code = code;
        this.code2 = null;

        this.setOnTouchListener();

        imgButtons.add(this);
    }

    ImgButton(Activity activity, ImageView imageView, Type type, String code, String code2)
    {
        this(activity, imageView, type, code);
        this.code2 = code2;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchListener()
    {
        handel = new Handler();

        imageView.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        updateAll(Update.UP);
                        down();
                        ((MainActivity)activity).analyzeButton(ImgButton.this, false);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        up();
                        break;
                }

                return true;
            }
        });
    }

    static boolean isSomethingPressed()
    {
        for(ImgButton imgButton: imgButtons)
        {
            if(imgButton.state)
                return true;
        }

        return false;
    }

    static void updateAll(Update update)
    {
        for(ImgButton imgButton: imgButtons)
        {
            switch (update)
            {
                case DOWN: imgButton.down(); break;
                case UP: imgButton.up(); break;
                case SWITCH: imgButton._switch(); break;
                case UPDATE: imgButton.update(); break;
            }
        }
    }

    Runnable run = new Runnable() {

        @Override
        public void run() {
            ((MainActivity)activity).analyzeButton(ImgButton.this, true);
        }
    };

    void down()
    {
        if (code2 != null) {
            handel.postDelayed(run, TIME_PRESSED);
        }

        state = true;

        imageView.setAlpha(0.5f);
    }

    void up()
    {
        if (code2 != null)
            handel.removeCallbacks(run);

        state = false;

        imageView.setAlpha(1f);
    }

    void _switch()
    {
        state = !state;

        update();
    }

    void update()
    {
        if (state) down();
        else up();
    }

    enum Type
    {
        ACTION,
        NUMBER;
    }

    enum Update
    {
        DOWN,
        UP,
        SWITCH,
        UPDATE;
    }
}
