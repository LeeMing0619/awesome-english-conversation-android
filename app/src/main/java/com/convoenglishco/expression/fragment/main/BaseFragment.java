package com.convoenglishllc.expression.fragment.main;

import android.os.Bundle;

import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.convoenglishllc.expression.activity.MainActivity;

public class BaseFragment extends StatedFragment{
    public String mTitle = null;
    public String mSubTitle = null;
    public String  getTitle() {
        return mTitle;
    }
    public String  getSubTitle() { return mSubTitle; }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setTitleWithSubtitle(mTitle, mSubTitle);
    }
}
