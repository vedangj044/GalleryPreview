package com.vedangj044.gallerypreview;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class ChatMediaViewModel extends AndroidViewModel {

    Application application;
    private LiveData<PagedList<ImageStatusObject>> imageStatusObjectPaged;

    public ChatMediaViewModel(Application application) {
        super(application);
        this.application = application;
        DataSource.Factory<Integer, ImageStatusObject> fine = MediaUploadDatabase.getInstance(application).mediaUploadDAO().getPagedMedia();

        imageStatusObjectPaged = new LivePagedListBuilder<>(fine, 10).build();
    }

    public LiveData<PagedList<ImageStatusObject>> getImageStatusObjectPaged() {
        return imageStatusObjectPaged;
    }

    public void invalidate(){
        imageStatusObjectPaged.getValue().getDataSource().invalidate();
    }
}
