package com.vedangj044.gallerypreview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.CancellationSignal;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Base64;
import android.util.Log;
import android.util.Size;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SlideImageHelper {

    Context context;

    public SlideImageHelper(Context context) {
        this.context = context;
    }

    // Returns thumbnail of video in Base64 string
    public String getVideoThumbnail(String uri) {

        Size mSize = new Size(96,96);
        CancellationSignal ca = new CancellationSignal();
        Bitmap bitmapThumbnail = null;

        // API 29 has content resolver method for thumbnail generation
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                bitmapThumbnail = ThumbnailUtils.createVideoThumbnail(new File(uri), mSize, ca);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            bitmapThumbnail = ThumbnailUtils.createVideoThumbnail(uri, MediaStore.Video.Thumbnails.MICRO_KIND);
        }

        // Thumbnail is converted to base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmapThumbnail.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64Thumbnail = Base64.encodeToString(byteArray, Base64.DEFAULT);

        Log.v("thum", base64Thumbnail);
        return base64Thumbnail;
    }

    // returns compressed/scaled bitmap
    public Bitmap getCompressedBitmap(Bitmap bmp){
        // compression of image happens here
        int nh = (int) ( bmp.getHeight() * (512.0 / bmp.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(bmp, 512, nh, true);

        return scaled;
    }

    // Generates the thumbnail for the bitmap
    public Bitmap getThumbnailBitmap(Bitmap bm) {

        // Scale by which image should be reduced
        int reduction = 100;

        int width = bm.getWidth();
        int height = bm.getHeight();

        int newWidth = width/reduction;
        int newHeight = height/reduction;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);

        // Blur the scaled down image
        RenderScript rs = RenderScript.create(context);

        final Allocation input = Allocation.createFromBitmap(rs, resizedBitmap); //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(8f);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(resizedBitmap);

        return resizedBitmap;
    }

}
