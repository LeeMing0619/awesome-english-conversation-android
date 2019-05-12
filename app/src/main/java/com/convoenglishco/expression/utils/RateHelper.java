package com.convoenglishllc.expression.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class RateHelper {
    private final String TAG = this.getClass().getSimpleName();

    private static RateHelper mInstance = null;
    private static String PREF_KEY_LAST_DATE = "LastShow";
    private static String PREF_KEY_VISIT_COUNT = "Counter";
    private static String PREF_KEY_LAST_ANSWER = "Answer";

    public static int ANSWER_LATER = 0;
    public static int ANSWER_NO = 1;
    public static int ANSWER_YES = 2;

    public static RateHelper initInstance(Context context) {
        if(mInstance == null) {
            mInstance = new RateHelper(context);
        }
        return mInstance;
    }

    public static RateHelper getInstance() {
        return mInstance;
    }

    private Context mContext = null;
    public RateHelper(Context context) {
        mContext = context;
    }

    public int getVisitCount() {
        return mContext.getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE).getInt(PREF_KEY_VISIT_COUNT, 0);
    }

    public void setVisitCount(int count) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE).edit();
        editor.putInt(PREF_KEY_VISIT_COUNT, count);
        editor.apply();
    }

    public int getLastAnswer() {
        return mContext.getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE).getInt(PREF_KEY_LAST_ANSWER, ANSWER_LATER);
    }

    public void setLastAnswer(int answer) {
        updateLastDate();
        SharedPreferences.Editor editor = mContext.getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE).edit();
        editor.putInt(PREF_KEY_LAST_ANSWER, answer);
        editor.apply();
    }

    public long getLastDate() {
        return mContext.getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE).getLong(PREF_KEY_LAST_DATE, System.currentTimeMillis());
    }

    public void updateLastDate() {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE).edit();
        editor.putLong(PREF_KEY_LAST_DATE, System.currentTimeMillis());
        editor.apply();
    }

    public synchronized boolean addVisitCount() {

        if(getLastAnswer() == ANSWER_YES) { //Rated
            return false;
        } else if(getLastAnswer() == ANSWER_LATER) {
            int currentCount = getVisitCount() + 1;
            if (currentCount > 10) {
                setVisitCount(0);
                return true;
            }
            setVisitCount(currentCount);

            long diff = System.currentTimeMillis() - getLastDate();
            if (diff > 24 * 3600 * 1000) {
                updateLastDate();
                return true;
            }
        } else if(getLastAnswer() == ANSWER_NO) {
            long diff = System.currentTimeMillis() - getLastDate();
            if (diff > 3 * 24 * 3600 * 1000) {
                updateLastDate();
                return true;
            }
        }

        return false;
    }


}
