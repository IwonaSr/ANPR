package com.example.ejwon.anpr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Ejwon on 2017-03-11.
 */
public class DrawView extends ImageView {

    public DrawView(Context context) {
        super(context);
       this.setWillNotDraw(false);
    }

    DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        float leftx = 100;
        float topy = 100;
        float rightx = 200;
        float bottomy = 200;
        canvas.drawRect(leftx, topy, rightx, bottomy, paint);

        postInvalidate();
    }
}

