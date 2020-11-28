package com.vedangj044.gallerypreview;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class ChatActivity extends AppCompatActivity {


    private ChatMediaPagedListAdapter pagedListAdapter;
    private DownloadHelper downloadHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        RecyclerView chatRecycle = findViewById(R.id.chat_recycle);

        LinearLayoutManager lp = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        chatRecycle.setLayoutManager(lp);

        int groupID = getIntent().getIntExtra("groupID", 0);
        downloadHelper = new DownloadHelper(this);
        UploadHelper uploadHelper = new UploadHelper(this);

        uploadHelper.enqueue(groupID);

        ChatMediaViewModel viewModel = new ViewModelProvider(this).get(ChatMediaViewModel.class);
        pagedListAdapter = new ChatMediaPagedListAdapter(new ImageStatusObjectDiffCallback(), downloadHelper, uploadHelper);
        chatRecycle.setAdapter(pagedListAdapter);

        viewModel.getImageStatusObjectPaged().observe(this, imageStatusObjects -> pagedListAdapter.submitList(imageStatusObjects));

        pagedListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if(positionStart == 0){
                    lp.scrollToPosition(0);
                }
            }
        });

//        CountDownTimer cnt = new CountDownTimer(10000, 5000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            @Override
//            public void onFinish() {
//                img1[0].setId(img1[0].getId()+1);
//                img1[0].setState(ImageStatusObject.DOWNLOAD_NOT_STARTED);
//                img1[0].setSender(false);
//                        img1[0].setImageURL("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4");
//                        img1[0].setVideo(true);
//                        img1[0].setFileName("videoOne.mp4");
//                img1[0].setImageURL(url);
//                chatMediaDaoMiddleware.insertChatMedia(img1[0]);
//            }
//        };
//        cnt.start();

    }

    @Override
    protected void onResume() {
        downloadHelper = new DownloadHelper(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        downloadHelper.unregister();
        super.onPause();
    }
}