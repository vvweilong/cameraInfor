package aprivate.oo.cameralession;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 *
 */
public class SquareCameraPreview extends SurfaceView {

    public static final String TAG = SquareCameraPreview.class.getSimpleName();
    private static final int INVALID_POINTER_ID = -1;

    private static final int ZOOM_OUT = 0;
    private static final int ZOOM_IN = 1;
    private static final int ZOOM_DELTA = 1;

    private static final int FOCUS_SQR_SIZE = 100;
    private static final int FOCUS_MAX_BOUND = 1000;
    private static final int FOCUS_MIN_BOUND = -FOCUS_MAX_BOUND;

    private static final double ASPECT_RATIO = 9.0 / 16.0;//比例 3：4  截取正方形图片  只要将下面的1/4截掉 预览 将其覆盖

    private Camera mCamera;

    private float mLastTouchX;
    private float mLastTouchY;

    // For scalingø
    private int mMaxZoom;
    private boolean mIsZoomSupported;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mScaleFactor = 1;
    private ScaleGestureDetector mScaleDetector;

    // For focus
    private boolean mIsFocus;
    private Camera.Area mFocusArea;
    private ArrayList<Camera.Area> mFocusAreas;

    public SquareCameraPreview(Context context) {
        super(context);
        init(context);
    }

    public SquareCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SquareCameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mFocusArea = new Camera.Area(new Rect(), 1000);
        mFocusAreas = new ArrayList<Camera.Area>();
        mFocusAreas.add(mFocusArea);
    }

    /**
     * Measure the view and its content to determine the measured width and the
     * measured height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
//按照比例  以宽或高来生成比例的 预览界面
        height = (int) (width / ASPECT_RATIO + 0.5);//以宽来设置尺寸
        Log.i(TAG, "width=" + width + " height=" + height);
        setMeasuredDimension(width, height);
    }

    public int getViewWidth() {
        return getWidth();
    }

    public int getViewHeight() {
        return getHeight();
    }

    public void setCamera(Camera camera) {
        mCamera = camera;

        if (camera != null) {
            Camera.Parameters params = camera.getParameters();
            mIsZoomSupported = params.isZoomSupported();
            if (mIsZoomSupported) {
                mMaxZoom = params.getMaxZoom();
            }
        }
    }


    private boolean setFocusBound(float x, float y) {
        int left = (int) (x - FOCUS_SQR_SIZE / 2);
        int right = (int) (x + FOCUS_SQR_SIZE / 2);
        int top = (int) (y - FOCUS_SQR_SIZE / 2);
        int bottom = (int) (y + FOCUS_SQR_SIZE / 2);

        if (FOCUS_MIN_BOUND > left || left > FOCUS_MAX_BOUND) return false;
        if (FOCUS_MIN_BOUND > right || right > FOCUS_MAX_BOUND) return false;
        if (FOCUS_MIN_BOUND > top || top > FOCUS_MAX_BOUND) return false;
        if (FOCUS_MIN_BOUND > bottom || bottom > FOCUS_MAX_BOUND) return false;

        mFocusArea.rect.set(left, top, right, bottom);

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor = (int) detector.getScaleFactor();
            return true;
        }
    }
}