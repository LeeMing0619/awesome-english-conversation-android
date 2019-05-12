package com.convoenglishllc.expression.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

public class AudioDownloader {
    public static String TAG = AudioDownloader.class.getSimpleName();

    public static boolean isDownloaded(Context context, String outputSource) {
        SharedPreferences pref = context.getSharedPreferences("BookmarkManager", Context.MODE_PRIVATE);
        long savedAudioSize = pref.getLong(outputSource, 0);
        File savedFile = new File(outputSource);
        if(savedFile.exists() && (savedFile.length() == savedAudioSize || savedFile.length() == 0)) {
            L.d(TAG, "already exist file " + outputSource);
            return true;
        }
        return false;
    }

//    public static synchronized void download(String strUrl, String outputSource) throws IOException {
//        if(isDownloaded(outputSource)) return;
//
//        if(!ConnectionMonitor.isNetworkAvailable(MainActivity.gContext)) {
//            L.e(TAG, "No internet connection");
//            throw new IOException();
//        }
//
//        URL url = new URL(strUrl);
//        HttpURLConnection connection = null;
//        try {
//            connection = (HttpURLConnection)url.openConnection();
//            connection.setConnectTimeout(2000);
//            connection.connect();
//
//            // expect HTTP 200 OK, so we don't mistakenly save error report
//            // instead of the file
//            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                L.d(TAG, "Server returned HTTP " + connection.getResponseCode()
//                        + " " + connection.getResponseMessage());
//                throw new IOException();
//            }
//
//            File f=new File(outputSource);
//            InputStream input = connection.getInputStream();
//            OutputStream output = new FileOutputStream(f);
//
//            byte data[] = new byte[4096];
//            long total = 0;
//            int count;
//            while ((count = input.read(data)) != -1) {
//                total += count;
//                L.d(TAG,outputSource + ":" + total);
//                output.write(data, 0, count);
//            }
//            output.flush();
//            output.close();
//            input.close();
//
//            SharedPreferences.Editor editor = BookmarkManager.getInstance().getPref().edit();
//            editor.putLong(outputSource, total);
//            editor.apply();
//        } catch (SocketTimeoutException e) {
//            e.printStackTrace();
//            throw new IOException();
//        }
//    }
}
