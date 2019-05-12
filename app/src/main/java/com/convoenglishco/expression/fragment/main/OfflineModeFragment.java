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


public class OfflineModeFragment extends BaseFragment implements PurchaseInfo.OnPurchaseInfoListener {
    final String TAG = this.getClass().getSimpleName();


    public static OfflineModeFragment newInstance() { return new OfflineModeFragment();}

    public OfflineModeFragment initData() {
        setRetainInstance(true);
        return this;
    }

    View rootView = null;
    LinearLayout viewPurchase = null;
    RelativeLayout btnPurchase = null;

    boolean clicked = false;



    private static final String OFFLINE_MODE = "offline";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        L.d(TAG, "onCreateView()");
        mTitle = getContext().getString(R.string.app_name);
        mSubTitle = getContext().getString(R.string.title_offline);
        View rootView = inflater.inflate(R.layout.fragment_main_offline, container, false);

        initData();
        this.rootView = rootView;

        this.viewPurchase = (LinearLayout)rootView.findViewById(R.id.offline_purchase);

        this.btnPurchase = (RelativeLayout)rootView.findViewById(R.id.ui_btn_offline_purchase);

        final SharedPreferences preferences = getActivity().getSharedPreferences("OfflineModeFragment", Context.MODE_PRIVATE);
        int countdown = preferences.getInt(OFFLINE_MODE, -1);

        if(countdown != -1){
            this.viewPurchase.setVisibility(View.VISIBLE);
        } else {
            this.viewPurchase.setVisibility(View.VISIBLE);
        }

        btnPurchase.setTag(PurchaseInfo.SKU_PREMIUM5);
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
                gotoDownloadDetail(v);

            }
        };

        btnPurchase.setOnClickListener(clickUpgrade);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = (MainActivity) getActivity();
        onPurchaseInfoRetrieved(PurchaseInfo.retrievePurchaseInfoList(activity, activity.getBillingService(), this));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onPurchaseInfoRetrieved(Hashtable<String, PurchaseInfo> purchaseInfos) {
        updateContent(purchaseInfos);
    }

    @Override
    public void onPurchaseInfoFailed() {

    }

    public void gotoDownloadDetail(View v){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        TextView dialogue_title;
        View titleView = inflater.inflate(R.layout.dialogue_title, null);
        dialogue_title = titleView.findViewById(R.id.dialogue_title);
        dialogue_title.setText(R.string.dialog_offline_title);

        alertDialogBuilder.setCustomTitle(titleView);
        alertDialogBuilder
                .setMessage("Do you want to purchase the Offline Mode feature?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //((MainActivity)getActivity()).onUpgradeAppButtonClicked((String) v.getTag());
                        dialog.cancel();
                        final SharedPreferences preferences = getActivity().getSharedPreferences("OfflineModeFragment", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt(OFFLINE_MODE, 0);
                        editor.apply();
                        LessonManager.updateOfflinePurchaseStatus(getContext());
                        pushFragment(OfflineModeDetailFragment.newInstance());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        pushFragment(OfflineModeDetailFragment.newInstance());
                    }
                });
        alertDialogBuilder.create().show();
    }


    private void updateContent(Hashtable<String, PurchaseInfo> purchaseInfos) {
        if(!isAdded()) return;

        if(purchaseInfos != null) {
            PurchaseInfo p0 = purchaseInfos.get(PurchaseInfo.SKU_PREMIUM5);
            if(p0 != null) {
                ((TextView) rootView.findViewById(R.id.ui_offline_price)).setText(p0.priceString);
            }
        }


        boolean offlineMode = PurchaseInfo.doOfflineMode(purchaseInfos);

        if (purchaseInfos == null) {
            final SharedPreferences preferences = getActivity().getSharedPreferences("OfflineModeFragment", Context.MODE_PRIVATE);
            int countdown = preferences.getInt(OFFLINE_MODE, -1);
            if(countdown == -1) {
                this.viewPurchase.setVisibility(View.VISIBLE);
            }
        }
        else if (offlineMode) {
            this.viewPurchase.setVisibility(View.VISIBLE);
            final SharedPreferences preferences = getActivity().getSharedPreferences("OfflineModeFragment", Context.MODE_PRIVATE);
            int countdown = preferences.getInt(OFFLINE_MODE, -1);
            if(countdown == -1) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(OFFLINE_MODE, 0);
                editor.apply();
            }
        } else {
            this.viewPurchase.setVisibility(View.VISIBLE);
        }
    }

    public void pushFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        //transaction.addToBackStack("fragment_container" + fm.getBackStackEntryCount());
        transaction.addToBackStack(null);
        //transaction.commit();
        transaction.detach(fragment).attach(fragment).commitAllowingStateLoss();
    }
}
