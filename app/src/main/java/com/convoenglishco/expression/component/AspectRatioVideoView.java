package com.convoenglishllc.expression.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class AspectRatioVideoView extends VideoView {
    private int mVideoWidth;
    private int mVideoHeight;

    public AspectRatioVideoView(Context context) {
        this(context, null);
    }

    public AspectRatioVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mVideoWidth <= 0 || mVideoHeight <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        float heightRatio = (float) mVideoHeight / (float) getHeight();
        float widthRatio = (float) mVideoWidth / (float) getWidth();

        int scaledHeight;
        int scaledWidth;

        if (heightRatio > widthRatio) {
            scaledHeight = (int) Math.ceil((float) mVideoHeight
                    / heightRatio);
            scaledWidth = (int) Math.ceil((float) mVideoWidth
                    / heightRatio);
        } else {
            scaledHeight = (int) Math.ceil((float) mVideoHeight
                    / widthRatio);
            scaledWidth = (int) Math.ceil((float) mVideoWidth
                    / widthRatio);
        }

        setMeasuredDimension(scaledWidth, scaledHeight);
    }

    /**
     * Source code originally from:
     * http://clseto.mysinablog.com/index.php?op=ViewArticle&articleId=2992625
     *
     * @param videoWidth
     * @param videoHeight
     */
    public void setVideoSize(int videoWidth, int videoHeight) {
        // Set the new video size
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;

        /**
         * If this isn't set the video is stretched across the
         * SurfaceHolders display surface (i.e. the SurfaceHolder
         * as the same size and the video is drawn to fit this
         * display area). We want the size to be the video size
         * and allow the aspectratio to handle how the surface is shown
         */
        getHolder().setFixedSize(videoWidth, videoHeight);

        requestLayout();
        invalidate();
    }
}
