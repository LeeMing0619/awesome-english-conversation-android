package com.convoenglishllc.expression.utils;


public class PermissionManager {
    private static PermissionManager mInstance = null;

    public static PermissionManager getInstance() {
        if(mInstance == null) mInstance = new PermissionManager();

        return mInstance;
    }

    public static int FLAG_AUDIO_RECORD_ENABLED = 1;
    public static int FLAG_WRITE_EXTERNAL_STORAGE_ENABLED = 2;

    private int mFlag = 0;

    public void setFlag(int newFlag) { mFlag |= newFlag; }

    public int getFlag() { return mFlag; }
}
