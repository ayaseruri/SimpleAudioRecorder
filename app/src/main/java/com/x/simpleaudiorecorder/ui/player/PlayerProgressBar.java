package com.x.simpleaudiorecorder.ui.player;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

import com.x.simpleaudiorecorder.player.Player;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wufeiyang on 2016/12/1.
 */
@EView
public class PlayerProgressBar extends AppCompatSeekBar {

    private static final int MSG_UPDATE_UI = 0;
    private static final int TIME_SPACE = 1000;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private Player mPlayer;

    public PlayerProgressBar(Context context) {
        super(context);
    }

    public PlayerProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void progressInit(Player player){
        mPlayer = player;
        setMax(mPlayer.getDuration());
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UPDATE_UI:
                    if(mPlayer.isPlaying()){
                        setProgress(mPlayer.getCurrentPosition());
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void progressStart(){
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(MSG_UPDATE_UI);
            }
        };
        mTimer.schedule(mTimerTask, TIME_SPACE, TIME_SPACE);
    }

    public void progressStop(){
        if(null != mTimer){
            mTimer.cancel();
            mTimer = null;
        }

        setProgress(0);
        setMax(0);
    }
}
