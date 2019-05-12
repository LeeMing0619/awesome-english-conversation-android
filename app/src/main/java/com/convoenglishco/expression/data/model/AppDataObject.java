package com.convoenglishllc.expression.data.model;

public class AppDataObject {
    public String app_title;
    public String app_summary;
    public String app_image;

    public AppDataObject(String app_title, String app_summary, String app_image) {
        this.app_title = app_title;
        this.app_summary = app_summary;
        this.app_image = app_image;
    }

    public static AppDataObject[] getRecommendedApps() {
        return new AppDataObject[] {
                new AppDataObject(
                        "English Interview Convo",
                        "Prepare for a job interview or just improve your business English.  Learn what to say at your interview, see and listen to natural examples, and practice with interview simulator.",
                        "apps/app1.jpg"),
                new AppDataObject(
                        "English Conversation",
                        "One of the most popular English conversations apps.  Improve your speaking, pronunciation, and fluency by practicing with real natural English conversations.",
                        "apps/app2.png"),
                new AppDataObject(
                        "English Speaking Practice",
                        "400 natural English conversations about daily life and business.  Practice having natural English conversations and improve your speaking skills.",
                        "apps/app3.jpg"),
                new AppDataObject(
                        "Learn to Speak English",
                        "Over 800 lessons and 8,000 audio files.  All completely free!",
                        "apps/app4.png"),
                new AppDataObject(
                        "English Vocabulary",
                        "Learn how to use the most important and common English words in sentences.  Study over 40,000 audio files and thousands of example sentences.",
                        "apps/app5.png"),
                new AppDataObject(
                        "English Listening",
                        "Improve your English listening with great lessons and fun quizzes.  Completely free!",
                        "apps/app6.png"),
                new AppDataObject(
                        "English Listening Player",
                        "Listen to English conversations and English lessons.  Create playlists and listen for hours without clicking anything!  100% free!",
                        "apps/app7.png"),
                new AppDataObject(
                        "Basic English for Beginners",
                        "Practice your English speaking using beginner level conversations (includes basic business conversations too).",
                        "apps/app8.png"),
                new AppDataObject(
                        "Learn English for Kids",
                        "The best way for children to learn English using an app.  There are thousands of pictures and audio files to help any child learn English.",
                        "apps/app9.png"),
                new AppDataObject(
                        "English Grammar Book",
                        "130 lessons with interactive quizzes that cover the most essential English grammar points.  Must have for any serious English learner!",
                        "apps/app10.png"),
        };
    }
}