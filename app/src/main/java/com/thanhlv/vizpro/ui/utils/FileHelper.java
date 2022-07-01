package com.thanhlv.vizpro.ui.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.thanhlv.vizpro.data.database.VideoDatabase;
import com.thanhlv.vizpro.data.entities.Video;
import com.thanhlv.vizpro.ui.adapters.VideoAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FileHelper {
    private static final String TAG = "FileHelper";

    private final Object mSync = new Object();

    private final VideoAdapter mVideoAdapter;

    private static FileHelper mInstance = null;

    private FileHelper(VideoAdapter mVideoAdapter) {
        this.mVideoAdapter = mVideoAdapter;
    }

    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    public static FileHelper getInstance(VideoAdapter mAdapter){
        if (mInstance == null && mAdapter != null) {
            synchronized (FileHelper.class) {
                mInstance = new FileHelper(mAdapter);
            }
        }
        return mInstance;
    }


    public void tryToRenameFile(final Video video, final String newTitle) throws Exception {
        if (MyUtils.isValidFilenameSynctax(newTitle))
            throw new Exception("A filename cannot contain any of the following charactor: \\/\":*<>| is not n");

        File file = new File(video.getLocalPath());

        final File fileWithNewName = new File(file.getParent(), newTitle);
        if (fileWithNewName.exists()) {
            throw new IOException("This filename is exists. Please choose another name");
        }

        // Rename file (or directory)
        boolean success = file.renameTo(fileWithNewName);

        if (!success) {
            // File was not successfully renamed
            throw new Exception("Cannot rename this video. This video file might not available.");
        } else {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    // and deleting
                    synchronized (mSync) {
                        Log.d(TAG, "run: before update called: " + video.toString());
                        video.updateTitle(newTitle, fileWithNewName.getAbsolutePath());
                        Log.d(TAG, "run: after update called : " + video.toString());
                        VideoDatabase.getInstance(mVideoAdapter.getContext()).getVideoDao().updateVideo(video);
                    }
                }
            });
        }
    }

    public void deleteVideosFromDatabase(final Video... videos) {
        if(videos.length > 0) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            // and deleting
                            synchronized (mSync) {
                                VideoDatabase.getInstance(mVideoAdapter.getContext()).getVideoDao().deleteVideos(videos);
                            }
                        }
                    });
                }
            });
        }
    }

    public Task<Void> deleteVideoFromDatabaseCallable(final Video... videos) {
        return Tasks.call(mExecutor, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if(videos.length > 0) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    // and deleting
                                    synchronized (mSync) {
                                        VideoDatabase.getInstance(mVideoAdapter.getContext()).getVideoDao().deleteVideos(videos);
                                    }
                                }
                            });
                        }
                    });
                }
                return null;
            }
        });


    }

    public void deleteFilesFromStorage(Video[] videos) {
        for(Video v: videos){
            File file = new File(v.getLocalPath());
            if(file.exists()){
                if(file.delete())
                    mVideoAdapter.removeVideo(v);
            }
        }
        mVideoAdapter.clearSelected();
        mVideoAdapter.showAllCheckboxes(false);
    }

    public Task<Void> deleteFilesFromStorageCallable(final Video[] videos) {
        return Tasks.call(mExecutor, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for(Video v: videos){
                    File file = new File(v.getLocalPath());
                    if(file.exists()){
                        if(file.delete());
                        mVideoAdapter.remove(v);
                    }
                }
                return null;
            }
        });
    }
}