package com.convoenglishllc.expression.data.model;

import android.database.Cursor;

public class CategoryPurchaseObject {
    private int mCategoryNo;
    private int mPurchaseStatus;
    private int mOfflinePurchase;

    public CategoryPurchaseObject(Cursor c) {
        mCategoryNo = c.getInt(0);
        mPurchaseStatus = c.getInt(1);
        mOfflinePurchase = c.getInt(2);
    }

    public int getCategoryNo() { return mCategoryNo; }
    public int getPurchaseStatus() { return mPurchaseStatus; }
    public int getOfflinePurchase() { return mOfflinePurchase; }
}
