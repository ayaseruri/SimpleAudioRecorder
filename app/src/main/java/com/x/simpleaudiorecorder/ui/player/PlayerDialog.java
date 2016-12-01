package com.x.simpleaudiorecorder.ui.player;

import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.x.simpleaudiorecorder.R;
import com.x.simpleaudiorecorder.player.Player;

/**
 * Created by wufeiyang on 2016/12/1.
 */

public class PlayerDialog extends Dialog implements View.OnClickListener{

    private static final int PLAY_START = 0;
    private static final int PLAY_PAUSE = 1;
    private static final int PLAY_COMPLETE = 2;

    private Player mPlayer;
    private Player.IPlayer mIPlayer;
    private String mMediaFilePath;
    private ImageButton mPlayBtn;
    private PlayerProgressBar mSeekBar;

    public PlayerDialog(Context context, String mediaFileName, String mediaFilePath) {
        super(context);
        this.mMediaFilePath = mediaFilePath;

        mPlayer = new Player();
        mIPlayer = new Player.IPlayer() {
            @Override
            public void onPlayStart() {
                mHandler.sendEmptyMessage(PLAY_START);
            }

            @Override
            public void onPlayPause() {
                mHandler.sendEmptyMessage(PLAY_PAUSE);
            }

            @Override
            public void onComplete() {
                mHandler.sendEmptyMessage(PLAY_COMPLETE);
            }

            @Override
            public void onPlayStop() {

            }
        };

        setContentView(R.layout.player_content_view);
        TextView fileName = (TextView) findViewById(R.id.file_name);
        fileName.setText(mediaFileName);

        mPlayBtn = (ImageButton) findViewById(R.id.play_btn);
        mPlayBtn.setOnClickListener(this);

        mSeekBar = (PlayerProgressBar) findViewById(R.id.seek_bar);

        mPlayer.playerInit(mIPlayer);
        mPlayer.playerStart(mMediaFilePath);
    }

    @Override
    public void onClick(View view) {
        if(null != mPlayer){
            if(mPlayer.isPlaying()){
                mPlayer.playerPause();
            }else {
                mPlayer.playerInit(mIPlayer);
                mPlayer.playerStart(mMediaFilePath);
            }
        }
    }

    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        if(null != window){
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PLAY_START:
                    mPlayBtn.setSelected(true);
                    mSeekBar.progressInit(mPlayer);
                    mSeekBar.progressStart();
                    break;
                case PLAY_PAUSE:
                    mPlayBtn.setSelected(false);
                    mSeekBar.progressStop();
                    break;
                case PLAY_COMPLETE:
                    mPlayBtn.setSelected(false);
                    mSeekBar.progressStop();
                    break;
                default:
                    break;
            }
        }
    };
}
