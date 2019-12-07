package com.example.textrecognation;






import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RectangleActivity extends AppCompatActivity {
        /** Called when the activity is first created. */

        Camera mCamera = null;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_rectangle);
            mCamera = getCameraInstance();
            Preview mPreview = new Preview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenCenterX = (size.x /2);
            int screenCenterY = (size.y/2) ;
            DrawOnTop mDraw = new DrawOnTop(this,screenCenterX,screenCenterY);
            addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            //Adding listener
            Button captureButton = (Button) findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mCamera.takePicture(null, null, mPicture);

                        }
                    });
        }
        /**
         * Helper method to access the camera returns null if
         * it cannot get the camera or does not exist
         * @return
         */
        private Camera getCameraInstance() {
            Camera camera = null;

            try {
                camera = Camera.open();
            } catch (Exception e) {
                // cannot get camera or does not exist
            }
            return camera;
        }
        PictureCallback mPicture = new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile();
                if (pictureFile == null){
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    Toast.makeText(RectangleActivity.this, "Photo saved to folder \"Pictures\\MyCameraApp\"", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {

                } catch (IOException e) {

                }
            }
        };

        private static File getOutputMediaFile(){
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyCameraApp");
            if (! mediaStorageDir.exists()){
                if (! mediaStorageDir.mkdirs()){
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }
            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile;
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");

            return mediaFile;
        }
    }

    class DrawOnTop extends View {
        int screenCenterX = 0;
        int screenCenterY = 0;
        final int radius = 50;
        public DrawOnTop(Context context, int screenCenterX, int screenCenterY) {
            super(context);
            this.screenCenterX = screenCenterX;
            this.screenCenterY = screenCenterY;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            Paint p = new Paint();
            p.setColor(Color.RED);
            DashPathEffect dashPath = new DashPathEffect(new float[]{5,5}, (float)1.0);
            p.setPathEffect(dashPath);
            p.setStyle(Style.STROKE);
            canvas.drawCircle(screenCenterX, screenCenterY, radius, p);
            invalidate();
            super.onDraw(canvas);
        }
    }

//----------------------------------------------------------------------

    class Preview extends SurfaceView implements SurfaceHolder.Callback {
        SurfaceHolder mHolder;
        Camera mCamera;
        Preview(Context context, Camera camera) {
            super(context);
            // Install a SurfaceHolder.Callback so we get notified when
            this.mCamera = camera;
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            //this is a deprecated method, is not required after 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, acquire the camera and tell
            // to draw.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the
            // Because the CameraDevice object is not a shared resource,
            // important to release it when the activity is paused.
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // Now that the size is known, set up the camera parameters
            // the preview.
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            // You need to choose the most appropriate previewSize for your app
            Camera.Size previewSize = previewSizes.get(0);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }


    }