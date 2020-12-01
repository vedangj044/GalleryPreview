package com.vedangj044.gallerypreview;

import android.os.Bundle;
import android.os.CountDownTimer;

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
        UploadHelper uploadHelper = new UploadHelper(this);

        uploadHelper.enqueue(groupID);

        downloadHelper = new DownloadHelper(this);
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


    }

    @Override
    protected void onResume() {
        this.downloadHelper.checkPreferences();
        this.downloadHelper.registerReceiver();
        super.onResume();
    }

    @Override
    protected void onPause() {
        downloadHelper.unregister();
        super.onPause();
    }
}