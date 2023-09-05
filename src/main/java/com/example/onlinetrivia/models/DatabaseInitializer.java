package com.example.onlinetrivia.models;

import android.content.Context;

import androidx.room.Room;

public class DatabaseInitializer {

    private static AppDatabase appDatabase;

    public static AppDatabase getInstance(Context context) {
        if (appDatabase == null) {
            synchronized (AppDatabase.class) {
                if (appDatabase == null) {
                    appDatabase = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "online_trivia_database")
                            .build();
                }
            }
        }
        return appDatabase;
    }
}
