package com.convoenglishllc.expression.data.model;

import android.database.Cursor;

public class QuizDataObject {
    public static int QUIZ_COUNT = 3;
    public static int CHOICE_COUNT = 4;
    class QA {
        public String mQuestion;
        public String[] mChoices = new String[CHOICE_COUNT];
        public int mAnswer;
    }

    private int mNo;
    private String mTitle;
    private QA mQa[] = new QA[QUIZ_COUNT];

    public int getNo() { return mNo; }
    public String getTitle() { return mTitle; }
    public String getQuestion(int i) { return mQa[i].mQuestion; }
    public String[] getChoices(int i) { return mQa[i].mChoices; }
    public int getAnswer(int i) { return mQa[i].mAnswer; }

    public QuizDataObject(Cursor c) {
        mNo = c.getInt(0);
        mTitle = c.getString(1);

        String correct = c.getString(17);
        for(int i=0; i<3; i++) {
            mQa[i] = new QA();
            mQa[i].mQuestion = c.getString(i*5 + 2);
            for(int j=0; j<4; j++) {
                mQa[i].mChoices[j] = c.getString(i*5 + 3 + j);
            }
            mQa[i].mAnswer = correct.charAt(i) - 'a';
        }
    }
}
