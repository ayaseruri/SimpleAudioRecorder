package com.x.simpleaudiorecorder.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/**
 * Created by wufeiyang on 2016/11/30.
 */

public class Utils {

    public static void startAppSysSetting(Context context){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    public static short[] byteArray2ShortArray(byte[] data, int length) {
        short[] retVal = new short[length];
        for (int i = 0; i < length; i++){
            retVal[i] = (short) ((data[i * 2] & 0xff) | (data[i * 2 + 1] & 0xff) << 8);
        }
        return retVal;
    }
}
