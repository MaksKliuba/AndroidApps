package com.maks_kliuba.rotarydialer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;

import java.util.ArrayList;
import java.util.List;

public class RotaryDialerView extends View {

    public interface DialListener {
        void onDial(int number);
    }

    Activity activity;
    private final List<DialListener> dialListeners = new ArrayList<DialListener>();
    private float rotorAngle, lastRotorAngle;
    private final Drawable rotorDrawable;
    private double lastFi, startFi;
    private boolean pressed = false, isAnim = false;
    private int number;
    private int rotarySound, rotaryClick, rotaryStop, numPoint;
    private boolean[] arrayArea = new boolean[11];
    private int delta = 20;
    private int[] arrayNumber = {315+delta, 45+delta, 75+delta , 105+delta, 135+delta, 165+delta, 195+delta, 225+delta, 255+delta, 285+delta};

    public RotaryDialerView(Context context) {
        this(context, null);
    }

    public RotaryDialerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotaryDialerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        rotorDrawable = context.getResources().getDrawable(R.drawable.rotary_dialer);
    }

    public void addDialListener(DialListener listener, Activity activity) {
        dialListeners.add(listener);
        this.activity = activity;

        rotarySound = R.raw.long_rotary_loud;
        rotaryClick = R.raw.num_click2;
        rotaryStop = R.raw.rotary_stop4;
        numPoint = R.raw.check_point2;
    }

    public void removeDialListener(DialListener listener) {
        dialListeners.remove(listener);
    }

    private void fireDialListenerEvent(int number) {
        for (DialListener listener : dialListeners) {
            listener.onDial(number);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int availableWidth = getRight() - getLeft();
        int availableHeight = getBottom() - getTop();

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        canvas.save();

        rotorDrawable.setBounds(0, 0, getWidth(), getHeight());

        if (rotorAngle != 0) {
            canvas.rotate(rotorAngle, x, y);
            lastRotorAngle = rotorAngle;
        }

        rotorDrawable.draw(canvas);

        canvas.restore();
    }

    private int checkArea(int fi)
    {
        if(fi > 30 +delta && fi <= 60+delta) return 1;
        else if(fi > 60+delta && fi <= 90+delta) return 2;
        else if(fi > 90+delta && fi <= 120+delta) return 3;
        else if(fi > 120+delta && fi <= 150+delta) return 4;
        else if(fi > 150+delta && fi <= 180+delta) return 5;
        else if(fi > 180+delta && fi <= 210+delta) return 6;
        else if(fi > 210+delta && fi <= 240+delta) return 7;
        else if(fi > 240+delta && fi <= 270+delta) return 8;
        else if(fi > 270+delta && fi <= 300+delta) return 9;
        else if(fi > 300+delta && fi <= 330+delta) return 0;
        else if(fi >= 0 && fi <= 10) return 10;
        else return -1;
    }

    private boolean checkArrayArea()
    {
        if(number == 0)
        {
            for(int i = 0; i < 10; i++)
            {
                if(!arrayArea[i])
                    return false;
            }
        }
        else
        {
            for(int i = 1; i <= number; i++)
            {
                if(!arrayArea[i])
                    return false;
            }
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(!isAnim)
        {
            final int x0 = getWidth() / 2;
            final int y0 = getHeight() / 2;
            final int r1 = getWidth() / 5;
            final int r2 = getHeight() / 2;
            int x1 = (int) event.getX();
            int y1 = (int) event.getY();
            int x = x0 - x1;
            int y = y0 - y1;
            double r = Math.sqrt(x * x + y * y);
            double sinfi = y / r;
            int fi = (int) Math.toDegrees(Math.asin(sinfi));

            if(x1 >= x0 && y0 >= y1) fi += delta;
            else if (x1 < x0 && y0 >= y1) fi = 180 + delta - fi;
            else if (x1 <= x0 && y1 > y0) fi = 180 + delta - fi;
            else if (x0 < x1 && y1 > y0)
            {
                fi += 360 + delta;

                if(fi > 360)
                    fi -= 360;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:

                    if(pressed)
                    {
                        if (r > r1 && r < r2)
                        {
                            rotorAngle = (float)(startFi - fi);

                            if(rotorAngle > lastRotorAngle)
                            {
                                lastFi = fi;

                                int i = checkArea(fi);

                                if(i >= 0 && !arrayArea[i])
                                {
                                    arrayArea[i] = true;
                                    ((MainActivity) activity).activityService.click(numPoint);
                                }

                                if(i == 10)
                                {
                                    if(checkArrayArea())
                                    {
                                        if(((MainActivity) activity).activityService.getVolume() != 0)
                                            ((MainActivity) activity).activityService.vibrate(30);

                                        pressed = false;
                                        fireDialListenerEvent(number);
                                    }
                                    else
                                    {
                                        pressed = false;
                                        rotorAngle = lastRotorAngle;
                                    }

                                }

                                invalidate();
                            }
                            else
                            {
                                if(checkArea(fi) == 10)
                                {
                                    pressed = false;
                                    rotorAngle = lastRotorAngle;
                                }
                            }

                            return true;
                        }
                        else
                        {
                            animationReturn();
                            return true;
                        }
                    }
                    else
                    {
                        return true;
                    }


                case MotionEvent.ACTION_DOWN:

                    if (r > r1 && r < r2) {

                        number = checkArea(fi);

                        ((MainActivity) activity).activityService.click(rotaryClick);

                        if(number >= 0 && number <=9)
                        {
                            startFi = arrayNumber[number];
                            pressed = true;
                        }
                        else
                        {
                            pressed = false;
                        }
                    }
                    else
                    {
                        pressed = false;
                    }

                    return true;

                case MotionEvent.ACTION_UP:

                    animationReturn();
                    return true;

                default:
                    break;
            }
        }

        return super.onTouchEvent(event);
    }

    void animationReturn()
    {
        pressed = false;

        if(rotorAngle != 0)
        {
            final float angle = Math.abs(rotorAngle);

            lastRotorAngle = 0;
            rotorAngle = 0;

            post(new Runnable() {
                public void run() {
                    float fromDegrees = angle;
                    Animation anim = new RotateAnimation(fromDegrees, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    anim.setInterpolator(AnimationUtils.loadInterpolator(getContext(), android.R.anim.decelerate_interpolator));
                    anim.setDuration((long) (angle * 4));

                    anim.setAnimationListener(new Animation.AnimationListener() {

                        public MediaPlayer mediaPlayer;

                        @Override
                        public void onAnimationStart(Animation animation) {
                            mediaPlayer = ((MainActivity)activity).activityService.playMedia(rotarySound);
                            isAnim = true;
                        }

                        @Override
                        public void onAnimationEnd(Animation animation)
                        {
                            ((MainActivity)activity).activityService.click(rotaryStop);
                            try {
                                Thread.sleep(60);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            ((MainActivity)activity).activityService.stopMedia(mediaPlayer);

                            isAnim = false;
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });

                    startAnimation(anim);
                }
            });
        }

        for(int i = 0; i < arrayArea.length; i++)
            arrayArea[i] = false;
    }
}