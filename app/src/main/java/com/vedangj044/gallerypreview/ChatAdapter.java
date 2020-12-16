package com.vedangj044.gallerypreview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public List<ImageStatusObject> mDataset = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_media_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

//        ImageStatusObject img = mDataset.get(position);
//
//        // Original image is set in invisible
//        if(!img.getVideo()){
//            Bitmap bmp = BitmapFactory.decodeFile(img.getImageURL());
//            holder.originalImage.setImageBitmap(bmp);
//        }
//
//        byte[] decodedString = Base64.decode(img.getThumbnailURL(), Base64.DEFAULT);
//        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//        holder.thumbnailImage.setImageBitmap(decodedByte);
//
//
//        if(img.getStatusUpload() == 0){
//
//            // Uploading
//            holder.uploadProgress.setVisibility(View.VISIBLE);
//            holder.retryButton.setVisibility(View.GONE);
//        }
//        else if (img.getStatusUpload() == -1){
//
//            // retry
//            holder.retryButton.setVisibility(View.VISIBLE);
//            holder.uploadProgress.setVisibility(View.GONE);
//        }
//        else if(img.getStatusUpload() == 1){
//
//            // uploaded
//            holder.uploadProgress.setVisibility(View.GONE);
//            holder.retryButton.setVisibility(View.GONE);
//
//            if(img.getVideo()){
//                holder.retryButton.setImageResource(R.drawable.ic_play_foreground);
//                holder.retryButton.setVisibility(View.VISIBLE);
//            }
//            else{
//                holder.thumbnailImage.setVisibility(View.GONE);
//                holder.originalImage.setVisibility(View.VISIBLE);
//            }
//
//            // Intent
//
//
//        }

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView thumbnailImage;
        ImageView retryButton;
        ProgressBar uploadProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbnailImage = itemView.findViewById(R.id.thumbnail_image_media);
            retryButton = itemView.findViewById(R.id.retry_button);
            uploadProgress = itemView.findViewById(R.id.upload_progress);

        }
    }

}
