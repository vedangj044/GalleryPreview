package com.vedangj044.gallerypreview;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class ChatMediaViewModel extends AndroidViewModel {

    Application application;
    private final LiveData<PagedList<ImageStatusObject>> imageStatusObjectPaged;

    public ChatMediaViewModel(Application application) {
        super(application);
        this.application = application;
        DataSource.Factory<Integer, ImageStatusObject> fine = ChatMediaDaoMiddleware.getInstance(application).getPagedList();

        imageStatusObjectPaged = new LivePagedListBuilder<>(fine, 10).build();
    }

    public LiveData<PagedList<ImageStatusObject>> getImageStatusObjectPaged() {
        return imageStatusObjectPaged;
    }

}
