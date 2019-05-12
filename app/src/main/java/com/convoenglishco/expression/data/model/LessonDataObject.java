package com.convoenglishllc.expression.data.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.convoenglishllc.expression.utils.GlobalConstants;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.security.MessageDigest;

public class LessonDataObject {
    private int mNo;
    private String mFirst;
    private String mSecond;
    private String mUrlChannelAll;
    private String mUrlChannelA;
    private String mUrlChannelB;
    private String mCategory;
    private String mSubCategory;
    private String mTitle;
    private String mText;
    private String mImage;


    public LessonDataObject(Cursor c) {
        mNo = c.getInt(0);
        mFirst = c.getString(1);
        mSecond = c.getString(2);
        mUrlChannelAll = c.getString(3);
        mUrlChannelA = c.getString(4);
        mUrlChannelB = c.getString(5);
        mCategory = c.getString(6);
        mSubCategory = c.getString(7);
        mTitle = c.getString(8);
        mText = c.getString(9);
        mImage = c.getString(10);
    }

    public int getNo() { return mNo; }
    public String getFirst() { return mFirst; }
    public String getSecond() { return mSecond; }
    public String getUrlChannelAll() { return GlobalConstants.AUDIO_URL + "/" + mUrlChannelAll; }
    public String getUrlChannelA() { return GlobalConstants.AUDIO_URL + "/" + mUrlChannelA; }
    public String getUrlChannelB() { return GlobalConstants.AUDIO_URL + "/" + mUrlChannelB; }
    public String getCategory() { return mCategory; }
    public String getSubCategory() { return mSubCategory; }
    public String getTitle() { return mTitle; }
    public String getText() { return mText; }
    public String getImage() { return mImage; }

    private static String getHashedString(String stringToEncrypt) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return stringToEncrypt;
        }
        md.update(stringToEncrypt.getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public String getDownloadPath_All(Context context) {
        return GlobalConstants.getAudioDownloadDir(context) + "/" + getHashedString(String.format("lesson%03d-%s", getNo(), GlobalConstants.MP3_ALL));
    }
    public String getDownloadPath_A(Context context) {
        return GlobalConstants.getAudioDownloadDir(context) + "/" + getHashedString(String.format("lesson%03d-%s", getNo(), GlobalConstants.MP3_A));
    }
    public String getDownloadPath_B(Context context) {
        return GlobalConstants.getAudioDownloadDir(context) + "/" + getHashedString(String.format("lesson%03d-%s", getNo(), GlobalConstants.MP3_B));
    }

    public Drawable getDrawable(Context context) {
        Drawable d = null;
        try {
            d = Drawable.createFromStream(context.getAssets().open(getDrawableAssetName()), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return d;
    }

    public String getDrawableAssetName() {
        return "images/" + getImage();
    }

    /*
    public Drawable getFirstDrawable() {
        Drawable d = null;
        try {
            d = Drawable.createFromStream(MainActivity.gContext.getAssets().open(getFirstDrawableAssetName()), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return d;
    }

    public Drawable getSecondDrawable() {
        Drawable d = null;
        try {
            d = Drawable.createFromStream(MainActivity.gContext.getAssets().open(getSecondDrawableAssetName()), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return d;
    }
    */

    public String getFirstDrawableAssetName() {
        return "people/" + getFirst();
    }

    public String getSecondDrawableAssetName() {
        return "people/" + getSecond();
    }

    public SpannableString getSpannableDialog() {
        String strText = this.getText();
        strText = strText.replace("\n", "\n\n");
        ArrayList<Integer> pos1 = new ArrayList<>();
        ArrayList<Integer> pos2 = new ArrayList<>();
        pos1.add(0);
        int k = 0;
        while(true) {
            if(k > 0) {
                int kkk = strText.indexOf("\"\n", pos2.get(k-1));
                if( kkk != -1)  pos1.add(kkk + 2);
                else break;
            }
            int kk = strText.indexOf(":", pos1.get(k));
            if(kk != -1) { pos2.add(kk); k++; }
            else break;
        }

        SpannableString ss1 = new SpannableString(strText);
        for(int i=0; i<pos2.size(); i++){
            int start = pos1.get(i);
            int end = pos2.get(i);

            ss1.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, 0);
        }
        return ss1;
    }

    public SpannableString getSpannableDialogA() {
        String strText = this.getText();
        strText = strText.replace("\n", "\n\n");
        strText = strText.replace(getTalkers()[1] + ":", "ME:");
        ArrayList<Integer> pos1 = new ArrayList<>();
        ArrayList<Integer> pos2 = new ArrayList<>();
        pos1.add(0);
        int k = 0;
        while(true) {
            if(k > 0) {
                int kkk = strText.indexOf("\"\n", pos2.get(k-1));
                if( kkk != -1)  pos1.add(kkk + 2);
                else break;
            }
            int kk = strText.indexOf(":", pos1.get(k));
            if(kk != -1) { pos2.add(kk); k++; }
            else break;
        }

        SpannableString ss1 = new SpannableString(strText);
        for(int i=0; i<pos2.size(); i++){
            int start = pos1.get(i);
            int end = pos2.get(i);

            ss1.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, 0);

            if(i % 2 == 0 && i < pos2.size() -  1) {
                ss1.setSpan(new ForegroundColorSpan(Color.rgb(0xb3, 0xb3, 0xb3)), pos1.get(i), pos1.get(i+1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        if(pos2.size() % 2 == 1) {
            ss1.setSpan(new ForegroundColorSpan(Color.rgb(0xb3, 0xb3, 0xb3)), pos1.get(pos2.size()-1), strText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss1;
    }

    public SpannableString getSpannableDialogB() {
        String strText = this.getText();
        strText = strText.replace("\n", "\n\n");
        strText = strText.replace(getTalkers()[0] + ":", "ME:");
        ArrayList<Integer> pos1 = new ArrayList<>();
        ArrayList<Integer> pos2 = new ArrayList<>();
        pos1.add(0);
        int k = 0;
        while(true) {
            if(k > 0) {
                int kkk = strText.indexOf("\"\n", pos2.get(k-1));
                if( kkk != -1)  pos1.add(kkk + 2);
                else break;
            }
            int kk = strText.indexOf(":", pos1.get(k));
            if(kk != -1) { pos2.add(kk); k++; }
            else break;
        }

        SpannableString ss1 = new SpannableString(strText);
        for(int i=0; i<pos2.size(); i++){
            int start = pos1.get(i);
            int end = pos2.get(i);

            ss1.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, 0);

            if(i % 2 == 1 && i < pos2.size() - 1) {
                ss1.setSpan(new ForegroundColorSpan(Color.rgb(0xb3, 0xb3, 0xb3)), pos1.get(i), pos1.get(i+1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        if(pos2.size() % 2 == 0) {
            ss1.setSpan(new ForegroundColorSpan(Color.rgb(0xb3, 0xb3, 0xb3)), pos1.get(pos2.size()-1), strText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss1;
    }

    public String[] getTalkers() {
        String strText = this.getText();
        ArrayList<Integer> pos1 = new ArrayList<>();
        ArrayList<Integer> pos2 = new ArrayList<>();
        pos1.add(0);
        ArrayList<String> talkers = new ArrayList<>();
        int k = 0;
        while(true) {
            if(k > 0) {
                int kkk = strText.indexOf("\"\n", pos2.get(k-1));
                if( kkk != -1)  pos1.add(kkk + 2);
                else break;
            }
            int kk = strText.indexOf(":", pos1.get(k));
            if(kk != -1) {
                pos2.add(kk);
                String talker = strText.substring(pos1.get(k), pos2.get(k));
                if(talker.equals("A") || talker.equals("B")) talker = "Person " + talker;
                if(!talkers.contains(talker)) {
                    talkers.add(talker);
                }
                k++;
            }
            else break;
        }
        return talkers.toArray(new String[talkers.size()]);
    }
}
