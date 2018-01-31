package com.rotai.dtjclient.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class customView extends View {

    public customView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    Paint paint = new Paint();
    {
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(2.5f);//设置线宽
        paint.setAlpha(100);
    };

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(new Rect(200, 200, 400, 500), paint);//绘制矩形
    }
}
