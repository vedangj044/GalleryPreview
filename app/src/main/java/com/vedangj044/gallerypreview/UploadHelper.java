package com.vedangj044.gallerypreview;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public class UploadHelper {

    private final ChatMediaDaoMiddleware chatMediaDaoMiddleware;
    private MediaFileUploadRequest mediaFileUploadRequest;
    private final Context context;

    public static String getBasicAuthenticationString() {
        String basicAuth;
        String baseData = basicAuthUsername + ":" + basicAuthPassword;
        basicAuth = "Basic " + Base64.encodeToString(baseData.getBytes(), Base64.NO_WRAP);
        return basicAuth;
    }

    // Basic authentication UserName and password
    public static final String basicAuthUsername = "admin";
    public static final String basicAuthPassword = "password";

    public interface MediaFileUploadRequest{

        @Multipart
        @POST("freepbx/fileUpload")
        Call<FileUploadResponseBean> uploadFileToServerApi(@Header("Authorization") String authHeader, @PartMap Map<String, RequestBody> partMap, @Part List<MultipartBody.Part> files);

    }

    public UploadHelper(Context context) {
        this.chatMediaDaoMiddleware = ChatMediaDaoMiddleware.getInstance(context);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://voip.vortexvt.com:8082/ItHub/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mediaFileUploadRequest = retrofit.create(MediaFileUploadRequest.class);


        this.context = context;
    }

    public void enqueue(int groupID){
        try {
            List<ImageStatusObject> lo = chatMediaDaoMiddleware.getChatMediaByGroupID(groupID);
            for (ImageStatusObject img: lo){
                uploadFile(img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(ImageStatusObject imageStatusObject) {

        if(imageStatusObject.getState() == ImageStatusObject.UPLOAD_RETRY){
            imageStatusObject.setState(ImageStatusObject.UPLOAD_PROCESS);
            chatMediaDaoMiddleware.updateChatMedia(imageStatusObject);
        }

        try {
            String filePath = imageStatusObject.getImageURL();

            List<MultipartBody.Part> parts = new ArrayList<>();
            MultipartBody.Part imagenPerfil;

            if (filePath != null) {
                File file = new File(filePath);
                String extension = filePath.substring(filePath.lastIndexOf(".")).replace(".", "");
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                RequestBody requestFile;
                requestFile = RequestBody.create(MediaType.parse(mimeType), file);

                // MultipartBody.Part is used to send also the actual file name
                imagenPerfil = MultipartBody.Part.createFormData("files", file.getName(), requestFile);
                parts.add(imagenPerfil);
            }

            String ownerJid = "000000012507";
            String friendJid = "000000012506";

            HashMap<String, RequestBody> map = new HashMap<>();
            map.put("receiver", createPartFromString(friendJid));
            map.put("sender", createPartFromString(ownerJid));
            map.put("type", createPartFromString("1"));

            Call<FileUploadResponseBean> call = mediaFileUploadRequest.uploadFileToServerApi(getBasicAuthenticationString(), map, parts);
            call.enqueue(new Callback<FileUploadResponseBean>() {
                @Override
                public void onResponse(Call<FileUploadResponseBean> call, Response<FileUploadResponseBean> response) {
                    imageStatusObject.setState(ImageStatusObject.UPLOAD_DONE);
                    chatMediaDaoMiddleware.updateChatMedia(imageStatusObject);
                }

                @Override
                public void onFailure(Call<FileUploadResponseBean> call, Throwable t) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MultipartBody.FORM, descriptionString);
    }
}
