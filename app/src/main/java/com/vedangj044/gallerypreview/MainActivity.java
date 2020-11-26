package com.vedangj044.gallerypreview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

    private TextView pictures;
    private TextView videos;
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private static final int NUM_PAGES = 2;

    int isActive = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pictures = findViewById(R.id.pictures_view);
        videos = findViewById(R.id.videos_view);
        viewPager = findViewById(R.id.view_pager);

        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(isActive != position) {
                    toggle();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        pictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isActive == 1){
                    toggle();
                    viewPager.setCurrentItem(0, true);
                }
            }
        });

        videos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isActive == 0){
                    toggle();
                    viewPager.setCurrentItem(1, true);
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(viewPager.getCurrentItem() == 0){
            super.onBackPressed();
        }
        else{
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter{

        public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if(position == 0){
                return new GalleryFragment(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
            }
            else{
                return new GalleryFragment(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    void toggle(){

        if(isActive == 0){
            isActive = 1;
            pictures.setBackgroundResource(R.drawable.bottom_border_unselected);
            videos.setBackgroundResource(R.drawable.bottom_border_selected);
        }
        else{
            isActive = 0;
            pictures.setBackgroundResource(R.drawable.bottom_border_selected);
            videos.setBackgroundResource(R.drawable.bottom_border_unselected);
        }

    }

}