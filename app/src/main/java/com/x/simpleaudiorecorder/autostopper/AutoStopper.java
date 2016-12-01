package com.x.simpleaudiorecorder.autostopper;

import com.x.simpleaudiorecorder.recorder.Recorder;

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

        private Recorder mRecorder;
        private int mTime, mThreadHold;

        public Checker(Recorder recorder,int time, int threadHold) {
            this.mRecorder = recorder;
            this.mTime = time;
            this.mThreadHold = threadHold;
        }

        @Override
        public void run() {
            long timePass = 0;
            do {
                if(mRecorder.getMaxVolume() > mThreadHold){
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
                    mRecorder.recordStop();
                }
            }while (null != mRecorder && mRecorder.isRecording());
        }
    }
}
