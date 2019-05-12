package com.convoenglishllc.expression;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Process;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.convoenglishllc.expression.activity.SplashActivity;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.data.model.LessonDataObject;
import com.convoenglishllc.expression.fragment.main.OfflineModeDetailFragment;
import com.convoenglishllc.expression.utils.AudioDownloader;
import com.convoenglishllc.expression.utils.ConnectionMonitor;
import com.convoenglishllc.expression.utils.L;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CancellationException;

public class MyApplication extends Application {
    private static final String PROPERTY_ID = "UA-71762818-1"; //STEVEN
    //private static final String PROPERTY_ID = "UA-71795911-1"; //TEST


    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        //GLOBAL_TRACKER, // Tracker used by all the apps
    }

    private static final String TAG = "MyApp";
    private static final String KEY_APP_CRASHED = "KEY_APP_CRASHED";


    public MyApplication() {
        super();
    }

    private FirebaseAnalytics mFirebaseAnalytics = null;
    synchronized public FirebaseAnalytics getTracker() {
        if (mFirebaseAnalytics == null) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }
        return mFirebaseAnalytics;
    }

    @Override
    public void onCreate ()
    {
        super.onCreate();

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });

        boolean bRestartAfterCrash = getSharedPreferences( TAG , Context.MODE_PRIVATE )
                .getBoolean( KEY_APP_CRASHED, false );
        if ( bRestartAfterCrash ) {
            // Clear crash flag.
            getSharedPreferences( TAG , Context.MODE_PRIVATE ).edit()
                    .putBoolean( KEY_APP_CRASHED, false ).apply();
            // Re-launch from root activity with cleared stack.
            Intent intent = new Intent( this, SplashActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
        }
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        getSharedPreferences( TAG , Context.MODE_PRIVATE ).edit()
                .putBoolean( KEY_APP_CRASHED, true ).apply();

        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        Intent intent = new Intent ();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        startActivity(intent);

        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    public DownloadFileAsync taskDownload = null;
    public void startDownload(int type) {
        if(taskDownload == null) {
            taskDownload = new DownloadFileAsync();
            taskDownload.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, type);
        }
    }

    public OfflineModeDetailFragment fragmentOfflineDetail;
    public int nTotalCount = 0;
    public int nCurrentDownloadCount = 0;

    class DownloadFileAsync extends AsyncTask<Integer, Integer, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            L.e(TAG, "Downloading onPreExecute...");
            if(!ConnectionMonitor.isNetworkAvailable(getApplicationContext())) {
                L.e(TAG, "No internet connection");
                if(fragmentOfflineDetail != null){
                    fragmentOfflineDetail.changeButtonTitle(true);
                }
                return;
            }
            if(fragmentOfflineDetail != null){
                fragmentOfflineDetail.changeButtonTitle(false);
                fragmentOfflineDetail.showProgressBar();
            }
            //nTotalCount = DBHelper.getInstance(getApplicationContext()).getAllLessonsCount() * 3;
            nTotalCount = 100 * 3;
            nCurrentDownloadCount = 0;
        }

        @Override
        protected String doInBackground(Integer... params) {
            try {
                if(!ConnectionMonitor.isNetworkAvailable(getApplicationContext())) {
                    L.e(TAG, "No internet connection");
                    throw new IOException();
                }
                int type = params[0];
                //LessonDataObject[] lessons = LessonManager.getAllLessons(getApplicationContext());

                LessonDataObject[] lessons = null;
                if (type==1){
                    lessons = LessonManager.getFirstCategoryLessons(getApplicationContext());
                }else if(type==2){
                    lessons = LessonManager.getSecondCategoryLessons(getApplicationContext());
                }else if(type==3){
                    lessons = LessonManager.getThirdCategoryLessons(getApplicationContext());
                }

                for(LessonDataObject lesson : lessons) {
                    if (downloadFile(lesson.getUrlChannelA(), lesson.getDownloadPath_A(getApplicationContext()))) {
                        nCurrentDownloadCount += 1;
                        publishProgress(nCurrentDownloadCount);
                    } else {
                        return null;
                    }
                    if (downloadFile(lesson.getUrlChannelB(), lesson.getDownloadPath_B(getApplicationContext()))){
                        nCurrentDownloadCount += 1;
                        publishProgress(nCurrentDownloadCount);
                    } else {
                        return null;
                    }
                    if (downloadFile(lesson.getUrlChannelAll(), lesson.getDownloadPath_All(getApplicationContext()))){
                        nCurrentDownloadCount += 1;
                        publishProgress(nCurrentDownloadCount);
                    } else {
                        return null;
                    }
                }

            } catch(CancellationException e) {
                L.e(TAG, "Downloading cancelled...");
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return "Success";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            //mProgressDialog.setMessage("Downloading Audio files... (" + nCurrentDownloadCount + "/" + nTotalCount + ")");
            if(fragmentOfflineDetail != null) {
                float fProgress = (float) nCurrentDownloadCount / (float) nTotalCount;
                fragmentOfflineDetail.setProgress(fProgress);
                fragmentOfflineDetail.changeButtonTitle(false);
            }
        }

        private boolean downloadFile(String strUrl, String outputSource) {
            try {
                if (AudioDownloader.isDownloaded(getApplicationContext(), outputSource)) return true;

                URL url = new URL(strUrl);
                HttpURLConnection connection = null;
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(2000);
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    L.d(TAG, "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage());
                    throw new IOException();
                }

                File f = new File(outputSource);
                InputStream input = connection.getInputStream();
                OutputStream output = new FileOutputStream(f);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        throw new CancellationException();
                    }
                    total += count;
                    L.d(TAG, outputSource + ":" + total);
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();

                SharedPreferences preferences = getApplicationContext().getSharedPreferences("BookmarkManager", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(outputSource, total);
                editor.apply();
                return true;
            }catch(CancellationException e) {
                L.e(TAG, "Downloading cancelled...");
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        private static final String OFFLINE_MODE = "offline";
        @Override
        protected void onPostExecute(String ret) {
            taskDownload = null;
            if(fragmentOfflineDetail != null) {
                fragmentOfflineDetail.hideProgressBar();
            }
            if(ret == null) {
                if(!isCancelled()) {
                    L.e(TAG, "Downloading retry");
                    if(fragmentOfflineDetail != null){
                        fragmentOfflineDetail.changeButtonTitle(true);
                    }
                    taskDownload = new DownloadFileAsync();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            taskDownload.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }, 1000);
                }
            } else {
                final SharedPreferences preferences = getApplicationContext().getSharedPreferences("OfflineModeFragment", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(OFFLINE_MODE, 1);
                editor.apply();
                if (fragmentOfflineDetail != null) {
                    fragmentOfflineDetail.setContentOffline(true);
                }
            }
        }

        @Override
        protected void onCancelled(String obj) {
            taskDownload = null;
            if(fragmentOfflineDetail != null) {
                fragmentOfflineDetail.hideProgressBar();
            }
        }
    }


}