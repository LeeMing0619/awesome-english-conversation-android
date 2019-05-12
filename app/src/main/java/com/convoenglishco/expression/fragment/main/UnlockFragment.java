package com.convoenglishllc.expression.fragment.main;

import android.content.DialogInterface;
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


public class UnlockFragment extends BaseFragment implements PurchaseInfo.OnPurchaseInfoListener {
    final String TAG = this.getClass().getSimpleName();

    public static UnlockFragment newInstance() { return new UnlockFragment();}
    public static boolean m_bPurchase = false;

    public UnlockFragment initData() {
        setRetainInstance(true);
        return this;
    }

    View rootView = null;
    LinearLayout section_purchase = null;
    boolean clicked = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        L.d(TAG, "onCreateView()");
        mTitle = getContext().getString(R.string.app_name);
        mSubTitle = getContext().getString(R.string.title_unlock);
        View rootView = inflater.inflate(R.layout.fragment_main_unlock, container, false);

        initData();
        this.rootView = rootView;
        this.section_purchase = (LinearLayout)rootView.findViewById(R.id.section_purchase);

        RelativeLayout purchaseNow = (RelativeLayout) rootView.findViewById(R.id.ui_btn_purchase);

        purchaseNow.setTag(PurchaseInfo.SKU_PREMIUM);

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
                ConfirmPurchaseDialogue();
                ((MainActivity)getActivity()).onUpgradeAppButtonClicked((String) v.getTag());
            }
        };

        purchaseNow.setOnClickListener(clickUpgrade);

        updateContent(null);

        if(LessonManager.isAllCategoryPurchased(getContext())){
            this.section_purchase.setVisibility(View.INVISIBLE);
        }

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
        }


        boolean removeAd = PurchaseInfo.doRemoveAd(purchaseInfos);

//        rootView.findViewById(R.id.section_purchase).setVisibility(View.VISIBLE);

//        if (purchaseInfos == null) {
//            rootView.findViewById(R.id.section_purchase).setVisibility(View.GONE);
//            purchaseStatus.setVisibility(View.GONE);
//        }
//        else if (removeAd) {
//            rootView.findViewById(R.id.section_purchase).setVisibility(View.GONE);
//            purchaseStatus.setVisibility(View.VISIBLE);
//        } else {
//            rootView.findViewById(R.id.section_purchase).setVisibility(View.VISIBLE);
//            purchaseStatus.setVisibility(View.GONE);
//        }
    }

    public void ConfirmPurchaseDialogue(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        LayoutInflater inflater = this.getLayoutInflater();
        TextView dialogue_title;
        View titleView = inflater.inflate(R.layout.dialogue_title, null);
        dialogue_title = titleView.findViewById(R.id.dialogue_title);
        dialogue_title.setText("Confirm Purchase");

        alertDialogBuilder.setCustomTitle(titleView);
        alertDialogBuilder
                .setMessage("Do you want to purchase and unlock 200 additional lessons?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //m_bPurchase = true;
                        LessonManager.updatePurchaseStatus(getContext());
                        section_purchase.setVisibility(View.INVISIBLE);
                        pushFragment(CategoryListFragment.newInstance());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
