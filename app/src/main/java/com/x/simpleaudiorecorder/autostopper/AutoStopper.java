package com.x.simpleaudiorecorder.autostopper;

import com.x.simpleaudiorecorder.recorder.Recorder;

import java.lang.ref.WeakReference;

/**
 * Created by wufeiyang on 2016/12/1.
 */

public class AutoStopper {

    private static final int TIME_SPACE = 100;

    /*
    time            :持续时间，单位ms
    threadHold      :音量
     */
    public void check(Recorder recorder, int time, int threadHold){
        new Checker(recorder, time, threadHold).start();
    }

    private static class Checker extends Thread{

        private WeakReference<Recorder> mRecorderWRf;
        private int mTime, mThreadHold;

        public Checker(Recorder recorder,int time, int threadHold) {
            mRecorderWRf = new WeakReference<>(recorder);
            this.mTime = time;
            this.mThreadHold = threadHold;
        }

        @Override
        public void run() {
            long timePass = 0;
            Recorder recorder = mRecorderWRf.get();
            do {
                if(recorder.getMaxVolume() > mThreadHold){
                    timePass = 0;
                }else {
                    timePass += TIME_SPACE;
                }

                if((mTime - timePass) > 0){
                    try {
                        sleep(TIME_SPACE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {
                    recorder.recordStop();
                }
            }while (null != recorder && recorder.isRecording());
        }
    }
}
