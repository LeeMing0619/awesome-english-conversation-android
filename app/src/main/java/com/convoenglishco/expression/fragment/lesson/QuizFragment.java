package com.convoenglishllc.expression.fragment.lesson;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.activity.LessonActivity;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.data.model.LessonDataObject;
import com.convoenglishllc.expression.data.model.QuizDataObject;
import com.convoenglishllc.expression.utils.ImageProcess;
import com.convoenglishllc.expression.utils.L;


public class QuizFragment extends Fragment implements FragmentLifeCycle {
    private final String TAG = this.getClass().getSimpleName();

    private static QuizFragment _instance = null;

    private View mView = null;

    private ImageView uiImage = null;
    private TextView uiTitle = null;
    private TextView uiCategory = null;
    private TextView uiQuestion = null;

    private Button uiNext = null;
    private Button uiRetake = null;

    private TextView uiCheck = null;

    private TextView uiResult = null;
    private TextView uiStep= null;

    private RadioGroup uiAnswerGroup = null;
    private int[] answerIds = new int[] {
            R.id.quiz_answerA,
            R.id.quiz_answerB,
            R.id.quiz_answerC,
            R.id.quiz_answerD
    };

    private LessonDataObject mLessonData = null;
    private QuizDataObject mQuizData = null;

    private int mQuestionNo = 0;
    private int mCorrectedCount = 0;

    private int mLastChecked = -1;
    private int mLastCorrect = -1;

    private boolean mClicked = false;

    private int mLessonNo = -1;

    public QuizFragment() {

    }

    public static QuizFragment newInstance(int lesson_no) {
        QuizFragment n = new QuizFragment();
        n.mLessonNo = lesson_no;
        return n;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("lesson_no", mLessonNo);
        outState.putInt("question_no", mQuestionNo);
        outState.putInt("correct_count", mCorrectedCount);
        outState.putBoolean("clicked", mClicked);
        outState.putInt("last_correct", mLastCorrect);
        outState.putInt("last_checked", mLastChecked);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey("lesson_no")) {
            mLessonNo = savedInstanceState.getInt("lesson_no");
            mQuestionNo = savedInstanceState.getInt("question_no");
            mCorrectedCount = savedInstanceState.getInt("correct_count");
            mClicked = savedInstanceState.getBoolean("clicked");

            mLastCorrect = savedInstanceState.getInt("last_correct");
            mLastChecked = savedInstanceState.getInt("last_checked");
        }
        mLessonData = LessonManager.getLessonByNo(getContext(), mLessonNo);
        mQuizData = LessonManager.getQuizByNo(getContext(), mLessonNo);
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

        mView = inflater.inflate(R.layout.fragment_lesson_quiz, container, false);
        uiImage = (ImageView) mView.findViewById(R.id.ui_image);
        uiTitle = (TextView) mView.findViewById(R.id.ui_title);
        uiCategory = (TextView) mView.findViewById(R.id.ui_category);
        uiQuestion= (TextView) mView.findViewById(R.id.quiz_question);
        uiResult = (TextView) mView.findViewById(R.id.ui_result);
        uiStep = (TextView) mView.findViewById(R.id.ui_step);
        uiCheck = (TextView) mView.findViewById(R.id.ui_check);
        uiAnswerGroup = (RadioGroup) mView.findViewById(R.id.quiz_answers);
        uiNext = (Button) mView.findViewById(R.id.quiz_next);
        uiRetake = (Button) mView.findViewById(R.id.quiz_retake);


        uiCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uiAnswerGroup.getCheckedRadioButtonId() == -1 || mClicked) return;
                mLastCorrect = mQuizData.getAnswer(mQuestionNo);
                mLastChecked = -1;
                int checkedId = uiAnswerGroup.getCheckedRadioButtonId();
                for (int i = 0; i < 4; i++) {
                    if (checkedId == answerIds[i]) {
                        mLastChecked = i;
                        break;
                    }
                }

                if (mLastChecked == mLastCorrect) mCorrectedCount++;
                updateQuestion();
                mClicked = true;
            }
        });

        uiRetake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuestionNo = 0;
                mCorrectedCount = 0;
                mLastCorrect = -1;
                mLastChecked = -1;
                updateQuestion();
            }
        });

        uiNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastChecked == -1) {
                    L.toast(getActivity(), "Please click check before going to the next question.");
                    return;
                }
                mQuestionNo++;
                if (mQuestionNo == 3) {
                    mQuestionNo = 0;
                    mCorrectedCount = 0;
                    mLastCorrect = -1;
                    mLastChecked = -1;
                    LessonActivity.gContext.gotoTab(3);
                }
                mLastCorrect = -1;
                mLastChecked = -1;
                updateQuestion();
            }
        });

        updateLessonData();
        updateQuestion();

        _instance = this;
        return mView;
    }

    private void updateLessonData() {
        if(mLessonData == null) return;
        ImageProcess.loadAssetImage(getActivity(), mLessonData.getDrawableAssetName(), uiImage);
        uiTitle.setText(mLessonData.getTitle());
        uiCategory.setText(mLessonData.getCategory());
    }

    private void updateQuestion() {
        if(mQuizData == null) return;

        uiStep.setText(String.format("%d/%d", mCorrectedCount, mQuestionNo + 1));
        mClicked = false;
        uiResult.setText("");

        if(mQuestionNo == 2) {
            if(mLastChecked != -1) {
                uiRetake.setVisibility(View.VISIBLE);
                uiRetake.setEnabled(true);
            } else {
                uiRetake.setVisibility(View.INVISIBLE);
                uiRetake.setEnabled(false);
            }
            uiNext.setText("Go to Practice");
        }
        else {
            uiRetake.setVisibility(View.INVISIBLE);
            uiRetake.setEnabled(false);
            uiNext.setText("Next");
        }

        uiQuestion.setText(mQuizData.getQuestion(mQuestionNo));
        for(int i=0; i<4; i++) {
            RadioButton b = (RadioButton)mView.findViewById(answerIds[i]);
            b.setText(mQuizData.getChoices(mQuestionNo)[i]);
            b.setTextColor(Color.rgb(0,0,0));
        }

        //UPDATE RADIO GROUP

        int correct = -1;
        if(mLastChecked != -1) {
            if(mLastChecked == mLastCorrect) correct = 1;
            else correct = 0;
        }
        if(correct == 0) {
            uiResult.setText("Incorrect");
            uiResult.setTextColor(Color.rgb(216, 0, 0));
            ((RadioButton) mView.findViewById(answerIds[mLastChecked])).setTextColor(Color.rgb(236, 9, 15));
            ((RadioButton) mView.findViewById(answerIds[mLastCorrect])).setTextColor(Color.rgb(0x09, 0xcd, 0x00));
        } else if(correct == 1) {
            uiResult.setText("Correct");
            uiResult.setTextColor(Color.rgb(79, 223, 79));
            ((RadioButton) mView.findViewById(answerIds[mLastCorrect])).setTextColor(Color.rgb(0x09, 0xcd, 0x00));
        } else {
            uiResult.setText("");
            uiAnswerGroup.clearCheck();
        }
    }

    @Override
    public void onPauseFragment() {
        L.d(TAG, "onPauseFragment()");
        if(_instance != null) _instance.onPause();
    }

    @Override
    public void onResumeFragment() {

    }
}
