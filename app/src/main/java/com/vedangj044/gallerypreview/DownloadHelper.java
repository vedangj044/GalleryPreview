package com.vedangj044.gallerypreview;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DownloadHelper {

    private DownloadManager dm;
    private Context mContext;
    private static HashMap<Long, ImageStatusObject> listOfQueuedDownloads = new HashMap<>();
    private ChatMediaDaoMiddleware chatMediaDaoMiddleware;


    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            Log.v("called", id+"");
            if(listOfQueuedDownloads.containsKey(id)){

                if(isSuccessfull(id)){
                    ImageStatusObject img = listOfQueuedDownloads.get(id);
                    img.setState(ImageStatusObject.DOWNLOAD_DONE);
                    updateStatus(img);
                }
                else{
                    ImageStatusObject img = listOfQueuedDownloads.get(id);
                    img.setState(ImageStatusObject.DOWNLOAD_RETRY);
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
        context.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        context.registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void enqueue(ImageStatusObject img){

        String path = "/ithubImages/";

        if(img.getVideo()){
            path =  "/ithubVideos/";
        }

        path += img.getFileName();

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
        img.setImageURL(Environment.DIRECTORY_DOWNLOADS + path);
        listOfQueuedDownloads.put(downloadID, img);

    }

    private void updateStatus(ImageStatusObject img){

        chatMediaDaoMiddleware.updateChatMedia(img);

    }

    public void unregister(){
        this.mContext.unregisterReceiver(onDownloadComplete);
        this.mContext.unregisterReceiver(networkStateReceiver);
    }
}
