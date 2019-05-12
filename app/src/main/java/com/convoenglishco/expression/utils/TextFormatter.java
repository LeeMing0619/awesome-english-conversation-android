package com.convoenglishllc.expression.utils;

import java.util.concurrent.TimeUnit;

public class TextFormatter {
    public static String getPlayTime(int time) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes((long) time),
                TimeUnit.MILLISECONDS.toSeconds((long) time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) time)));
    }

    public static String getDisplayableTime(long delta)
    {
        long difference;
        Long mDate = java.lang.System.currentTimeMillis();

        if(mDate > delta)
        {
            difference= mDate - delta;
            final long seconds = difference/1000;
            final long minutes = seconds/60;
            final long hours = minutes/60;
            final long days = hours/24;
            final long months = days/31;
            final long years = days/365;

            if (seconds < 0) {
                return "not yet";
            } else if (seconds == 0) {
                return "now";
            } else if (seconds < 60) {
                return seconds == 1 ? "one second ago" : seconds + " seconds ago";
            } else if (seconds < 120) {
                return "a minute ago";
            } else if (seconds < 2700) {// 45 * 60
                return minutes + " minutes ago";
            } else if (seconds < 5400) {// 90 * 60
                return "an hour ago";
            } else if (seconds < 86400) {// 24 * 60 * 60
                return hours + " hours ago";
            } else if (seconds < 172800) {// 48 * 60 * 60
                return "yesterday";
            } else if (seconds < 2592000) {// 30 * 24 * 60 * 60
                return days + " days ago";
            } else if (seconds < 31104000) {// 12 * 30 * 24 * 60 * 60
                return months <= 1 ? "one month ago" : days + " months ago";
            } else {
                return years <= 1 ? "one year ago" : years + " years ago";
            }
        }
        return null;
    }
}
