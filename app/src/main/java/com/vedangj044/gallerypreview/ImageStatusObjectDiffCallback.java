package com.vedangj044.gallerypreview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class ImageStatusObjectDiffCallback extends DiffUtil.ItemCallback<ImageStatusObject> {

    @Override
    public boolean areItemsTheSame(@NonNull ImageStatusObject oldItem, @NonNull ImageStatusObject newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull ImageStatusObject oldItem, @NonNull ImageStatusObject newItem) {
        return oldItem.equals(newItem);
    }
}
