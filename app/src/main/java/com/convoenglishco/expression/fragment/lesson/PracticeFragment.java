package com.convoenglishllc.expression.fragment.lesson;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.animation.ArcTranslateAnimation;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.data.model.LessonDataObject;
import com.convoenglishllc.expression.utils.ImageProcess;
import com.convoenglishllc.expression.utils.L;
import com.convoenglishllc.expression.utils.PositionCalc;
import com.convoenglishllc.expression.utils.TextFormatter;

import at.markushi.ui.CircleButton;

public class PracticeFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener, FragmentLifeCycle {
    private static final int MESSAGE_ARG_PLAY = 1000;
    private static final int MESSAGE_ARG_INIT = 1001;

    private static final int PERSON_A = 0;
    private static final int PERSON_B = 1;

    private static PracticeFragment _instance = null;

    private final String TAG = this.getClass().getSimpleName();

    private View mView = null;
    private TextView uiDialog = null;
    private TextView uiCurrentDuration = null;
    private CircleButton uiPlay = null;
    private SeekBar uiSeekBar = null;

    private TextView uiPartnerTitle = null;
    private ImageView uiFirstImage = null;
    private TextView uiFirstName = null;
    private ImageView uiFirstCheck = null;

    private ImageView uiSecondImage = null;
    private TextView uiSecondName = null;
    private ImageView uiSecondCheck = null;

    private ImageView uiAnimateView = null;

    /////////////////////////////////////////////
    private LessonDataObject mLessonData = null;
    private int mSelectedPerson = -1;
    private MediaPlayer mMediaPlayer;

    private boolean mDownloaded = false;

    private boolean mAnimated = false;

    private int mLessonNo = -1;
    public PracticeFragment() { }


    public static PracticeFragment newInstance(int lesson_no) {
        PracticeFragment n = new PracticeFragment();
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
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            onClickPlayImpl();
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

        ImageProcess.preCacheAssetImagesInGray(getActivity(), mLessonData.getFirstDrawableAssetName());
        ImageProcess.preCacheAssetImagesInGray(getActivity(), mLessonData.getSecondDrawableAssetName());
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*
        if(mLessonNo == -1) {
            mLessonNo = ((LessonActivity)getActivity()).getLessonNo();
            initData();
        }
        */


        mView = inflater.inflate(R.layout.fragment_lesson_practice, container, false);

        uiDialog  = (TextView) mView.findViewById(R.id.ui_dialog);
        uiCurrentDuration  = (TextView) mView.findViewById(R.id.ui_current_duration);
        uiSeekBar = (SeekBar) mView.findViewById(R.id.ui_seek_bar);
        uiPlay = (CircleButton) mView.findViewById(R.id.ui_play);

        uiPartnerTitle = (TextView) mView.findViewById(R.id.ui_partner_title);
        uiFirstName = (TextView) mView.findViewById(R.id.ui_first_name);
        uiFirstImage = (ImageView) mView.findViewById(R.id.ui_first_image);
        uiFirstCheck= (ImageView) mView.findViewById(R.id.ui_first_check);

        uiSecondName = (TextView) mView.findViewById(R.id.ui_second_name);
        uiSecondImage = (ImageView) mView.findViewById(R.id.ui_second_image);
        uiSecondCheck= (ImageView) mView.findViewById(R.id.ui_second_check);

        uiAnimateView = (ImageView) mView.findViewById(R.id.ui_animate);

        uiPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPlay();
            }
        });
        if(mMediaPlayer != null) uiSeekBar.setMax(mMediaPlayer.getDuration());
        uiSeekBar.setOnSeekBarChangeListener(this);

        RelativeLayout uiLeftPanel = (RelativeLayout) mView.findViewById(R.id.ui_first_panel);
        RelativeLayout uiRightPanel = (RelativeLayout) mView.findViewById(R.id.ui_second_panel);

        uiLeftPanel.setOnClickListener(
                new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           if(mSelectedPerson == PERSON_A) {
                               return;
                           } else if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                               L.toast(getActivity(), getString(R.string.toast_select_after_practice_stop));
                               return;
                           }
                           mSelectedPerson = PERSON_A;
                           animateUI();
                           initMediaPlayer(PERSON_A);
                       }});
        uiRightPanel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSelectedPerson == PERSON_B) {
                            return;
                        } else if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                            L.toast(getActivity(), getString(R.string.toast_select_after_practice_stop));
                            return;
                        }
                        mSelectedPerson = PERSON_B;
                        animateUI();
                        initMediaPlayer(PERSON_B);
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
        uiPartnerTitle.setText("Who do you want to be?");
        if(mSelectedPerson == -1) {
            uiFirstCheck.setVisibility(View.INVISIBLE);
            uiSecondCheck.setVisibility(View.INVISIBLE);
            uiDialog.setText(mLessonData.getSpannableDialog());

            ImageProcess.loadAssetImage(getActivity(), mLessonData.getFirstDrawableAssetName(), uiFirstImage);
            ImageProcess.loadAssetImage(getActivity(), mLessonData.getSecondDrawableAssetName(), uiSecondImage);

            uiPlay.setEnabled(true);
            uiSeekBar.setEnabled(false);
        } else if(mSelectedPerson == PERSON_A) {
            uiFirstCheck.setVisibility(View.VISIBLE);
            uiSecondCheck.setVisibility(View.INVISIBLE);
            uiDialog.setText(mLessonData.getSpannableDialogB());

            ImageProcess.loadAssetImage(getActivity(), mLessonData.getFirstDrawableAssetName(), uiFirstImage);
            ImageProcess.loadGrayedAssetImage(getActivity(), mLessonData.getSecondDrawableAssetName(), uiSecondImage);

            uiPlay.setEnabled(true);
            uiSeekBar.setEnabled(true);
        } else if(mSelectedPerson == PERSON_B) {
            uiFirstCheck.setVisibility(View.INVISIBLE);
            uiSecondCheck.setVisibility(View.VISIBLE);
            uiDialog.setText(mLessonData.getSpannableDialogA());

            ImageProcess.loadGrayedAssetImage(getActivity(), mLessonData.getFirstDrawableAssetName(), uiFirstImage);
            ImageProcess.loadAssetImage(getActivity(), mLessonData.getSecondDrawableAssetName(), uiSecondImage);
            uiPlay.setEnabled(true);
            uiSeekBar.setEnabled(true);
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

    private Handler mUpdateProgressHandler = new Handler();

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
        setupMediaPlayerListener();

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
        if(mSelectedPerson == -1) {
            L.toast(getActivity(), getString(R.string.toast_select_conversation_partner));
            return;
        }
        if(mMediaPlayer == null) {
            if(mSelectedPerson == PERSON_A) initMediaPlayer(PERSON_A);
            else if(mSelectedPerson == PERSON_B) initMediaPlayer(PERSON_B);
        }
        onClickPlayImpl();
        /*
        if(mMediaPlayer ==  null) {
            Message msgPlay = new Message();
            msgPlay.arg1 = MESSAGE_ARG_PLAY;
            downloadAllData(msgPlay);
        } else {
            onClickPlayImpl();
        }*/

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
        if (mMediaPlayer == null) return;
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

        L.d(TAG, "onCompletion()");
    }
    /*********************************/

    private void setupMediaPlayerListener() {
        mMediaPlayer.setOnCompletionListener(this);
        uiSeekBar.setMax(mMediaPlayer.getDuration());
    }

    /*
    private void downloadAllData(Message msg) {
        new DownloadFileAsync().execute(msg,
                mLessonData.getUrlChannelA(), mLessonData.getDownloadPath_A(),
                mLessonData.getUrlChannelB(), mLessonData.getDownloadPath_B()
        );
    }
    */

    private void initMediaPlayer(int person) {
        //if(mMediaPlayer != null && mMediaPlayer.isPlaying() && person == mSelectedPerson) return;
        /*
        if(!mDownloaded){ //download now
            Message msgPlay = new Message();
            msgPlay.arg1 = MESSAGE_ARG_INIT;
            msgPlay.arg2 = person;
            downloadAllData(msgPlay);
            return;
        }
        */
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mMediaPlayer = null;

        if(person == PERSON_A) mMediaPlayer = MediaPlayer.create(getActivity(), Uri.parse(mLessonData.getDownloadPath_A(getActivity())));
        else if(person == PERSON_B) mMediaPlayer = MediaPlayer.create(getActivity(), Uri.parse(mLessonData.getDownloadPath_B(getActivity())));
        else return;

        mSelectedPerson = person;
        setupMediaPlayerListener();
        updateProgressBarOnce();


        //onClickPlayImpl();
        /*****************************************************************/
        //if(!mAnimated)
        updateTopUI();
        //else updateTopUI();
    }

    private void animateUI() {
        float startX = 0, startY = 0;
        float dx = (uiPlay.getWidth() - uiFirstCheck.getWidth()) / 2;
        float dy = (uiPlay.getHeight() - uiFirstCheck.getHeight()) / 2;
        if(mSelectedPerson == PERSON_A) {
            startX = PositionCalc.getRelativeLeft(uiFirstCheck, mView);
            startY = PositionCalc.getRelativeTop(uiFirstCheck, mView);
        } else if(mSelectedPerson == PERSON_B) {
            startX = PositionCalc.getRelativeLeft(uiSecondCheck, mView);
            startY = PositionCalc.getRelativeTop(uiSecondCheck, mView);
        }

        float endX = PositionCalc.getRelativeLeft(uiPlay, mView);
        float endY = PositionCalc.getRelativeTop(uiPlay, mView);

        L.e(TAG, String.format("PositionCalc %f %f %f %f %f %f", startX, startY, endX, endY, dx, dy));

        uiAnimateView.setX(startX);
        uiAnimateView.setY(startY);
        uiAnimateView.setVisibility(View.VISIBLE);

        uiAnimateView.bringToFront();

        final Handler redrawHandler = new Handler();

        final Runnable redrawTask = new Runnable() {
            @Override
            public void run() {
                if(!mAnimated) {
                    mView.invalidate();
                    //L.d(TAG, "redraw");
                    redrawHandler.postDelayed(this, 3);
                }
            }
        };

        ArcTranslateAnimation moveAnimation = new ArcTranslateAnimation(0, endX - startX + dx, 0, endY - startY + dy);
        moveAnimation.setInterpolator(new LinearInterpolator());
        moveAnimation.setDuration(1000);

        Animation fadeoutAnimation = new AlphaAnimation(1.f, 0.0f);
        fadeoutAnimation.setStartOffset(1000);
        fadeoutAnimation.setDuration(500);
        fadeoutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mAnimated = false;
                redrawHandler.postDelayed(redrawTask, 3);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                uiAnimateView.setVisibility(View.GONE);
                mAnimated = true;
                redrawHandler.removeCallbacks(redrawTask);
                //updateTopUI();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new LinearInterpolator());

        animSet.addAnimation(moveAnimation);
        animSet.addAnimation(fadeoutAnimation);

        uiAnimateView.startAnimation(animSet);
    }

    /*********************************/
    /*
    private Handler.Callback postCallBack = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            L.d(TAG, "handleMessage() " + msg.arg1);
            if(msg.arg1 == MESSAGE_ARG_PLAY) { //clicked play button
                if(mSelectedPerson == PERSON_A) initMediaPlayer(PERSON_A);
                else if(mSelectedPerson == PERSON_B) initMediaPlayer(PERSON_B);
                onClickPlayImpl();
            } else if(msg.arg1 == MESSAGE_ARG_INIT) {
                initMediaPlayer(msg.arg2);
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
                mDownloaded = true;
                postCallBack.handleMessage((Message)ret);
            }
        }
    }
    */
}
