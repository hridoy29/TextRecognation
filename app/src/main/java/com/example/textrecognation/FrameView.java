package com.example.textrecognation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class FrameView extends View {
    private Paint mFramePaint = null;
    private final String TAG = "FrameView";

    public FrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFramePaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mFramePaint.setStyle(Paint.Style.FILL);
        mFramePaint.setColor(Color.GREEN);

        Rect drawRect = new Rect(300,300,300,300);

        canvas.drawRect(drawRect, mFramePaint);
    }
}