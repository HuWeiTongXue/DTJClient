package com.rotai.dtjclient.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.rotai.dtjclient.Interface.setTimerState;
import com.rotai.dtjclient.R;

public class MyDialog extends Dialog {

    private TextView contentTxt,titleTxt,jishiTxt;
    private Context mContext;
    private String content="测量未完成，请继续测量",title="提示";
    public  boolean flag=true;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1001){
                jishiTxt.setText(msg.arg1+" s之后返回广告界面");
            }
        }
    };

    public MyDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    /*protected MyDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }*/

    setTimerState setStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_main_notice);
        initView();
        setCanceledOnTouchOutside(true);
        new timer().start();
    }

    @Override
    protected void onStop() {
        flag=false;
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            dismiss();
        }
    }

    private void initView(){
        contentTxt = (TextView)findViewById(R.id.dialog_content);
        titleTxt = (TextView)findViewById(R.id.dialog_title);
        jishiTxt= (TextView) findViewById(R.id.dialog_timer);
        contentTxt.setText(content);
        titleTxt.setText(title);
    }

    class timer extends Thread{
        int time=5;
        @Override
        public void run() {
            while (flag){
                try {
                    Thread.sleep(1000);
                    Message message=handler.obtainMessage();
                    message.what=1001;
                    message.arg1=time;
                    time--;
                    handler.sendMessage(message);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setStateListener.setState(time);
                        }
                    },300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setonstatelister(setTimerState setTimerState){
        this.setStateListener=setTimerState;
    }

}