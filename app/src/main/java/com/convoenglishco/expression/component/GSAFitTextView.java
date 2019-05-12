package com.convoenglishllc.expression.component;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

public class GSAFitTextView extends TextView {

    public GSAFitTextView(Context context) {
        super(context);
    }

    public GSAFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GSAFitTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void adjustTextSize(int nWidth, int nHeight) {
        float fTextSize = nHeight * 0.9f;

        String szText = getText().toString();
        Rect rcText = new Rect();

        Paint paint = new Paint();
        paint.setTextSize(fTextSize);
        paint.setTypeface(getTypeface());
        paint.getTextBounds(szText, 0, szText.length(), rcText);

        if (rcText.width() > nWidth * 0.9f)
            fTextSize = fTextSize * nWidth * 0.9f / rcText.width();

        setTextSize(fTextSize / getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        adjustTextSize(right - left, bottom - top);
        super.onLayout(changed, left, top, right, bottom);
    }
}