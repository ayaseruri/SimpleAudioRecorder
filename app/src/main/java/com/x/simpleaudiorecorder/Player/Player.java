package com.x.simpleaudiorecorder.player;

import android.media.MediaPlayer;

import java.io.File;
import java.io.IOException;

/**
 * Created by wufeiyang on 2016/12/1.
 */

public class Player {

    private IPlayer mIPlayer;
    private MediaPlayer mMediaPlayer;
    private boolean isPlaying;

    public void playerInit(IPlayer iPlayer){
        mIPlayer = iPlayer;
        isPlaying = false;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                isPlaying = false;
                if(null != mIPlayer){
                    mIPlayer.onComplete();
                }
            }
        });
    }

    public void playerStart(String mediaFilePath){
        try {
            mMediaPlayer.setDataSource(mediaFilePath);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mMediaPlayer.start();
                    isPlaying = true;
                    if(null != mIPlayer){
                        mIPlayer.onPlayStart();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playerPause(){
        if(null != mMediaPlayer){
            mMediaPlayer.pause();
            isPlaying = false;
            if(null != mIPlayer){
                mIPlayer.onPlayPause();
            }
        }
    }

    public void playerStop(){
        if(null != mMediaPlayer){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            isPlaying = false;
            if(null != mIPlayer){
                mIPlayer.onPlayStop();
            }
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getCurrentPosition(){
        if(null != mMediaPlayer){
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration(){
        if(null != mMediaPlayer){
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public interface IPlayer{
        void onPlayStart();
        void onPlayPause();
        void onComplete();
        void onPlayStop();
    }
}
