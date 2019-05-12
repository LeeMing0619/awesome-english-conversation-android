package com.convoenglishllc.expression.data.model;

import android.database.Cursor;

public class VocabDataObject {
    private String mLesson;
    private String mLesson_Title;
    private String mWord;
    private String mVocab_and_Word_Phrase;
    private String mMeaning;
    private String mExample1;
    private String mExample2;

    public VocabDataObject(Cursor c) {
        mLesson = c.getString(0);
        mLesson_Title = c.getString(1);
        mWord = c.getString(2);
        mVocab_and_Word_Phrase = c.getString(3);
        mMeaning = c.getString(4);
        mExample1 = c.getString(5);
        mExample2 = c.getString(6);
    }

    public String getLesson() { return mLesson; }
    public String getLessonTitle() { return mLesson_Title; }
    public String getWord() { return mWord; }
    public String getVocabWordPhrase() { return mVocab_and_Word_Phrase; }
    public String getMeaning() { return mMeaning; }
    public String getExample1() { return mExample1; }
    public String getExample2() { return mExample2; }
}
