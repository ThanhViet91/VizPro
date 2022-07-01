package com.thanhlv.vizpro.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.thanhlv.vizpro.data.dao.VideoDAO;
import com.thanhlv.vizpro.data.entities.Video;


@Database(entities = {Video.class}, version = VideoDatabase.DATABASE_VERSION, exportSchema = false)
public abstract class VideoDatabase extends RoomDatabase {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Video-Room-database";

    public abstract VideoDAO getVideoDao();


//    SingleTon DP
    private static VideoDatabase mInstance;

    public static VideoDatabase getInstance(Context context) {

        if (mInstance == null) {
            synchronized (VideoDatabase.class) {
                mInstance = Room.databaseBuilder(context, VideoDatabase.class, DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build();
            }
        }
        return mInstance;
    }
}
