package com.example.textrecognation;
import android.graphics.Rect;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.example.textrecognation.camera.CameraManager;
import com.example.textrecognation.camera.ShutterButton;






public class ImageProcess extends AppCompatActivity {

        private static final String TAG = ImageProcess.class.getSimpleName();

// Note: These constants will be overridden by any default values defined in preferences.xml.

        public static final String KEY_PLAY_BEEP = "KEY_PLAY_BEEP";
        public static final String KEY_AUTO_FOCUS = "KEY_AUTO_FOCUS";
        public static final String KEY_DISABLE_CONTINUOUS_FOCUS = "KEY_DISABLE_CONTINUOUS_FOCUS";
        public static final String KEY_TOGGLE_LIGHT = "KEY_TOGGLE_LIGHT";
        public static final String KEY_REVERSE_IMAGE = "KEY_REVERSE_IMAGE";

        /**
         * ISO 639-3 language code indicating the default recognition language.
         */
        public static final String DEFAULT_SOURCE_LANGUAGE_CODE = "eng";

        /**
         * ISO 639-1 language code indicating the default target language for translation.
         */
        public static final String DEFAULT_TARGET_LANGUAGE_CODE = "es";

        /**
         * The default OCR engine to use.
         */
        public static final String DEFAULT_OCR_ENGINE_MODE = "Tesseract";

        /**
         * The default page segmentation mode to use.
         */
        public static final String DEFAULT_PAGE_SEGMENTATION_MODE = "Auto";

        /**
         * Whether to use autofocus by default.
         */
        public static final boolean DEFAULT_TOGGLE_AUTO_FOCUS = true;

        /**
         * Whether to initially disable continuous-picture and continuous-video focus modes.
         */
        public static final boolean DEFAULT_DISABLE_CONTINUOUS_FOCUS = true;

        /**
         * Whether to beep by default when the shutter button is pressed.
         */
        public static final boolean DEFAULT_TOGGLE_BEEP = false;

        /**
         * Whether to initially show a looping, real-time OCR display.
         */
        public static final boolean DEFAULT_TOGGLE_CONTINUOUS = false;

        /**
         * Whether to initially reverse the image returned by the camera.
         */
        public static final boolean DEFAULT_TOGGLE_REVERSED_IMAGE = false;


        /**
         * Whether the light should be initially activated by default.
         */
        public static final boolean DEFAULT_TOGGLE_LIGHT = false;


        /**
         * Flag to display the real-time recognition results at the top of the scanning screen.
         */
        private static final boolean CONTINUOUS_DISPLAY_RECOGNIZED_TEXT = true;

        /**
         * Flag to display recognition-related statistics on the scanning screen.
         */
        private static final boolean CONTINUOUS_DISPLAY_METADATA = true;

        /**
         * Flag to enable display of the on-screen shutter button.
         */
        private static final boolean DISPLAY_SHUTTER_BUTTON = true;

        /**
         * Languages for which Cube data is available.
         */
        static final String[] CUBE_SUPPORTED_LANGUAGES = {
                "ara", // Arabic
                "eng", // English
                "hin" // Hindi
        };

        /**
         * Languages that require Cube, and cannot run using Tesseract.
         */
        private static final String[] CUBE_REQUIRED_LANGUAGES = {
                "ara" // Arabic
        };

        //    /**
//     * Resource to use for data file downloads.
//     */
        static final String DOWNLOAD_BASE = "http://tesseract-ocr.googlecode.com/files/";
        //
//    /**
//     * Download filename for orientation and script detection (OSD) data.
//     */
        static final String OSD_FILENAME = "tesseract-ocr-3.01.osd.tar";

        /**
         * Destination filename for orientation and script detection (OSD) data.
         */
        static final String OSD_FILENAME_BASE = "osd.traineddata";


        private CameraManager cameraManager;
        //private CaptureActivityHandler handler;
        private ViewfinderView viewfinderView;
        private SurfaceView surfaceView;
        private SurfaceHolder surfaceHolder;
        private TextView statusViewBottom;
        private TextView statusViewTop;
        private TextView ocrResultView;
        private View cameraButtonView;
        private View resultView;
        //private OcrResult lastResult;
        private Bitmap lastBitmap;
        private boolean hasSurface;
        //private BeepManager beepManager;
//private TessBaseAPI baseApi; // Java interface for the Tesseract OCR engine
        private String sourceLanguageCodeOcr; // ISO 639-3 language code
        private String sourceLanguageReadable; // Language name, for example, "English"
        private String sourceLanguageCodeTranslation; // ISO 639-1 language code
        private String targetLanguageCodeTranslation; // ISO 639-1 language code
        private String targetLanguageReadable; // Language name, for example, "English"
        //private int pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD;
// int ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
        private String characterBlacklist;
        private String characterWhitelist;
        private ShutterButton shutterButton;
        private boolean isTranslationActive; // Whether we want to show translations
        private boolean isContinuousModeActive; // Whether we are doing OCR in continuous mode
        private SharedPreferences prefs;
        private OnSharedPreferenceChangeListener listener;
        private ProgressDialog dialog; // for initOcr - language download & unzip
        private ProgressDialog indeterminateDialog; // also for initOcr - init OCR engine
        private boolean isEngineReady;
        private boolean isPaused;
        private static boolean isFirstLaunch; // True if this is the first time the app is being run

        private Button btnEqual;
        private TextView txtDisplay;

       /* Handler getHandler() {
        return handler;
        }

        TessBaseAPI getBaseApi() {
        return baseApi;
        }*/

        CameraManager getCameraManager() {
                return cameraManager;
        }

        @Override

        protected void onCreate(Bundle savedInstanceState) {

                super.onCreate(savedInstanceState);
                if (isFirstLaunch) {
                        //setDefaultPreferences();
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
                        setContentView(R.layout.activity_image_process);

                        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

                        cameraButtonView = findViewById(R.id.camera_button_view);
                        resultView = findViewById(R.id.result_view);

                        statusViewBottom = (TextView) findViewById(R.id.status_view_bottom);
                        registerForContextMenu(statusViewBottom);
                        statusViewTop = (TextView) findViewById(R.id.status_view_top);
                        registerForContextMenu(statusViewTop);

                        //handler = null;
                        // lastResult = null;
                        hasSurface = false;
                        //  beepManager = new BeepManager(this);

                        // Camera shutter button
                        if (DISPLAY_SHUTTER_BUTTON) {
                                shutterButton = (ShutterButton) findViewById(R.id.shutter_button);
                                //shutterButton.setOnShutterButtonListener(this);
                        }

                        ocrResultView = (TextView) findViewById(R.id.ocr_result_text_view);
                        registerForContextMenu(ocrResultView);

                        cameraManager = new CameraManager(getApplication());
                        viewfinderView.setCameraManager(cameraManager) ;
                        // Set listener to change the size of the viewfinder rectangle.
                        viewfinderView.setOnTouchListener(new View.OnTouchListener() {
                                int lastX = -1;
                                int lastY = -1;

                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                        switch (event.getAction()) {
                                                case MotionEvent.ACTION_DOWN:
                                                        lastX = -1;
                                                        lastY = -1;
                                                        return true;
                                                case MotionEvent.ACTION_MOVE:
                                                        int currentX = (int) event.getX();
                                                        int currentY = (int) event.getY();

                                                        try {
                                                                Rect rect = cameraManager.getFramingRect();

                                                                final int BUFFER = 50;
                                                                final int BIG_BUFFER = 60;
                                                                if (lastX >= 0) {
                                                                        // Adjust the size of the viewfinder rectangle. Check if the touch event occurs in the corner areas first, because the regions overlap.
                                                                        if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                                                                                && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                                                                                // Top left corner: adjust both top and left sides
                                                                                cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (lastY - currentY));
                                                                               // viewfinderView.removeResultText();
                                                                        } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
                                                                                && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                                                                                // Top right corner: adjust both top and right sides
                                                                                cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (lastY - currentY));
                                                                               // viewfinderView.removeResultText();
                                                                        } else if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                                                                                && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                                                                                // Bottom left corner: adjust both bottom and left sides
                                                                                cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (currentY - lastY));
                                                                              //  viewfinderView.removeResultText();
                                                                        } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
                                                                                && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                                                                                // Bottom right corner: adjust both bottom and right sides
                                                                                cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (currentY - lastY));
                                                                              //  viewfinderView.removeResultText();
                                                                        } else if (((currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER) || (lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER))
                                                                                && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                                                                                // Adjusting left side: event falls within BUFFER pixels of left side, and between top and bottom side limits
                                                                                cameraManager.adjustFramingRect(2 * (lastX - currentX), 0);
                                                                              //  viewfinderView.removeResultText();
                                                                        } else if (((currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER) || (lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER))
                                                                                && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                                                                                // Adjusting right side: event falls within BUFFER pixels of right side, and between top and bottom side limits
                                                                                cameraManager.adjustFramingRect(2 * (currentX - lastX), 0);
                                                                             //   viewfinderView.removeResultText();
                                                                        } else if (((currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER) || (lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER))
                                                                                && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                                                                                // Adjusting top side: event falls within BUFFER pixels of top side, and between left and right side limits
                                                                                cameraManager.adjustFramingRect(0, 2 * (lastY - currentY));
                                                                             //   viewfinderView.removeResultText();
                                                                        } else if (((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER) || (lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER))
                                                                                && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                                                                                // Adjusting bottom side: event falls within BUFFER pixels of bottom side, and between left and right side limits
                                                                                cameraManager.adjustFramingRect(0, 2 * (currentY - lastY));
                                                                              //  viewfinderView.removeResultText();
                                                                        }
                                                                }
                                                        } catch (NullPointerException e) {
                                                                Log.e(TAG, "Framing rect not available", e);
                                                        }
                                                        v.invalidate();
                                                        lastX = currentX;
                                                        lastY = currentY;
                                                        return true;
                                                case MotionEvent.ACTION_UP:
                                                        lastX = -1;
                                                        lastY = -1;
                                                        return true;
                                        }
                                        return false;
                                }
                        });

                        isEngineReady = false;


//        hasil calculator
                        btnEqual = (Button) findViewById(R.id.btnEqual);
                        btnEqual.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                        txtDisplay = (TextView) findViewById(R.id.txtDisplay);
                                        // Read the expression
                                        String txt = ocrResultView.getText().toString();
                                        // Create an Expression (A class from exp4j library)
                                        // Expression expression = new ExpressionBuilder(txt).build();
                                        // Calculate the result and display
                                        // double result = expression.evaluate();
                                        // txtDisplay.setText(Double.toString(result));
                                }
                        });
                }
        }
}













/*---------------------------------------------------------------------------------------------------*/
