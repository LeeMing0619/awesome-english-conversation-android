package com.convoenglishllc.expression.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.convoenglishllc.expression.BuildConfig;
import com.convoenglishllc.expression.MyApplication;
import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.data.manager.BookmarkManager;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.fragment.lesson.FragmentLifeCycle;
import com.convoenglishllc.expression.fragment.lesson.ListenFragment;
import com.convoenglishllc.expression.fragment.lesson.VocabFragment;
import com.convoenglishllc.expression.fragment.lesson.PracticeFragment;
import com.convoenglishllc.expression.fragment.lesson.QuizFragment;
import com.convoenglishllc.expression.fragment.lesson.RecordFragment;
import com.convoenglishllc.expression.utils.GlobalConstants;
import com.convoenglishllc.expression.utils.L;
import com.convoenglishllc.expression.utils.PurchaseInfo;

import java.util.Hashtable;

public class LessonActivity extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, PurchaseInfo.OnPurchaseInfoListener {
    public static LessonActivity gContext = null;

    private final String TAG = this.getClass().getSimpleName();

    private ViewPager mViewPager  = null;
    private HorizontalScrollView mTabScroll = null;
    private LinearLayout mTabLayout = null;

    private int mLessonNo = -1;

    private int nShowedAds = -1;
    private boolean bCheckRate = false;

    public void sendAnalytic(String action, String label) {
        L.d(TAG, "===================================sendAnalytic()" + action + "---" + label);

        Bundle params = new Bundle();
        params.putString("Action", label);
        ((MyApplication) getApplication()).getTracker().logEvent("Lesson Activity - " + action, params);
    }

    @Override
    public void onStart() {
        super.onStart();
//        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        bCheckRate = checkRate();
        if(bCheckRate == false)
            checkPurchase();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    public void saveLessonNo(int lesson_no) {
        SharedPreferences.Editor editor = getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE).edit();
        editor.putInt("LESSON_NO", lesson_no);
        editor.apply();
    }

    public int getLessonNo() {
        return getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE).getInt("LESSON_NO", 1);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gContext = this;

        L.d(TAG, "onCreate()");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /*
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            Slide slide = new Slide();
            slide.setInterpolator(new LinearInterpolator());
            slide.setSlideEdge(Gravity.RIGHT);
            slide.excludeTarget(android.R.id.statusBarBackground, true);
            slide.excludeTarget(android.R.id.navigationBarBackground, true);
            window.setEnterTransition(slide); // The Transition to use to move Views into the initial Scene.
            window.setReturnTransition(slide); // The Transition to use to move Views out of the Scene when the Window is preparing to close.
        }
        */
        setContentView(R.layout.activity_lesson);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_content);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        toolbar.setNavigationIcon(R.drawable.ic_navigation);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(getIntent() != null) {
            mLessonNo = getIntent().getIntExtra(GlobalConstants.EXTRA_LESSON_NO, 1);
            saveLessonNo(mLessonNo);
        } else {
            mLessonNo = getLessonNo();
        }

        setTitle(LessonManager.getLessonByNo(getApplicationContext(), mLessonNo).getTitle());

        final SectionsPagerAdapter mSectionsPagerAdapter;
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                for(int i=0; i < mSectionsPagerAdapter.getCount(); i++) {
                    FragmentLifeCycle fragment = (FragmentLifeCycle) mSectionsPagerAdapter.getItem(i);
                    if(i != position) {
                        fragment.onPauseFragment();
                    }
                }
                gotoTab(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) { }
        });
        //TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        //tabLayout.setupWithViewPager(mViewPager);

        mTabLayout = (LinearLayout) findViewById(R.id.layout_tab);
        mTabScroll = (HorizontalScrollView) findViewById(R.id.scroll_tab);
        mTabScroll.setSmoothScrollingEnabled(true);

        Drawable d = getResources().getDrawable(R.drawable.tab1);
        int tabbarWidth = (int)(d.getIntrinsicWidth() / 5 * 3.5);
        int tabWidth = (tabbarWidth - 20) / 5;


        LinearLayout ll1 = (LinearLayout) findViewById((R.id.linearLayout1));
        LayoutParams params = ll1.getLayoutParams();
        params.width = tabWidth + 20;
        ll1.setLayoutParams(params);
        ll1.setOnClickListener(this);

        LinearLayout ll2 = (LinearLayout) findViewById((R.id.linearLayout2));
        params = ll2.getLayoutParams();
        params.width = tabWidth;
        ll2.setLayoutParams(params);
        ll2.setOnClickListener(this);

        LinearLayout ll3 = (LinearLayout) findViewById((R.id.linearLayout3));
        params = ll3.getLayoutParams();
        params.width = tabWidth;
        ll3.setLayoutParams(params);
        ll3.setOnClickListener(this);

        LinearLayout ll4 = (LinearLayout) findViewById((R.id.linearLayout4));
        params = ll4.getLayoutParams();
        params.width = tabWidth;
        ll4.setLayoutParams(params);
        ll4.setOnClickListener(this);

        LinearLayout ll5 = (LinearLayout) findViewById((R.id.linearLayout5));
        params = ll5.getLayoutParams();
        params.width = tabWidth;
        ll5.setLayoutParams(params);
        ll5.setOnClickListener(this);


        ((MyApplication) getApplication()).getTracker().setCurrentScreen(this, "Lesson Activity", null);
        //----------------------------------------------------------------------------------//
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onBackPressed() {
        L.d(TAG, "onBackPressed()");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_content);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showBannerAd() {
        L.d(TAG, "showAd()");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setVisibility(View.VISIBLE);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void hideBannerAd() {
        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setVisibility(View.GONE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lesson, menu);

        boolean isBookmarked = BookmarkManager.isBookmarked(getApplicationContext(), mLessonNo);

        if(isBookmarked) menu.findItem(R.id.action_bookmark).setIcon(R.drawable.star_yellow);
        else menu.findItem(R.id.action_bookmark).setIcon(R.drawable.star);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_bookmark) {
            sendAnalytic("Bookmark", String.format("Lesson No : %03d", mLessonNo));

            boolean isBookmarked = BookmarkManager.isBookmarked(getApplicationContext(), mLessonNo);
            if(isBookmarked) {
                BookmarkManager.removeId(getApplicationContext(), mLessonNo);
                item.setIcon(R.drawable.star);
            } else {
                BookmarkManager.addId(getApplicationContext(), mLessonNo);
                item.setIcon(R.drawable.star_yellow);
//                item.setIcon(R.mipmap.ic_toolbar_bookmark_on);
                //L.toast(this, getString(R.string.toast_bookmarked));
                //L.toastBookmark(this);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void gotoTab(int position) {
        if(mViewPager.getCurrentItem() != position) {
            mViewPager.setCurrentItem(position, true);
        }
        if(position == 0) {
            mTabLayout.setBackgroundResource(R.mipmap.tab1);
            mTabScroll.smoothScrollTo(0, mTabScroll.getScrollY());
        } else if(position == 1) {
            mTabLayout.setBackgroundResource(R.mipmap.tab2);
            mTabScroll.smoothScrollTo(mTabScroll.getMaxScrollAmount() / 5, mTabScroll.getScrollY());
        } else if(position == 2) {
            mTabLayout.setBackgroundResource(R.mipmap.tab3);
            mTabScroll.smoothScrollTo(mTabScroll.getMaxScrollAmount() * 3 / 5, mTabScroll.getScrollY());
        } else if(position == 3) {
            mTabLayout.setBackgroundResource(R.mipmap.tab4);
            mTabScroll.smoothScrollTo(mTabScroll.getMaxScrollAmount() * 4 / 5, mTabScroll.getScrollY());
        } else if(position == 4) {
            mTabLayout.setBackgroundResource(R.mipmap.tab5);
            mTabScroll.smoothScrollTo(mTabScroll.getMaxScrollAmount(), mTabScroll.getScrollY());
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.linearLayout1) {
            gotoTab(0);
        } else if(id == R.id.linearLayout2) {
            gotoTab(1);
        } else if(id == R.id.linearLayout3) {
            gotoTab(2);
        } else if(id == R.id.linearLayout4) {
            gotoTab(3);
        } else if(id == R.id.linearLayout5) {
            gotoTab(4);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_content);
        drawer.closeDrawer(GravityCompat.START);

        boolean goToMain = false;
        int id = item.getItemId();

        if( id == R.id.nav_bookmark || id == R.id.nav_purchase || id == R.id.nav_apps || id == R.id.nav_home || id == R.id.nav_offline) goToMain = true;

        if(goToMain) {
            setResult(RESULT_OK, new Intent().putExtra("RESULT", id));
            finishActivity(MainActivity.LESSON_START_REQUEST_CODE);
            finish();
            return true;
        }
        //return MainActivity.gContext.onNavigationItemSelected(item);
        onNavMenuClicked(id);
        return true;
    }

    public void onNavMenuClicked(int id) {
        if(id == R.id.nav_contact) {
            sendAnalytic("Navigation Menu", "Clicked contact");
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","support@talkenglish.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "English Conversation for Android");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } else if(id == R.id.nav_website) {
            sendAnalytic("Navigation Menu", "Clicked website");
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("http://www.talkenglish.com"));
            startActivity(i);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private Fragment[] mFragments = new Fragment[5];
        private String[] tabTitles = new String[] {"LISTEN", "VOCAB", "QUIZ", "PRACTICE", "RECORD"};

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragments[0] = ListenFragment.newInstance(LessonActivity.this.mLessonNo);
            mFragments[1] = VocabFragment.newInstance(LessonActivity.this.mLessonNo);
            mFragments[2] = QuizFragment.newInstance(LessonActivity.this.mLessonNo);
            mFragments[3] = PracticeFragment.newInstance(LessonActivity.this.mLessonNo);
            mFragments[4] = RecordFragment.newInstance(LessonActivity.this.mLessonNo);
        }
        @Override
        public Fragment getItem(int position) { return mFragments[position]; }

        @Override
        public int getCount() { return 5; }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }


    private static final String RATE_COUNTDOWN_KEY = "rate_countdown_lesson";
    private static final int INITIAL_RATE_COUNTDOWN = 60;
    private static final int RATE_COUNTDOWN = 80;

    public boolean checkRate() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        int countdown = preferences.getInt(RATE_COUNTDOWN_KEY, -1);
        if(countdown < 0) {
            preferences.edit().putInt(RATE_COUNTDOWN_KEY, INITIAL_RATE_COUNTDOWN).apply();
            return false;
        }
        else  if(countdown == 1) {
            sendAnalytic("Show Rate Dialog", "");

//            ContextThemeWrapper themedContext;
//            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
//                themedContext = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
//            }
//            else {
//                themedContext = new ContextThemeWrapper(this, android.R.style.Theme_Light_NoTitleBar);
//            }
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setMessage(R.string.rate_app_01)
                    .setPositiveButton(R.string.rate_app_02, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            preferences.edit().putInt(RATE_COUNTDOWN_KEY, 0).apply();

                            sendAnalytic("Rate App", "Yes");

                            final String appPackageName = BuildConfig.APPLICATION_ID;
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                        }
                    })
                    .setNeutralButton(R.string.rate_app_04, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendAnalytic("Rate App", "Later");
                            preferences.edit().putInt(RATE_COUNTDOWN_KEY, RATE_COUNTDOWN).apply();
                        }
                    })
                    .setNegativeButton(R.string.rate_app_03, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendAnalytic("Rate App", "No");
                            preferences.edit().putInt(RATE_COUNTDOWN_KEY, 0).apply();

                        }
                    })
                    .show();
            preferences.edit().putInt(RATE_COUNTDOWN_KEY, RATE_COUNTDOWN).apply();
            return true;
        }
        else if(countdown > 0){
            preferences.edit().putInt(RATE_COUNTDOWN_KEY, countdown - 1).apply();
            return false;
        }
        return false;
    }

    private static final String PURCHASE_COUNTDOWN_KEY = "purchase_countdown";
    private static final int INITIAL_PURCHASE_COUNTDOWN = 80;
    private static final int PURCHASE_COUNTDOWN = 80;

    public void checkPurchase() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (nShowedAds != 1)
            return;
        int countdown = preferences.getInt(PURCHASE_COUNTDOWN_KEY, -1);
        Log.e("APP", "" + countdown);
        if(countdown < 0) {
            preferences.edit().putInt(PURCHASE_COUNTDOWN_KEY, INITIAL_PURCHASE_COUNTDOWN).apply();
        }
        else  if(countdown == 1) {
            sendAnalytic("Show Purchase Dialog", "");
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setMessage(R.string.purchase_app_01)
                    .setPositiveButton(R.string.purchase_app_02, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            preferences.edit().putInt(PURCHASE_COUNTDOWN_KEY, 0).apply();

                            sendAnalytic("Purchase App", "Yes");

                            setResult(RESULT_OK, new Intent().putExtra("RESULT", R.id.nav_purchase));
                            finishActivity(MainActivity.LESSON_START_REQUEST_CODE);
                            finish();
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
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onPurchaseInfoRetrieved(PurchaseInfo.retrievePurchaseInfoList(LessonActivity.this, mBillingService, LessonActivity.this));
            }
        });
    }

    public IInAppBillingService getBillingService() {
        return mBillingService;
    }

    @Override
    public void onPurchaseInfoRetrieved(Hashtable<String, PurchaseInfo> purchaseInfos) {
        if(PurchaseInfo.doRemoveAd(purchaseInfos)) {
            hideBannerAd();
            nShowedAds = 0;
        }
        else {
            showBannerAd();
            if(nShowedAds == -1) {
                if(bCheckRate == false){
                    nShowedAds = 1;
                    checkPurchase();
                }
            }
            nShowedAds = 1;
        }
    }

    @Override
    public void onPurchaseInfoFailed() {

    }
}
