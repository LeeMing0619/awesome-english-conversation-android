package com.convoenglishllc.expression.data.model;

public class RecordDataObject {
    public int lesson_no;
    public String lesson_image;
    public String record_title;
    public String record_path;
    public long record_date;

    public RecordDataObject(int lesson_no, String lesson_image, String record_title, String record_path, long record_date) {
        this.lesson_no = lesson_no;
        this.lesson_image = lesson_image;
        this.record_title = record_title;
        this.record_path = record_path;
        this.record_date = record_date;
    }
}