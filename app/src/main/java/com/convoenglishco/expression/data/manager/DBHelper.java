package com.convoenglishllc.expression.data.manager;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.convoenglishllc.expression.data.model.CategoryPurchaseObject;
import com.convoenglishllc.expression.data.model.LessonDataObject;
import com.convoenglishllc.expression.data.model.QuizDataObject;
import com.convoenglishllc.expression.data.model.VocabDataObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper{
    private static final String TAG = "SQLiteDatabaseAdapter";

    private static final String DATABASE_ASSET_FILE = "data.jpg";
    private static final String DB_NAME = "data.sqlite";

    public static final String TBL_LESSON = "Lessons";
    public static final String FLD_LESSON_NO = "No";
    public static final String FLD_CATEGORY = "Category";
    public static final String FLD_SUB_CATEGORY = "SubCategory";

    public static final String TBL_QUIZ = "QuizZ";
    public static final String FLD_QUIZ_NO = FLD_LESSON_NO;

    public static final String TBL_VOCAB = "Vocab";
    public static final String FLD_VOCAB_NO = "Lesson";

    public static final String TBL_CATEGORY_PURCHASE = "Purchase";

    private static File DATABASE_FILE;

    private static final int DATABASE_VERSION = 16;

    private boolean mInvalidDatabaseFile = false;
    private boolean mIsUpgraded = false;
    private Context mContext;
    private int mOpenConnections = 0;

    private static DBHelper mInstance = null;
    public static DBHelper getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new DBHelper(context);
        }
        return mInstance;
    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            if (db != null) {
                db.close();
            }

            DATABASE_FILE = context.getDatabasePath(DB_NAME);

            if (mInvalidDatabaseFile) {
                copyDatabase();
            }
            if (mIsUpgraded) {
                doUpgrade();
            }
        } catch (SQLiteException e) {
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mInvalidDatabaseFile = true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase database,
                          int old_version, int new_version) {
        mInvalidDatabaseFile = true;
        mIsUpgraded = true;
    }

    private void doUpgrade() {
        copyDatabase();
    }

    @Override
    public synchronized void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // increment the number of users of the database connection.
        mOpenConnections++;
    }

    /**
     * implementation to avoid closing the database connection while it is in
     * use by others.
     */
    @Override
    public synchronized void close() {
        mOpenConnections--;
        if (mOpenConnections == 0) {
            super.close();
        }
    }

    private void copyDatabase() {
        AssetManager assetManager = mContext.getResources().getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(DATABASE_ASSET_FILE);
            out = new FileOutputStream(DATABASE_FILE);
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {}
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {}
            }
        }
        setDatabaseVersion();
        mInvalidDatabaseFile = false;
    }

    private void setDatabaseVersion() {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(DATABASE_FILE.getAbsolutePath(), null,
                    SQLiteDatabase.OPEN_READWRITE);
            db.execSQL("PRAGMA user_version = " + DATABASE_VERSION);
        } catch (SQLiteException e ) {
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public QuizDataObject getQuizByNo(int no)  {
        QuizDataObject quiz = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor  res = db.rawQuery("SELECT * FROM " + TBL_QUIZ + " WHERE " + FLD_QUIZ_NO + " = " + no, null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            quiz = new QuizDataObject(res);
            res.moveToNext();
        }
        res.close();
        return quiz;
    }

    public ArrayList<VocabDataObject> getVocabByNo(int no)  {
        ArrayList<VocabDataObject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor  res = db.rawQuery("SELECT * FROM " + TBL_VOCAB + " WHERE " + FLD_VOCAB_NO + " = " + no, null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            VocabDataObject vocab = new VocabDataObject(res);
            list.add(vocab);
            res.moveToNext();
        }
        res.close();
        return list;
    }

    public LessonDataObject[] getLessonData() {
        ArrayList<LessonDataObject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor  res = db.rawQuery("SELECT * FROM " + TBL_LESSON,  null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            LessonDataObject one = new LessonDataObject(res);
            list.add(one);
            res.moveToNext();
        }
        res.close();
        return list.toArray(new LessonDataObject[list.size()]);
    }

    public ArrayList<LessonDataObject> getDataByCategory(String category){
        ArrayList<LessonDataObject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor  res = db.rawQuery("SELECT * FROM " + TBL_LESSON + " WHERE " + FLD_CATEGORY + " = '" + category + "'",  null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            LessonDataObject one = new LessonDataObject(res);
            list.add(one);
            res.moveToNext();
        }
        res.close();
        return list;
    }
    public int getAllLessonsCount() {
        int nCount = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM " + TBL_LESSON, null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            nCount = res.getInt(0);
            res.moveToNext();
        }
        res.close();
        return nCount;
    }
    public ArrayList<LessonDataObject> getAllLessons(){
        ArrayList<LessonDataObject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor  res = db.rawQuery("SELECT * FROM " + TBL_LESSON,  null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            LessonDataObject one = new LessonDataObject(res);
            list.add(one);
            res.moveToNext();
        }
        res.close();
        return list;
    }

    public ArrayList<LessonDataObject> getFirstCategoryLessons(){
        ArrayList<LessonDataObject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor  res = db.rawQuery(" SELECT * FROM " + TBL_LESSON + " WHERE " + FLD_LESSON_NO + " >= 1 and " + FLD_LESSON_NO + " <= 100 " + " ORDER BY " + FLD_LESSON_NO,  null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            LessonDataObject one = new LessonDataObject(res);
            list.add(one);
            res.moveToNext();
        }
        res.close();
        return list;
    }

    public ArrayList<LessonDataObject> getSecondCategoryLessons(){
        ArrayList<LessonDataObject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor  res = db.rawQuery(" SELECT * FROM " + TBL_LESSON + " WHERE " + FLD_LESSON_NO + " >= 101 and " + FLD_LESSON_NO + " <= 200 " + " ORDER BY " + FLD_LESSON_NO,  null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            LessonDataObject one = new LessonDataObject(res);
            list.add(one);
            res.moveToNext();
        }
        res.close();
        return list;
    }

    public ArrayList<LessonDataObject> getThirdCategoryLessons(){
        ArrayList<LessonDataObject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor  res = db.rawQuery(" SELECT * FROM " + TBL_LESSON + " WHERE " + FLD_LESSON_NO + " >= 201 and " + FLD_LESSON_NO + " <= 300 " + " ORDER BY " + FLD_LESSON_NO,  null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            LessonDataObject one = new LessonDataObject(res);
            list.add(one);
            res.moveToNext();
        }
        res.close();
        return list;
    }

    public int getCategoryCount() {
        int nCount = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM " + TBL_LESSON + " GROUP BY " + FLD_CATEGORY + " ORDER BY " + FLD_LESSON_NO, null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            nCount = res.getInt(0);
            res.moveToNext();
        }
        res.close();
        return nCount;
    }

    public ArrayList<String> getCategories() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT " + FLD_CATEGORY + " FROM " + TBL_LESSON + " GROUP BY " + FLD_CATEGORY + " ORDER BY " + FLD_LESSON_NO, null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            String one = res.getString(0);
            list.add(one);
            res.moveToNext();
        }
        res.close();
        return list;
    }

    public ArrayList<String> getSubCategories(String category) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT " + FLD_SUB_CATEGORY + " FROM " + TBL_LESSON + " WHERE " + FLD_CATEGORY + " = '" + category + "' GROUP BY " + FLD_SUB_CATEGORY + " ORDER BY " + FLD_LESSON_NO, null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            String one = res.getString(0);
            list.add(one);
            res.moveToNext();
        }
        res.close();
        return list;
    }

    public LessonDataObject getLessonByNo(int no){
        LessonDataObject lesson = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TBL_LESSON + " WHERE " + FLD_LESSON_NO + " = " + no, null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            lesson = new LessonDataObject(res);
            res.moveToNext();
        }
        res.close();
        return lesson;
    }

    public LessonDataObject getLessonImageByNo(int no){
        LessonDataObject lesson = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TBL_LESSON + " WHERE " + FLD_LESSON_NO + " = " + no, null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            lesson = new LessonDataObject(res);
            res.moveToNext();
        }
        res.close();
        return lesson;
    }

    public ArrayList<CategoryPurchaseObject>  getPurchaseStatus(){
        ArrayList<CategoryPurchaseObject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TBL_CATEGORY_PURCHASE, null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            CategoryPurchaseObject purchase = new CategoryPurchaseObject(res);
            list.add(purchase);
            res.moveToNext();
        }
        res.close();
        return list;
    }

    public void updatePurchaseStatus(){
        String updateQuery = "UPDATE Purchase SET PurchaseStatus = 1 WHERE PurchaseStatus = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(updateQuery, null);
        c.moveToFirst();
    }

    public void updateOfflineStatus(){
        String updateQuery = "UPDATE Purchase SET offlinePurchase = 1 WHERE offlinePurchase = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(updateQuery, null);
        c.moveToFirst();
    }
}
