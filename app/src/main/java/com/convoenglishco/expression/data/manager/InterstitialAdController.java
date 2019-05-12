package com.convoenglishllc.expression.data.manager;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.convoenglishllc.expression.R;

/**
 * Created by indy on 16. 2. 2..
 */
public class InterstitialAdController {


    private static final long MIN_INTERVAL = 20 * 1000;
    private static final long MIN_RETRY_INTERVAL = 120 * 1000;

    private Context mContext;
    private InterstitialAd mInterstitialAd;

    private long mLastTimeAdShown = 0;
    private long mLastTimeLoadTried = 0;

    public InterstitialAdController(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        // Create the InterstitialAd and set the adUnitId.
        mInterstitialAd = new InterstitialAd(mContext);
        // Defined in values/strings.xml
        mInterstitialAd.setAdUnitId(mContext.getString(R.string.interstitial_ad_unit_id));

        mLastTimeAdShown = System.currentTimeMillis();

        loadAd();
    }

    private void loadAd() {
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        mInterstitialAd.loadAd(adRequestBuilder.build());
        mLastTimeLoadTried = System.currentTimeMillis();
    }

    public void show(final Runnable runnable) {
        long timestamp = System.currentTimeMillis();

        if (mInterstitialAd != null && mInterstitialAd.isLoaded() && timestamp > mLastTimeAdShown + MIN_INTERVAL) {
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {

                }

                @Override
                public void onAdClosed() {
                    if(runnable != null) runnable.run();
                    loadAd();
                }
            });
            mInterstitialAd.show();
            mLastTimeAdShown = timestamp;
        }
        else {
            if(runnable != null) runnable.run();

            if(timestamp > mLastTimeLoadTried + MIN_RETRY_INTERVAL) {
                loadAd();
            }
        }
    }

}
