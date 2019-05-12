package com.convoenglishllc.expression.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class ImageSizeHelper {
    public static int getAutoScaledSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int orientation = (size.x < size.y) ? 1 : 2; //orientation 1: portrait, 2 : landscape
        int baseWidth;
        if(orientation == 1) baseWidth = size.x * 3 / 10;
        else baseWidth = size.y * 3 / 20;

        return baseWidth;
    }

}
