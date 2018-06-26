package artem122ya.unsplashclient.photoviewer;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class ScalableImageView extends android.support.v7.widget.AppCompatImageView {


    private float minScaleFactor = 1f;
    private float maxScaleFactor = 3f;

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private static final int CLICK = 3;
    private int currentMode = NONE;


    private PointF lastPoint = new PointF();
    private PointF startPoint = new PointF();

    private Matrix matrix;
    private float[] m;

    private float scaleFactor = 1f;

    private int viewWidth, viewHeight;
    private float imageWidth, imageHeight;
    private int oldMeasuredWidth, oldMeasuredHeight;

    private ScaleGestureDetector scaleDetector;


    public ScalableImageView(Context context) {
        super(context);
        init(context);
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScalableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        super.setClickable(true);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);


        // Rescales image on rotation
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0)
            return;
        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;

        if (scaleFactor == 1) {
            //Fit to screen.
            float scale;

            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
                return;
            int bitmapWidth = drawable.getIntrinsicWidth();
            int bitmapHeight = drawable.getIntrinsicHeight();


            float scaleX = (float) viewWidth / (float) bitmapWidth;
            float scaleY = (float) viewHeight / (float) bitmapHeight;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);

            // Center the image
            float redundantYSpace = (float) viewHeight - (scale * (float) bitmapHeight);
            float redundantXSpace = (float) viewWidth - (scale * (float) bitmapWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            imageWidth = viewWidth - 2 * redundantXSpace;
            imageHeight = viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);
        }
        panTranslations();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getDrawable() == null) return true;
        scaleDetector.onTouchEvent(event);
        PointF currentPoint = new PointF(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastPoint.set(currentPoint);
                startPoint.set(lastPoint);
                currentMode = DRAG;
                break;

            case MotionEvent.ACTION_MOVE:
                if (currentMode == DRAG) {
                    float deltaX = currentPoint.x - lastPoint.x;
                    float deltaY = currentPoint.y - lastPoint.y;
                    float fixedTranslationX = getPannedDragTranslation(deltaX, viewWidth, imageWidth * scaleFactor);
                    float fixedTranslationY = getPannedDragTranslation(deltaY, viewHeight, imageHeight * scaleFactor);
                    matrix.postTranslate(fixedTranslationX, fixedTranslationY);
                    panTranslations();
                    lastPoint.set(currentPoint.x, currentPoint.y);
                }
                break;

            case MotionEvent.ACTION_UP:
                currentMode = NONE;
                int xDifference = (int) Math.abs(currentPoint.x - startPoint.x);
                int yDifference = (int) Math.abs(currentPoint.y - startPoint.y);
                if (xDifference < CLICK && yDifference < CLICK)
                    performClick();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                currentMode = NONE;
                break;
        }

        setImageMatrix(matrix);
        invalidate();
        return true;
    }



    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            currentMode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float newScaleFactor = detector.getScaleFactor();
            float lastScaleFactor = scaleFactor;
            scaleFactor *= newScaleFactor;
            if (scaleFactor > maxScaleFactor) {
                scaleFactor = maxScaleFactor;
                newScaleFactor = maxScaleFactor / lastScaleFactor;
            } else if (scaleFactor < minScaleFactor) {
                scaleFactor = minScaleFactor;
                newScaleFactor = minScaleFactor / lastScaleFactor;
            }

            if (imageWidth * scaleFactor <= viewWidth || imageHeight * scaleFactor <= viewHeight)
                matrix.postScale(newScaleFactor, newScaleFactor, viewWidth / 2, viewHeight / 2);
            else
                matrix.postScale(newScaleFactor, newScaleFactor, detector.getFocusX(), detector.getFocusY());

            panTranslations();
            return true;
        }
    }

    private void panTranslations() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getPannedTranslation(transX, viewWidth, imageWidth * scaleFactor);
        float fixTransY = getPannedTranslation(transY, viewHeight, imageHeight * scaleFactor);

        if (fixTransX != 0 || fixTransY != 0)
            matrix.postTranslate(fixTransX, fixTransY);
    }

    private float getPannedTranslation(float translation, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (translation < minTrans)
            return -translation + minTrans;
        if (translation > maxTrans)
            return -translation + maxTrans;
        return 0;
    }

    private float getPannedDragTranslation(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }
}