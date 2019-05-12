package com.convoenglishllc.expression.utils;

import android.content.Context;

import java.io.File;

public class GlobalConstants {

    public static final int DELAY_CLICK_LESSON_ITEM = 1000; //1s

    public static final String EXTRA_RECORD_URL = "RECORD_URL";
    public static final String EXTRA_LESSON_NO = "LESSON_NO";

    public static final String WEBSERVICE_URL = "http://www.convoenglishco.com";

    public static final String PREF_KEY_BOOKMARK = "Bookmarks";

    public static final String PREF_KEY_DOWNLOADED = "Downloaded";
    public static final String PREF_KEY_SKIPPED = "Skipped";

    public static final String INSTRUCTION_VIDEO_URL = WEBSERVICE_URL + "/apps/conversation/video/instruction/en_instruction.mp4";
    public static final String INSTRUCTION_VIDEO_NAME = "en_instruction.mp4";
    public static final String AUDIO_URL = WEBSERVICE_URL + "/apps/expression/audio";

    public static final String MP3_ALL = "a.mp3";
    public static final String MP3_A = "b.mp3";
    public static final String MP3_B = "c.mp3";

    public static boolean PURCHASED = false;

    public static String getTempFileRecordBg(Context context) {
        return getTempDir(context) + "/" + "tmp_bg.wav";
    }

    public static String getTempFileRecordFg(Context context) {
        return getTempDir(context) + "/" + "tmp_fg.wav";
    }

    public static String getTempDir(Context context) {
        //String path = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + "temp";
        String path = context.getExternalFilesDir(null) + "/" + "temp";
        File f = new File(path);
        if(!f.exists()) f.mkdirs();
        return path;
    }

    public static String getRecordDir(Context context) {
        //String path = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + "record";
        String path = context.getExternalFilesDir(null) + "/" + "record";
        File f = new File(path);
        if(!f.exists()) f.mkdirs();
        return path;
    }

    public static String getAudioDownloadDir(Context context) {
        String path = context.getExternalFilesDir(null) + "/" + "cache";
        File f = new File(path);
        if(!f.exists()) f.mkdirs();
        return path;
    }

    public static String getVideoDownloadDir(Context context) {
        String path = context.getExternalFilesDir(null) + "/" + "download";
        File f = new File(path);
        if(!f.exists()) f.mkdirs();
        return path;
    }



}
