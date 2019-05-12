package com.convoenglishllc.expression.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.convoenglishllc.expression.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * Created by indy on 16. 2. 3..
 */
public class PurchaseInfo {

    private static final String LOG_TAG = "PurchaseInfo";

    public static String SKU_PREMIUM = "conv.premium.publish";
    public static String SKU_PREMIUM1 = "conv.premium_plus1.publish3";
    public static String SKU_PREMIUM3 = "conv.premium_plus3.publish";
    public static String SKU_PREMIUM5 = "conv.premium_plus5.offline";

    final private static String[] SKU_REMOVE_AD = {
            SKU_PREMIUM,
            SKU_PREMIUM1,
            SKU_PREMIUM3,
            SKU_PREMIUM5,
    };

    public interface OnPurchaseInfoListener {
        void onPurchaseInfoRetrieved(Hashtable<String, PurchaseInfo> purchaseInfos);
        void onPurchaseInfoFailed();
    }

    private static Hashtable<String, PurchaseInfo> sCache;

    private static void updateCache(Hashtable<String, PurchaseInfo> purchaseInfos) {
        sCache = purchaseInfos;
    }

    public static boolean doRemoveAd(Hashtable<String, PurchaseInfo> purchaseInfos) {
        if(purchaseInfos == null) return true;
        boolean removeAd = false;
        for(PurchaseInfo i : purchaseInfos.values()) {
            if(i.sku != SKU_PREMIUM5) {
                removeAd = removeAd || i.purchased;
            }
        }
        return removeAd;
    }

    public static boolean doOfflineMode(Hashtable<String, PurchaseInfo> purchaseInfos) {
        if(purchaseInfos == null) return false;
        boolean removeAd = false;
        PurchaseInfo i = purchaseInfos.get(SKU_PREMIUM5);
        if(i == null) return false;
        return i.purchased;
    }

    public static boolean doOfflineMode() {
        return doOfflineMode(sCache);
    }
    public static boolean doRemoveAd() {
        return doRemoveAd(sCache);
    }

    public static Hashtable<String, PurchaseInfo> retrievePurchaseInfoList(final Context context, final IInAppBillingService service, final OnPurchaseInfoListener listener) {
        if(service == null) {
            listener.onPurchaseInfoFailed();
            return null;
        }

        AsyncTask<Void, Void, Hashtable<String, PurchaseInfo>> task = new AsyncTask<Void, Void, Hashtable<String, PurchaseInfo>>() {
            @Override
            protected Hashtable<String, PurchaseInfo> doInBackground(Void... params) {
                ArrayList<String> skuList = new ArrayList<> ();
                for(String sku : SKU_REMOVE_AD) skuList.add(sku);
                Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

                try {
                    Bundle skuDetails = service.getSkuDetails(3, context.getPackageName(), "inapp", querySkus);
                    int response = skuDetails.getInt("RESPONSE_CODE");
                    Log.v(LOG_TAG, "getSkuDetails returns: " + response);
                    if (response != 0) {

                        return null;
                    }

                    ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                    if(responseList == null) return null;

                    HashSet<String> purchasedSkus = getPurchasedSkus(context, service);
                    Log.v(LOG_TAG, "purchased skus: " + purchasedSkus);

                    Hashtable<String, PurchaseInfo> cache = new Hashtable<>();

                    for (String thisResponse : responseList) {
                        Log.v(LOG_TAG, "getSkuDetails: " + thisResponse);

                        JSONObject object = new JSONObject(thisResponse);
                        String sku = object.getString("productId");
                        {
                            PurchaseInfo info = new PurchaseInfo();
                            info.purchased = purchasedSkus != null && purchasedSkus.contains(sku);
                            info.priceString = object.getString("price");
                            info.sku = sku;
                            Currency currency = Currency.getInstance(object.getString("price_currency_code"));
                            info.currencySymbol = currency.getSymbol();
                            info.priceValue = ((double) object.getLong("price_amount_micros")) / 1000000.0;
                            cache.put(sku, info);
                        }
                    }

                    Log.v(LOG_TAG, "purchase info: " + cache);

                    return cache;
                } catch (RemoteException | JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Hashtable<String, PurchaseInfo> cache) {
                if(cache != null) {
                    updateCache(cache);
                    listener.onPurchaseInfoRetrieved(cache);
                }
                else {
                    listener.onPurchaseInfoFailed();
                }
            }
        };
        task.execute();

        return sCache;
    }

    private static HashSet<String> getPurchasedSkus(final Context context, final IInAppBillingService service) throws RemoteException {
        Bundle ownedItems = service.getPurchases(3, context.getPackageName(), "inapp", null);

        int response = ownedItems.getInt("RESPONSE_CODE");
        if (response == 0) {
            ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            if(ownedSkus == null) return null;

            return new HashSet<>(ownedSkus);
        }
        else {
            Log.v(LOG_TAG, "getPurchases returns: " + response);
        }
        return null;
    }

    final private static int REQUEST_CODE_PURCHASE = 12315;

    public static void purchase(Activity activity, IInAppBillingService service, String sku) {
        if(service == null) return;

        String piece1 = "GRGBDFFBGGGFG3DFUISDFVWE";
        String piece2 = "$#VADVNAJ8bjFFASDFN";
        String piece3 = "DFBFN4F8FFDFFPAAF";
        String piece4 = "AFVAD0FEEFPFAFVdRFEF";

        try {
            Bundle buyIntentBundle = service.getBuyIntent(3, activity.getPackageName(),
                    sku, "inapp", piece1 + piece2 + piece3 + piece4);
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            activity.startIntentSenderForResult(pendingIntent.getIntentSender(), REQUEST_CODE_PURCHASE, new Intent(), 0, 0, 0);
        } catch (Throwable e) {
        }
    }

    public static boolean onActivityResult(MainActivity activity, int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_PURCHASE) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == Activity.RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    String payload = jo.getString("developerPayload");
                    String packagetName = jo.getString("packageName");
                    boolean purchased = jo.getInt("purchaseState") == 0;

                    String piece1 = "GRGBDFFBGGGFG3DFUISDFVWE";
                    String piece2 = "$#VADVNAJ8bjFFASDFN";
                    String piece3 = "DFBFN4F8FFDFFPAAF";
                    String piece4 = "AFVAD0FEEFPFAFVdRFEF";

                    if((piece1 + piece2 + piece3 + piece4).equals(payload) &&
                            activity.getPackageName().equals(packagetName) &&
                            purchased) {
                        Log.v(LOG_TAG, "onActivityResult: " + "purchased");
                        if(sCache != null) {
                            PurchaseInfo p = sCache.get(sku);
                            p.purchased = true;
                            Log.v(LOG_TAG, "onActivityResult: " + sCache);
                            activity.onPurchaseInfoRetrieved(sCache);
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                Log.v(LOG_TAG, "onActivityResult: " + resultCode);
            }

            return true;
        }
        else {
            return false;
        }
    }

    public String priceString;
    public boolean purchased;
    public String sku;
    public String currencySymbol;
    public double priceValue;

    @Override
    public String toString() {
        return "[" + priceString + ", " + purchased + "]";
    }

    @Override
    public boolean equals(Object o) {
        if(this != o) return false;
        if(!o.getClass().equals(PurchaseInfo.class)) return false;

        PurchaseInfo p = (PurchaseInfo)o;
        return (this.priceString != null && this.priceString.equals(p.priceString) || this.priceString == p.priceString) &&
                this.purchased == p.purchased &&
                (this.currencySymbol != null && this.currencySymbol.equals(p.currencySymbol) || this.currencySymbol == p.currencySymbol) &&
                this.priceValue == p.priceValue;
    }
}
