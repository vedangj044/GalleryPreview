package com.vedangj044.gallerypreview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;


public class ChatMediaPagedListAdapter extends PagedListAdapter<ImageStatusObject, RecyclerView.ViewHolder> {

    private static final int TYPE_MEDIA = 1;
    private DownloadHelper downloadHelper;

    protected ChatMediaPagedListAdapter(@NonNull DiffUtil.ItemCallback<ImageStatusObject> diffCallback, DownloadHelper downloadHelper) {
        super(diffCallback);
        this.downloadHelper = downloadHelper;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_media_item, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_MEDIA;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ImageStatusObject img = getItem(position);
        MediaViewHolder hold = (MediaViewHolder) holder;

        ConstraintSet set = new ConstraintSet();
        set.clone(hold.layout);

        assert img != null;
        if(img.isSender()){
            set.clear(hold.originalImage.getId(), ConstraintSet.START);
        }
        else{
            set.clear(hold.originalImage.getId(), ConstraintSet.END);
        }

        set.applyTo(hold.layout);

        // Original image is set in invisible
        if(!img.getVideo() && img.isSender()){
            Bitmap bmp = BitmapFactory.decodeFile(img.getImageURL());
            hold.originalImage.setImageBitmap(bmp);
        }

        if (img.getState() == ImageStatusObject.UPLOAD_PROCESS){

            hold.thumbnailImage.setImageBitmap(setThumbnail(img.getThumbnailURL()));

            // Uploading
            hold.uploadProgress.setVisibility(View.VISIBLE);
            hold.retryButton.setVisibility(View.GONE);
        }

        if(img.getState() == ImageStatusObject.DOWNLOAD_NOT_STARTED){
            hold.thumbnailImage.setImageBitmap(setThumbnail(img.getThumbnailURL()));
            hold.uploadProgress.setVisibility(View.GONE);
            hold.retryButton.setImageResource(R.drawable.ic_download_foreground);
            hold.retryButton.setVisibility(View.VISIBLE);

            hold.retryButton.setOnClickListener(v -> downloadHelper.enqueue(img));
        }

        if(img.getState() == ImageStatusObject.DOWNLOAD_PROCESS){

            hold.uploadProgress.setVisibility(View.VISIBLE);
            hold.retryButton.setVisibility(View.GONE);

        }

        if(img.getState() == ImageStatusObject.DOWNLOAD_RETRY || img.getState() == ImageStatusObject.UPLOAD_RETRY){

            hold.retryButton.setImageResource(R.drawable.ic_retry_foreground);
            hold.retryButton.setVisibility(View.VISIBLE);
            hold.uploadProgress.setVisibility(View.GONE);

        }

        if(img.getState() == ImageStatusObject.UPLOAD_DONE || img.getState() == ImageStatusObject.DOWNLOAD_DONE){

            hold.uploadProgress.setVisibility(View.GONE);
            hold.retryButton.setVisibility(View.GONE);

            if(img.getVideo()){
                hold.retryButton.setImageResource(R.drawable.ic_play_foreground);
                hold.retryButton.setVisibility(View.VISIBLE);
            }
            else{
                hold.thumbnailImage.setVisibility(View.GONE);
                hold.originalImage.setVisibility(View.VISIBLE);
            }

            // Intent
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

    public static class MediaViewHolder extends RecyclerView.ViewHolder{

        ImageView thumbnailImage;
        ImageView retryButton;
        ProgressBar uploadProgress;
        ImageView originalImage;
        ConstraintLayout layout;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbnailImage = itemView.findViewById(R.id.thumbnail_image_media);
            retryButton = itemView.findViewById(R.id.retry_button);
            uploadProgress = itemView.findViewById(R.id.upload_progress);
            originalImage = itemView.findViewById(R.id.background_image);
            layout = itemView.findViewById(R.id.layout_item);

        }
    }
}
