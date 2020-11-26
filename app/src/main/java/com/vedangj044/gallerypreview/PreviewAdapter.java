package com.vedangj044.gallerypreview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ViewHolder> {

    private List<String> mDataset = new ArrayList<>();
    private Context mContext;

    private static int CACHE_LIMIT = 100;
    private Boolean isVideos = false;

    // Array holds the state of all images either selected or not selected
    private Boolean[] selectedState;

    // URI of all the selected images or videos
    private List<String> selectionPath;

    // Maximum number of media that can be selected at once
    private final int MaxSelectionLimit = 5;


    public PreviewAdapter(List<String> mDataset, Context mContext, Boolean isVideo) {
        this.mDataset = mDataset;
        this.mContext = mContext;
        this.isVideos = isVideo;

        this.selectedState = new Boolean[mDataset.size()];
        Arrays.fill(selectedState, false);

        this.selectionPath = new ArrayList<>();
    }

    private notifyImageDate listener;

    // set listened to this instance
    public void setOnNotifyImageData(notifyImageDate listener){
        this.listener = listener;
    }


    // This listener notifies the GalleryViewFragment of
    // The month of the current image displayed in recyclerView
    // The List of selected images
    public interface notifyImageDate{
        void sendSignal(String text);
        void sendSelectedItem(List<String> s);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_card_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        String timePeriod = calculateTimePeriod(mDataset.get(position));
        listener.sendSignal(timePeriod);

        if(isVideos){
            holder.videoIcon.setVisibility(View.VISIBLE);
        }
        else{
            holder.videoIcon.setVisibility(View.GONE);
        }


        if(position > CACHE_LIMIT){
            Glide.with(mContext).load(mDataset.get(position))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.imageView);
        }
        else{
            Glide.with(mContext).load(mDataset.get(position)).into(holder.imageView);
        }

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // When any media is already selected then others can be selected just by clicking
                // or long press
                if(selectionPath.size() > 0){
                    setState(position, holder.isSelected);
                }
                else {
                    // When no media is selected then path of the image is sent to the UploadActivity
                    Intent intent = new Intent(mContext, SlideImage.class);
                    List<String> itemList = new ArrayList<>();
                    itemList.add(mDataset.get(position));
                    intent.putStringArrayListExtra("selectedPath", (ArrayList<String>) itemList);
                    mContext.startActivity(intent);
                }

            }
        });

        // LongClick to select media
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return setState(position, holder.isSelected);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView, isSelected, videoIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.thumbnail_image);
            isSelected = itemView.findViewById(R.id.selected_tick);
            videoIcon = itemView.findViewById(R.id.video_icon);
        }
    }


    // Function changes the styling according to the state (selected or not selected) of the ImageView
    private boolean setState(int position, ImageView isSelected){

        // When this position is not selected in the selectedState Array
        if(!selectedState[position]){

            // Number of selections is more than max
            if(selectionPath.size() == MaxSelectionLimit){
                Toast.makeText(mContext, "Can't select more than 5", Toast.LENGTH_SHORT).show();
                return true;
            }

            // path of the current Image is added to the List
            selectionPath.add(mDataset.get(position));

            // A tick is displayed over the image
            isSelected.setImageResource(R.drawable.ic_tick_foreground);

            // opacity is set to 60%
            isSelected.setBackgroundColor(Color.parseColor("#99000000"));
        }
        else{

            // path is removed from the selectionPath array
            selectionPath.remove(mDataset.get(position));

            // Styling is restored
            isSelected.setImageResource(0);
            isSelected.setBackgroundResource(0);

        }

        // The GalleryViewFragment is notified of all the changes in selectionPath array
        listener.sendSelectedItem(selectionPath);

        // state is toggled in the selectedState Array
        selectedState[position] = !selectedState[position];
        return true;
    }

    // Calculate the Month of capture of the Media from its path
    private String calculateTimePeriod(String arrPass) {

        Calendar cal = Calendar.getInstance();

        File s = new File(arrPass);
        cal.setTimeInMillis(s.lastModified());
        return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
    }
}
