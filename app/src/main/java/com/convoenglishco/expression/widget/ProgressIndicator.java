package com.convoenglishllc.expression.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.convoenglishllc.expression.R;

/**
 * Created by inkyfox on 14. 11. 26..
 */
public class ProgressIndicator extends View {

    private float mDensity;

    private float mProgress;
    private Paint mPaint = new Paint();
    private int mColor;

    public ProgressIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ProgressIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProgressIndicator(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        mDensity = context.getResources().getDisplayMetrics().density;
        mColor = context.getResources().getColor(R.color.my_action_bar_bg);
        mProgress = 0f;
    }

    public void setProgress(float progress) {
        mProgress = Math.max(0f, Math.min(1.0f, progress));
        invalidate();
    }

    public float getProgress() {
        return mProgress;
    }

    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        if(width == 0 || height == 0) return;
        int barWidth = (int)(width * mProgress);
        mPaint.setAntiAlias(false);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mColor);
        canvas.drawRect(0, 0, barWidth, height, mPaint);
    }


}