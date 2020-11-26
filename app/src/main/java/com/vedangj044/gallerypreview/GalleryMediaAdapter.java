package com.vedangj044.gallerypreview;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GalleryMediaAdapter extends RecyclerView.Adapter<GalleryMediaAdapter.ViewHolder> {

    private int MEDIA_TYPE = 0;
    private Context mContext;

    private static int CACHE_LIMIT = 100;

    private HashMap<String, List<String>> arrMap = new HashMap<>();
    private List<String> folderNameList = new ArrayList<>();

    public GalleryMediaAdapter(Context context, int MEDIA_TYPE) {
        this.MEDIA_TYPE = MEDIA_TYPE;
        this.mContext = context;
        populateMap();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;

        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_view_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final List<String> filesOfFolder = arrMap.get(folderNameList.get(position));

        holder.folderName.setText(folderNameList.get(position));
        holder.folderItemCount.setText(String.valueOf(filesOfFolder.size()));

        if(position > CACHE_LIMIT){
            Glide.with(mContext).load(filesOfFolder.get(0))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(1000, 1000)
                    .into(holder.thumbnailImage);
        }
        else{
            Glide.with(mContext).load(filesOfFolder.get(0))
                    .override(1000, 1000)
                    .into(holder.thumbnailImage);
        }

        holder.thumbnailImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext.getApplicationContext(), PreviewActivity.class);
                intent.putStringArrayListExtra("pathList", (ArrayList<String>)filesOfFolder);
                if(MEDIA_TYPE == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE){
                    intent.putExtra("isVideo", false);
                }
                else{
                    intent.putExtra("isVideo", true);
                }
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return folderNameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView thumbnailImage;
        private TextView folderName, folderItemCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbnailImage = itemView.findViewById(R.id.thumbnail_image);
            folderItemCount = itemView.findViewById(R.id.folder_item_count);
            folderName = itemView.findViewById(R.id.folder_name);
        }
    }

    private void populateMap(){

        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MEDIA_TYPE;

        CursorLoader cursorLoader = new CursorLoader(mContext, MediaStore.Files.getContentUri("external"),
                columns,
                selection,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC");

        Cursor cursor = cursorLoader.loadInBackground();
        int count = cursor.getCount();

        for(int i = 0; i < count; i++){
            cursor.moveToPosition(i);
            int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

            String filePath = cursor.getString(dataColumnIndex);

            List<String> folderList = new ArrayList<>();

            String name = "";
            for(int j = 1; j < filePath.length(); j++){
                if(filePath.charAt(j) == '/'){
                    folderList.add(name);
                    name = "";
                }
                else{
                    name += filePath.charAt(j);
                }
            }

            String folderName = folderList.get(folderList.size() - 1);

            if(arrMap.containsKey(folderName)){
                arrMap.get(folderName).add(filePath);
            }
            else{
                arrMap.put(folderName, new ArrayList<String>());
                arrMap.get(folderName).add(filePath);
                folderNameList.add(folderName);
            }

        }

    }
}
