package com.vedangj044.gallerypreview;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DownloadHelper {

    private DownloadManager dm;
    private Context mContext;
    private static HashMap<Long, ImageStatusObject> listOfQueuedDownloads = new HashMap<>();
    private ChatMediaDaoMiddleware chatMediaDaoMiddleware;
    private SharedPreferences sharedPreferences;

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            Log.v("called", id+"");
            if(listOfQueuedDownloads.containsKey(id)){

                if(isSuccessfull(id)){
                    ImageStatusObject img = listOfQueuedDownloads.get(id);
                    img.setState(ImageStatusObject.DOWNLOAD_DONE);
                    img.setImageURL(Environment.DIRECTORY_DOWNLOADS + getPath(img.getVideo(), img.getFileName()));
                    updateStatus(img);
                }
            }

        }
    };

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            Log.v("ima", "net");

            if(!isConnected){
                for (Map.Entry element: listOfQueuedDownloads.entrySet()){
                    if(isSuccessfull((Long)element.getKey())){
                        dm.remove((Long)element.getKey());
                    }
                }

                chatMediaDaoMiddleware.cancelAllDownloads();
                chatMediaDaoMiddleware.cancelAllUploads();
            }

        }
    };

    private boolean isSuccessfull(long id){
        return this.dm.getUriForDownloadedFile(id) != null;
    }


    public DownloadHelper(Context context) {
        this.mContext = context;
        chatMediaDaoMiddleware = ChatMediaDaoMiddleware.getInstance(context);
        dm = (DownloadManager) this.mContext.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    public void registerReceiver(){
        this.mContext.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        this.mContext.registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void checkPreferences(){

        List<String> removeTheseKeys = new ArrayList<>();

        Map<String, ?> all = sharedPreferences.getAll();
        for(Map.Entry<String,?> entry : all.entrySet()){
            Cursor cursor = this.dm.query(new DownloadManager.Query().setFilterById(Long.parseLong(entry.getKey())));
            if(cursor.moveToFirst()){
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if(status == DownloadManager.STATUS_SUCCESSFUL){
                    removeTheseKeys.add(entry.getKey());
                    String path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    chatMediaDaoMiddleware.updateMediaByID(path, entry.getValue().toString(), ImageStatusObject.DOWNLOAD_DONE);
                }
                else if(status == DownloadManager.STATUS_RUNNING){
                    chatMediaDaoMiddleware.updateMediaByID(null, entry.getValue().toString(), ImageStatusObject.DOWNLOAD_PROCESS);
                }
                else{
                    removeTheseKeys.add(entry.getKey());
                    chatMediaDaoMiddleware.updateMediaByID(null, entry.getValue().toString(), ImageStatusObject.DOWNLOAD_RETRY);
                }
            }
        }

        for(String s: removeTheseKeys){
            sharedPreferences.edit().remove(s).apply();
        }
    }

    private String getPath(boolean isVideo, String filename){
        String path = "/ithubImages/";

        if(isVideo){
            path =  "/ithubVideos/";
        }

        path += filename;

        return path;
    }

    public void enqueue(ImageStatusObject img){

        String path = getPath(img.getVideo(), img.getFileName());

        File temp_file = new File(path);
        if(!temp_file.exists()){
            temp_file.mkdir();
        }

        Uri downloadURI = Uri.parse(img.getImageURL());

        DownloadManager.Request request = new DownloadManager.Request(downloadURI);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, path);

        long downloadID = this.dm.enqueue(request);

        Log.v("changed", img.toString());
        img.setState(ImageStatusObject.DOWNLOAD_PROCESS);
        updateStatus(img);
        listOfQueuedDownloads.put(downloadID, img);

        sharedPreferences.edit().putString(String.valueOf(downloadID), String.valueOf(img.getId())).apply();

    }

    private void updateStatus(final ImageStatusObject img){

        chatMediaDaoMiddleware.updateChatMedia(img);

    }

    public void unregister(){
        this.mContext.unregisterReceiver(onDownloadComplete);
        this.mContext.unregisterReceiver(networkStateReceiver);
    }
}
