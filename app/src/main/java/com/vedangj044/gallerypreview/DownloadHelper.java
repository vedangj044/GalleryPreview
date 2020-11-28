package com.vedangj044.gallerypreview;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DownloadHelper {

    private DownloadManager dm;
    private Context mContext;
    private static HashMap<Long, ImageStatusObject> listOfQueuedDownloads = new HashMap<>();
    private ExecutorService executor = SlideImage.ExecutorHelper.getInstanceExecutor();
    private MediaUploadDatabase mediaUploadDatabase;


    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            Log.v("called", id+"");
            if(listOfQueuedDownloads.containsKey(id)){

                ImageStatusObject img = listOfQueuedDownloads.get(id);
                img.setState(ImageStatusObject.DOWNLOAD_DONE);

                updateStatus(img);
            }

        }
    };


    public DownloadHelper(Context context) {
        this.mContext = context;
        this.mediaUploadDatabase = MediaUploadDatabase.getInstance(this.mContext);
        dm = (DownloadManager) this.mContext.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        context.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void enqueue(ImageStatusObject img){

        String path = "file://";

        if(img.getVideo()){
            path += this.mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/ithubVideos/";
        }
        else{
            path += this.mContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/ithubImages/";
        }

        path += img.getFileName();

        File temp_file = new File(path);
        if(!temp_file.exists()){
            temp_file.mkdir();
        }

        Uri downloadURI = Uri.parse(img.getImageURL());

        DownloadManager.Request request = new DownloadManager.Request(downloadURI);
        request.setDestinationUri(Uri.parse(path));

        long downloadID = this.dm.enqueue(request);

        Log.v("changed", img.toString());
        img.setState(ImageStatusObject.DOWNLOAD_PROCESS);
        path = path.substring(7);
        img.setImageURL(path);
        updateStatus(img);
        listOfQueuedDownloads.put(downloadID, img);

    }

    private void updateStatus(ImageStatusObject img){
        Future<Void> task = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                int i = mediaUploadDatabase.mediaUploadDAO().update(img);
                Log.v("changed1", img.toString() + "____" + i);
                return null;
            }
        });

    }

    public void unregister(){
        this.mContext.unregisterReceiver(onDownloadComplete);
    }
}
