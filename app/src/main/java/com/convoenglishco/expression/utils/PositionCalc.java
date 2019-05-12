package com.convoenglishllc.expression.utils;

import android.view.View;

public class PositionCalc {
    public static int getRelativeLeft(View myView, View parentView) {
        if (myView.getParent() == parentView)
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent(), parentView);
    }

    public static int getRelativeTop(View myView, View parentView) {
        if (myView.getParent() == parentView)
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent(), parentView);
    }
}
