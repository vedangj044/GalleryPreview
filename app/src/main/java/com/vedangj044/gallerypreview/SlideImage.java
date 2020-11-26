package com.vedangj044.gallerypreview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SlideImage extends AppCompatActivity {

    // List received from intent
    private List<String> arg;

    // List created using arg list
    private List<MediaPreview> mediaPreviewsList;

    // UI Elements
    private ViewPager2 slideViewPager;
    private ImageButton delete_button;
    private ImageButton rotate_button;
    private FloatingActionButton send_button;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    // Adapter for preview images
    StatusPreviewAdapter statusPreviewAdapter;
    StatusIconPreviewAdapter statusIconPreviewAdapter;

    // Helper class
    SlideImageHelper slideImageHelper;

    private int groupID;

    private ExecutorService executor = ExecutorHelper.getInstanceExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_image);

        // unique group id for each instance
        groupID = genGroupID();

        // database instance
        MediaUploadDatabase mediaUploadDatabase = MediaUploadDatabase.getInstance(this);

        mediaPreviewsList = new ArrayList<>();

        // Intent is received from GalleyViewFragment
        arg = getIntent().getStringArrayListExtra("selectedPath");

        // MediaPreviewList is populated and the index is assigned as ID to each
        for(String path: arg){
            mediaPreviewsList.add(new MediaPreview(arg.indexOf(path), path));
        }

        // 0th item is selected initially
        mediaPreviewsList.get(0).setActive(true);

        // Bottom selector
        recyclerView = findViewById(R.id.recycle_slide);
        LinearLayoutManager lp = new LinearLayoutManager(this);
        lp.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(lp);

        // When only one status is present bottom selector is not visible
        if(mediaPreviewsList.size() == 1){
            recyclerView.setVisibility(View.GONE);
        }

        // view pager
        slideViewPager = findViewById(R.id.slide_view_page1);

        statusPreviewAdapter = new StatusPreviewAdapter();
        slideViewPager.setAdapter(statusPreviewAdapter);

        statusIconPreviewAdapter = new StatusIconPreviewAdapter(this);
        recyclerView.setAdapter(statusIconPreviewAdapter);

        slideViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {

                for(MediaPreview mp: mediaPreviewsList){
                    mp.setActive(false);
                }

                mediaPreviewsList.get(position).setActive(true);
                statusIconPreviewAdapter.notifyDataSetChanged();
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        delete_button = findViewById(R.id.delete_button);
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Case 1 when list has only one item
                // Case 2 when list has multiple item
                if(mediaPreviewsList.size() == 1){
                    finish();
                }
                else{
                    mediaPreviewsList.remove(slideViewPager.getCurrentItem());
                    if(slideViewPager.getCurrentItem() >= mediaPreviewsList.size()){
                        mediaPreviewsList.get(0).setActive(true);
                    }
                    else{
                        mediaPreviewsList.get(slideViewPager.getCurrentItem()).setActive(true);
                    }
                    statusPreviewAdapter.notifyDataSetChanged();
                    statusIconPreviewAdapter.notifyDataSetChanged();
                }

            }
        });

        rotate_button = findViewById(R.id.rotation_button);
        rotate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // View is updated
                MediaPreview m1 = mediaPreviewsList.get(slideViewPager.getCurrentItem());
                if(!m1.isVideo()){
                    CropImage.activity(Uri.fromFile(new File(m1.getPath()))).start(SlideImage.this);
                }
            }
        });

        slideImageHelper = new SlideImageHelper(this);

        send_button = findViewById(R.id.send_button);
        progressBar = findViewById(R.id.progressbar);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // FINAL LIST containing all the url required to sent to server
                List<ImageStatusObject> compressedPath = new ArrayList<>();

                // Iteration to mediaPreviewList
                for(MediaPreview m1: mediaPreviewsList){

                    if(m1.isVideo()){
                        progressBar.setVisibility(View.VISIBLE);
                        // When video trimming is completed this listener is called

                        final String outputVideoFileDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                        final File outputVideoFileDirUri = new File(outputVideoFileDir);
                        final Uri trimmedVideoURL = Uri.parse(m1.getPath());

                        final String thumbnail = slideImageHelper.getVideoThumbnail(m1.getPath());

                        // thread handles compression of video
                        Thread thread = new Thread(){
                            @Override
                            public void run() {

                                // Handles rotation of video
                                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                retriever.setDataSource(SlideImage.this, trimmedVideoURL);

                                int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                                int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                                int rotation = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));

                                if(rotation != 0){
                                    width = 0;
                                    height = 0;
                                }

                                boolean isConverted = VideoConverter.getInstance().convertVideo(SlideImage.this,
                                        trimmedVideoURL,
                                        outputVideoFileDirUri, width, height, 0);

                                if (isConverted) {

                                    File name = new File(VideoConverter.cachedFile.getAbsolutePath());

                                    // when compression is complete the compressPath list is updated
                                    ImageStatusObject img1 = new ImageStatusObject(thumbnail, VideoConverter.cachedFile.getPath(), true, name.getName(), groupID, true);
                                    mediaUploadDatabase.mediaUploadDAO().insetImageStatusObject(img1);
                                    compressedPath.add(img1);

                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                                }
                                else{
                                    Log.v("hey", "converting");
                                }
                            }
                        };
                        thread.start();

                        // If video is of less than 2 seconds duration it is directly copied
                        // otherwise trim
                    }
                    else{

                        // Creating compressed bitmap
                        Bitmap compressedBitmap = slideImageHelper.getCompressedBitmap(BitmapFactory.decodeFile(m1.getPath()));

                        // Saving compressed bitmap
                        File file = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                        File output = new File(file, m1.getFileName());
                        try (FileOutputStream out = new FileOutputStream(output)){
                            compressedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }

                        // Creating Thumbnail bitmap
                        Bitmap thumbnail = slideImageHelper.getThumbnailBitmap(compressedBitmap);

                        // Saving Thumbnail bitmap
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        thumbnail.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        String base64Thumbnail = Base64.encodeToString(byteArray, Base64.DEFAULT);

                        // Creating image status object and adding to list
                        ImageStatusObject img1 = new ImageStatusObject(base64Thumbnail, output.getAbsolutePath(), false, m1.getFileName(), groupID, true);

                        Future<Void> task =  executor.submit(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                mediaUploadDatabase.mediaUploadDAO().insetImageStatusObject(img1);
                                return null;
                            }
                        });

                        try {
                            task.get();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                        compressedPath.add(img1);

                    }

                }
            }
        });

        mediaUploadDatabase.mediaUploadDAO().getCountByGroupID(groupID).observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer == mediaPreviewsList.size()){
                    Intent intent = new Intent(SlideImage.this, ChatActivity.class);
                    intent.putExtra("groupID", groupID);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public int genGroupID(){
        Random r = new Random(System.currentTimeMillis());
        return 10000 + r.nextInt(20000);
    }

    public static class ExecutorHelper{

        private static ExecutorService instanceExecutor;

        public static synchronized ExecutorService getInstanceExecutor(){
            if(instanceExecutor == null){
                instanceExecutor = Executors.newSingleThreadExecutor();
            }
            return instanceExecutor;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Triggers when cropped image is sent back to the activity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                // change the image to the cropped image
                MediaPreview m1 = mediaPreviewsList.get(slideViewPager.getCurrentItem());
                m1.setPath(resultUri.getPath());
                statusPreviewAdapter.notifyDataSetChanged();
                statusIconPreviewAdapter.notifyDataSetChanged();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public class StatusIconPreviewAdapter extends RecyclerView.Adapter<StatusIconPreviewAdapter.ViewHolder> {

        private Context context;

        public StatusIconPreviewAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.small_image_icon, parent, false);
            return new ViewHolder(inflater);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            if(mediaPreviewsList.get(position).getActive()){
                holder.imageView.setBackgroundResource(R.drawable.item_selected);
            }
            else{
                holder.imageView.setBackgroundResource(0);
            }

            Glide.with(holder.context).load(mediaPreviewsList.get(position).getPath()).into(holder.imageView);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    slideViewPager.setCurrentItem(position, true);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mediaPreviewsList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            ImageView imageView;
            Context context;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.slide_icon_image);
                context = itemView.getContext();
            }
        }
    }

    public class StatusPreviewAdapter extends RecyclerView.Adapter<StatusPreviewAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_video_fragment, parent, false);
            return new ViewHolder(inflater);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            MediaPreview mp = mediaPreviewsList.get(position);
            if(mp.isVideo()){
                holder.videoView.setVideoPath(mp.getPath());
                rotate_button.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.VISIBLE);

                holder.videoView.seekTo(100);

                holder.playPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.videoView.start();
                        holder.playPause.setVisibility(View.GONE);
                    }
                });

                holder.videoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(holder.videoView.isPlaying()){
                            holder.videoView.pause();
                            holder.playPause.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }
            else{
                Bitmap bmp = BitmapFactory.decodeFile(mediaPreviewsList.get(position).getPath());
                holder.imageView.setImageBitmap(bmp);
                rotate_button.setVisibility(View.VISIBLE);
                holder.videoView.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onViewAttachedToWindow(@NonNull ViewHolder holder) {

            if(holder.videoView.getVisibility() == View.VISIBLE){
                holder.videoView.seekTo(100);
                holder.playPause.setVisibility(View.VISIBLE);
                holder.playPause.setImageResource(R.drawable.ic_play_foreground);
            }
            else{
                holder.playPause.setVisibility(View.GONE);
            }

            super.onViewAttachedToWindow(holder);
        }



        @Override
        public int getItemCount() {
            return mediaPreviewsList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            ImageView imageView;
            VideoView videoView;
            ImageView playPause;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_status);
                videoView = itemView.findViewById(R.id.video_status);
                playPause = itemView.findViewById(R.id.play_pause);
            }
        }

    }
}