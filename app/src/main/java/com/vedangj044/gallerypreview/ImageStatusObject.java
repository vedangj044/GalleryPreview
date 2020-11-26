package com.vedangj044.gallerypreview;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chatMedia")
public class ImageStatusObject {

    public static final int UPLOAD_PROCESS = 1;
    public static final int UPLOAD_RETRY = 2;
    public static final int UPLOAD_DONE = 3;

    public static final int DOWNLOAD_NOT_STARTED = 4;
    public static final int DOWNLOAD_PROCESS = 5;
    public static final int DOWNLOAD_RETRY = 6;
    public static final int DOWNLOAD_DONE = 7;

    private static final int[] stateArray = {UPLOAD_PROCESS, UPLOAD_RETRY, UPLOAD_DONE,
                                            DOWNLOAD_NOT_STARTED, DOWNLOAD_PROCESS, DOWNLOAD_RETRY,
                                            DOWNLOAD_DONE};

    @PrimaryKey(autoGenerate = true)
    private int id;

    // this is not a URL it is a base64 string
    @ColumnInfo(name = "thumbnailURL")
    private String thumbnailURL;

    @ColumnInfo(name = "imageURL")
    private String ImageURL;

    @ColumnInfo(name = "isVideo")
    private Boolean isVideo;

    @ColumnInfo(name = "state")
    @NonNull
    private Integer state;

    @ColumnInfo(name = "groupID")
    private int groupID;

    @ColumnInfo(name = "fileName")
    private String fileName;

    @ColumnInfo(name = "isSender")
    private boolean isSender;

    public ImageStatusObject(String thumbnailURL, String imageURL, Boolean isVideo, String fileName, int groupID, boolean isSender) {
        this.thumbnailURL = thumbnailURL;
        ImageURL = imageURL;
        this.isVideo = isVideo;
        this.groupID = groupID;
        this.fileName = fileName;
        this.isSender = isSender;

        if(isSender){
            this.state = UPLOAD_PROCESS;
        }
        else{
            this.state = DOWNLOAD_NOT_STARTED;
        }
    }

    public ImageStatusObject() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    public Boolean getVideo() {
        return isVideo;
    }

    public void setVideo(Boolean video) {
        isVideo = video;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public boolean isSender() {
        return isSender;
    }

    public void setSender(boolean sender) {
        isSender = sender;
    }

    @NonNull
    public Integer getState() {
        return this.state;
    }

    public void setState(Integer state) {
        for (int n: stateArray){
            if (n == state) {
                this.state = state;
                return;
            }
        }
        Log.e("ImageStatusObject", "Value of state variable invalid.");
    }

    @Override
    public String toString() {
        return "ImageStatusObject{" +
                "id=" + id +
                ", thumbnailURL='" + thumbnailURL + '\'' +
                ", ImageURL='" + ImageURL + '\'' +
                ", isVideo=" + isVideo +
                ", state=" + state +
                ", groupID=" + groupID +
                ", fileName='" + fileName + '\'' +
                ", isSender=" + isSender +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        assert obj != null;
        if(obj.getClass() == ImageStatusObject.class && obj.toString() != null){
            return this.toString().equals(obj.toString());
        }
        return false;
    }
}
