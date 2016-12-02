package com.x.simpleaudiorecorder.application;

import android.app.Application;
import android.widget.Toast;

import com.x.simpleaudiorecorder.R;

import org.androidannotations.annotations.EApplication;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;

/**
 * Created by wufeiyang on 2016/12/1.
 */
@EApplication
public class MApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {

            }
            @Override
            public void onFailure(Exception error) {
                error.printStackTrace();
                Toast.makeText(MApplication.this
                        , R.string.loading_converter_error
                        , Toast.LENGTH_LONG).show();
            }
        });
    }
}
