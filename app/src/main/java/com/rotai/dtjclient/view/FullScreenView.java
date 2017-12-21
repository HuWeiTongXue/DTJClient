package com.rotai.dtjclient.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * 自定义全屏广告播放器
 */
public class FullScreenView extends VideoView {

    public FullScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }



    public FullScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



    public FullScreenView(Context context) {
        super(context);
    }


    /**
     *  重写onMeasure方法
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//
//        int width = getDefaultSize(0, widthMeasureSpec);//得到默认的大小（0，宽度测量规范）
//
//        int height = getDefaultSize(0, heightMeasureSpec);//得到默认的大小（0，高度度测量规范）
//
//        setMeasuredDimension(width, height); //设置测量尺寸,将高和宽放进去


        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//        int heightSize = widthSize*9/16;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if(widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY)
        {
            setMeasuredDimension(widthSize,heightSize);
        }
        else
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }
}