package com.vedangj044.gallerypreview;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = ImageStatusObject.class, exportSchema = false, version = 1)
public abstract class MediaUploadDatabase extends RoomDatabase {

    private static final String DB_NAME = "mediaUploadDatabase";
    private static MediaUploadDatabase instance;

    public static synchronized MediaUploadDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), MediaUploadDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract MediaUploadDAO mediaUploadDAO();
}
