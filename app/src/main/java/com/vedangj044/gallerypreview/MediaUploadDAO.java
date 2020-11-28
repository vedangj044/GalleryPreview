package com.vedangj044.gallerypreview;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MediaUploadDAO {

    @Query("SELECT * FROM chatMedia WHERE groupID == :id")
    List<ImageStatusObject> getMediaByGroup(int id);

    @Query("SELECT COUNT(*) FROM chatMedia WHERE groupID == :id")
    LiveData<Integer> getCountByGroupID(int id);

    @Update
    int update(ImageStatusObject imageStatusObject);

    @Query("SELECT * FROM chatMedia ORDER BY id DESC")
    DataSource.Factory<Integer, ImageStatusObject> getPagedMedia();

    @Insert
    void insetImageStatusObject(ImageStatusObject imageStatusObject);

    @Query("UPDATE chatMedia SET state = 6 WHERE state = 5")
    void cancelAllDownloads();

    @Query("UPDATE chatMedia SET state = 2 WHERE state = 1")
    void cancelAllUploads();

}
