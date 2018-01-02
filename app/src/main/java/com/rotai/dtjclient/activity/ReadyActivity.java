package com.rotai.dtjclient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.rotai.dtjclient.R;
import com.rotai.dtjclient.base.BaseActivity;

public class ReadyActivity extends BaseActivity {

    private TextView time;

    Handler queue=new Handler(Looper.myLooper());

    /**
     * data
     */
    AssetFileDescriptor file1,file2;
    MediaPlayer mediaPlayer1,mediaPlayer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);
        time=findViewById(R.id.time);

        file1 = this.getResources().openRawResourceFd(R.raw.ready);
        file2 = this.getResources().openRawResourceFd(R.raw.ready2);

        mediaPlayer1 = buildMediaPlayer(this, file1);
        mediaPlayer2 = buildMediaPlayer(this, file2);

        mediaPlayer1.start();

        final CountDownTimer downTimer = new CountDownTimer(5 * 1000+1050, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                time.setText((millisUntilFinished / 1000-1) + "");
            }

            @Override
            public void onFinish() {
                mediaPlayer2.start();
                mediaPlayer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        startActivity(new Intent(ReadyActivity.this, QRCodeActivity.class));
                    }
                });

            }
        };

        mediaPlayer1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                downTimer.start();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer1.release();
        mediaPlayer2.release();
        mediaPlayer1=null;
        mediaPlayer2=null;
        finish();
    }
}
