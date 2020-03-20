package com.maks_kliuba.rotarydialer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

import java.util.ArrayList;

public class ActivityService
{
    Activity activity;

    private AudioManager manager;
    private Vibrator vibrator;
    ArrayList<MediaPlayer> media = new ArrayList<>();

    ActivityService(Activity activity)
    {
        this.activity = activity;

        manager = (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void click(int resid)
    {
        if(manager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0)
        {
            vibrate(20);
        }
        else
        {
            playMedia(resid);
        }
    }

    public MediaPlayer playMedia(int resid)
    {
        MediaPlayer mediaPlayer = MediaPlayer.create(activity, resid);
        mediaPlayer.start();

        media.add(mediaPlayer);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(mediaPlayer != null)
                {
                    media.remove(mediaPlayer);
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        });

        return mediaPlayer;
    }

    public void stopMedia(MediaPlayer mediaPlayer)
    {
        if(mediaPlayer != null && media.contains(mediaPlayer) && mediaPlayer.isPlaying())
        {
            media.remove(mediaPlayer);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void stopAll()
    {
        for (MediaPlayer el: media) {
            stopMedia(el);
        }

        media.clear();
    }

    public void vibrate(int time)
    {
        vibrator.vibrate(time);
    }

    public int getVolume()
    {
        return manager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
}
