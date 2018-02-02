package com.rotai.dtjclient.base;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.rotai.dtjclient.util.LogUtil;

import java.io.IOException;
import java.util.Calendar;

import static com.rotai.dtjclient.base.Application.MAX_BRIGHTNESS;

public class BaseActivity extends AppCompatActivity {

    public static final String TAG = "dtjclient";

    private boolean isWorking = false;

    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(params);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final View decorView = getWindow().getDecorView();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(uiOptions);
                }
            }
        });

        AppManager.getInstance().addActivity(this);
        checkActivity();

        Application.mApplication.queue.post(new Runnable() {
            @Override
            public void run() {

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                Log.d(TAG, "当前小时数=="+hour +",当前的亮度值="+getSysScreenBrightness());

                if((hour>20&&hour<24)||(hour>0&&hour<6)){
                    setActScreenBrightness(BaseActivity.this,190);
                }else {
                    setActScreenBrightness(BaseActivity.this,0);
                }

                Application.mApplication.queue.postDelayed(this,5000);
            }
        });


    }

    private void checkActivity() {
        if (getClass().getSimpleName().equals("ReadyActivity")
                || getClass().getSimpleName().equals("HeightActivity")
                || getClass().getSimpleName().equals("WeightActivity")
                || getClass().getSimpleName().equals("BFPActivity")
                || getClass().getSimpleName().equals("FaceActivity")
                || getClass().getSimpleName().equals("CompleteActivity")
                || getClass().getSimpleName().equals("QRCodeActivity")
                || getClass().getSimpleName().equals("ReportActivity")
                || getClass().getSimpleName().equals("SplashActivity")
                || getClass().getSimpleName().equals("LoadingActivity")
                ) {
            isWorking = true;
        } else {
            isWorking = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getInstance().finishActivity(this);
    }

    public static MediaPlayer buildMediaPlayer(Context context, AssetFileDescriptor file) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // When the beep has finished playing, rewind to queue up another one.
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer player) {
                player.seekTo(0);
            }
        });

        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.setVolume(0.5f, 0.5f);
            mediaPlayer.prepare();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            mediaPlayer = null;
        }
        return mediaPlayer;
    }

    public static int isBackground() {
        if (Application.stateCount == 0) {
            LogUtil.d(TAG, "isBackground");
            return 0;
        } else {
            LogUtil.d(TAG, "isForeground");
            return 1;
        }
    }

    public static int getVersionCode(Context mContext) {
        int versionCode = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    /**
     * 设置屏幕亮度，这会反映到真实屏幕上
     */
    public static void setActScreenBrightness(final Activity activity,
                                              final int brightness) {
        final WindowManager.LayoutParams lp = activity.getWindow()
                .getAttributes();
        lp.screenBrightness = brightness / (float) MAX_BRIGHTNESS;
        activity.getWindow().setAttributes(lp);
    }

    /**
     * 获得当前系统的亮度值： 0~255
     */
    public int getSysScreenBrightness() {
        int screenBrightness = MAX_BRIGHTNESS;
        try {
            screenBrightness = Settings.System.getInt(
                    getApplicationContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            Log.e(TAG, "获得当前系统的亮度值失败：", e);
        }
        return screenBrightness;
    }
}