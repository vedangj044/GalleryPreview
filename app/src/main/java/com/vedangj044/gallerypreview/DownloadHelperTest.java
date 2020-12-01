//package com.vedangj044.gallerypreview;
//
//import android.content.Context;
//import android.os.Environment;
//import android.util.Log;
//
//import com.downloader.OnDownloadListener;
//import com.downloader.OnStartOrResumeListener;
//import com.downloader.PRDownloader;
//import com.tonyodev.fetch2.Download;
//import com.tonyodev.fetch2.Error;
//import com.tonyodev.fetch2.Fetch;
//import com.tonyodev.fetch2.FetchConfiguration;
//import com.tonyodev.fetch2.FetchListener;
//import com.tonyodev.fetch2.Request;
//import com.tonyodev.fetch2core.DownloadBlock;
//
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.io.File;
//import java.util.List;
//
//public class DownloadHelperTest {
//
//    private Fetch fetch;
//    private ChatMediaDaoMiddleware chatMediaDaoMiddleware;
//    private String url;
//
//    public DownloadHelperTest(Context context) {
////        PRDownloader.initialize(context.getApplicationContext());
//
//        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(context)
//                .build();
//
//        fetch = Fetch.Impl.getInstance(fetchConfiguration);
//        chatMediaDaoMiddleware = ChatMediaDaoMiddleware.getInstance(context);
//        url = Environment.DIRECTORY_DOWNLOADS;
//
//    }
//
//    private String getPath(boolean isVideo){
//        String path = "/ithubImages/";
//
//        if(isVideo){
//            path =  "/ithubVideos/";
//        }
//        Log.v("spATH", url+path);
//        return url + path;
//    }
//
//
//    public void enqueue(ImageStatusObject im){
//        final Request request = new Request(im.getImageURL(), getPath(im.getVideo()) + im.getFileName());
//
//        fetch.enqueue(request, u -> {
//            im.setState(ImageStatusObject.DOWNLOAD_PROCESS);
//            updateStatus(im);
//        }, error -> {
//            im.setState(ImageStatusObject.DOWNLOAD_RETRY);
//            updateStatus(im);
//        });
//
//        FetchListener fetchListener = new FetchListener() {
//            @Override
//            public void onAdded(@NotNull Download download) {
//
//            }
//
//            @Override
//            public void onQueued(@NotNull Download download, boolean b) {
//
//            }
//
//            @Override
//            public void onWaitingNetwork(@NotNull Download download) {
//
//            }
//
//            @Override
//            public void onCompleted(@NotNull Download download) {
//                im.setState(ImageStatusObject.DOWNLOAD_DONE);
//                updateStatus(im);
//            }
//
//            @Override
//            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
//                Log.v("error", error.name()+"nn");
//            }
//
//            @Override
//            public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {
//
//            }
//
//            @Override
//            public void onStarted(@NotNull Download download, @NotNull List<? extends DownloadBlock> list, int i) {
//
//            }
//
//            @Override
//            public void onProgress(@NotNull Download download, long l, long l1) {
//
//            }
//
//            @Override
//            public void onPaused(@NotNull Download download) {
//
//            }
//
//            @Override
//            public void onResumed(@NotNull Download download) {
//
//            }
//
//            @Override
//            public void onCancelled(@NotNull Download download) {
//
//            }
//
//            @Override
//            public void onRemoved(@NotNull Download download) {
//
//            }
//
//            @Override
//            public void onDeleted(@NotNull Download download) {
//
//            }
//        };
//
//        fetch.addListener(fetchListener);
//    }
//
////    public void enqueue(ImageStatusObject im){
////
////        int id = PRDownloader.download(im.getImageURL(),getPath(im.getVideo()), im.getFileName())
////                .build()
////                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
////                    @Override
////                    public void onStartOrResume() {
////                        im.setState(ImageStatusObject.DOWNLOAD_PROCESS);
////                        updateStatus(im);
////                    }
////                })
////                .start(new OnDownloadListener() {
////                    @Override
////                    public void onDownloadComplete() {
////                        im.setState(ImageStatusObject.DOWNLOAD_DONE);
////                        im.setImageURL(getPath(im.getVideo()) + im.getFileName());
////                        updateStatus(im);
////                    }
////
////                    @Override
////                    public void onError(Error error) {
////                        im.setState(ImageStatusObject.DOWNLOAD_RETRY);
////                        updateStatus(im);
////                    }
////                });
////
////    }
//
//    private void updateStatus(final ImageStatusObject img){
//        chatMediaDaoMiddleware.updateChatMedia(img);
//    }
//
//}
