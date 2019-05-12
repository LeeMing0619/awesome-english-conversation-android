package com.convoenglishllc.expression.fragment.lesson;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.activity.LessonActivity;
import com.convoenglishllc.expression.activity.RecordListActivity;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.data.model.LessonDataObject;
import com.convoenglishllc.expression.utils.ExtAudioRecorder;
import com.convoenglishllc.expression.utils.GlobalConstants;
import com.convoenglishllc.expression.utils.ImageProcess;
import com.convoenglishllc.expression.utils.L;
import com.convoenglishllc.expression.utils.PermissionManager;

import net.sourceforge.sox.ShellUtils;
import net.sourceforge.sox.SoxController;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.markushi.ui.CircleButton;

public class RecordFragment extends Fragment implements MediaPlayer.OnCompletionListener, FragmentLifeCycle {
    private static final int MESSAGE_ARG_INIT = 1001;

    private static RecordFragment _instance = null;

    private static final int PERSON_A = 0;
    private static final int PERSON_B = 1;

    private static final int STATUS_STOPPED = 0;
    private static final int STATUS_RECORDING = 1;

    private final String TAG = this.getClass().getSimpleName();

    private View mView = null;
    private TextView uiDialog = null;

    private TextView uiPartnerTitle = null;

    private ImageView uiFirstImage = null;
    private TextView uiFirstName = null;
    private ImageView uiFirstCheck = null;

    private ImageView uiSecondImage = null;
    private TextView uiSecondName = null;
    private ImageView uiSecondCheck = null;

    private TextView uiRecordStatus = null;
    private CircleButton uiRecordStart = null;
    private CircleButton uiRecordList = null;

    /////////////////////////////////////////////
    private LessonDataObject mLessonData = null;
    private int mSelectedPerson = -1;
    private MediaPlayer mMediaPlayer;

    private boolean mDownloaded = false;
    /////////////////////////////////////////////
    private int mRecordState = 0;
    private ExtAudioRecorder mAudioRecorder = null;



    private int mLessonNo = -1;
    public SoxController mSoxController = null;

    public RecordFragment() {

    }

    public static RecordFragment newInstance(int lesson_no) {
        RecordFragment n = new RecordFragment();
        n.mLessonNo = lesson_no;
        return n;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("lesson_no", mLessonNo);
        outState.putInt("selected_person", mSelectedPerson);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAudioRecorder != null) {
            stopRecording(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey("lesson_no")) {
            mLessonNo = savedInstanceState.getInt("lesson_no");
            mSelectedPerson = savedInstanceState.getInt("selected_person");
        }
        mLessonData = LessonManager.getLessonByNo(getContext(), mLessonNo);
        mMediaPlayer = null;
        mAudioRecorder = null;
        setRetainInstance(true);

        mSoxController = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*if(mLessonNo == -1) {
            mLessonNo = ((LessonActivity)getActivity()).getLessonNo();
            initData();
            ImageProcess.preCacheAssetImagesInGray(getActivity(), mLessonData.getFirstDrawableAssetName());
            ImageProcess.preCacheAssetImagesInGray(getActivity(), mLessonData.getSecondDrawableAssetName());
        }*/

        mView = inflater.inflate(R.layout.fragment_leseson_record, container, false);

        uiDialog  = (TextView) mView.findViewById(R.id.ui_dialog);

        uiFirstName = (TextView) mView.findViewById(R.id.ui_first_name);
        uiFirstImage = (ImageView) mView.findViewById(R.id.ui_first_image);
        uiFirstCheck= (ImageView) mView.findViewById(R.id.ui_first_check);

        uiSecondName = (TextView) mView.findViewById(R.id.ui_second_name);
        uiSecondImage = (ImageView) mView.findViewById(R.id.ui_second_image);
        uiSecondCheck= (ImageView) mView.findViewById(R.id.ui_second_check);

        uiRecordStatus = (TextView) mView.findViewById(R.id.ui_record_status);
        uiRecordStart = (CircleButton) mView.findViewById(R.id.ui_record_start);
        uiRecordList = (CircleButton) mView.findViewById(R.id.ui_record_list);

        uiPartnerTitle = (TextView) mView.findViewById(R.id.ui_partner_title);

        if(mRecordState == -1) mRecordState = STATUS_STOPPED;

        RelativeLayout uiLeftPanel = (RelativeLayout) mView.findViewById(R.id.ui_first_panel);
        RelativeLayout uiRightPanel = (RelativeLayout) mView.findViewById(R.id.ui_second_panel);

        uiLeftPanel.setOnClickListener(
                new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           if(mRecordState == STATUS_RECORDING) {
                               L.toast(getActivity(), getString(R.string.toast_select_after_record_stop));
                               return;
                           }
                           initMediaPlayer(PERSON_A);
                       }});

        uiRightPanel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mRecordState == STATUS_RECORDING) {
                            L.toast(getActivity(), getString(R.string.toast_select_after_record_stop));
                            return;
                        }
                        initMediaPlayer(PERSON_B);
                    }});

        uiRecordStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRecordStart();
            }
        });

        uiRecordList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRecordList(null);
            }
        });

        updateLessonData();

        _instance = this;
        return mView;
    }

    private void updateLessonData() {
        if(mLessonData == null) return;

        uiDialog.setText(mLessonData.getSpannableDialog());
        uiFirstName.setText(mLessonData.getTalkers()[0]);
        uiSecondName.setText(mLessonData.getTalkers()[1]);

        updateTopUI();
    }

    private void updateTopUI() {
        uiPartnerTitle.setText(R.string.lesson_partner_text);

        if(mSelectedPerson == -1) {
            uiFirstCheck.setVisibility(View.INVISIBLE);
            uiSecondCheck.setVisibility(View.INVISIBLE);
            uiDialog.setText(mLessonData.getSpannableDialog());

            ImageProcess.loadAssetImage(getActivity(), mLessonData.getFirstDrawableAssetName(), uiFirstImage);
            ImageProcess.loadAssetImage(getActivity(), mLessonData.getSecondDrawableAssetName(), uiSecondImage);
        } else if(mSelectedPerson == PERSON_A) {
            uiFirstCheck.setVisibility(View.VISIBLE);
            uiSecondCheck.setVisibility(View.INVISIBLE);
            uiDialog.setText(mLessonData.getSpannableDialogB());

            ImageProcess.loadAssetImage(getActivity(), mLessonData.getFirstDrawableAssetName(), uiFirstImage);
            ImageProcess.loadGrayedAssetImage(getActivity(), mLessonData.getSecondDrawableAssetName(), uiSecondImage);
        } else if(mSelectedPerson == PERSON_B) {
            uiFirstCheck.setVisibility(View.INVISIBLE);
            uiSecondCheck.setVisibility(View.VISIBLE);
            uiDialog.setText(mLessonData.getSpannableDialogA());

            ImageProcess.loadGrayedAssetImage(getActivity(), mLessonData.getFirstDrawableAssetName(), uiFirstImage);
            ImageProcess.loadAssetImage(getActivity(), mLessonData.getSecondDrawableAssetName(), uiSecondImage);
        }

        if(mRecordState == STATUS_STOPPED) {
            uiRecordStart.setColor(Color.rgb(8, 102, 169));
            uiRecordStart.setImageResource(R.drawable.ic_record_start);
            uiRecordStatus.setText(R.string.start_recording);
        } else if(mRecordState == STATUS_RECORDING) {
            uiRecordStart.setImageResource(R.drawable.ic_record_stop);
            uiRecordStatus.setText(R.string.stop_recording);
        }
    }

    @Override
    public void onDestroy() {
        L.d(TAG, "onDestroy()");
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    /*********************************/
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mp != mMediaPlayer) return;
        mMediaPlayer.seekTo(0);

        if (mAudioRecorder != null) stopRecording(false);
        L.d(TAG, "onCompletion()");
    }

    public boolean checkSelfPermissions() {
        final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
        final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= 23){
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.RECORD_AUDIO)) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                }
            } else {
                L.d(TAG, "Already allowed - RECORD_AUDIO");
                PermissionManager.getInstance().setFlag(PermissionManager.FLAG_AUDIO_RECORD_ENABLED);
            }

            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(getActivity(),
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

    ProgressDialog mProgressDialog = null;

    private void onClickRecordStart() {
        if( (PermissionManager.getInstance().getFlag() & PermissionManager.FLAG_AUDIO_RECORD_ENABLED) == 0) {
            L.toast(getActivity(), "Record Permission denied. Try again after allow Record Permission.");
            checkSelfPermissions();
            return;
        }

        if(mRecordState == STATUS_STOPPED) {
            if(mSelectedPerson== -1) {
                L.toast(getActivity(), getString(R.string.toast_select_conversation_partner));
                return;
            }
            if (!isHeadsetOn()) {
                startRecording();
            } else if(mSoxController != null) {
                startRecording();
            } else {
                mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.title_load_soxlibrary), true, false, null);
                mProgressDialog.setCanceledOnTouchOutside(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mSoxController = new SoxController(getContext(), mShellCallback);
                        } catch (IOException e) {
                            L.toast(getContext(), getString(R.string.toast_sox_init_failed));
                            e.printStackTrace();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                startRecording();
                            }
                        });
                    }
                }).start();
            }
        } else if(mRecordState == STATUS_RECORDING) {
            stopRecording(false);
        }
    }

    ShellUtils.ShellCallback mShellCallback = new ShellUtils.ShellCallback() {
        @Override
        public void shellOut(String shellLine) {
            System.out.println("sxCon> " + shellLine);
        }

        @Override
        public void processComplete(int exitValue) {
            if (exitValue != 0) {
                System.err.println("sxCon> EXIT=" + exitValue);
                RuntimeException re = new RuntimeException("non-zero exit: "
                        + exitValue);
                re.printStackTrace();
                throw re;
            }
        }
    };

    private void onClickRecordList(String url) {
        Intent intent = new Intent(getActivity(), RecordListActivity.class);
        if(url != null) intent.putExtra(GlobalConstants.EXTRA_RECORD_URL, url);
        getActivity().startActivity(intent);
    }
    /*********************************/
    private int mCC = 1;
    Handler mBlinkingTaskHandler = new Handler();
    private Runnable mBlinkingTask = new Runnable() {
        public void run() {
            if(mCC == 1) {
                uiRecordStart.setColor(Color.rgb(200, 50, 50));
            } else {
                uiRecordStart.setColor(Color.rgb(8, 102, 169));
            }
            mCC = -1 * mCC;
            mBlinkingTaskHandler.postDelayed(mBlinkingTask, 1000);
        }
    };

    private void startRecording() {
        L.d(TAG, "startRecording()");

        mAudioRecorder = ExtAudioRecorder.getInstance(false);
        mAudioRecorder.setOutputFile(GlobalConstants.getTempFileRecordFg(getContext()));
        try {
            if(initMediaPlayer(mSelectedPerson) == false) throw new Exception("init media player error");
            mMediaPlayer.start();
            mAudioRecorder.prepare();
            mAudioRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            L.toast(getActivity(), getString(R.string.toast_prepare_record_fail));
            mAudioRecorder = null;
            return;
        }

        mRecordState = STATUS_RECORDING;
        mCC = 1;  mBlinkingTaskHandler.postDelayed(mBlinkingTask, 1000);
        updateTopUI();
    }

    private void stopRecording(boolean forced) {
        L.d(TAG, "stopRecording()");
        if(mAudioRecorder == null) return;
        mAudioRecorder.stop();
        mAudioRecorder.reset();
        mAudioRecorder.release();
        mAudioRecorder = null;

        mRecordState = STATUS_STOPPED;
        mMediaPlayer.release();

        mBlinkingTaskHandler.removeCallbacks(mBlinkingTask);
        updateTopUI();

        if(!forced) showPostRecordDialog();
        else L.toast(LessonActivity.gContext, LessonActivity.gContext.getString(R.string.toast_record_stopped));
    }

    private void setupMediaPlayerListener() {
        mMediaPlayer.setOnCompletionListener(this);
    }

    private void showPostRecordDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        LayoutInflater inflater = this.getLayoutInflater();
        TextView dialogue_title;
        View titleView = inflater.inflate(R.layout.dialogue_title, null);
        dialogue_title = titleView.findViewById(R.id.dialogue_title);
        dialogue_title.setText(R.string.dialog_save_record_title);

        alertDialogBuilder.setCustomTitle(titleView);
        alertDialogBuilder
                .setMessage(getActivity().getString(R.string.dialog_save_record_confirm))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String outputWav = GlobalConstants.getRecordDir(getContext()) + "/" + String.format("[%s]-Talk with %s-%d.wav", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), mLessonData.getTalkers()[mSelectedPerson], mLessonData.getNo());
                        if (!isHeadsetOn()) {
                            L.d(TAG, "Headset is not plugged");
                            try {
                                new File(GlobalConstants.getTempFileRecordFg(getContext())).renameTo(new File(outputWav));
                            } catch (Exception e) {
                                e.printStackTrace();
                                L.toast(getActivity(), getString(R.string.toast_save_record_fail));
                                return;
                            }
                        } else {
                            double length = mSoxController.getLength(GlobalConstants.getTempFileRecordFg(getContext()));
                            L.d(TAG, "Headset is plugged");
                            String bgWav = null;
                            try {
                                if (mSelectedPerson == PERSON_A)
                                    bgWav = mSoxController.trimAudio(mLessonData.getDownloadPath_A(getContext()), GlobalConstants.getTempFileRecordBg(getContext()), 0, length);
                                else if (mSelectedPerson == PERSON_B)
                                    bgWav = mSoxController.trimAudio(mLessonData.getDownloadPath_B(getContext()), GlobalConstants.getTempFileRecordBg(getContext()), 0, length);
                                List<String> mixList = new ArrayList<>();
                                mixList.add(bgWav);
                                mixList.add(GlobalConstants.getTempFileRecordFg(getContext()));
                                mSoxController.combineMix(mixList, outputWav);
                            } catch (Exception e) {
                                e.printStackTrace();
                                L.toast(getActivity(), getString(R.string.toast_mix_audio_fail));
                                return;
                            }
                        }
                        onClickRecordList(outputWav);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /*********************************/
    public boolean isHeadsetOn() {
        AudioManager am1 = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        return am1.isWiredHeadsetOn();
    }

    /*
    private void downloadAllData(Message msg) {
        new DownloadFileAsync().execute(msg,
                mLessonData.getUrlChannelA(), mLessonData.getDownloadPath_A(),
                mLessonData.getUrlChannelB(), mLessonData.getDownloadPath_B()
        );
    }
    */

    private boolean initMediaPlayer(int person) {
        //if(person == mSelectedPerson) return;
        if(mRecordState == STATUS_RECORDING) {
            L.toast(getActivity(), getString(R.string.toast_select_after_record_stop));
            return false;
        }
        /*
        if(!mDownloaded){ //download now
            Message msgPlay = new Message();
            msgPlay.arg1 = MESSAGE_ARG_INIT;
            msgPlay.arg2 = person;
            downloadAllData(msgPlay);
            return false;
        }
        */

        if(mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mMediaPlayer = null;

        if(person == PERSON_A) mMediaPlayer = MediaPlayer.create(getActivity(), Uri.parse(mLessonData.getDownloadPath_A(getContext())));
        else if(person == PERSON_B) mMediaPlayer = MediaPlayer.create(getActivity(), Uri.parse(mLessonData.getDownloadPath_B(getContext())));
        else return false;

        mSelectedPerson = person;
        setupMediaPlayerListener();

        updateTopUI();

        return true;
    }

    /*********************************/
    private Handler.Callback postCallBack = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            L.d(TAG, "handleMessage() " + msg.arg1);
            if(msg.arg1 == MESSAGE_ARG_INIT) {
                initMediaPlayer(msg.arg2);
            }
            return false;
        }
    };

    @Override
    public void onPauseFragment() {
        L.d(TAG, "onPauseFragment()");
        if(_instance != null) _instance.onPause();
    }

    @Override
    public void onResumeFragment() {

    }


    /*
    //ProgressDialog mProgressDialog = null;
    class DownloadFileAsync extends AsyncTask<Object, Object, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mProgressDialog = ProgressDialog.show(getActivity(), "", "Please wait for few seconds...", true);
        }

        @Override
        protected Object doInBackground(Object... params) {
//            try {
//                for(int i = 1; i < params.length; i += 2) {
//                    AudioDownloader.download((String) params[i], (String) params[i + 1]);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
            return params[0];
        }

        @Override
        protected void onPostExecute(Object ret) {
            //if(mProgressDialog != null) mProgressDialog.dismiss();
            if(ret == null) {//failed
                Toast.makeText(getActivity(), getString(R.string.toast_download_audio_fail), Toast.LENGTH_LONG ).show();
            } else { //successed
                mDownloaded = true;
                postCallBack.handleMessage((Message)ret);
            }
        }
    }
    */
}
