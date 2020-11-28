package com.vedangj044.gallerypreview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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


public class ChatActivity extends AppCompatActivity {

    public static String getBasicAuthenticationString() {
        String basicAuth = "";
        String baseData = basicAuthUsername + ":" + basicAuthPassword;
        basicAuth = "Basic " + Base64.encodeToString(baseData.getBytes(), Base64.NO_WRAP);
        return basicAuth;
    }

    // Basic authentication UserName and password
    public static final String basicAuthUsername = "admin";
    public static final String basicAuthPassword = "password";

    private RecyclerView chatRecycle;
    private ChatAdapter adapter;
    private ChatMediaPagedListAdapter pagedListAdapter;
    private int groupID;
    private int runningUpload = 0;

    private Retrofit retrofit;
    private MediaFileUploadRequest mediaFileUploadRequest;

    private ExecutorService executor = SlideImage.ExecutorHelper.getInstanceExecutor();
    private MediaUploadDatabase mediaUploadDatabase;

    private ChatMediaViewModel viewModel;

    private DownloadHelper downloadHelper;

    private String url;

    public interface MediaFileUploadRequest{

        @Multipart
        @POST("freepbx/fileUpload")
        Call<FileUploadResponseBean> uploadFileToServerApi(@Header("Authorization") String authHeader, @PartMap Map<String, RequestBody> partMap, @Part List<MultipartBody.Part> files);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatRecycle = findViewById(R.id.chat_recycle);

        LinearLayoutManager lp = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        chatRecycle.setLayoutManager(lp);

        downloadHelper = new DownloadHelper(this);



        adapter = new ChatAdapter();
//        chatRecycle.setAdapter(adapter);

        groupID = getIntent().getIntExtra("groupID", 0);

        mediaUploadDatabase = MediaUploadDatabase.getInstance(this);


        retrofit = new Retrofit.Builder()
                .baseUrl("https://voip.vortexvt.com:8082/ItHub/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mediaFileUploadRequest = retrofit.create(MediaFileUploadRequest.class);

        final ImageStatusObject[] img1 = {null};
        Future<Void> task = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<ImageStatusObject> lo = mediaUploadDatabase.mediaUploadDAO().getMediaByGroup(groupID);
//                for (ImageStatusObject img: lo){
//                    uploadFile(img);
//                }
                img1[0] = lo.get(0);
                return null;
            }
        });



        viewModel = new ViewModelProvider(this).get(ChatMediaViewModel.class);
        pagedListAdapter = new ChatMediaPagedListAdapter(new ImageStatusObjectDiffCallback(), downloadHelper);
        chatRecycle.setAdapter(pagedListAdapter);

        viewModel.getImageStatusObjectPaged().observe(this, new Observer<PagedList<ImageStatusObject>>() {
            @Override
            public void onChanged(PagedList<ImageStatusObject> imageStatusObjects) {
                pagedListAdapter.submitList(imageStatusObjects);
            }
        });

        pagedListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if(positionStart == 0){
                    lp.scrollToPosition(0);
                }
            }
        });


        CountDownTimer cnt = new CountDownTimer(10000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                Future<Void> task = executor.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        img1[0].setId(img1[0].getId()+1);
                        img1[0].setState(ImageStatusObject.DOWNLOAD_NOT_STARTED);
                        img1[0].setSender(false);
                        img1[0].setImageURL("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4");
                        img1[0].setVideo(true);
                        img1[0].setFileName("videoOne.mp4");
                        mediaUploadDatabase.mediaUploadDAO().insetImageStatusObject(img1[0]);
                        return null;
                    }
                });
            }
        };
        cnt.start();

    }

    public void uploadFile(ImageStatusObject imageStatusObject) {


        try {
            String filePath = imageStatusObject.getImageURL();

            List<MultipartBody.Part> parts = new ArrayList<>();
            MultipartBody.Part imagenPerfil = null;

            if (filePath != null) {
                File file = new File(filePath);
                String extension = filePath.substring(filePath.lastIndexOf(".")).replace(".", "");
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                RequestBody requestFile = null;
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

                    url = response.body().getData().get(0);
                    imageStatusObject.setState(ImageStatusObject.UPLOAD_DONE);
                    updateStatus(imageStatusObject);

                }

                @Override
                public void onFailure(Call<FileUploadResponseBean> call, Throwable t) {

                    Log.v("err", t.getMessage());

                    imageStatusObject.setState(ImageStatusObject.UPLOAD_RETRY);
                    updateStatus(imageStatusObject);
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

    private void updateStatus(ImageStatusObject img){
        Future<Void> task = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mediaUploadDatabase.mediaUploadDAO().update(img);
                return null;
            }
        });

    }

    @Override
    protected void onDestroy() {
        downloadHelper.unregister();
        super.onDestroy();
    }
}