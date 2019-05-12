package com.convoenglishllc.expression.fragment.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.activity.MainActivity;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.utils.GlobalConstants;
import com.convoenglishllc.expression.utils.L;
import com.convoenglishllc.expression.utils.PurchaseInfo;



import java.util.Hashtable;


public class PurchaseFragment extends BaseFragment implements PurchaseInfo.OnPurchaseInfoListener {
    final String TAG = this.getClass().getSimpleName();

    public static PurchaseFragment newInstance() { return new PurchaseFragment();}

    public PurchaseFragment initData() {
        setRetainInstance(true);
        return this;
    }

    View rootView = null;
    boolean clicked = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        L.d(TAG, "onCreateView()");
        mTitle = getContext().getString(R.string.app_name);
        mSubTitle = getContext().getString(R.string.title_purchase);
        View rootView = inflater.inflate(R.layout.fragment_main_purchase, container, false);

        initData();
        this.rootView = rootView;

        RelativeLayout purchaseNow = (RelativeLayout) rootView.findViewById(R.id.ui_btn_purchase);
        RelativeLayout purchaseNow1 = (RelativeLayout) rootView.findViewById(R.id.ui_btn_purchase1);
        RelativeLayout purchaseNow3 = (RelativeLayout) rootView.findViewById(R.id.ui_btn_purchase3);

        purchaseNow.setTag(PurchaseInfo.SKU_PREMIUM);
        purchaseNow1.setTag(PurchaseInfo.SKU_PREMIUM1);
        purchaseNow3.setTag(PurchaseInfo.SKU_PREMIUM3);
        View.OnClickListener clickUpgrade = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clicked) return;
                clicked = true;
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        clicked = false;
                    }
                }, GlobalConstants.DELAY_CLICK_LESSON_ITEM);

                ((MainActivity)getActivity()).onUpgradeAppButtonClicked((String) v.getTag());
                removeAdsDialogue();

            }
        };

        purchaseNow.setOnClickListener(clickUpgrade);
        purchaseNow1.setOnClickListener(clickUpgrade);
        purchaseNow3.setOnClickListener(clickUpgrade);

        updateContent(null);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = (MainActivity) getActivity();
        onPurchaseInfoRetrieved(PurchaseInfo.retrievePurchaseInfoList(activity, activity.getBillingService(), this));
    }

    @Override
    public void onPurchaseInfoRetrieved(Hashtable<String, PurchaseInfo> purchaseInfos) {
        updateContent(purchaseInfos);
    }

    @Override
    public void onPurchaseInfoFailed() {

    }

    private void updateContent(Hashtable<String, PurchaseInfo> purchaseInfos) {
        if(!isAdded()) return;

        if(purchaseInfos != null) {
            PurchaseInfo p0 = purchaseInfos.get(PurchaseInfo.SKU_PREMIUM);
            if(p0 != null) {
                ((TextView) rootView.findViewById(R.id.ui_price)).setText(p0.priceString);
            }
            PurchaseInfo p1 = purchaseInfos.get(PurchaseInfo.SKU_PREMIUM1);
            if(p1 != null) {
                ((TextView) rootView.findViewById(R.id.ui_price1)).setText(p1.priceString);
            }
            PurchaseInfo p3 = purchaseInfos.get(PurchaseInfo.SKU_PREMIUM3);
            if(p3 != null) {
                ((TextView) rootView.findViewById(R.id.ui_price3)).setText(p3.priceString);
            }
        }


        boolean removeAd = PurchaseInfo.doRemoveAd(purchaseInfos);

        LinearLayout purchaseStatus = (LinearLayout) rootView.findViewById(R.id.ui_purchase_status);
        if (purchaseInfos == null) {
            rootView.findViewById(R.id.section_purchase).setVisibility(View.VISIBLE);
            purchaseStatus.setVisibility(View.GONE);
        }
        else if (removeAd) {
            rootView.findViewById(R.id.section_purchase).setVisibility(View.GONE);
            purchaseStatus.setVisibility(View.VISIBLE);
        } else {
            rootView.findViewById(R.id.section_purchase).setVisibility(View.VISIBLE);
            purchaseStatus.setVisibility(View.GONE);
        }
    }

    public void removeAdsDialogue(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        TextView dialogue_title;
        View titleView = inflater.inflate(R.layout.dialogue_title, null);
        dialogue_title = titleView.findViewById(R.id.dialogue_title);
        dialogue_title.setText(R.string.dialog_ads_title);

        alertDialogBuilder.setCustomTitle(titleView);
        alertDialogBuilder
                .setMessage("Do you want to purchase the remove all ads feature?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //((MainActivity)getActivity()).onUpgradeAppButtonClicked((String) v.getTag());
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.create().show();
    }

}
