package com.x.simpleaudiorecorder.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wufeiyang on 2016/12/1.
 */

public class RecordFileInfo implements Parcelable{
    private String mFileName;
    private String mFilePath;

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String mFilePath) {
        this.mFilePath = mFilePath;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mFileName);
        dest.writeString(this.mFilePath);
    }

    public RecordFileInfo() {
    }

    protected RecordFileInfo(Parcel in) {
        this.mFileName = in.readString();
        this.mFilePath = in.readString();
    }

    public static final Creator<RecordFileInfo> CREATOR = new Creator<RecordFileInfo>() {
        @Override
        public RecordFileInfo createFromParcel(Parcel source) {
            return new RecordFileInfo(source);
        }

        @Override
        public RecordFileInfo[] newArray(int size) {
            return new RecordFileInfo[size];
        }
    };
}
