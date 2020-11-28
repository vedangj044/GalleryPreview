package com.vedangj044.gallerypreview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import retrofit2.http.POST;


public class ChatMediaPagedListAdapter extends PagedListAdapter<ImageStatusObject, RecyclerView.ViewHolder> {

    private static final int TYPE_MEDIA_SENDER = 1;
    private static final int TYPE_MEDIA_RECEIVER = 2;
    private DownloadHelper downloadHelper;
    private UploadHelper uploadHelper;

    protected ChatMediaPagedListAdapter(@NonNull DiffUtil.ItemCallback<ImageStatusObject> diffCallback, DownloadHelper downloadHelper, UploadHelper uploadHelper) {
        super(diffCallback);
        this.downloadHelper = downloadHelper;
        this.uploadHelper = uploadHelper;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == TYPE_MEDIA_SENDER){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_media_interaction, parent, false);
            return new MediaViewHolderSender(view);
        }
        else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_media_item, parent, false);
            return new MediaViewHolderReceiver(view);
        }


    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position).isSender()){
            return TYPE_MEDIA_SENDER;
        }
        else{
            return TYPE_MEDIA_RECEIVER;
        }
    }

    private String visibilityStateArray = "GGG";
    // G -> GONE | V -> VISIBLE | I -> INVISIBLE

    // Index -> view
    // 1 -> thumbnailImage
    // 2 -> retryButton
    // 3 -> uploadProgress

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ImageStatusObject img = getItem(position);

        if(getItemViewType(position) == TYPE_MEDIA_RECEIVER){

            MediaViewHolderReceiver hold = (MediaViewHolderReceiver) holder;

            switch (img.getState()){

                case ImageStatusObject.DOWNLOAD_NOT_STARTED:
                    Log.v("callled", "claslls1111");

                    hold.thumbnailImage.setImageBitmap(setThumbnail(img.getThumbnailURL()));
                    hold.retryButton.setImageResource(R.drawable.ic_download_foreground);
                    hold.retryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            downloadHelper.enqueue(img);
                            hold.retryButton.setVisibility(View.GONE);
                            hold.uploadProgress.setVisibility(View.VISIBLE);
                        }
                    });

                    visibilityStateArray = "VVG";
                    break;

                case ImageStatusObject.DOWNLOAD_PROCESS:
                    Log.v("callled", "claslls");
                    hold.thumbnailImage.setImageBitmap(setThumbnail(img.getThumbnailURL()));
                    visibilityStateArray = "VGV";
                    break;

                case ImageStatusObject.DOWNLOAD_DONE:

                    if (img.getVideo()){
                        hold.thumbnailImage.setImageBitmap(setThumbnail(img.getThumbnailURL()));
                        hold.retryButton.setImageResource(R.drawable.ic_play_foreground);
                        visibilityStateArray = "VVG";
                    }
                    else{
                        hold.originalImage.setImageBitmap(BitmapFactory.decodeFile(img.getImageURL()));
                        hold.thumbnailImage.setImageBitmap(BitmapFactory.decodeFile(img.getImageURL()));
                        visibilityStateArray = "VGG";
                    }

                    break;

                case ImageStatusObject.DOWNLOAD_RETRY:
                    hold.thumbnailImage.setImageBitmap(setThumbnail(img.getThumbnailURL()));
                    hold.retryButton.setImageResource(R.drawable.ic_retry_foreground);
                    hold.retryButton.setOnClickListener(v -> downloadHelper.enqueue(img));
                    visibilityStateArray = "VVG";
                    break;
            }

            hold.thumbnailImage.setVisibility(valueVisibility(visibilityStateArray.charAt(0)));
            hold.retryButton.setVisibility(valueVisibility(visibilityStateArray.charAt(1)));
            hold.uploadProgress.setVisibility(valueVisibility(visibilityStateArray.charAt(2)));

        }
        else {
            MediaViewHolderSender hold = (MediaViewHolderSender) holder;

            switch (img.getState()) {
                case ImageStatusObject.UPLOAD_PROCESS:
                    if (!img.getVideo()) {
                        hold.originalImage.setImageBitmap(BitmapFactory.decodeFile(img.getImageURL()));
                    }
                    hold.thumbnailImage.setImageBitmap(setThumbnail(img.getThumbnailURL()));
                    visibilityStateArray = "VGV";
                    break;

                case ImageStatusObject.UPLOAD_DONE:

                    if (img.getVideo()) {
                        hold.thumbnailImage.setImageBitmap(setThumbnail(img.getThumbnailURL()));
                        hold.retryButton.setImageResource(R.drawable.ic_play_foreground);
                        visibilityStateArray = "VVG";
                    } else {
                        hold.originalImage.setImageBitmap(BitmapFactory.decodeFile(img.getImageURL()));
                        hold.thumbnailImage.setImageBitmap(BitmapFactory.decodeFile(img.getImageURL()));
                        visibilityStateArray = "VGG";
                    }
                    break;

                case ImageStatusObject.UPLOAD_RETRY:
                    hold.thumbnailImage.setImageBitmap(setThumbnail(img.getThumbnailURL()));
                    hold.retryButton.setImageResource(R.drawable.ic_retry_foreground);
                    hold.retryButton.setOnClickListener(v -> uploadHelper.uploadFile(img));
                    visibilityStateArray = "VVG";
                    break;
            }

            hold.thumbnailImage.setVisibility(valueVisibility(visibilityStateArray.charAt(0)));
            hold.retryButton.setVisibility(valueVisibility(visibilityStateArray.charAt(1)));
            hold.uploadProgress.setVisibility(valueVisibility(visibilityStateArray.charAt(2)));
        }


    }

    private int valueVisibility(char s){
        if(s == 'V'){
            return View.VISIBLE;
        }
        else if(s == 'G'){
            return View.GONE;
        }
        else {
            return View.INVISIBLE;
        }
    }

    private Bitmap setThumbnail(String thumbnailURL){
        byte[] decodedString = Base64.decode(thumbnailURL, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public static class MediaViewHolderReceiver extends RecyclerView.ViewHolder{

        ImageView thumbnailImage;
        ImageView retryButton;
        ProgressBar uploadProgress;
        ImageView originalImage;
        ConstraintLayout layout;

        public MediaViewHolderReceiver(@NonNull View itemView) {
            super(itemView);

            thumbnailImage = itemView.findViewById(R.id.thumbnail_image_media);
            retryButton = itemView.findViewById(R.id.retry_button);
            uploadProgress = itemView.findViewById(R.id.upload_progress);
            originalImage = itemView.findViewById(R.id.background_image);
            layout = itemView.findViewById(R.id.layout_item);

        }
    }

    public static class MediaViewHolderSender extends RecyclerView.ViewHolder{

        ImageView thumbnailImage;
        ImageView retryButton;
        ProgressBar uploadProgress;
        ImageView originalImage;
        ConstraintLayout layout;

        public MediaViewHolderSender(@NonNull View itemView) {
            super(itemView);

            thumbnailImage = itemView.findViewById(R.id.thumbnail_image_media);
            retryButton = itemView.findViewById(R.id.retry_button);
            uploadProgress = itemView.findViewById(R.id.upload_progress);
            originalImage = itemView.findViewById(R.id.background_image);
            layout = itemView.findViewById(R.id.layout_item);

        }
    }
}
