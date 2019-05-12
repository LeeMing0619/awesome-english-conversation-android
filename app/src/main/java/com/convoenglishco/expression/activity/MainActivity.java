package com.convoenglishllc.expression.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.convoenglishllc.expression.MyApplication;
import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.data.manager.InterstitialAdController;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.data.model.LessonDataObject;
import com.convoenglishllc.expression.fragment.main.AppListFragment;
import com.convoenglishllc.expression.fragment.main.BaseFragment;
import com.convoenglishllc.expression.fragment.main.BookmarkFragment;
import com.convoenglishllc.expression.fragment.main.CategoryListFragment;
import com.convoenglishllc.expression.fragment.main.LessonListFragment;
import com.convoenglishllc.expression.fragment.main.OfflineModeFragment;
import com.convoenglishllc.expression.fragment.main.OfflineModeDetailFragment;
import com.convoenglishllc.expression.fragment.main.PurchaseFragment;
import com.convoenglishllc.expression.fragment.main.UnlockFragment;
import com.convoenglishllc.expression.utils.AudioDownloader;
import com.convoenglishllc.expression.utils.ConnectionMonitor;
import com.convoenglishllc.expression.utils.GlobalConstants;
import com.convoenglishllc.expression.utils.L;
import com.convoenglishllc.expression.utils.PermissionManager;
import com.convoenglishllc.expression.utils.PurchaseInfo;
import com.convoenglishllc.expression.utils.RateHelper;
import com.convoenglishllc.expression.utils.StorageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.concurrent.CancellationException;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PurchaseInfo.OnPurchaseInfoListener {

    private final String TAG = this.getClass().getSimpleName();

    public static final int LESSON_START_REQUEST_CODE = 2001;
    private static final int MESSAGE_GO_LESSON = 1001;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;

    public static boolean isDownloading = false;
    public static boolean bOfflinePurchaseStatus = false;

    InterstitialAdController mAdController;

    boolean bShowedAds;

    public void sendAnalytic(String action, String label) {
        L.i(TAG, "===================================sendAnalytic() " + action + "---" + label);

        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Main Activity");
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, action);
        ((MyApplication) getApplication()).getTracker().logEvent(label, params);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bShowedAds = false;

        L.i(TAG, "Started main activity, package name= " + getPackageName());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /*
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            Slide slide = new Slide();
            slide.setInterpolator(new LinearInterpolator());
            slide.setSlideEdge(Gravity.LEFT);
            slide.excludeTarget(android.R.id.statusBarBackground, true);
            slide.excludeTarget(android.R.id.navigationBarBackground, true);
            window.setExitTransition(slide); // The Transition to use to move Views out of the scene when calling a new Activity.
            window.setReenterTransition(slide); // The Transition to use to move Views into the scene when reentering from a previously-started Activity.
        }
        */

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_navigation);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //for initate data
        RateHelper.initInstance(this);

        //default go to categories
        if(savedInstanceState == null){
            showCategoriesFragment();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(getListener());

        checkSelfPermissions();

        ((MyApplication) getApplication()).getTracker().setCurrentScreen(this, "Main Activity", null);
        //----------------------------------------------------------------------------------//
        mAdController = new InterstitialAdController(MainActivity.this);
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        bOfflinePurchaseStatus = LessonManager.isOfflinePurchased(getApplicationContext());

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConn);
    }

    @Override
    public void onStart() {
        super.onStart();
//        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        L.d(TAG, "onBackPressed()");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }/* else if(mProgressDialog != null && downloadTask != null) {
            downloadTask.cancel(true);
        }*/ else if (isLastFragment()) {
            if (doubleBackToExitPressedOnce) {
                finish();
            } else {
                MainActivity.this.doubleBackToExitPressedOnce = true;
                Toast.makeText(MainActivity.this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        } else {
            super.onBackPressed();
        }
    }

    private FragmentManager.OnBackStackChangedListener getListener() {
        return new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                FragmentManager manager = getSupportFragmentManager();
                if (manager != null) {
                    int backStackEntryCount = manager.getBackStackEntryCount();
                    if(backStackEntryCount == 0) return;
                    try {
                        BaseFragment fragment = (BaseFragment)manager.getFragments()
                                .get(backStackEntryCount - 1);
                        fragment.onResume();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //setTitleWithSubtitle(fragment.getTitle(), fragment.getSubTitle());
                }
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_share) {
            Intent i=new Intent(android.content.Intent.ACTION_SEND);
            i.setType("text/plain");
//            i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Great way to practice English Conversation.  It is all completely FREE!");
            i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Expressions, Slang, and Idioms");
            i.putExtra(android.content.Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(i, "Share via"));
        }
        return super.onOptionsItemSelected(item);
    }

    private static final String PURCHASE_COUNTDOWN_KEY = "purchase_countdown";
    private static final int INITIAL_PURCHASE_COUNTDOWN = 80;
    private static final int PURCHASE_COUNTDOWN = 80;

    public void checkPurchase() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (bShowedAds == false)
            return;
        int countdown = preferences.getInt(PURCHASE_COUNTDOWN_KEY, -1);
        Log.e("APP", "" + countdown);
        if(countdown < 0) {
            preferences.edit().putInt(PURCHASE_COUNTDOWN_KEY, INITIAL_PURCHASE_COUNTDOWN).apply();
        }
        else  if(countdown == 1) {
            sendAnalytic("Show Purchase Dialog", "");

//            ContextThemeWrapper themedContext;
//            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
//                themedContext = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
//            }
//            else {
//                themedContext = new ContextThemeWrapper(this, android.R.style.Theme_Light_NoTitleBar);
//            }
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setMessage(R.string.purchase_app_01)
                    .setPositiveButton(R.string.purchase_app_02, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            preferences.edit().putInt(PURCHASE_COUNTDOWN_KEY, 0).apply();

                            sendAnalytic("Purchase App", "Yes");

                            showPurchaseFragment();
                        }
                    })
                    .setNeutralButton(R.string.purchase_app_04, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendAnalytic("Purchase App", "Later");
                            preferences.edit().putInt(PURCHASE_COUNTDOWN_KEY, PURCHASE_COUNTDOWN).apply();
                        }
                    })
                    .setNegativeButton(R.string.purchase_app_03, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendAnalytic("Purchase App", "No");
                            preferences.edit().putInt(PURCHASE_COUNTDOWN_KEY, 0).apply();

                        }
                    })
                    .show();
            preferences.edit().putInt(PURCHASE_COUNTDOWN_KEY, PURCHASE_COUNTDOWN).apply();
        }
        else if(countdown > 0){
            preferences.edit().putInt(PURCHASE_COUNTDOWN_KEY, countdown - 1).apply();
        }
    }

    public void onNavMenuClicked(int id) {

        if(id == R.id.nav_bookmark) {
            sendAnalytic("Navigation Menu", "Clicked bookmark");
            showBookmarksFragment();
        } else if(id == R.id.nav_contact) {
            sendAnalytic("Navigation Menu", "Clicked contact");
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","support@talkenglish.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "English Conversation for Android");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } else if(id == R.id.nav_purchase) {
            sendAnalytic("Navigation Menu", "Clicked remove ads");
            showPurchaseFragment();
        } else if(id == R.id.nav_apps) {
            sendAnalytic("Navigation Menu", "Clicked recommended apps");
            showAppsFragment();
        } else if(id == R.id.nav_home) {
            showCategoriesFragment();
        } else if(id == R.id.nav_website) {
            sendAnalytic("Navigation Menu", "Clicked website");
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("http://www.talkenglish.com"));
            startActivity(i);
        } else if(id == R.id.nav_offline) {
            sendAnalytic("Navigation Menu", "Clicked Offline mode");
            if(LessonManager.isOfflinePurchased(getApplicationContext())){
                showOfflineDetailFragment();
            }else{
                showOfflineFragment();
            }
        } else if(id == R.id.nav_unlock) {
            sendAnalytic("Navigation Menu", "Clicked Unlock all lessons");
            showUnlockFragment();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        onNavMenuClicked(id);
        return true;
    }

    public void showCategoriesFragment() {
        //getSupportFragmentManager().popBackStack("fragment_container", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        /*FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.fragment_container, CategoryListFragment.newInstance().initData()).addToBackStack(null).commitAllowingStateLoss();*/

        /*for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }*/

        if(getActiveFragment() instanceof CategoryListFragment) return;
        CategoryListFragment f = CategoryListFragment.newInstance();
        pushFragment(CategoryListFragment.newInstance());
        checkPurchase();
    }

    public void showLessonsFragment(int categoryNo) {
        sendAnalytic("Choose Category", String.format("Category Name : %s", LessonManager.getCategories(getApplicationContext())[categoryNo]));

        BaseFragment f = getActiveFragment();
        if(f instanceof LessonListFragment && ((LessonListFragment)f).getCategoryNo() == categoryNo) return;
        pushFragment(LessonListFragment.newInstance(categoryNo));
        checkPurchase();
    }

    public void showBookmarksFragment() {
        BaseFragment f = getActiveFragment();
        if(f instanceof BookmarkFragment) return;
        pushFragment(BookmarkFragment.newInstance());
        checkPurchase();
    }

    public void showPurchaseFragment() {
        if(getActiveFragment() instanceof PurchaseFragment) return;
        pushFragment(PurchaseFragment.newInstance().initData());
    }

    public void showAppsFragment() {
        if(getActiveFragment() instanceof AppListFragment) return;
        pushFragment(AppListFragment.newInstance());
        checkPurchase();
    }

    public void showOfflineFragment() {
        if (getActiveFragment() instanceof OfflineModeFragment) return;
        pushFragment(OfflineModeFragment.newInstance());
        checkPurchase();
    }

    public void showOfflineDetailFragment() {
        if (getActiveFragment() instanceof OfflineModeDetailFragment) return;
        pushFragment(OfflineModeDetailFragment.newInstance());
        checkPurchase();
    }

    public void showUnlockFragment() {
        if (getActiveFragment() instanceof UnlockFragment) return;
        pushFragment(UnlockFragment.newInstance());
        checkPurchase();
    }

    public void pushFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        //transaction.addToBackStack("fragment_container" + fm.getBackStackEntryCount());
        transaction.addToBackStack(null);
        //transaction.commit();
        transaction.detach(fragment).attach(fragment).commitAllowingStateLoss();
    }

    public BaseFragment getActiveFragment() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof BaseFragment)
            return (BaseFragment)f;
        return null;
    }

    public boolean isLastFragment() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        L.d(TAG, "isLastFragment(), current count=" + fm.getBackStackEntryCount());
        return (fm.getBackStackEntryCount() <= 1);
    }

    @Override
    public void setTitle(CharSequence title) {
        //noinspection ConstantConditions
        this.getSupportActionBar().setDisplayShowCustomEnabled(true);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.item_title, null);

        ((TextView)v.findViewById(R.id.main_title)).setText(title);

        this.getSupportActionBar().setCustomView(v);
    }

    public void setTitleWithSubtitle(CharSequence title, CharSequence subtitle) {
        setTitle(title);
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(subtitle);
    }

    DownloadFileAsync downloadTask = null;
    public synchronized void startLessonActivity(int lessonNo) {
        if(!isDownloading) isDownloading = true;
        else return;
        sendAnalytic("Choose Lesson", String.format("Lesson No : %03d", lessonNo));

        //CLEAR TEMP DIR BEFORE START THIS ACTIVITY
        StorageManager.cleanTempDir(this);

        Message msgGo = new Message();
        msgGo.arg1 = MESSAGE_GO_LESSON;
        msgGo.arg2 = lessonNo;

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        LessonDataObject lessonData = LessonManager.getLessonByNo(getApplicationContext(), lessonNo);
        if( AudioDownloader.isDownloaded(getApplicationContext(), lessonData.getDownloadPath_All(getApplicationContext())) &&
            AudioDownloader.isDownloaded(getApplicationContext(), lessonData.getDownloadPath_A(getApplicationContext())) &&
            AudioDownloader.isDownloaded(getApplicationContext(), lessonData.getDownloadPath_B(getApplicationContext()))) {
            postCallBack.handleMessage(msgGo);
        } else {
            downloadTask = new DownloadFileAsync();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msgGo,
                        lessonData.getUrlChannelAll(), lessonData.getDownloadPath_All(getApplicationContext()),
                        lessonData.getUrlChannelA(), lessonData.getDownloadPath_A(getApplicationContext()),
                        lessonData.getUrlChannelB(), lessonData.getDownloadPath_B(getApplicationContext())
                );
            } else {
                downloadTask.execute(msgGo,
                        lessonData.getUrlChannelAll(), lessonData.getDownloadPath_All(getApplicationContext()),
                        lessonData.getUrlChannelA(), lessonData.getDownloadPath_A(getApplicationContext()),
                        lessonData.getUrlChannelB(), lessonData.getDownloadPath_B(getApplicationContext())
                );
            }
        }
    }

    /*********************************/
    private Handler.Callback postCallBack = new Handler.Callback() {
        @Override
        public boolean handleMessage(final Message msg) {
            L.d(TAG, "handleMessage() " + msg.arg1);
            if(msg.arg1 == MESSAGE_GO_LESSON) {
                final int lessonNo = msg.arg2;

                if(PurchaseInfo.doRemoveAd()) {
                    Intent intent = new Intent(MainActivity.this, LessonActivity.class);
                    intent.putExtra(GlobalConstants.EXTRA_LESSON_NO, lessonNo);
                    startActivityForResult(intent, LESSON_START_REQUEST_CODE);
                }
                else {
                    mAdController.show(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MainActivity.this, LessonActivity.class);
                            intent.putExtra(GlobalConstants.EXTRA_LESSON_NO, lessonNo);
                            startActivityForResult(intent, LESSON_START_REQUEST_CODE);
                        }
                    });
                }
            }
            isDownloading = false;
            return false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        L.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (PurchaseInfo.onActivityResult(this, requestCode, resultCode, data)) {

        }
        else if (requestCode == LESSON_START_REQUEST_CODE && resultCode == RESULT_OK) {
            int id = data.getIntExtra("RESULT", 0);
            if (id != 0) onNavMenuClicked(id);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showBannerAd() {
        L.d(TAG, "showBannerAd()");
        bShowedAds = true;
        AdView mAdView = (AdView) findViewById(R.id.adView);
        if(mAdView == null) return;
        mAdView.setVisibility(View.VISIBLE);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void hideBannerAd() {
        bShowedAds = false;
        AdView mAdView = (AdView) findViewById(R.id.adView);
        if(mAdView != null) mAdView.setVisibility(View.GONE);
    }

    ProgressDialog mProgressDialog = null;

    class DownloadFileAsync extends AsyncTask<Object, Object, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = ProgressDialog.show(MainActivity.this, "", getString(R.string.title_download_progress), true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressDialog.dismiss();
                    DownloadFileAsync.this.cancel(false);
                }
            });
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected Object doInBackground(Object... params) {
            try {
                if(!ConnectionMonitor.isNetworkAvailable(MainActivity.this)) {
                    L.e(TAG, "No internet connection");
                    throw new IOException();
                }
                for(int i = 1; i < params.length; i += 2) {
                    //AudioDownloader.download((String) params[i], (String) params[i + 1]);
                    String strUrl = (String) params[i];
                    String outputSource = (String) params[i+1];
                    if(AudioDownloader.isDownloaded(getApplicationContext(), outputSource)) continue;

                    URL url = new URL(strUrl);
                    HttpURLConnection connection = null;
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setConnectTimeout(2000);
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        L.d(TAG, "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage());
                        throw new IOException();
                    }

                    File f=new File(outputSource);
                    InputStream input = connection.getInputStream();
                    OutputStream output = new FileOutputStream(f);

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        if(isCancelled()) {
                            throw new CancellationException();
                        }
                        total += count;
                        L.d(TAG,outputSource + ":" + total);
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();

                    SharedPreferences pref = getSharedPreferences("BookmarkManager", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putLong(outputSource, total);
                    editor.apply();
                }
            } catch(CancellationException e) {
                L.e(TAG, "Downloading cancelled...");
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(Object ret) {
            if(mProgressDialog != null) mProgressDialog.dismiss();
            //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            downloadTask = null;
            if(ret == null) {//failed
                Toast.makeText(MainActivity.this, getString(R.string.toast_download_audio_fail), Toast.LENGTH_LONG).show();
                isDownloading = false;
            } else  { //successed
                postCallBack.handleMessage((Message)ret);
            }
        }

        @Override
        protected void onCancelled(Object obj) {
            if(mProgressDialog != null) mProgressDialog.dismiss();
            downloadTask = null;
            isDownloading = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    L.d(TAG, "PERMISSION GRANTED - RECORD_AUDIO");
                    PermissionManager.getInstance().setFlag(PermissionManager.FLAG_AUDIO_RECORD_ENABLED);
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    L.d(TAG, "PERMISSION GRANTED - WRITE_EXTERNAL_STORAGE");
                    PermissionManager.getInstance().setFlag(PermissionManager.FLAG_WRITE_EXTERNAL_STORAGE_ENABLED);
                }
            }
        }
    }

    public boolean checkSelfPermissions() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= 23){
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO)) {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.RECORD_AUDIO},
                                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                        }
            } else {
                L.d(TAG, "Already allowed - RECORD_AUDIO");
                PermissionManager.getInstance().setFlag(PermissionManager.FLAG_AUDIO_RECORD_ENABLED);
            }

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            } else {
                L.d(TAG, "Already allowed - WRITE_EXTERNAL_STORAGE");
                PermissionManager.getInstance().setFlag(PermissionManager.FLAG_WRITE_EXTERNAL_STORAGE_ENABLED);
            }
        } else{
            L.d(TAG, "Ignore permission request");
            PermissionManager.getInstance().setFlag(PermissionManager.FLAG_AUDIO_RECORD_ENABLED);
            PermissionManager.getInstance().setFlag(PermissionManager.FLAG_WRITE_EXTERNAL_STORAGE_ENABLED);
        }
        return true;
    }

    /**------------------------------------------------- IN-APP BILLING -------------------------------------------------**/

    // User clicked the "Upgrade to Premium" button.
    public void onUpgradeAppButtonClicked(String sku_type) {
        PurchaseInfo.purchase(this, mBillingService, sku_type);
    }

    IInAppBillingService mBillingService;
    Handler mHandler = new Handler();

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBillingService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBillingService = IInAppBillingService.Stub.asInterface(service);
            onBillingServiceUpdated(mBillingService);
        }
    };

    protected void onBillingServiceUpdated(IInAppBillingService service) {
        onPurchaseInfoRetrieved(PurchaseInfo.retrievePurchaseInfoList(MainActivity.this, mBillingService, MainActivity.this));
    }

    public IInAppBillingService getBillingService() {
        return mBillingService;
    }

    @Override
    public void onPurchaseInfoRetrieved(final Hashtable<String, PurchaseInfo> purchaseInfos) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (PurchaseInfo.doRemoveAd(purchaseInfos)) {
                    hideBannerAd();
                } else {
                    showBannerAd();
                }

                BaseFragment purchaseFragment = getActiveFragment();
                if (purchaseFragment instanceof PurchaseFragment) {
                    ((PurchaseFragment) purchaseFragment).onPurchaseInfoRetrieved(purchaseInfos);
                }
                BaseFragment offlineFragment = getActiveFragment();
                if (offlineFragment instanceof OfflineModeFragment) {
                    ((OfflineModeFragment) offlineFragment).onPurchaseInfoRetrieved(purchaseInfos);
                }
            }
        });
    }

    @Override
    public void onPurchaseInfoFailed() {

    }



}

