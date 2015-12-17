package com.wzq.customrefresh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wzq on 15/12/11.
 */
public class AnimationView extends View {

    private int PULL_HEIGHT;
    private int PULL_DELTA;
    private float mWidthOffset;
    private int circleY;


    private AnimatorStatus mAniStatus = AnimatorStatus.PULL_DOWN;

    enum AnimatorStatus {
        PULL_DOWN,
        DRAG_DOWN,
        REL_DRAG,
        SPRING_UP, // rebound to up, the position is less than PULL_HEIGHT
        POP_BALL,
        OUTER_CIR,
        REFRESHING,
        DONE,
        STOP,
        UP;

        @Override
        public String toString() {
            switch (this) {
                case PULL_DOWN:
                    return "pull down";
                case DRAG_DOWN:
                    return "drag down";
                case REL_DRAG:
                    return "release drag";
                case SPRING_UP:
                    return "spring up";
                case POP_BALL:
                    return "pop ball";
                case OUTER_CIR:
                    return "outer circle";
                case REFRESHING:
                    return "refreshing...";
                case DONE:
                    return "done!";
                case STOP:
                    return "stop";
                case UP:
                    return "up";
                default:
                    return "unknown state";
            }
        }
    }


    private Paint mBackPaint;
    private Paint mOutPaint;
    private Paint mInPaint;
    private Paint mTextPaint;
    private Path mPath;


    public AnimationView(Context context) {
        this(context, null, 0);
    }

    public AnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {

        PULL_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());
        PULL_DELTA = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());
        mWidthOffset = 0.5f;

        mBackPaint = new Paint();
        mBackPaint.setAntiAlias(true);
        mBackPaint.setStyle(Paint.Style.FILL);

        mOutPaint = new Paint();
        mOutPaint.setAntiAlias(true);
        mOutPaint.setStyle(Paint.Style.STROKE);
        mOutPaint.setStrokeWidth(5);


        mInPaint = new Paint();
        mInPaint.setAntiAlias(true);
        mInPaint.setStyle(Paint.Style.STROKE);
        mInPaint.setStrokeWidth(5);

        mTextPaint = new Paint( Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mPath = new Path();

    }

    private int mRadius;
    private int mWidth;
    private int mHeight;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (height > PULL_DELTA + PULL_HEIGHT) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(PULL_DELTA + PULL_HEIGHT, MeasureSpec.getMode(heightMeasureSpec));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mRadius = PULL_HEIGHT / 6;
            circleY = PULL_HEIGHT - PULL_DELTA / 2 - mRadius*3;
            mWidth = getWidth();
            mHeight = getHeight();

            if (mHeight < 10) mAniStatus = AnimatorStatus.PULL_DOWN;
            if (mHeight < PULL_HEIGHT && mAniStatus!=AnimatorStatus.UP) {
                mAniStatus = AnimatorStatus.PULL_DOWN;
            }


            switch (mAniStatus) {
                case PULL_DOWN:
                    if (mHeight >= PULL_HEIGHT) {
                        mAniStatus = AnimatorStatus.DRAG_DOWN;
                    }
                    break;
                case REL_DRAG:
                    break;
            }

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (mAniStatus) {
            case PULL_DOWN:
                drawCircle(canvas);
                setTips(canvas,"下拉刷新" );
                break;
            case REL_DRAG:
            case DRAG_DOWN:
                drawCircle(canvas);
                drawDrag(canvas);
                setTips(canvas, "松开刷新");
                break;
            case OUTER_CIR:
                drawOutCir(canvas);
                invalidate();
                break;
            case REFRESHING:
                drawRefreshing(canvas);
                setTips(canvas, "刷新中");
                invalidate();
                break;
            case UP:
                drawCircle(canvas);
                setTips(canvas, new SimpleDateFormat().format(new Date()));
                break;

        }

        if (mAniStatus == AnimatorStatus.REL_DRAG) {
            ViewGroup.LayoutParams params = getLayoutParams();
            int height;
            // NOTICE: If the height equals mLastHeight, then the requestLayout() will not work correctly
            do {
                height = getRelHeight();
            } while (height == mLastHeight && getRelRatio() != 1);
            mLastHeight = height;
            params.height = PULL_HEIGHT + height;
            requestLayout();
        }


    }

    private void drawCircle(Canvas canvas) {
        canvas.drawRect(0, 0, mWidth, mHeight > PULL_HEIGHT ? PULL_HEIGHT : mHeight, mBackPaint);

        int angle = 360 * (mHeight) / PULL_HEIGHT;

        canvas.drawCircle(mWidth / 2, circleY, mRadius, mInPaint);

        canvas.drawArc(new RectF(mWidth / 2 - mRadius, circleY - mRadius, mWidth / 2 + mRadius, circleY + mRadius),
                -90, angle, false, mOutPaint);

    }

    private void setTips(Canvas canvas, String s) {
        canvas.drawText(s, mWidth / 2, circleY + mRadius*2 + 20, mTextPaint);
    }

    private void drawDrag(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(0, PULL_HEIGHT);
        mPath.quadTo(mWidthOffset * mWidth, PULL_HEIGHT + (mHeight - PULL_HEIGHT) * 2,
                mWidth, PULL_HEIGHT);
        canvas.drawPath(mPath, mBackPaint);
    }


    private void drawOutCir(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.lineTo(0, PULL_HEIGHT);
        mPath.quadTo(mWidth / 2, PULL_HEIGHT - (1 - getOutRatio()) * PULL_DELTA,
                mWidth, PULL_HEIGHT);
        mPath.lineTo(mWidth, 0);
        canvas.drawPath(mPath, mBackPaint);
    }

    private int mRefreshStart = 90;
    private int mRefreshStop = 90;
    private int TARGET_DEGREE = 270;
    private boolean mIsStart = true;
    private boolean mIsRefreshing = true;

    private void drawRefreshing(Canvas canvas) {
        canvas.drawRect(0, 0, mWidth, mHeight, mBackPaint);
        int innerY = circleY;
        //canvas.drawCircle(mWidth / 2, innerY, mRadius, mBallPaint);
        int outerR = mRadius;

        mRefreshStart += mIsStart ? 3 : 10;
        mRefreshStop += mIsStart ? 10 : 3;
        mRefreshStart = mRefreshStart % 360;
        mRefreshStop = mRefreshStop % 360;

        int swipe = mRefreshStop - mRefreshStart;
        swipe = swipe < 0 ? swipe + 360 : swipe;

        canvas.drawCircle(mWidth / 2, innerY, mRadius, mInPaint);

        canvas.drawArc(new RectF(mWidth / 2 - outerR, innerY - outerR, mWidth / 2 + outerR, innerY + outerR),
                mRefreshStart, swipe, false, mOutPaint);
        if (swipe >= TARGET_DEGREE) {
            mIsStart = false;
        } else if (swipe <= 10) {
            mIsStart = true;
        }
        if (!mIsRefreshing) {
            applyDone();

        }

    }

    // stop refreshing
    public void setRefreshing(boolean isFresh) {
        mIsRefreshing = isFresh;
    }

    private int mLastHeight;

    private int getRelHeight() {
        return (int) (mSpriDeta * (1 - getRelRatio()));
    }


    private static long REL_DRAG_DUR = 200;

    private long mStart;
    private long mStop;
    private int mSpriDeta;

    public void releaseDrag() {
        mStart = System.currentTimeMillis();
        mStop = mStart + REL_DRAG_DUR;
        mAniStatus = AnimatorStatus.REL_DRAG;
        mSpriDeta = mHeight - PULL_HEIGHT;
        requestLayout();
    }

    private float getRelRatio() {
        if (System.currentTimeMillis() >= mStop) {
            springUp();
            return 1;
        }
        float ratio = (System.currentTimeMillis() - mStart) / (float) REL_DRAG_DUR;
        return Math.min(ratio, 1);
    }

    private static long SPRING_DUR = 200;
    private long mSprStart;
    private long mSprStop;


    private void springUp() {
        mSprStart = System.currentTimeMillis();
        mSprStop = mSprStart + SPRING_DUR;
        mAniStatus = AnimatorStatus.OUTER_CIR;
        invalidate();
    }


    private static final long OUTER_DUR = 200;
    private long mOutStart;
    private long mOutStop;

    private void startOutCir() {
        mOutStart = System.currentTimeMillis();
        mOutStop = mOutStart + OUTER_DUR;
        mAniStatus = AnimatorStatus.OUTER_CIR;
        mRefreshStart = 90;
        mRefreshStop = 90;
        TARGET_DEGREE = 270;
        mIsStart = true;
        mIsRefreshing = true;
        invalidate();
    }

    private float getOutRatio() {
        if (System.currentTimeMillis() >= mOutStop) {
            mAniStatus = AnimatorStatus.REFRESHING;
            mIsRefreshing = true;
            return 1;
        }
        float ratio = (System.currentTimeMillis() - mOutStart) / (float) OUTER_DUR;
        return Math.min(ratio, 1);
    }

    private static final long DONE_DUR = 1000;
    private long mDoneStart;
    private long mDoneStop;

    private void applyDone() {
        mDoneStart = System.currentTimeMillis();
        mDoneStop = mDoneStart + DONE_DUR;
        mAniStatus = AnimatorStatus.UP;
        if (onViewAniDone != null) {
            onViewAniDone.viewAniDone();
        }
    }



    private OnViewAniDone onViewAniDone;

    public void setOnViewAniDone(OnViewAniDone onViewAniDone) {
        this.onViewAniDone = onViewAniDone;
    }

    interface OnViewAniDone {
        void viewAniDone();
    }


    public void setAniBackColor(int color) {
        mBackPaint.setColor(color);
    }

    public void setAniSwipeColor(int color) {
        mOutPaint.setColor(color);
    }

    // the height of view is smallTimes times of circle radius
    public void setRadius(int smallTimes) {
        mRadius = smallTimes;
    }

    public void setAniCircleColor(int color){
        mInPaint.setColor(color);
    }

    public void setTipTextColor(int color){
        mTextPaint.setColor(color);
    }

    public void setTipTextSize(float size){
        mTextPaint.setTextSize(size);
    }
}
