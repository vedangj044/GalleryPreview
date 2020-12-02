package com.vedangj044.gallerypreview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.http.POST;


public class ChatMediaPagedListAdapter extends PagedListAdapter<ImageStatusObject, RecyclerView.ViewHolder> {

    private static final int TYPE_MEDIA_SENDER = 1;
    private static final int TYPE_MEDIA_RECEIVER = 2;
    private DownloadHelper downloadHelper;
    private UploadHelper uploadHelper;
    private Context context;

    protected ChatMediaPagedListAdapter(@NonNull DiffUtil.ItemCallback<ImageStatusObject> diffCallback, DownloadHelper downloadHelper, UploadHelper uploadHelper, Context context) {
        super(diffCallback);
        this.downloadHelper = downloadHelper;
        this.uploadHelper = uploadHelper;
        this.context = context;
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
        String dataHeader = isDateHeaderVisible(position);

        if(dataHeader != null){
            listener.change(dataHeader);
        }

        if(getItemViewType(position) == TYPE_MEDIA_RECEIVER){

            MediaViewHolderReceiver hold = (MediaViewHolderReceiver) holder;

            if(dataHeader != null){
                hold.dateTextView.setText(dataHeader);
                hold.dateTextView.setVisibility(View.VISIBLE);
            }
            else{
                hold.dateTextView.setVisibility(View.GONE);
            }

            switch (img.getState()){

                case ImageStatusObject.DOWNLOAD_NOT_STARTED:
                    Log.v("callled", "claslls1111");

                    hold.thumbnailImage.setImageBitmap(setThumbnail(img.getThumbnailURL()));
                    hold.retryButton.setImageResource(R.drawable.ic_download_foreground);
                    hold.retryButton.setOnClickListener(v -> {
                        downloadHelper.enqueue(img);
                        hold.retryButton.setVisibility(View.GONE);
                        hold.uploadProgress.setVisibility(View.VISIBLE);
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
                        hold.retryButton.setOnClickListener(v -> startPlayback(img.getImageURL()));
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
            if(dataHeader != null){
                hold.dateTextView.setText(dataHeader);
                hold.dateTextView.setVisibility(View.VISIBLE);
            }
            else {
                hold.dateTextView.setVisibility(View.GONE);
            }

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
                        hold.retryButton.setOnClickListener(v -> startPlayback(img.getImageURL()));
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


    private String isDateHeaderVisible(int position){

        boolean isDateHeaderVisible = false;

        Calendar dateNow = Calendar.getInstance();
        dateNow.setTime(new Date(getItem(position).getTimeStamp()));

        Calendar dateNext = Calendar.getInstance();
        if(position == getItemCount() - 1){
            isDateHeaderVisible = true;
        }
        else{
            dateNext.setTime(new Date(getItem(position + 1).getTimeStamp()));
            if(dateNow.get(Calendar.YEAR) != dateNext.get(Calendar.YEAR) ||
                    dateNow.get(Calendar.MONTH) != dateNext.get(Calendar.MONTH) ||
                    dateNow.get(Calendar.DATE) != dateNext.get(Calendar.DATE)){
                isDateHeaderVisible = true;
            }
        }

        if(isDateHeaderVisible){
            String date = dateNow.get(Calendar.DATE) +"/"+ dateNow.get(Calendar.MONTH)+ "/" + dateNow.get(Calendar.YEAR);

            if(isToday(date)){
                return "Today";
            }
            else if(isYesterday(date)){
                return "Yesterday";
            }
            else{
                return date;
            }
        }
        return null;
    }

    private boolean isToday(String date){
        Calendar today = Calendar.getInstance();
        today.setTime(new Date(System.currentTimeMillis()));

        String var = today.get(Calendar.DATE) + "/" + today.get(Calendar.MONTH) + "/" + today.get(Calendar.YEAR);
        return var.equals(date);
    }

    private boolean isYesterday(String date){
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(new Date(System.currentTimeMillis()));
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        String var = yesterday.get(Calendar.DATE) + "/" + yesterday.get(Calendar.MONTH) + "/" + yesterday.get(Calendar.YEAR);
        return var.equals(date);
    }

    public interface ChangeDateListener{
        void change(String date);
    }

    private ChangeDateListener listener;

    public void setChangeDateListener(ChangeDateListener listener){
        this.listener = listener;
    }

    private void startPlayback(String path){
        Intent intent = new Intent(context.getApplicationContext(), VideoPlayerActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
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
        TextView dateTextView;

        public MediaViewHolderReceiver(@NonNull View itemView) {
            super(itemView);

            thumbnailImage = itemView.findViewById(R.id.thumbnail_image_media);
            retryButton = itemView.findViewById(R.id.retry_button);
            uploadProgress = itemView.findViewById(R.id.upload_progress);
            originalImage = itemView.findViewById(R.id.background_image);
            layout = itemView.findViewById(R.id.layout_item);
            dateTextView = itemView.findViewById(R.id.date_text_view);
        }
    }

    public static class MediaViewHolderSender extends RecyclerView.ViewHolder{

        ImageView thumbnailImage;
        ImageView retryButton;
        ProgressBar uploadProgress;
        ImageView originalImage;
        ConstraintLayout layout;
        TextView dateTextView;

        public MediaViewHolderSender(@NonNull View itemView) {
            super(itemView);

            thumbnailImage = itemView.findViewById(R.id.thumbnail_image_media);
            retryButton = itemView.findViewById(R.id.retry_button);
            uploadProgress = itemView.findViewById(R.id.upload_progress);
            originalImage = itemView.findViewById(R.id.background_image);
            layout = itemView.findViewById(R.id.layout_item);
            dateTextView = itemView.findViewById(R.id.date_text_view);

        }
    }
}
