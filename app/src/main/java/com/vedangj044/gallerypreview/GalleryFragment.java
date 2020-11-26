package com.vedangj044.gallerypreview;

import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView TimePeriod, SelectedItem;
    private List<String> selectedItemList;
    private ImageView SelectionDone;

    private int MEDIA_TYPE = 0;

    public GalleryFragment(int mediaType) {
        this.MEDIA_TYPE = mediaType;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_fragment, container, false);

        recyclerView = view.findViewById(R.id.gallery_recycle);
        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 2));

        TimePeriod = view.findViewById(R.id.text_time_period);

        SelectionDone = view.findViewById(R.id.selection_done);

        SelectedItem = view.findViewById(R.id.selected_item_count);

        GalleryMediaAdapter grp = new GalleryMediaAdapter(view.getContext(), MEDIA_TYPE);
        recyclerView.setAdapter(grp);
        
        return view;
    }
}
