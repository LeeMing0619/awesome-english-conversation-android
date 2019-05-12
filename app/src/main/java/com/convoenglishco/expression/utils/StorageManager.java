package com.convoenglishllc.expression.utils;

import android.content.Context;

import java.io.File;
public class StorageManager {
    public static final String TAG = StorageManager.class.getSimpleName();
    public static void cleanTempDir(Context context) {
        File f = new File(GlobalConstants.getTempDir(context));
        File file[] = f.listFiles();
        for(File ff : file) {
            L.d(TAG, "delete " + ff.getAbsolutePath() + " " + ff.delete());
        }
    }
}
