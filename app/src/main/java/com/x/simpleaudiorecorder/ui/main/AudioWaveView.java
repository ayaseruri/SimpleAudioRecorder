package com.x.simpleaudiorecorder.ui.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.x.simpleaudiorecorder.R;
import com.x.simpleaudiorecorder.recorder.Recorder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EView;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.res.ColorRes;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wufeiyang on 2016/11/30.
 */
@EView
public class AudioWaveView extends View {

    private static final int MIN_TIME_SPACE = 100;  //最小的取样间隔
    private static final int DEFAULT_THREADHOLD = 4000;
    private static final int MAX_VOLUME = 8000;     //最大的音量等级是8000
    private static final int MSG_UPDATE_UI = 0;
    private static final int PIX_SPACE = 10;
    private static final float STROKE_WIDTH = 5f;

    @ColorRes(R.color.colorPrimary)
    int mWaveColor;
    @ColorRes(R.color.colorAccent)
    int mThreadHoldColor;

    private Recorder mRecorder;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Paint mPaint;
    private Path mPath;
    private ArrayList<Point> mPoints;
    private int mThreadHold = DEFAULT_THREADHOLD;

    public AudioWaveView(Context context) {
        super(context);
    }

    public AudioWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @AfterViews
    void init(){
        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(STROKE_WIDTH);
    }

    private Handler mHandler = new UIHandler(this);
    private static class UIHandler extends Handler{

        WeakReference<AudioWaveView> waveViewWeakReference;

        public UIHandler(AudioWaveView view) {
            waveViewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AudioWaveView view = waveViewWeakReference.get();
            if(null != view){
                switch (msg.what){
                    case MSG_UPDATE_UI:
                        view.invalidate();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void init(Recorder recorder){
        mRecorder = recorder;

        mPoints = new ArrayList<>();
        post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < getWidth() / 2; i += PIX_SPACE){
                    Point point = new Point(i, getHeight() / 2);
                    mPoints.add(point);
                }
                invalidate();
            }
        });
    }

    public void waveStart(long time){
        if(time < MIN_TIME_SPACE){
            time = MIN_TIME_SPACE;
        }

        if(null != mTimer){
            mTimer.cancel();
        }

        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Point point = new Point(getWidth() / 2, getY(mRecorder.getMaxVolume()));
                mPoints.add(point);
                mHandler.sendEmptyMessage(MSG_UPDATE_UI);
            }
        };

        mTimer.schedule(mTimerTask, time, time);
    }

    public void waveStop(){
        if(null != mTimer){
            mTimer.cancel();
            mTimer = null;
        }
        if(null != mHandler){
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public int getThreadHold() {
        return mThreadHold;
    }

    public void setThreadHold(int mThreadHold) {
        this.mThreadHold = mThreadHold;
    }

    private int getY(int volume){
        return getHeight() / 2 - volume * getHeight() / (2 * MAX_VOLUME) ;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mPoints.size(); i ++){
            mPoints.get(i).x -= PIX_SPACE;
            if(mPoints.get(i).x < 0){
                mPoints.remove(i);
            }
        }

        mPath.reset();
        for (int i = 0; i < mPoints.size(); i ++){
            if(0 == i){
                mPath.moveTo(mPoints.get(i).x, mPoints.get(i).y);
            }else {
                mPath.lineTo(mPoints.get(i).x, mPoints.get(i).y);
            }
        }

        mPaint.setColor(mWaveColor);
        canvas.drawPath(mPath, mPaint);

        mPaint.setColor(mThreadHoldColor);

        mPath.reset();
        mPath.moveTo(0, getY(mThreadHold));
        mPath.lineTo(getWidth(), getY(mThreadHold));
        canvas.drawPath(mPath, mPaint);

        mPath.reset();
        mPath.moveTo(0, getY(-mThreadHold));
        mPath.lineTo(getWidth(), getY(-mThreadHold));
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        waveStop();
    }
}
