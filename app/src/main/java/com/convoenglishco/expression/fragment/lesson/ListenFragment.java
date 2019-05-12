package com.convoenglishllc.expression.fragment.lesson;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.data.model.LessonDataObject;
import com.convoenglishllc.expression.utils.ImageProcess;
import com.convoenglishllc.expression.utils.L;
import com.convoenglishllc.expression.utils.TextFormatter;

import at.markushi.ui.CircleButton;

public class ListenFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener, FragmentLifeCycle {
    private final String TAG = this.getClass().getSimpleName();

    private int MESSAGE_ARG_PLAY = 1000;

    private static ListenFragment _instance = null;

    //UI Items
    private View mView = null;
    private ImageView uiImage = null;
    private TextView uiTitle = null;
    private TextView uiCategory = null;
    private TextView uiDialog = null;
    private TextView uiCurrentDuration = null;
    private CircleButton uiPlay = null;
    private SeekBar uiSeekBar = null;

    //Controls
    private LessonDataObject mLessonData = null;
    private MediaPlayer mMediaPlayer = null;
    private Boolean mIsPlaying = false;

    private int mLessonNo = -1;

    public ListenFragment() {

    }

    public static ListenFragment newInstance(int lessonNo) {
        ListenFragment n = new ListenFragment();
        n.mLessonNo = lessonNo;
        return n;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("lesson_no", mLessonNo);
        /*if(mMediaPlayer != null && mMediaPlayer.isPlaying()) outState.putBoolean("is_playing", true);
        else outState.putBoolean("is_playing", false);*/
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            onClickPlayImpl();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey("lesson_no")) {
            mLessonNo = savedInstanceState.getInt("lesson_no");
        }
        mLessonData = LessonManager.getLessonByNo(getContext(),mLessonNo);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
/*
        if(mLessonNo == -1) {
            mLessonNo = ((LessonActivity)getActivity()).getLessonNo();
            initData();
        }
*/

        mView = inflater.inflate(R.layout.fragment_lesson_listen, container, false);
        uiImage = (ImageView) mView.findViewById(R.id.ui_image);
        uiTitle = (TextView) mView.findViewById(R.id.ui_title);
        uiCategory = (TextView) mView.findViewById(R.id.ui_category);
        uiDialog  = (TextView) mView.findViewById(R.id.ui_dialog);
        uiCurrentDuration  = (TextView) mView.findViewById(R.id.ui_current_duration);
        uiSeekBar = (SeekBar) mView.findViewById(R.id.ui_seek_bar);
        uiPlay = (CircleButton) mView.findViewById(R.id.ui_play);

        updateLessonData();

        uiPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPlay();
            }
        });
        if(mMediaPlayer != null) {
            mMediaPlayer.setOnCompletionListener(this);
            uiSeekBar.setMax(mMediaPlayer.getDuration());
            /*if(mIsPlaying) {
                onClickPlay();
            }*/
        }
        uiSeekBar.setOnSeekBarChangeListener(this);
        _instance = this;
        return mView;
    }

    @Override
    public void onDestroy() {
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    /*********************************/
    private Handler mUpdateProgressHandler = new Handler();

    private void updateLessonData() {
        if(mLessonData == null) return;
        ImageProcess.loadAssetImage(getActivity(), mLessonData.getDrawableAssetName(), uiImage);
        uiTitle.setText(mLessonData.getTitle());
        uiCategory.setText(mLessonData.getCategory());
        uiDialog.setText(mLessonData.getSpannableDialog());
    }

    public void updateProgressBar() {
        mUpdateProgressHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private void updateProgressBarOnce() {
        if(mMediaPlayer == null) return;

        if(mMediaPlayer.isPlaying()) {
            uiPlay.setImageResource(R.drawable.ic_action_audio_pause);
        } else {
            uiPlay.setImageResource(R.drawable.ic_action_audio_play);
        }
        uiCurrentDuration.setText(TextFormatter.getPlayTime(mMediaPlayer.getCurrentPosition()));
        uiSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            updateProgressBarOnce();
            mUpdateProgressHandler.postDelayed(this, 100);
        }
    };
    /*********************************/
    private void onClickPlayImpl() {
        if(mMediaPlayer == null) return;
        mMediaPlayer.setOnCompletionListener(this);
        uiSeekBar.setMax(mMediaPlayer.getDuration());

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mUpdateProgressHandler.removeCallbacks(mUpdateTimeTask);
            updateProgressBarOnce();
        }
        else {
            mMediaPlayer.start();
            updateProgressBar();
        }
    }

    private void onClickPlay() {
        L.d(TAG, "onClickPlay()");
        /*
        if(mMediaPlayer ==  null) {
            Message msgPlay = new Message();
            msgPlay.arg1 = MESSAGE_ARG_PLAY;
            new DownloadFileAsync().execute(msgPlay, mLessonData.getUrlChannelAll(), mLessonData.getDownloadPath_All());
        } else {
            onClickPlayImpl();
        }*/
        if(mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(getActivity(), Uri.parse(mLessonData.getDownloadPath_All(getActivity())));
        }
        onClickPlayImpl();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {}

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if(mMediaPlayer == null) return;
        mUpdateProgressHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(mMediaPlayer == null) return;
        mUpdateProgressHandler.removeCallbacks(mUpdateTimeTask, 100);
        mMediaPlayer.seekTo(seekBar.getProgress());
        if(mMediaPlayer.isPlaying()) updateProgressBar();
        else updateProgressBarOnce();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mp != mMediaPlayer) return;
        mMediaPlayer.seekTo(0);
        mUpdateProgressHandler.removeCallbacks(mUpdateTimeTask);
        updateProgressBarOnce();
    }
    /*********************************/
    /*
    private Callback postCallBack = new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.arg1 == MESSAGE_ARG_PLAY) { //clicked play button
                mMediaPlayer = MediaPlayer.create(getActivity(), Uri.parse(mLessonData.getDownloadPath_All()));
                onClickPlayImpl();
            }
            return false;
        }
    };
    */

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
            //mProgressDialog = ProgressDialog.show(getActivity(), "","Please wait for few seconds...", true);
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
                postCallBack.handleMessage((Message)ret);
            }
        }
    }
    */
}
