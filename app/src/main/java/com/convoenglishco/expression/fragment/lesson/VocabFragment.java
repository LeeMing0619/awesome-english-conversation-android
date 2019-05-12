package com.convoenglishllc.expression.fragment.lesson;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.data.model.LessonDataObject;
import com.convoenglishllc.expression.data.model.VocabDataObject;
import com.convoenglishllc.expression.utils.ImageProcess;
import com.convoenglishllc.expression.utils.L;

import java.util.ArrayList;


public class VocabFragment extends Fragment implements FragmentLifeCycle {
    private final String TAG = this.getClass().getSimpleName();
    private static VocabFragment _instance = null;

    //UI Items
    private View mView = null;
    private ImageView uiImage = null;
    private TextView uiTitle = null;
    private TextView uiCategory = null;
    private TextView uiTitle1 = null;
    private TextView uiMeaning1 = null;
    private TextView uiExample1 = null;
    private TextView uiExample2 = null;

    private TextView uiTitle2 = null;
    private TextView uiMeaning2 = null;
    private TextView uiExample11 = null;
    private TextView uiExample22 = null;

    private TextView uiTitle3 = null;
    private TextView uiMeaning3 = null;
    private TextView uiExample111 = null;
    private TextView uiExample222 = null;


    //Controls
    private LessonDataObject mLessonData = null;
    private ArrayList<VocabDataObject> mVocabData = null;

    private int mLessonNo = -1;

    public VocabFragment() {

    }

    public static VocabFragment newInstance(int lessonNo) {
        VocabFragment n = new VocabFragment();
        n.mLessonNo = lessonNo;
        return n;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("lesson_no", mLessonNo);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey("lesson_no")) {
            mLessonNo = savedInstanceState.getInt("lesson_no");
        }
        mLessonData = LessonManager.getLessonByNo(getContext(), mLessonNo);
        mVocabData = LessonManager.getVocabByNo(getContext(), mLessonNo);
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

        mView = inflater.inflate(R.layout.fragment_lesson_vocab, container, false);
        uiImage = (ImageView) mView.findViewById(R.id.ui_image);
        uiTitle = (TextView) mView.findViewById(R.id.ui_title);
        uiCategory = (TextView) mView.findViewById(R.id.ui_category);
        uiTitle1 = (TextView) mView.findViewById(R.id.tv_title1);
        uiMeaning1 = (TextView) mView.findViewById(R.id.tv_meaning1);
        uiExample1 = (TextView) mView.findViewById(R.id.tv_example1);
        uiExample2 = (TextView) mView.findViewById(R.id.tv_example2);

        uiTitle2 = (TextView) mView.findViewById(R.id.tv_title2);
        uiMeaning2 = (TextView) mView.findViewById(R.id.tv_meaning2);
        uiExample11 = (TextView) mView.findViewById(R.id.tv_example11);
        uiExample22 = (TextView) mView.findViewById(R.id.tv_example22);

        uiTitle3 = (TextView) mView.findViewById(R.id.tv_title3);
        uiMeaning3 = (TextView) mView.findViewById(R.id.tv_meaning3);
        uiExample111 = (TextView) mView.findViewById(R.id.tv_example111);
        uiExample222 = (TextView) mView.findViewById(R.id.tv_example222);

        _instance = this;
        showLessonData();
        showVocabData();
        return mView;
    }

    private void showVocabData(){
        try{
            uiTitle1.setText("1. "+Html.fromHtml(mVocabData.get(0).getVocabWordPhrase()));
            uiTitle2.setText("2. "+Html.fromHtml(mVocabData.get(1).getVocabWordPhrase()));
            uiTitle3.setText("3. "+Html.fromHtml(mVocabData.get(2).getVocabWordPhrase()));

            uiMeaning1.setText(Html.fromHtml("<b>Meaning: </b>"+mVocabData.get(0).getMeaning()));
            uiMeaning2.setText(Html.fromHtml("<b>Meaning: </b>"+mVocabData.get(1).getMeaning()));
            uiMeaning3.setText(Html.fromHtml("<b>Meaning: </b>"+mVocabData.get(2).getMeaning()));

            uiExample1.setText(Html.fromHtml("i. "+mVocabData.get(0).getExample1()));
            uiExample11.setText(Html.fromHtml("i. "+mVocabData.get(1).getExample1()));
            uiExample111.setText(Html.fromHtml("i. "+mVocabData.get(2).getExample1()));

            uiExample2.setText(Html.fromHtml("ii. "+mVocabData.get(0).getExample2()));
            uiExample22.setText(Html.fromHtml("ii. "+mVocabData.get(1).getExample2()));
            uiExample222.setText(Html.fromHtml("ii. "+mVocabData.get(2).getExample2()));

        }catch (Exception e){

        }
    }

    private void showLessonData(){
        if(mLessonData == null) return;
        ImageProcess.loadAssetImage(getActivity(), mLessonData.getDrawableAssetName(), uiImage);
        uiTitle.setText(mLessonData.getTitle());
        uiCategory.setText(mLessonData.getCategory());
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
