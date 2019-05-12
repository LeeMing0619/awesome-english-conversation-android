package com.convoenglishllc.expression.fragment.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.convoenglishllc.expression.MyApplication;
import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.activity.MainActivity;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.data.model.LessonDataObject;

import com.convoenglishllc.expression.utils.AudioDownloader;
import com.convoenglishllc.expression.utils.ConnectionMonitor;
import com.convoenglishllc.expression.utils.L;
import com.convoenglishllc.expression.utils.PurchaseInfo;
import com.convoenglishllc.expression.widget.ProgressIndicator;

import java.io.File;
import java.util.Hashtable;


public class OfflineModeDetailFragment extends BaseFragment implements PurchaseInfo.OnPurchaseInfoListener {
    final String TAG = this.getClass().getSimpleName();

    public int mCategoryType = 0;
    public static OfflineModeDetailFragment newInstance() { return new OfflineModeDetailFragment();}

    public OfflineModeDetailFragment initData() {
        setRetainInstance(true);
        return this;
    }

    View rootView = null;
    LinearLayout viewPurchase = null;
    RelativeLayout btnDownload1 = null;
    RelativeLayout btnDownload2 = null;
    RelativeLayout btnDownload3 = null;
    TextView btnDeleteAll = null;
    TextView ui_offline_text1 = null;
    TextView ui_offline_text2 = null;
    TextView ui_offline_text3 = null;

    ImageView ui_download_lock2 = null;
    ImageView ui_download_lock3 = null;
    boolean clicked = false;
    boolean offlineON1 = false;
    boolean offlineON2 = false;
    boolean offlineON3 = false;
    boolean offlinePurchaseStatus = false;

    LinearLayout viewDownloadDlg = null;
    ProgressIndicator pi_download_status = null;

    private static final String OFFLINE_MODE = "offline";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        offlinePurchaseStatus = LessonManager.isOfflinePurchased(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        L.d(TAG, "onCreateView()");
        mTitle = getContext().getString(R.string.app_name);
        mSubTitle = getContext().getString(R.string.title_offline);
        View rootView = inflater.inflate(R.layout.fragment_main_offline_detail, container, false);

        initData();

        this.rootView = rootView;

        this.viewPurchase = (LinearLayout)rootView.findViewById(R.id.offline_purchase);

        this.btnDownload1 = (RelativeLayout)rootView.findViewById(R.id.ui_btn_offline_download_first);
        this.btnDownload2 = (RelativeLayout)rootView.findViewById(R.id.ui_btn_offline_download_second);
        this.btnDownload3 = (RelativeLayout)rootView.findViewById(R.id.ui_btn_offline_download_third);
        this.btnDeleteAll = (TextView)rootView.findViewById(R.id.ui_btn_deleteall);
        this.ui_offline_text1 = (TextView)rootView.findViewById(R.id.ui_offline_text1);
        this.ui_offline_text2 = (TextView)rootView.findViewById(R.id.ui_offline_text2);
        this.ui_offline_text3 = (TextView)rootView.findViewById(R.id.ui_offline_text3);

        this.ui_download_lock2 = (ImageView)rootView.findViewById(R.id.ui_download_lock2);
        this.ui_download_lock3 = (ImageView)rootView.findViewById(R.id.ui_download_lock3);

        this.viewDownloadDlg = (LinearLayout)rootView.findViewById(R.id.download_windows);
        this.pi_download_status = (ProgressIndicator)rootView.findViewById(R.id.pi_download_status);

        final SharedPreferences preferences = getActivity().getSharedPreferences("OfflineModeFragment", Context.MODE_PRIVATE);
        int countdown = preferences.getInt(OFFLINE_MODE, -1);
        if(countdown == 1){
            setContentOffline(true);
        } else {
            setContentOffline(false);
        }

        if(countdown != -1){
            this.viewPurchase.setVisibility(View.VISIBLE);
        } else {
            this.viewPurchase.setVisibility(View.VISIBLE);
        }

        this.btnDownload1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mCategoryType = 1;
                if(offlineON1) {
                    deleteAll();
                } else {
                    downloadAll(mCategoryType);
                }
            }
        });

        this.btnDownload2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(offlinePurchaseStatus){
                    mCategoryType = 2;
                    if(offlineON2) {
                        deleteAll();
                    } else {
                        downloadAll(mCategoryType);
                    }
                }else{
                    unlockDiaglogue();
                }

            }
        });

        this.btnDownload3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(offlinePurchaseStatus) {
                    mCategoryType = 3;
                    if (offlineON3) {
                        deleteAll();
                    } else {
                        downloadAll(mCategoryType);
                    }
                }else{
                    unlockDiaglogue();
                }
            }
        });

        MyApplication app = (MyApplication)getActivity().getApplication();
        hideProgressBar();
        if(app != null){
            if(app.taskDownload != null){
                if(!ConnectionMonitor.isNetworkAvailable(getContext())) {
                    L.e(TAG, "No internet connection");
                    changeButtonTitle(true);
                } else {
                    this.viewDownloadDlg.setVisibility(View.VISIBLE);
                    if (app.nTotalCount > 0) {
                        float fProgress = (float) app.nCurrentDownloadCount / (float) app.nTotalCount;
                        setProgress(fProgress);
                    } else {
                        setProgress(0);
                    }
                }
            }
        }

        app.fragmentOfflineDetail = this;

        if(offlinePurchaseStatus){
            searchAudioFiles();
            this.ui_download_lock2.setVisibility(View.INVISIBLE);
            this.ui_download_lock3.setVisibility(View.INVISIBLE);
        }else{
            btnDownload2.setBackgroundColor(getResources().getColor(R.color.gradient_start));
            btnDownload3.setBackgroundColor(getResources().getColor(R.color.gradient_start));
            searchFirstAudioFiles();
        }



        return rootView;
    }

    public void showProgressBar(){
        this.viewDownloadDlg.setVisibility(View.VISIBLE);
        this.pi_download_status.setProgress(0);
        this.btnDeleteAll.setVisibility(View.INVISIBLE);
    }

    public void changeButtonTitle(Boolean bFailed) {
        if(bFailed == true) {
            String str = "Please check your internet connection";
            Toast toast = Toast.makeText(getContext(), str, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            if (mCategoryType == 1){
                this.ui_offline_text1.setText("Download");
                this.btnDownload1.setBackgroundColor(getResources().getColor(R.color.colorDownload));
                offlineON1 = false;
            }else if (mCategoryType == 2 ){
                this.ui_offline_text2.setText("Download");
                this.btnDownload2.setBackgroundColor(getResources().getColor(R.color.colorDownload));
                offlineON2 = false;
            }else if (mCategoryType == 3 ){
                this.ui_offline_text3.setText("Download");
                this.btnDownload3.setBackgroundColor(getResources().getColor(R.color.colorDownload));
                offlineON3 = false;
            }
        }
    }

    public void hideProgressBar(){
        this.viewDownloadDlg.setVisibility(View.GONE);
        this.btnDeleteAll.setVisibility(View.VISIBLE);
    }

    public void setProgress(float fProgress){
        this.viewDownloadDlg.setVisibility(View.VISIBLE);
        this.btnDeleteAll.setVisibility(View.INVISIBLE);
        this.pi_download_status.setProgress(fProgress);
    }

    public void showRetryDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        //alertDialogBuilder.setTitle("Error");
        alertDialogBuilder
                .setMessage(this.getString(R.string.toast_download_audio_failed))
                .setCancelable(false)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        downloadAll(mCategoryType);
                    }
                })
                .setNegativeButton("Skip", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void setContentOffline(Boolean bOn){
        if(bOn){
            if (mCategoryType == 1){
                this.ui_offline_text1.setText("Delete");
                this.btnDownload1.setBackgroundColor(getResources().getColor(R.color.colorRed));
                offlineON1 = true;
            }else if (mCategoryType == 2 ){
                this.ui_offline_text2.setText("Delete");
                this.btnDownload2.setBackgroundColor(getResources().getColor(R.color.colorRed));
                offlineON2 = true;
            }else if (mCategoryType == 3 ){
                this.ui_offline_text3.setText("Delete");
                this.btnDownload3.setBackgroundColor(getResources().getColor(R.color.colorRed));
                offlineON3 = true;
            }
        } else {
            if (mCategoryType == 1){
                this.ui_offline_text1.setText("Download");
                this.btnDownload1.setBackgroundColor(getResources().getColor(R.color.colorDownload));
                offlineON1 = false;
            }else if (mCategoryType == 2 ){
                this.ui_offline_text2.setText("Download");
                this.btnDownload2.setBackgroundColor(getResources().getColor(R.color.colorDownload));
                offlineON2 = false;
            }else if (mCategoryType == 3 ){
                this.ui_offline_text3.setText("Download");
                this.btnDownload3.setBackgroundColor(getResources().getColor(R.color.colorDownload));
                offlineON3 = false;
            }
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        MyApplication app = (MyApplication)getActivity().getApplication();
        app.fragmentOfflineDetail = this;

        MainActivity activity = (MainActivity) getActivity();
        onPurchaseInfoRetrieved(PurchaseInfo.retrievePurchaseInfoList(activity, activity.getBillingService(), this));
    }

    @Override
    public void onResume() {
        super.onResume();
        MyApplication app = (MyApplication)getActivity().getApplication();
        app.fragmentOfflineDetail = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        MyApplication app = (MyApplication)getActivity().getApplication();
        app.fragmentOfflineDetail = null;
    }

    @Override
    public void onPurchaseInfoRetrieved(Hashtable<String, PurchaseInfo> purchaseInfos) {

    }

    @Override
    public void onPurchaseInfoFailed() {

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


    public void unlockDiaglogue(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = this.getLayoutInflater();
        TextView dialogue_title;
        View titleView = inflater.inflate(R.layout.dialogue_title, null);
        dialogue_title = titleView.findViewById(R.id.dialogue_title);
        dialogue_title.setText("Lessons Locked");

        alertDialogBuilder.setCustomTitle(titleView);
        alertDialogBuilder
                .setMessage("You have not purchased access to these categories. Do you want to unlock 200 additional lessons now?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        pushFragment(UnlockFragment.newInstance());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.create().show();
    }

    public void deleteAll(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = this.getLayoutInflater();
        TextView dialogue_title;
        View titleView = inflater.inflate(R.layout.dialogue_title, null);
        dialogue_title = titleView.findViewById(R.id.dialogue_title);
        dialogue_title.setText(R.string.dialog_offline_delete_audio);

        alertDialogBuilder.setCustomTitle(titleView);
        alertDialogBuilder
                .setMessage("Are you sure you want to delete all the audio files?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //LessonDataObject[] lessons = LessonManager.getAllLessons(getContext());
                        LessonDataObject[] lessons = null;
                        if (mCategoryType==1){
                            lessons = LessonManager.getFirstCategoryLessons(getContext());
                        }else if(mCategoryType==2){
                            lessons = LessonManager.getSecondCategoryLessons(getContext());
                        }else if(mCategoryType==3){
                            lessons = LessonManager.getThirdCategoryLessons(getContext());
                        }
                        for(LessonDataObject lesson : lessons) {
                            String filePathA = lesson.getDownloadPath_A(getContext());
                            File fA = new File(filePathA);
                            try {
                                if(fA.exists()){
                                    SharedPreferences pref = getContext().getSharedPreferences("BookmarkManager", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putLong(filePathA, 0);
                                    editor.apply();
                                    fA.delete();
                                }
                            } catch (Exception ignored) {}

                            String filePathB = lesson.getDownloadPath_B(getContext());
                            File fB = new File(filePathB);
                            try {
                                if(fB.exists()){
                                    SharedPreferences pref = getContext().getSharedPreferences("BookmarkManager", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putLong(filePathB, 0);
                                    editor.apply();
                                    fB.delete();
                                }
                            } catch (Exception ignored) {}


                            String filePathAll = lesson.getDownloadPath_All(getContext());
                            File fAll = new File(filePathAll);
                            try {
                                if(fAll.exists()){
                                    SharedPreferences pref = getContext().getSharedPreferences("BookmarkManager", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putLong(filePathAll, 0);
                                    editor.apply();
                                    fAll.delete();
                                }
                            } catch (Exception ignored) {}
                        }
                        dialog.cancel();
                        final SharedPreferences preferences = getActivity().getSharedPreferences("OfflineModeFragment", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt(OFFLINE_MODE, 0);
                        editor.apply();
                        setContentOffline(false);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.create().show();
    }

    public void downloadAll(int type) {
        MyApplication app = (MyApplication)getActivity().getApplication();
        if(app != null){
            app.startDownload(type);
        }
    }

    public void searchFirstAudioFiles(){
        LessonDataObject[] lessons1 = LessonManager.getFirstCategoryLessons(getContext());
        int audioCount1 = 0;
        for(LessonDataObject lesson : lessons1){
            if (AudioDownloader.isDownloaded(getContext(), lesson.getDownloadPath_All(getContext()))) audioCount1++;
            if (AudioDownloader.isDownloaded(getContext(), lesson.getDownloadPath_A(getContext()))) audioCount1++;
            if (AudioDownloader.isDownloaded(getContext(), lesson.getDownloadPath_B(getContext()))) audioCount1++;
        }
        if (audioCount1 == 300){
            this.ui_offline_text1.setText("Delete");
            this.btnDownload1.setBackgroundColor(getResources().getColor(R.color.colorRed));
            offlineON1 = true;
        }
    }

    public void searchAudioFiles() {
        LessonDataObject[] lessons1 = LessonManager.getFirstCategoryLessons(getContext());
        int audioCount1 = 0, audioCount2 = 0, audioCount3 = 0;
        for(LessonDataObject lesson : lessons1){
            if (AudioDownloader.isDownloaded(getContext(), lesson.getDownloadPath_All(getContext()))) audioCount1++;
            if (AudioDownloader.isDownloaded(getContext(), lesson.getDownloadPath_A(getContext()))) audioCount1++;
            if (AudioDownloader.isDownloaded(getContext(), lesson.getDownloadPath_B(getContext()))) audioCount1++;
        }
        if (audioCount1 == 300){
            this.ui_offline_text1.setText("Delete");
            this.btnDownload1.setBackgroundColor(getResources().getColor(R.color.colorRed));
            offlineON1 = true;
        }

        LessonDataObject[] lessons2 = LessonManager.getSecondCategoryLessons(getContext());
        for(LessonDataObject lesson : lessons2){
            if (AudioDownloader.isDownloaded(getContext(), lesson.getDownloadPath_All(getContext()))) audioCount2++;
            if (AudioDownloader.isDownloaded(getContext(), lesson.getDownloadPath_A(getContext()))) audioCount2++;
            if (AudioDownloader.isDownloaded(getContext(), lesson.getDownloadPath_B(getContext()))) audioCount2++;
        }
        if (audioCount2 == 300){
            this.ui_offline_text2.setText("Delete");
            this.btnDownload2.setBackgroundColor(getResources().getColor(R.color.colorRed));
            offlineON2 = true;
        }

        LessonDataObject[] lessons3 = LessonManager.getThirdCategoryLessons(getContext());
        for(LessonDataObject lesson : lessons3){
            if (AudioDownloader.isDownloaded(getContext(), lesson.getDownloadPath_All(getContext()))) audioCount3++;
            if (AudioDownloader.isDownloaded(getContext(), lesson.getDownloadPath_A(getContext()))) audioCount3++;
            if (AudioDownloader.isDownloaded(getContext(), lesson.getDownloadPath_B(getContext()))) audioCount3++;
        }
        if (audioCount3 == 300){
            this.ui_offline_text3.setText("Delete");
            this.btnDownload3.setBackgroundColor(getResources().getColor(R.color.colorRed));
            offlineON3 = true;
        }
    }

}
