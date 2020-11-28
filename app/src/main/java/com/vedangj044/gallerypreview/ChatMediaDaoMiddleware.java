package com.vedangj044.gallerypreview;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ChatMediaDaoMiddleware{

    private final MediaUploadDatabase mediaUploadDatabase;
    private final ExecutorService executor = ExecutorHelper.getInstanceExecutor();

    private static ChatMediaDaoMiddleware instance;
    public static ChatMediaDaoMiddleware getInstance(Context context){
        if(instance == null){
            instance = new ChatMediaDaoMiddleware(context);
        }
        return instance;
    }

    public ChatMediaDaoMiddleware(Context context){
        mediaUploadDatabase = MediaUploadDatabase.getInstance(context);
    }

    public static class ExecutorHelper{

        private static ExecutorService instanceExecutor;

        public static synchronized ExecutorService getInstanceExecutor(){
            if(instanceExecutor == null){
                instanceExecutor = Executors.newSingleThreadExecutor();
            }
            return instanceExecutor;
        }
    }

    public void insertChatMedia(ImageStatusObject imageStatusObject){

        Callable<Void> insert = () -> {
            mediaUploadDatabase.mediaUploadDAO().insetImageStatusObject(imageStatusObject);
            return null;
        };

        executor.submit(insert);
    }

    public void updateChatMedia(ImageStatusObject imageStatusObject){

        Callable<Void> update = () -> {
            int i = mediaUploadDatabase.mediaUploadDAO().update(imageStatusObject);
            Log.v("Database", i + " Rows updated");
            return null;
        };

        executor.submit(update);
    }

    public List<ImageStatusObject> getChatMediaByGroupID(int groupID) throws ExecutionException, InterruptedException {

        Callable<List<ImageStatusObject>> fetch = () -> mediaUploadDatabase.mediaUploadDAO().getMediaByGroup(groupID);
        return executor.submit(fetch).get();
    }

    public LiveData<Integer> getCountChatMediaByGroupID(int groupID){
        return mediaUploadDatabase.mediaUploadDAO().getCountByGroupID(groupID);
    }

    public DataSource.Factory<Integer, ImageStatusObject> getPagedList(){
        return mediaUploadDatabase.mediaUploadDAO().getPagedMedia();
    }

    public void cancelAllDownloads(){
        Callable<Void> cancel = () -> {
            mediaUploadDatabase.mediaUploadDAO().cancelAllDownloads();
            return null;
        };

        executor.submit(cancel);
    }

    public void cancelAllUploads(){
        Callable<Void> cancel = () -> {
            mediaUploadDatabase.mediaUploadDAO().cancelAllUploads();
            return null;
        };

        executor.submit(cancel);
    }
}