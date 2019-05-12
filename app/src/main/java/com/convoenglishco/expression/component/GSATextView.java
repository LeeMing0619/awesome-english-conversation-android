package com.convoenglishllc.expression.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class GSATextView extends TextView {

    public GSATextView(Context context) {
        super(context);
    }

    public GSATextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GSATextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void adjustTextSize(int nHeight) {
        float fTextSize = nHeight * 0.75f;
        setTextSize(fTextSize / getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        adjustTextSize(bottom - top);
        super.onLayout(changed, left, top, right, bottom);
    }

}