package com.convoenglishllc.expression.data.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.convoenglishllc.expression.utils.GlobalConstants;
import com.convoenglishllc.expression.utils.L;

import java.util.ArrayList;

public class BookmarkManager {
    private static ArrayList<Integer> initData(Context context) {
        ArrayList<Integer>
                mLessonNoList = new ArrayList<>();
        SharedPreferences pref = context.getSharedPreferences("BookmarkManager", Context.MODE_PRIVATE);
        String savedData = pref.getString(GlobalConstants.PREF_KEY_BOOKMARK, "");
        if(!savedData.equals("")) {
            String[] allIds = savedData.split(",");
            for(String id:allIds) {
                mLessonNoList.add(Integer.valueOf(id));
            }
        }
        L.d("BookmarkManager", "initData() count=" + mLessonNoList.size());
        return mLessonNoList;
    }

    public static int[] getBookedIds(Context context) {
        ArrayList<Integer> mLessonNoList = initData(context);
        int[] ret = new int[mLessonNoList.size()];
        int i=0;
        for(Integer _id : mLessonNoList) {
            ret[i++] = _id;
        }
        return ret;
    }

    public static boolean isBookmarked(Context context, int id) {
        ArrayList<Integer> mLessonNoList = initData(context);
        for(Integer _id: mLessonNoList) {
            if(_id == id) return true;
        }
        return false;
    }

    public static void removeId(Context context, int id) {
        ArrayList<Integer> mLessonNoList = initData(context);
        for(Integer _id : mLessonNoList) {
            if(_id == id) {
                mLessonNoList.remove(_id);
                break;
            }
        }
        String s = getArchivedString(mLessonNoList);
        SharedPreferences pref = context.getSharedPreferences("BookmarkManager", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(GlobalConstants.PREF_KEY_BOOKMARK, s);
        editor.apply();
    }

    public static void addId(Context context, int id) {
        if(isBookmarked(context, id)) return;
        ArrayList<Integer> mLessonNoList = initData(context);
        mLessonNoList.add(id);
        String s = getArchivedString(mLessonNoList);
        SharedPreferences pref = context.getSharedPreferences("BookmarkManager", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(GlobalConstants.PREF_KEY_BOOKMARK, s);
        editor.apply();
    }

    private static String getArchivedString(ArrayList<Integer> arr) {
        String s = "";
        for(int i=0; i<arr.size(); i++) {
            s += arr.get(i).toString();
            if(i < arr.size() -1) s += ",";
        }
        return s;
    }

}
