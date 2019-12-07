package com.example.textrecognation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MySurface extends SurfaceView {
    private Paint mPaint;
    private Path mPath;
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private float mX, mY, newX, newY;

    public MySurface(Context context) {
        super(context);
        initi(context);
    }

    private void initi(Context context) {
        mSurfaceHolder = getHolder();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(12);
        mPaint.setColor(Color.RED);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mX = event.getX();
                mY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                newX = event.getX();
                newY = event.getY();
                break;
            default:
                // Do nothing
        }
        drawRect();
        invalidate();
        return true;
    }
    private void drawRect() {
        mPath = new Path();
        mPath.moveTo(mX, mY);
        mCanvas = mSurfaceHolder.lockCanvas();
        mCanvas.save();
        mPath.addRect(mX, mY, newX, newY, Path.Direction.CCW);
        mCanvas.drawPath(mPath, mPaint);
        mCanvas.restore();
        mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        mX = newX;
        mY = newY;
    }
}
