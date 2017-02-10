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
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
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
    private ImageView btn_play, full_screen, iv_volume, center_process;
    private TextView time_current_tv, time_total_tv;
    private FrameLayout frameLayout;

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

        frameLayout = (FrameLayout) findViewById(R.id.id_aaa);
        frameLayout.setVisibility(View.GONE);
        center_process = (ImageView) findViewById(R.id.id_percent_progress);

        //横竖屏处理
        onConfigurationChanged(getResources().getConfiguration());

        volum_progress.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volum_progress.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {//用户按下屏幕就会触发
            if (controllerbar_layout.getVisibility() == View.VISIBLE) {
//                controllerbar_layout.setVisibility(View.GONE);
            } else {
                controllerbar_layout.setVisibility(View.VISIBLE);
            }
            frameLayout.setVisibility(View.VISIBLE);
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            frameLayout.setVisibility(View.GONE);
            /**
             * 从名子也可以看出,一次单独的轻击抬起操作,也就是轻击一下屏幕，立刻抬起来，才会有这个触发，
             * 当然,如果除了Down以外还有其它操作,那就不再算是Single操作了,所以也就不会触发这个事件
             * 触发顺序：
             * 点击一下非常快的（不滑动）Touchup：onDown->onSingleTapUp->onSingleTapConfirmed
             * 点击一下稍微慢点的（不滑动）Touchup：onDown->onShowPress->onSingleTapUp->onSingleTapConfirmed
             */
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            /**
             * 在屏幕上拖动事件。无论是用手拖动view，或者是以抛的动作滚动，都会多次触发,这个方法
             * 在ACTION_MOVE动作发生时就会触发滑屏：手指触动屏幕后，稍微滑动后立即松开
             onDown-----》onScroll----》onScroll----》onScroll----》………----->onFling
             拖动
             onDown------》onScroll----》onScroll------》onFiling
             可见，无论是滑屏，还是拖动，影响的只是中间OnScroll触发的数量多少而已，最终都会触发onFling事件！
             */
//            float offsetX = e1.getX() - e2.getX();
//            float offsetY = e1.getY() - e2.getY();
//            float absOffsetX = Math.abs(offsetX);
//            float absOffsetY = Math.abs(offsetY);
            if (Math.abs(distanceX) < Math.abs(distanceY)) {
                Log.e("Main", "distanceX=" + distanceX + ",distanceY=" + distanceY);
                if ((e1.getX() < screenWidth / 2) && (e2.getX() < screenWidth / 2)) {
                    changeBrightness(distanceY);
                } else if ((e1.getX() > screenWidth / 2) && (e2.getX() > screenWidth / 2)) {
                    changeVolume(distanceY);
                }
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            /**
             * 滑屏，用户按下触摸屏、快速移动后松开，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE, 1个ACTION_UP触发
             * 参数解释：
             e1：第1个ACTION_DOWN MotionEvent
             e2：最后一个ACTION_MOVE MotionEvent
             velocityX：X轴上的移动速度，像素/秒
             velocityY：Y轴上的移动速度，像素/秒
             */
            return false;
        }
    };

    //调整声音
    private void changeVolume(float offset) {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int index = (int) (offset / screenHeight * maxVolume * 3);// 为了变化更加明显，所以*3
        int volume = Math.max(currentVolume + index, 0);
        volume = Math.min(volume, maxVolume);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        volum_progress.setProgress(volume);
        Log.e("Main", "声音从" + currentVolume + "到" + volume);

        ViewGroup.LayoutParams p = center_process.getLayoutParams();
        p.width = (int) (Utils.dp2px(this, 94) * volume / maxVolume);
        center_process.setLayoutParams(p);
    }

    //调整亮度
    private void changeBrightness(float offset) {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        float brightness = attributes.screenBrightness;
        float index = offset / screenHeight;// 除3是为了弱化效果
        float newBrightness = Math.max(brightness + index, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF);
        newBrightness = Math.min(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, newBrightness);
        attributes.screenBrightness = newBrightness;
        getWindow().setAttributes(attributes);
        Log.e("Main", "亮度从" + brightness + "到" + newBrightness);

        ViewGroup.LayoutParams p = center_process.getLayoutParams();
        p.width = (int) (Utils.dp2px(this, 94) * newBrightness);
        center_process.setLayoutParams(p);
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
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setSystemUiVisible();
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(this, 240));
            //视图显示控制
            iv_volume.setVisibility(View.GONE);
            volum_progress.setVisibility(View.GONE);
            full_screen.setImageResource(R.drawable.full_screen);

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setSystemUiHide();
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //视图显示控制
            iv_volume.setVisibility(View.VISIBLE);
            volum_progress.setVisibility(View.VISIBLE);
            full_screen.setImageResource(R.drawable.exit_full_screen);

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
