package com.rotai.dtjclient.fragment;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.Constant;
import com.rotai.dtjclient.util.SDCardUtil;
import com.rotai.dtjclient.view.FullScreenView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class VideoFragment extends Fragment {


    /**
     * UI
     */
    private View mContentView;
    private FullScreenView mFullScreenView;

    /**
     * data
     */
    private List<String> videoPaths; // 视频地址集合
    private List<File> fileList = new ArrayList<>();// 所有扩展卡路径
    private int video_index;// 资源索引
    private String SDCARD_PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    //    private String SDCARD_PATH= "/mnt/internal_sd/";


    public VideoFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_video, container, false);
        initView();

        return mContentView;
    }

    private void initView() {
        mFullScreenView= mContentView.findViewById(R.id.videoView);
        startReadytVideo(Constant.fileName_rotai);
    }

    private void startReadytVideo(String fileName) {
        mFullScreenView.setVideoPath(SDCARD_PATH+fileName);

        mFullScreenView.start();
    }

    @Override
    public void onResume() {
        initCompletionListener();
        super.onResume();

    }

    /**
     * ================================初始化播放完成事件=================================
     */
    private void  initCompletionListener() {

        // 获取所有视频路径
        videoPaths = SDCardUtil.getAllVideoFilePath(fileList,SDCARD_PATH,"mp4");

        // 播放完监听
        mFullScreenView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                video_index = new Random().nextInt(videoPaths.size() - 1);  //随机获取视频播放地址

                if (video_index == videoPaths.size() - 1)
                    video_index = 0;

                //播放视频
                startPlayVideo(video_index);
            }
        });

        // 获取所有视频路径
        videoPaths = SDCardUtil.getAllVideoFilePath(fileList,SDCARD_PATH,"mp4");

    }

    /**
     * 播放视频
     */
    private void startPlayVideo(int videoIndex) {

        if (videoPaths.size() == 0) {
            Toast.makeText(getContext(), "视频不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        mFullScreenView.setVideoPath(videoPaths.get(videoIndex));

        mFullScreenView.start();

    }
}
