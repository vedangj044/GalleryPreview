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

    @Query("UPDATE chatMedia SET state = :response WHERE groupID =:id AND fileName = :name")
    int updateUploadStatus(int response, int id, String name);

    @Update
    int update(ImageStatusObject imageStatusObject);

    @Query("SELECT * FROM chatMedia")
    LiveData<List<ImageStatusObject>> getMedia();

    @Query("SELECT * FROM chatMedia ORDER BY id DESC")
    DataSource.Factory<Integer, ImageStatusObject> getPagedMedia();

    @Insert
    void insetImageStatusObject(ImageStatusObject imageStatusObject);

    @Delete
    void deleteImageStatusObject(ImageStatusObject imageStatusObject);
}
