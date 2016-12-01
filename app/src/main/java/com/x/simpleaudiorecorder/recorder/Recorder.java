package com.x.simpleaudiorecorder.recorder;

import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.x.simpleaudiorecorder.bean.RecordFileInfo;
import com.x.simpleaudiorecorder.utils.FileUtils;
import com.x.simpleaudiorecorder.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;

/**
 * Created by wufeiyang on 2016/11/30.
 */

public class Recorder {

    private static final int CHANNEL_CONFIG = CHANNEL_IN_MONO;  //只有单声道（CHANNEL_IN_MONO）是所有设备都支持的
    private static final int AUDIO_FORMAT = ENCODING_PCM_16BIT; //只有pcm_16bit是所有设备都支持的
    private static final int SAMPLE_RATE_HZ = 44100;            //只有44100的是所有设备都支持的
    private static final int CHANNEL_COUNT = 1;
    private static final int PCM_FORMAT = 1;
    private static final int BIT_PER_SAMPLE = 16;
    private static final int SUB_CHUNK = 16;

    private AudioRecord mAudioRecord;
    private RandomAccessFile mSaveRandomAccessFile;
    private boolean isRecording;
    private int mMinBufferSize;
    private short mMaxVolume;
    private IORecord mIoRecord;

    public void recordInit(IORecord ioRecord)    {
        mIoRecord = ioRecord;

        mMinBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_HZ,
                CHANNEL_CONFIG, AUDIO_FORMAT);

        Log.d("minBufferSize", "minBufferSize:" + mMinBufferSize);

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC
                , SAMPLE_RATE_HZ
                , CHANNEL_CONFIG
                , AUDIO_FORMAT
                , mMinBufferSize);
    }

    public void recordStart(File saveFile){
        isRecording = true;
        mAudioRecord.startRecording();
        new RecordThread(saveFile).start();
    }

    public void recordStop(){
        if (null != mAudioRecord) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
            isRecording = false;
        }
    }

    public short getMaxVolume(){
        return mMaxVolume;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public interface IORecord{
        void onStart();
        void onRecord();
        void onSuccess(RecordFileInfo info);
        void onError();
    }

    private class RecordThread extends Thread {

        private File mSaveFile;

        private RecordThread(File saveFile) {
            this.mSaveFile = saveFile;
        }

        @Override
        public void run() {
            if(null != mIoRecord){
                mIoRecord.onStart();
            }

            if(!mSaveFile.exists()){
                try {
                    mSaveFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                mSaveRandomAccessFile = new RandomAccessFile(mSaveFile, "rw");

                mSaveRandomAccessFile.setLength(0);
                mSaveRandomAccessFile.writeBytes("RIFF");
                mSaveRandomAccessFile.writeInt(0);
                mSaveRandomAccessFile.writeBytes("WAVE");
                mSaveRandomAccessFile.writeBytes("fmt ");
                mSaveRandomAccessFile.writeInt(Integer.reverseBytes(SUB_CHUNK));
                mSaveRandomAccessFile.writeShort(Short.reverseBytes((short) PCM_FORMAT));
                mSaveRandomAccessFile.writeShort(Short.reverseBytes((short) CHANNEL_COUNT));
                mSaveRandomAccessFile.writeInt(Integer.reverseBytes(SAMPLE_RATE_HZ));
                mSaveRandomAccessFile.writeInt(Integer.reverseBytes(SAMPLE_RATE_HZ * CHANNEL_COUNT * BIT_PER_SAMPLE / 8));
                mSaveRandomAccessFile.writeShort(Short.reverseBytes((short) (CHANNEL_COUNT * BIT_PER_SAMPLE / 8)));
                mSaveRandomAccessFile.writeShort(Short.reverseBytes((short) BIT_PER_SAMPLE));
                mSaveRandomAccessFile.writeBytes("data");
                mSaveRandomAccessFile.writeInt(0);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] bufferData = new byte[mMinBufferSize];
            int readSize, totalSize = 0;
            while (isRecording) {
                readSize = mAudioRecord.read(bufferData, 0, mMinBufferSize);
                if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                    totalSize += readSize;
                    mMaxVolume = getMax(Utils.byteArray2ShortArray(bufferData, readSize/2));
                    try {
                        mSaveRandomAccessFile.write(bufferData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(null != mIoRecord){
                    mIoRecord.onRecord();
                }
            }

            try {
                mSaveRandomAccessFile.seek(4);
                mSaveRandomAccessFile.writeInt(Integer.reverseBytes(36 + totalSize));

                mSaveRandomAccessFile.seek(40);
                mSaveRandomAccessFile.writeInt(Integer.reverseBytes(totalSize));

                if(null != mIoRecord){
                    RecordFileInfo info = new RecordFileInfo();
                    info.setFileName(mSaveFile.getName());
                    info.setFilePath(mSaveFile.getAbsolutePath());
                    mIoRecord.onSuccess(info);
                }

                mSaveRandomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private short getMax(short[] shortBuffer){
            short max = 0;
            for (int i = 0; i < shortBuffer.length; i++){
                if(Math.abs(shortBuffer[i]) > max){
                    max = shortBuffer[i];
                }
            }
            return max;
        }
    }
}
