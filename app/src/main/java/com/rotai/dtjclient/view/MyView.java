package com.rotai.dtjclient.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.TextureView;


public class MyView extends android.support.v7.widget.AppCompatImageView {
//public class MyView extends TextureView {

    private Paint paint,paint2;
    private Paint mAreaPaint;
    private float count = 0;


    public MyView(Context context) {
        this(context, null);
    }

    public MyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {

        mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint=new Paint();

        //设置画布为阴影，黑色，透明度
        mAreaPaint.setColor(Color.parseColor("#0000ff"));
        mAreaPaint.setStyle(Paint.Style.FILL);
        mAreaPaint.setAlpha(50);

        //设置画布为蓝色，用于设置扫描窗口边缘的四个角
        paint.setColor(Color.parseColor("#00ff00"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(7f);

        paint2 = new Paint();
        //设置画笔颜色
        paint2.setColor(Color.parseColor("#ff0000"));
        //抗锯齿效果
        paint2.setAntiAlias(true);
        //设置画笔的宽度
        paint2.setStrokeWidth(4);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //获取布局的画面大小
        float screenWidth =canvas.getWidth();
        float screenHeight =canvas.getHeight();

//        int X = (int) (screenWidth/1.26);
        int X = (int) (screenWidth/1.1);
//        int Y = (int) (screenHeight/3.36);
        int Y = (int) (screenHeight/1.1);
        int XX = (int) (screenWidth/25);

//        /***画阴影部分的矩形框****/
//        canvas.drawRect(0,0,screenWidth,(screenHeight - Y)/2,mAreaPaint);
//        canvas.drawRect(0,(screenHeight - Y)/2,(screenWidth-X)/2,screenHeight,mAreaPaint);
//        canvas.drawRect((screenWidth-X)/2+X,(screenHeight - Y)/2,screenWidth,screenHeight,mAreaPaint);
//        canvas.drawRect((screenWidth-X)/2,(screenHeight - Y)/2+Y,(screenWidth-X)/2+X,screenHeight,mAreaPaint);

//        /***四个角***/
//        canvas.drawLine((screenWidth-X)/2,(screenHeight - Y)/2,(screenWidth-X)/2+XX,(screenHeight - Y)/2,paint);
//        canvas.drawLine((screenWidth-X)/2,(screenHeight - Y)/2,(screenWidth-X)/2,(screenHeight - Y)/2+XX,paint);
//        canvas.drawLine((screenWidth-X)/2+X-XX,(screenHeight - Y)/2,(screenWidth-X)/2+X,(screenHeight - Y)/2,paint);
//        canvas.drawLine((screenWidth-X)/2+X,(screenHeight - Y)/2,(screenWidth-X)/2+X,(screenHeight - Y)/2+XX,paint);
//        canvas.drawLine((screenWidth-X)/2,(screenHeight - Y)/2+Y-XX,(screenWidth-X)/2,(screenHeight - Y)/2+Y,paint);
//        canvas.drawLine((screenWidth-X)/2,(screenHeight - Y)/2+Y,(screenWidth-X)/2+XX,(screenHeight - Y)/2+Y,paint);
//        canvas.drawLine((screenWidth-X)/2+X,(screenHeight - Y)/2+Y-XX,(screenWidth-X)/2+X,(screenHeight - Y)/2+Y,paint);
//        canvas.drawLine((screenWidth-X)/2+X-XX,(screenHeight - Y)/2+Y,(screenWidth-X)/2+X,(screenHeight - Y)/2+Y,paint);




//        /*******扫描线*********/
//        if(count >Y-20)
//            count = 0;
//        canvas.drawLine((screenWidth-X)/2+20,(screenHeight - Y)/2+10+count,(screenWidth-X)/2+X-20,(screenHeight - Y)/2+10+count,paint2);
//        count+=3;

        invalidate();

    }
}
