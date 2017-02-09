package com.demo.videoplayer;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

public class MainActivity extends Activity {

    /**
     * 播放进度更新消息
     */
    private static final int UPDATE_TIME = 1;

    private VideoView videoView;
    private LinearLayout controllerbar_layout;
    private SeekBar play_progress, volum_progress;
    private ImageView btn_play, full_screen, iv_volume;
    private TextView time_current_tv, time_total_tv;

    /**
     * 屏幕屏幕宽、高
     */
    private int screenWidth;
    private int screenHeight;

    private int currentPosition;

    /**
     * 音频管理器
     */
    private AudioManager audioManager;

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int currentTime = videoView.getCurrentPosition();
            // 更新时间、进度条
            Utils.updateTimeFormat(time_current_tv, currentTime);
            play_progress.setProgress(currentTime);
            play_progress.setProgress(videoView.getDuration());
            uiHandler.sendEmptyMessageDelayed(UPDATE_TIME, 500);
        }
    };

    private void initView() {
        controllerbar_layout = (LinearLayout) findViewById(R.id.controllerbar_layout);
        play_progress = (SeekBar) findViewById(R.id.play_progress);
        volum_progress = (SeekBar) findViewById(R.id.volum_progress);
        btn_play = (ImageView) findViewById(R.id.btn_play);
        full_screen = (ImageView) findViewById(R.id.full_screen);
        time_current_tv = (TextView) findViewById(R.id.time_current_tv);
        time_total_tv = (TextView) findViewById(R.id.time_total_tv);

        volum_progress.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volum_progress.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        videoView = (VideoView) findViewById(R.id.id_video_view);
        /**
         * 本地视频播放
         */
        //为videoView设置视频路径
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        videoView.setVideoPath(path + "/DCIM/Camera/VID_20170131_111239.mp4");
        /**
         * 网络视频播放
         */
//        videoView.setVideoURI(null);
//        MediaController mediaController = new MediaController(this);
//        mediaController.setMediaPlayer(videoView);
//        videoView.setMediaController(mediaController);

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        /**
         * 自定义控制视图
         */
        initView();
        initEvent();
    }

    private void initEvent() {
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoView.isPlaying()) {
                    setPauseStatus();
                    videoView.pause();
                    uiHandler.removeMessages(UPDATE_TIME);
                } else {
                    setPlayStatus();
                    videoView.start();
                    uiHandler.sendEmptyMessage(UPDATE_TIME);
                }
            }
        });
    }

    private void setPlayStatus() {
        Utils.updateTimeFormat(time_total_tv, videoView.getDuration());
        play_progress.setProgress(videoView.getDuration());
        btn_play.setImageResource(R.drawable.pause_btn_style);
    }

    private void setPauseStatus() {
        btn_play.setImageResource(R.drawable.play_btn_style);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHandler.removeMessages(UPDATE_TIME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHandler.sendEmptyMessage(UPDATE_TIME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeMessages(UPDATE_TIME);
    }
}
