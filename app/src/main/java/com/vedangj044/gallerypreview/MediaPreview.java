package com.vedangj044.gallerypreview;

import android.webkit.MimeTypeMap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MediaPreview {

    private int id;
    private String path;
    private Boolean isVideo;
    private String fileName;

    private Boolean isActive;

    public MediaPreview(int id, String path) {
        this.id = id;
        this.path = path;
        this.isVideo = isVideoFinder(path);
        this.isActive = false;
        createFileName();

    }

    private boolean isVideoFinder(String url) {

        url = url.substring(url.lastIndexOf("."));
        String mimeType = MimeTypeMap.getFileExtensionFromUrl(url);
        if(MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeType).contains("video")){
            return true;
        }
        return false;
    }

    // create file name which is IMG + timeStamp + random + extension
    private void createFileName(){
        String prefix = "";
        String extension = "";

        if(this.isVideo){
            prefix = "VID_";
            extension = ".mp4";
        }
        else{
            prefix = "IMG_";
            extension = ".png";
        }

        Random r = new Random();
        String alphabet = String.valueOf((1 + r.nextInt(2)) * 10000 + r.nextInt(10000));

        String name = prefix +
                new SimpleDateFormat("yyyyMMdd_HHmmss_", Locale.US).format(new Date()) +
                alphabet + extension;

        this.fileName = name;
    }

    public Boolean getVideo() {
        return isVideo;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean isVideo() {
        return isVideo;
    }

    public void setVideo(Boolean video) {
        isVideo = video;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
