package com.convoenglishllc.expression.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import com.squareup.picasso.Transformation;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;

public class GrayscaleTransformation implements Transformation {
    @Override public Bitmap transform(Bitmap source) {
        Bitmap result = createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);

        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setColorFilter(filter);

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(source, 0, 0, paint);

        source.recycle();
        return result;
    }

    @Override public String key() {
        return "grayscaleTransformation()";
    }
}
