package com.vedangj044.gallerypreview;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

public class VideoPlayerActivity extends AppCompatActivity {

    private static String VIDEO_SAMPLE = null;
    private VideoView mVideoView;
    private int mCurrentPosition = 0;

    private static final String PLAYBACK_TIME = "play_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        mVideoView = findViewById(R.id.videoview);

        VIDEO_SAMPLE = getIntent().getStringExtra("path");

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(PLAYBACK_TIME);
        }

        MediaController controller = new MediaController(this);
        controller.setMediaPlayer(mVideoView);
        mVideoView.setMediaController(controller);

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mVideoView.seekTo(1);
            }
        });
    }

    private void initializePlayer() {
        if(VIDEO_SAMPLE != null){
            Uri videoUri = Uri.parse(VIDEO_SAMPLE);
            mVideoView.setVideoURI(videoUri);

            if (mCurrentPosition > 0) {
                mVideoView.seekTo(mCurrentPosition);
            } else {
                // Skipping to 1 shows the first frame of the video.
                mVideoView.seekTo(1);
            }

            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mVideoView.start();
                }
            });
        }
    }

    private void releasePlayer() {
        mVideoView.stopPlayback();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PLAYBACK_TIME, mVideoView.getCurrentPosition());
    }
}