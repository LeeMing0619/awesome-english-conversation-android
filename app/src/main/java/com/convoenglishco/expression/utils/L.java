package com.convoenglishllc.expression.utils;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.convoenglishllc.expression.BuildConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.activity.LessonActivity;

public class L {

    public static void d(String tag, Object msg) {
        if (BuildConfig.DEBUG) {
            for (String s : getMessage(msg)) {
                Log.d(tag, s);
            }
        }
    }

    public static void d(String tag, Object msg, Throwable t) {
        if (BuildConfig.DEBUG) {
            for (String s : getMessage(msg)) {
                Log.d(tag, s, t);
            }
        }
    }

    private static List<String> getMessage(Object msg) {
        if (msg == null) {
            return Collections.singletonList("null");
        } else if (msg.equals("")) {
            return Collections.singletonList("empty");
        } else {
            String fullMsg = String.valueOf(msg);
            int cutAt = 4000;
            List<String> split = new ArrayList<String>(fullMsg.length() / cutAt);
            for (int i = 0; i < fullMsg.length(); i += cutAt) {
                split.add(fullMsg.substring(i, Math.min(fullMsg.length(), i + cutAt)));
            }
            return split;
        }
    }

    public static void e(String tag, Object msg) {
        if (BuildConfig.DEBUG) {
            for (String s : getMessage(msg)) {
                Log.e(tag, s);
            }
        }
    }

    public static void e(String tag, Object msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            for (String s : getMessage(msg)) {
                Log.e(tag, s, tr);
            }
        }
    }

    public static void i(String tag, Object msg) {
        if (BuildConfig.DEBUG) {
            for (String s : getMessage(msg)) {
                Log.i(tag, s);
            }
        }
    }

    public static void v(String tag, Object msg) {
        if (BuildConfig.DEBUG) {
            for (String s : getMessage(msg)) {
                Log.v(tag, s);
            }
        }
    }

    public static void toast(Context context, String str) {
        Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void toastBookmark(Context context) {
        // Inflate the Layout
        LayoutInflater inflater = LessonActivity.gContext.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_bookmark,
                (ViewGroup) LessonActivity.gContext.findViewById(R.id.custom_toast_layout_id));

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
