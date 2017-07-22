package ph.edu.dlsu.mhealthtest;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.atomic.AtomicReference;

import ph.edu.dlsu.mhealth.android.CustomCameraView;
import ph.edu.dlsu.mhealth.vision.AkazeSymmetryMatcher;
import ph.edu.dlsu.mhealth.vision.BriskSymmetryMatcher;
import ph.edu.dlsu.mhealth.vision.OrbSymmetryMatcher;

/**
 * Created by cobalt on 1/8/16.
 */
public class ImageMatchingActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private static final String TAG = "tag.ImageMatching";


    /*********************  Update this if you add more filters  ***************/

    public static final int VIEW_MODE_RGBA        = 0;
    public static final int VIEW_MODE_ORB_MATCHER = 1;
    public static final int VIEW_MODE_AKAZE_MATCHER = 2;
    public static final int VIEW_MODE_BRISK_MATCHER = 3;

    public static int viewMode = VIEW_MODE_RGBA;

    /// Frame holders
    private Mat mRgba;
    private Mat mGray;

    /// For Image Reduction
    private static int scale = 2;
    private static final int WIDTH = 1280 / scale;
    private static final int HEIGHT = 720 / scale;

    private CustomCameraView mCameraView;

    // ROI Selection
    SurfaceHolder _holder;
    private int _canvasImgYOffset;
    private int _canvasImgXOffset;
    private AtomicReference<Point> trackedBox1stCorner;
    private Paint rectPaint;

    private Rect _trackedBox = null;


    /// Matcher
    private OrbSymmetryMatcher mOrbMatcher;
    private AkazeSymmetryMatcher mAkazeMatcher;
    private BriskSymmetryMatcher mBriskMatcher;


    // Parameters for handling the Camera
    private static final String STATE_CAMERA_INDEX = "cameraIndex";
    private boolean mIsCameraFrontFacing;
    private int mNumCameras;
    private int mCameraIndex;  // default to front camera


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {

                    // Load native library after(!) OpenCV initialization success
                    System.loadLibrary("mhealth_vision");

                    mCameraView.enableView();

                    mCameraView.enableFpsMeter();

                    mCameraView.setOnTouchListener(ImageMatchingActivity.this);

                    /// Initialize the matchers
                    mOrbMatcher = new OrbSymmetryMatcher();
                    mAkazeMatcher = new AkazeSymmetryMatcher();
                    mBriskMatcher = new BriskSymmetryMatcher();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public ImageMatchingActivity() {
        // Log.i(TAG, "Instantiated new " + this.getClass());
    }


    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (savedInstanceState != null) {
            mCameraIndex = savedInstanceState.getInt(STATE_CAMERA_INDEX, 0);
        }

        setContentView(R.layout.activity_image_matching);

        mCameraView = (CustomCameraView) findViewById(R.id.image_matching_view);
        mCameraView.setCameraIndex(mCameraIndex);
        mCameraView.setVisibility(SurfaceView.VISIBLE);
        mNumCameras = mCameraView.getNumberOfCameras();
        mIsCameraFrontFacing = mCameraView.isCameraFrontFacing();
        //mCameraView.setMaxFrameSize(WIDTH, HEIGHT);
        mCameraView.setCvCameraViewListener(this);

        _holder = mCameraView.getHolder();

        // ROI selection initialization
        trackedBox1stCorner = new AtomicReference<Point>();
        rectPaint = new Paint();
        rectPaint.setColor(Color.rgb(0, 255, 0));
        rectPaint.setStrokeWidth(5);
        rectPaint.setStyle(Paint.Style.STROKE);

    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_CAMERA_INDEX, mCameraIndex);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onPause() {
        super.onPause();

        if (mCameraView != null)
            mCameraView.disableView();

        _trackedBox = null;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mCameraView != null)
            mCameraView.disableView();

        mOrbMatcher.release();
        mAkazeMatcher.release();
        mBriskMatcher.release();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mIsCameraFrontFacing) {
            // Mirror (horizontally flip) the preview.
            Core.flip(mRgba, mRgba, 1);
            Core.flip(mGray, mGray, 1);
        }


        /*********************  Update this if you add more filters  ***************/


        switch (ImageMatchingActivity.viewMode) {

            case ImageMatchingActivity.VIEW_MODE_RGBA:
                break;

            case ImageMatchingActivity.VIEW_MODE_ORB_MATCHER:
                mOrbMatcher.apply(mGray, mRgba);
                break;

            case ImageMatchingActivity.VIEW_MODE_AKAZE_MATCHER:
                mAkazeMatcher.apply(mGray, mRgba);
                break;

            case ImageMatchingActivity.VIEW_MODE_BRISK_MATCHER:
                mAkazeMatcher.apply(mGray, mRgba);
                break;

        } // END SWITCH


        return mRgba;
    }


    private Mat Reduce(Mat m) {
        Mat dst = new Mat();
        Imgproc.resize(m, dst, new org.opencv.core.Size(WIDTH, HEIGHT));
        return dst;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        /*********************  Update this layout if you add more filters  ***************/
        inflater.inflate(R.menu.activity_image_matching, menu);

        return super.onCreateOptionsMenu(menu);
    }




    /*********************  Update this if you add more filters  ***************/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        long action = item.getItemId();

        if (action == R.id.action_rgba) {
            viewMode = VIEW_MODE_RGBA;
        }
        else if (action == R.id.action_orb_matcher) {
            viewMode = VIEW_MODE_ORB_MATCHER;
        }
        else if (action == R.id.action_akaze_matcher) {
            viewMode = VIEW_MODE_AKAZE_MATCHER;
        }
        else if (action == R.id.action_brisk_matcher) {
            viewMode = VIEW_MODE_AKAZE_MATCHER;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {


        final Point corner = new Point(
                event.getX() - _canvasImgXOffset, event.getY()
                - _canvasImgYOffset);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                trackedBox1stCorner.set(corner);
                //Log.i("TAG", "1st corner: " + corner);
                break;

            case MotionEvent.ACTION_UP:
                _trackedBox = new Rect(trackedBox1stCorner.get(), corner);
                if (_trackedBox.area() > 100) {
                    // Log.i("TAG", "Tracked box DEFINED: " + _trackedBox);

                } else {
                    _trackedBox = null;
                }
                break;


            case MotionEvent.ACTION_MOVE:
                final android.graphics.Rect rect = new android.graphics.Rect(
                        (int) trackedBox1stCorner.get().x
                                + _canvasImgXOffset,
                        (int) trackedBox1stCorner.get().y
                                + _canvasImgYOffset, (int) corner.x
                        + _canvasImgXOffset, (int) corner.y
                        + _canvasImgYOffset);
                final Canvas canvas = _holder.lockCanvas(rect);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                canvas.drawRect(rect, rectPaint);
                _holder.unlockCanvasAndPost(canvas);

                break;
        }

        return true; //  all follow up calls of this touch event like
        // ACTION_MOVE or ACTION_UP will be delivered

    }


    public void onClickReverseCamera(View v) {
        mCameraIndex++;
        if (mCameraIndex == mNumCameras) {
            mCameraIndex = 0;
        }
        recreate();
    }
}
