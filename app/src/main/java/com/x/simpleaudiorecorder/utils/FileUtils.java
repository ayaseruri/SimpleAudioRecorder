package com.x.simpleaudiorecorder.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.x.simpleaudiorecorder.bean.RecordFileInfo;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/**
 * Created by wufeiyang on 2016/11/30.
 */

public class FileUtils {
    public static File getExternalCacheDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            File path = context.getExternalCacheDir();
            if (path != null) {
                return path;
            }
        }
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static File[] getFileWithExtension(String dirPath, final String... extensions){
        return new File(dirPath).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                for (int i = 0; i < extensions.length; i ++){
                    if(name.endsWith(extensions[i])){
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
