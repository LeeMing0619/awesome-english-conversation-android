package com.convoenglishllc.expression.utils;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import com.convoenglishllc.expression.R;

public class ImageProcess {
    public static Drawable convertToGrayScale(Drawable drawable)
    {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

        drawable.setColorFilter(filter);

        return drawable;
    }

    public static void preCacheAssetImage(Context context, String assetName) {
        Picasso.with(context).load("file:///android_asset/" + assetName).fetch();
    }

    public static void preCacheAssetImagesInGray(Context context, String assetName) {
        Picasso.with(context).load("file:///android_asset/" + assetName).transform(new GrayscaleTransformation()).fetch();
    }

    public static void loadAssetImage(final Context context, final String assetName, final ImageView view) {
        /*view.post(new Runnable() {
            @Override
            public void run() {
                int width = view.getWidth();
                int height = view.getHeight();

                if(assetName.startsWith("images")) height = width;
                if(width <= 0 || height <= 0) return;
                Picasso.with(context).load("file:///android_asset/" + assetName).placeholder(R.drawable.loading).resize(width, height).into(view);
            }
        });*/
        Picasso.with(context).load("file:///android_asset/" + assetName).placeholder(R.drawable.loading).into(view);
    }

    public static void loadGrayedAssetImage(Context context, String assetName, ImageView view) {
        Picasso.with(context).load("file:///android_asset/" + assetName).placeholder(R.drawable.loading).transform(new GrayscaleTransformation()).into(view);
    }
}
