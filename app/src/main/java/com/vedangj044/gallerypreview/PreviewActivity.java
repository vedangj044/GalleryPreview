package com.vedangj044.gallerypreview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class PreviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView TimePeriod, SelectedItem;
    private List<String> selectedItemList;
    private ImageView SelectionDone;

    private List<String> pathList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_activty);

        recyclerView = findViewById(R.id.gallery_recycle1);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        TimePeriod = findViewById(R.id.text_time_period1);

        SelectionDone = findViewById(R.id.selection_done1);

        SelectedItem = findViewById(R.id.selected_item_count1);

        LinearLayout linearLayout = findViewById(R.id.linear1234);
        linearLayout.setVisibility(View.VISIBLE);

        pathList = getIntent().getStringArrayListExtra("pathList");
        Boolean isVideo = getIntent().getExtras().getBoolean("isVideo", false);

        PreviewAdapter pr = new PreviewAdapter(pathList, this, isVideo);

        pr.setOnNotifyImageData(new PreviewAdapter.notifyImageDate() {
            @Override
            public void sendSignal(String text) {
                TimePeriod.setText(text);
            }

            @Override
            public void sendSelectedItem(List<String> s) {
                selectedItemList = s;

                SelectedItem.setText(String.valueOf(s.size()));
                if(s.size() > 0){
                    SelectionDone.setVisibility(View.VISIBLE);
                }
                else{
                    SelectedItem.setText("");
                    SelectionDone.setVisibility(View.GONE);
                }
            }
        });


        SelectionDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PreviewActivity.this, SlideImage.class);
                intent.putStringArrayListExtra("selectedPath", (ArrayList<String>) selectedItemList);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(pr);

    }
}