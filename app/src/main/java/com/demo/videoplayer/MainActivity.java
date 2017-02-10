package com.demo.videoplayer;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

public class MainActivity extends Activity {

    /**
     * 播放进度更新消息
     */
    private static final int UPDATE_TIME = 1;

    private VideoView videoView;
    private RelativeLayout videoViewLayout;
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
    /**
     * 音量变化广播接收器
     */
    private VolumeReceiver volumeReceiver;
    /**
     * 手势识别
     */
    private GestureDetector detector;

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int currentTime = videoView.getCurrentPosition();
            // 更新时间、进度条
            Utils.updateTimeFormat(time_current_tv, currentTime);
            play_progress.setProgress(currentTime);
            uiHandler.sendEmptyMessageDelayed(UPDATE_TIME, 500);
        }
    };

    private void initView() {
        videoViewLayout = (RelativeLayout) findViewById(R.id.id_video_layout);
        controllerbar_layout = (LinearLayout) findViewById(R.id.controllerbar_layout);
        play_progress = (SeekBar) findViewById(R.id.play_progress);
        volum_progress = (SeekBar) findViewById(R.id.volum_progress);
        iv_volume = (ImageView) findViewById(R.id.id_volum_icon);
        btn_play = (ImageView) findViewById(R.id.btn_play);
        full_screen = (ImageView) findViewById(R.id.full_screen);
        time_current_tv = (TextView) findViewById(R.id.time_current_tv);
        time_total_tv = (TextView) findViewById(R.id.time_total_tv);

        //横竖屏处理
        onConfigurationChanged(getResources().getConfiguration());

        volum_progress.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volum_progress.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            if (controllerbar_layout.getVisibility() == View.VISIBLE) {
                controllerbar_layout.setVisibility(View.GONE);
            } else {
                controllerbar_layout.setVisibility(View.VISIBLE);
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float offsetX = e1.getX() - e2.getX();
            float offsetY = e1.getY() - e2.getY();
            float absOffsetX = Math.abs(offsetX);
            float absOffsetY = Math.abs(offsetY);
            if ((e1.getX() < screenWidth / 2) && (e2.getX() < screenWidth / 2) && (absOffsetX < absOffsetY)) {
                changeBrightness(offsetY);
            } else if ((e1.getX() > screenWidth / 2) && (e2.getX() > screenWidth / 2) && (absOffsetX < absOffsetY)) {
                changeVolume(offsetY);
            }
            return true;
        }
    };

    //调整声音
    private void changeVolume(float offset) {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int index = (int) (offset / screenHeight * maxVolume);
        int volume = Math.max(currentVolume + index, 0);
        volume = Math.min(volume, maxVolume);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        volum_progress.setProgress(volume);
    }

    //调整亮度
    private void changeBrightness(float offset) {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        float brightness = attributes.screenBrightness;
        float index = offset / screenHeight / 2;
        brightness = Math.max(brightness + index, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF);
        brightness = Math.min(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, brightness);
        attributes.screenBrightness = brightness;
        getWindow().setAttributes(attributes);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        detector = new GestureDetector(this, gestureListener);

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

        //注册音量变化广播接收器
        volumeReceiver = new VolumeReceiver(MainActivity.this, iv_volume, volum_progress);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(volumeReceiver, filter);
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
        play_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                    Utils.updateTimeFormat(time_current_tv, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                uiHandler.removeMessages(UPDATE_TIME);
                if (!videoView.isPlaying()) {
                    setPlayStatus();
                    videoView.start();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                uiHandler.sendEmptyMessage(UPDATE_TIME);
            }
        });
        volum_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                uiHandler.removeMessages(UPDATE_TIME);
                Utils.updateTimeFormat(time_current_tv, 0);
                btn_play.setImageResource(R.drawable.play_btn_style);
                videoView.seekTo(0);
                play_progress.setProgress(0);
                videoView.pause();
            }
        });
        full_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });
    }

    //设置播放状态
    private void setPlayStatus() {
        Utils.updateTimeFormat(time_total_tv, videoView.getDuration());
        play_progress.setMax(videoView.getDuration());
        btn_play.setImageResource(R.drawable.pause_btn_style);
    }

    //暂停播放处理
    private void setPauseStatus() {
        btn_play.setImageResource(R.drawable.play_btn_style);
    }

    /**
     * 监听屏幕方向改变
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setSystemUiVisible();
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(this, 240));
            //视图显示控制
            iv_volume.setVisibility(View.GONE);
            volum_progress.setVisibility(View.GONE);
            full_screen.setImageResource(R.drawable.full_screen);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setSystemUiHide();
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //视图显示控制
            iv_volume.setVisibility(View.VISIBLE);
            volum_progress.setVisibility(View.VISIBLE);
            full_screen.setImageResource(R.drawable.exit_full_screen);
        }
    }

    private void setVideoViewScale(int width, int height) {
        ViewGroup.LayoutParams params = videoView.getLayoutParams();
        params.height = height;
        params.width = width;
        videoView.setLayoutParams(params);

        ViewGroup.LayoutParams params1 = videoViewLayout.getLayoutParams();
        params1.height = height;
        params1.width = width;
        videoViewLayout.setLayoutParams(params1);
    }

    private void setSystemUiHide() {
        //TODO
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void setSystemUiVisible() {
        //TODO
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHandler.removeMessages(UPDATE_TIME);
        if (videoView.canPause()) {
            videoView.pause();
            currentPosition = videoView.getCurrentPosition();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHandler.sendEmptyMessage(UPDATE_TIME);
        if (videoView.canSeekForward()) {
            videoView.seekTo(currentPosition);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeMessages(UPDATE_TIME);
        if (videoView.canPause()) {
            videoView.pause();
        }
        unregisterReceiver(volumeReceiver);
    }
}
