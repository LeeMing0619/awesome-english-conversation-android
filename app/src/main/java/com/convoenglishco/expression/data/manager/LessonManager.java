package com.convoenglishllc.expression.data.manager;

import android.content.Context;

import com.convoenglishllc.expression.data.model.LessonDataObject;
import com.convoenglishllc.expression.data.model.QuizDataObject;
import com.convoenglishllc.expression.data.model.VocabDataObject;
import com.convoenglishllc.expression.data.model.CategoryPurchaseObject;
import com.convoenglishllc.expression.utils.L;

import java.util.ArrayList;

public class LessonManager {
    public static LessonDataObject[] getDataByCategory(Context context, String category) {
        ArrayList<LessonDataObject> list = DBHelper.getInstance(context).getDataByCategory(category);
        return list.toArray(new LessonDataObject[list.size()]);
    }

    public static LessonDataObject[] getAllLessons(Context context){
        ArrayList<LessonDataObject> list = DBHelper.getInstance(context).getAllLessons();
        return list.toArray(new LessonDataObject[list.size()]);
    }

    public static LessonDataObject[] getFirstCategoryLessons(Context context){
        ArrayList<LessonDataObject> list = DBHelper.getInstance(context).getFirstCategoryLessons();
        return list.toArray(new LessonDataObject[list.size()]);
    }

    public static LessonDataObject[] getSecondCategoryLessons(Context context){
        ArrayList<LessonDataObject> list = DBHelper.getInstance(context).getSecondCategoryLessons();
        return list.toArray(new LessonDataObject[list.size()]);
    }

    public static LessonDataObject[] getThirdCategoryLessons(Context context){
        ArrayList<LessonDataObject> list = DBHelper.getInstance(context).getThirdCategoryLessons();
        return list.toArray(new LessonDataObject[list.size()]);
    }

    public static int getAllLessonsCount(Context context){
        int nCount = DBHelper.getInstance(context).getAllLessonsCount();
        return nCount;
    }

    public static String[] getSubCategories(Context context, String category) {
        ArrayList<String> list = DBHelper.getInstance(context).getSubCategories(category);
        return list.toArray(new String[list.size()]);
    }

    public static String[] getCategories(Context context) {
        ArrayList<String> mCategories = DBHelper.getInstance(context).getCategories();
        return mCategories.toArray(new String[mCategories.size()]);
    }

    public static int getCategoryCount(Context context) {
        int nCount = DBHelper.getInstance(context).getCategoryCount();
        return nCount;
    }

    public static String getLessonImageByNo(Context context, int no) {
        LessonDataObject lesson = DBHelper.getInstance(context).getLessonImageByNo(no);
        return lesson.getImage();
    }

    public static LessonDataObject getLessonByNo(Context context, int no) {
        LessonDataObject lesson = DBHelper.getInstance(context).getLessonByNo(no);
        return lesson;
    }

    public static QuizDataObject getQuizByNo(Context context, int no) {
        QuizDataObject quiz = DBHelper.getInstance(context).getQuizByNo(no);
        return quiz;
    }

    public static ArrayList<VocabDataObject> getVocabByNo(Context context, int no) {
        ArrayList<VocabDataObject> list = DBHelper.getInstance(context).getVocabByNo(no);
        return list;
    }

    public static ArrayList<CategoryPurchaseObject> getCategoryPurchaseStatus(Context context){
        ArrayList<CategoryPurchaseObject> purchase = null;
        try{
            purchase = DBHelper.getInstance(context).getPurchaseStatus();
        }catch(Exception e){
            L.d("Purchase Exception:", e);
        }

        return purchase;
    }

    public static void updatePurchaseStatus(Context context){
        DBHelper.getInstance(context).updatePurchaseStatus();
    }

    public static void updateOfflinePurchaseStatus(Context context){
        DBHelper.getInstance(context).updateOfflineStatus();
    }

    public static boolean isAllCategoryPurchased(Context context){
        ArrayList<CategoryPurchaseObject> purchase = null;
        try{
            purchase = DBHelper.getInstance(context).getPurchaseStatus();
        }catch(Exception e){
            L.d("OfflinePurchase Exception:", e);
        }
        boolean bFlag = false;
        int size = purchase.size();
        int index = 0;

        for ( index = 5 ; index < size; index++){
            if (purchase.get(index).getPurchaseStatus() == 1){
                continue;
            }else{
                bFlag = false;
                break;
            }
        }
        if (index == size){
            bFlag = true;
        }
        return bFlag;
    }

    public static boolean isOfflinePurchased(Context context){
        ArrayList<CategoryPurchaseObject> purchase = null;
        try{
            purchase = DBHelper.getInstance(context).getPurchaseStatus();
        }catch(Exception e){
            L.d("OfflinePurchase Exception:", e);
        }
        boolean bFlag = false;
        int size = purchase.size();
        int index = 0;

        for ( index = 5 ; index < size; index++){
            if (purchase.get(index).getOfflinePurchase() == 1){
                continue;
            }else{
                bFlag = false;
                break;
            }
        }
        if (index == size){
            bFlag = true;
        }
        return bFlag;
    }

    public static String getCategoryAssetName(int categoryNo) {
        String[] categoryImages = new String[] {
                "category_image1.jpg",
                "category_image2.jpg",
                "category_image3.jpg",
                "category_image4.jpg",
                "category_image5.jpg",
                "category_image6.jpg",
                "category_image7.jpg",
                "category_image8.jpg",
                "category_image9.jpg",
                "category_image10.jpg",
                "category_image11.jpg",
                "category_image12.jpg",
                "category_image13.jpg",
                "category_image14.jpg",
                "category_image15.jpg"
        };
        return "images/" + categoryImages[categoryNo];
    }

    public static String[] getCategoryAssetNames() {
        String[] categoryImages = new String[] {
                "category_image1.jpg",
                "category_image2.jpg",
                "category_image3.jpg",
                "category_image4.jpg",
                "category_image5.jpg",
                "category_image6.jpg",
                "category_image7.jpg",
                "category_image8.jpg",
                "category_image9.jpg",
                "category_image10.jpg",
                "category_image11.jpg",
                "category_image12.jpg",
                "category_image13.jpg",
                "category_image14.jpg",
                "category_image15.jpg"
        };
        ArrayList<String> retList = new ArrayList<>();
        for(String url : categoryImages) retList.add("images/" + url);
        return retList.toArray(new String[retList.size()]);
    }
}
