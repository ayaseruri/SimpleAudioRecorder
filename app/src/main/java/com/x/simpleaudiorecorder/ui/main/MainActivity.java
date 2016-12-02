package com.x.simpleaudiorecorder.ui.main;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.x.simpleaudiorecorder.R;
import com.x.simpleaudiorecorder.autostopper.AutoStopper;
import com.x.simpleaudiorecorder.bean.RecordFileInfo;
import com.x.simpleaudiorecorder.recorder.Recorder;
import com.x.simpleaudiorecorder.utils.FileUtils;
import com.x.simpleaudiorecorder.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;
import cn.pedant.SweetAlert.SweetAlertDialog;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private static final int MSG_PERMISSION_ALERT = 0;

    private static final int MSG_RECORD_START = 1;
    private static final int MSG_RECORD_ING = 2;

    private static final int MSG_RECORD_SUCCESS = 3;
    private static final int MSG_RECORD_ERROR = 4;

    private static final int MSG_CONVERT_BEIGIN = 5;
    private static final int MSG_CONVERT_SUCCESS = 6;
    private static final int MSG_CONVERT_ERROR = 7;

    private static final int TIME_TO_STOP = 3000;   //不说话TIME_TO_STOP ms 后自动停止录音

    private static final int THREADHOLD = 1000;     //不说话定义为音量小于THREADHOLD

    private static final String KEY_RECORD_FILE_INFO = "file_info";

    @ViewById(R.id.audio_wave_view)
    AudioWaveView mAudioWaveView;
    @ViewById(R.id.fab_progress)
    FABProgressCircle mFabProgress;
    @ViewById(R.id.fab)
    FloatingActionButton mFab;
    @ViewById(R.id.record_result_view)
    RecordResultView mRecordResultView;
    @ViewById(R.id.app_bar_layout)
    AppBarLayout mAppBarLayout;
    @ViewById(R.id.stop_time_hint)
    TextView mStopTimeHint;

    private Recorder mRecorder;
    private AutoStopper mAutoStopper;
    private Recorder.IORecord mIonRecord;
    private IConvertCallback mIConvertCallback;
    private RxPermissions mRxPermissions;

    @AfterViews
    void init(){
        mRxPermissions = new RxPermissions(this);

        mAutoStopper = new AutoStopper();

        mRecorder = new Recorder();
        mIonRecord = new Recorder.IORecord() {
            @Override
            public void onStart() {
                mHandler.sendEmptyMessage(MSG_RECORD_START);
            }

            @Override
            public void onRecord() {
                mHandler.sendEmptyMessage(MSG_RECORD_ING);
            }

            @Override
            public void onSuccess(RecordFileInfo info) {
                Message message = new Message();
                message.what = MSG_RECORD_SUCCESS;
                setMsgData(message, info);
                mHandler.sendMessage(message);

                mHandler.sendEmptyMessage(MSG_CONVERT_BEIGIN);
                AndroidAudioConverter.with(MainActivity.this)
                        .setFile(new File(info.getFilePath()))
                        .setFormat(AudioFormat.FLAC)
                        .setCallback(mIConvertCallback)
                        .convert();
            }

            @Override
            public void onError(Exception e) {
                mHandler.sendEmptyMessage(MSG_RECORD_ERROR);
            }
        };

        mIConvertCallback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                RecordFileInfo info = new RecordFileInfo();
                info.setFileName(convertedFile.getName());
                info.setFilePath(convertedFile.getAbsolutePath());

                Message message = new Message();
                message.what = MSG_CONVERT_SUCCESS;
                setMsgData(message, info);
                mHandler.sendMessage(message);
            }
            @Override
            public void onFailure(Exception error) {
                error.printStackTrace();
            }
        };

        mAudioWaveView.init(mRecorder);
        mAudioWaveView.setThreadHold(THREADHOLD);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarOnOffsetChangedListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, int state) {
                switch (state){
                    case EXPANDED:
                        mFab.show();
                        break;
                    case COLLAPSED:
                        mFab.hide();
                        break;
                    default:
                        break;
                }
            }
        });

        mFabProgress.attachListener(new FABProgressListener() {
            @Override
            public void onFABProgressAnimationEnd() {
                mFab.setImageResource(R.drawable.ic_record_start);
            }
        });

        mStopTimeHint.setText(String.format(getString(R.string.stop_record_time_hint)
                , TIME_TO_STOP / 1000));
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecordResultView();
    }

    @Click(R.id.fab)
    void onFab(){
        if(mRecorder.isRecording()){
            mRecorder.recordStop();
        }else {
            mRxPermissions.request(Manifest.permission.RECORD_AUDIO
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean ok) {
                            if(ok){
                                File saveFile = new File(FileUtils.getExternalCacheDir(MainActivity.this)
                                        , "record_" + System.currentTimeMillis() + ".wav");
                                mRecorder.recordInit(mIonRecord);
                                mRecorder.recordStart(saveFile);
                                mAutoStopper.check(mRecorder, TIME_TO_STOP, THREADHOLD);
                            }else {
                                mHandler.sendEmptyMessage(MSG_PERMISSION_ALERT);
                            }
                        }
                    });
        }
    }

    private void initRecordResultView(){
        Observable.create(new Observable.OnSubscribe<List<RecordFileInfo>>() {
            @Override
            public void call(Subscriber<? super List<RecordFileInfo>> subscriber) {
                File[] files = FileUtils.getFileWithExtension(
                        FileUtils.getExternalCacheDir(MainActivity.this).getAbsolutePath()
                        , ".wav", ".flac");
                ArrayList<RecordFileInfo> infos = new ArrayList<>();
                for (File file : files){
                    RecordFileInfo info = new RecordFileInfo();
                    info.setFileName(file.getName());
                    info.setFilePath(file.getAbsolutePath());
                    infos.add(info);
                }
                subscriber.onNext(infos);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<RecordFileInfo>>() {
                    @Override
                    public void call(List<RecordFileInfo> recordFileInfos) {
                        mRecordResultView.refresh(recordFileInfos);
                    }
                });
    }

    private void setMsgData(Message message, Parcelable data){
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_RECORD_FILE_INFO, data);
        message.setData(bundle);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RecordFileInfo info;
            switch (msg.what){
                case MSG_PERMISSION_ALERT:
                    SweetAlertDialog dialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
                    dialog.setTitleText(getString(R.string.record_permission_alert_title));
                    dialog.setContentText(getString(R.string.record_permission_alert_detail));
                    dialog.setConfirmText(getString(R.string.settings));
                    dialog.setCancelText(getString(R.string.cancle));
                    dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            Utils.startAppSysSetting(MainActivity.this);
                        }
                    });
                    dialog.show();
                    break;
                case MSG_RECORD_START:
                    mAudioWaveView.waveStart(100);
                    mFab.setImageResource(R.drawable.ic_record_stop);
                    break;
                case MSG_RECORD_ING:
                    break;
                case MSG_RECORD_SUCCESS:
                    mAudioWaveView.waveStop();
                    info = msg.getData().getParcelable(KEY_RECORD_FILE_INFO);
                    mRecordResultView.add(info);
                    break;
                case MSG_RECORD_ERROR:
                    mAudioWaveView.waveStop();
                    break;
                case MSG_CONVERT_BEIGIN:
                    mFabProgress.show();
                    break;
                case MSG_CONVERT_SUCCESS:
                    mFabProgress.beginFinalAnimation();
                    info = msg.getData().getParcelable(KEY_RECORD_FILE_INFO);
                    mRecordResultView.add(info);
                    break;
                case MSG_CONVERT_ERROR:
                    Toast.makeText(MainActivity.this, R.string.conver_error, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };
}
