package com.wzq.customrefresh;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by wzq on 15/12/11.
 */
public class CustomRefreshLayout extends FrameLayout {

    private static final long BACK_TOP_DUR = 600;
    private static final long REL_DRAG_DUR = 200;

    private int mHeaderSwipeColor = 0xffff3d00;
    private int mHeaderCircleColor = 0xffffccbc;
    private int mHeaderBackColor = 0xfff5f5f5;
    private int mHeaderRadius = 14;
    private int mHeaderTextColor = Color.GRAY;
    private float mHeaderTextSize = 12;


    private float mPullHeight;
    private float mHeaderHeight;
    private View mChildView;
    private AnimationView mHeader;

    private boolean mIsRefreshing;

    private float mTouchStartY;

    private float mTouchCurY;

    private ValueAnimator mUpBackAnimator;
    private ValueAnimator mUpTopAnimator;

    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(10);

    public CustomRefreshLayout(Context context) {
        this(context, null, 0);
    }

    public CustomRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        if (getChildCount() > 1) {
            throw new RuntimeException("you can only attach one child");
        }

        setAttrs(attrs);
        mHeaderRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mHeaderRadius, context.getResources().getDisplayMetrics());
        mHeaderTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mHeaderTextSize, context.getResources().getDisplayMetrics());

        mPullHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130, context.getResources().getDisplayMetrics());
        mHeaderHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());

        this.post(new Runnable() {
            @Override
            public void run() {
                mChildView = getChildAt(0);
                addHeaderView();
            }
        });

    }

    private void setAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomRefreshLayout);

        mHeaderBackColor = a.getColor(R.styleable.CustomRefreshLayout_AnimBackColor, mHeaderBackColor);
        mHeaderCircleColor = a.getColor(R.styleable.CustomRefreshLayout_AnimCircleColor, mHeaderCircleColor);
        mHeaderSwipeColor = a.getColor(R.styleable.CustomRefreshLayout_AnimSwipeColor, mHeaderSwipeColor);
        mHeaderRadius = a.getInt(R.styleable.CustomRefreshLayout_AnimRadius, mHeaderRadius);
        mHeaderTextColor = a.getInt(R.styleable.CustomRefreshLayout_AnimTextColor, mHeaderTextColor);
        mHeaderTextSize = a.getFloat(R.styleable.CustomRefreshLayout_AnimTextSize, mHeaderTextSize);

        a.recycle();
    }

    private void addHeaderView() {
        mHeader = new AnimationView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        params.gravity = Gravity.TOP;
        mHeader.setLayoutParams(params);

        addViewInternal(mHeader);
        mHeader.setAniBackColor(mHeaderBackColor);
        mHeader.setAniSwipeColor(mHeaderSwipeColor);
        mHeader.setAniCircleColor(mHeaderCircleColor);
        mHeader.setRadius(mHeaderRadius);
        mHeader.setTipTextColor(mHeaderTextColor);
        mHeader.setTipTextSize(mHeaderTextSize);

        setUpChildAnimation();
    }

    private void setUpChildAnimation() {
        if (mChildView == null) {
            return;
        }
        mUpBackAnimator = ValueAnimator.ofFloat(mPullHeight, mHeaderHeight);
        mUpBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                if (mChildView != null) {
                    mChildView.setTranslationY(val);
                }
            }
        });
        mUpBackAnimator.setDuration(REL_DRAG_DUR);
        mUpTopAnimator = ValueAnimator.ofFloat(mHeaderHeight, 0);
        mUpTopAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                val = decelerateInterpolator.getInterpolation(val / mHeaderHeight) * val;
                if (mChildView != null) {
                    mChildView.setTranslationY(val);
                }
                mHeader.getLayoutParams().height = (int) val;
                mHeader.requestLayout();
            }
        });
        mUpTopAnimator.setDuration(BACK_TOP_DUR);

        mHeader.setOnViewAniDone(new AnimationView.OnViewAniDone() {
            @Override
            public void viewAniDone() {
//                Log.i(TAG, "should invoke");
                mUpTopAnimator.start();
            }
        });


    }

    private void addViewInternal(@NonNull View child) {
        super.addView(child);
    }

    @Override
    public void addView(View child) {
        if (getChildCount() >= 1) {
            throw new RuntimeException("you can only attach one child");
        }

        mChildView = child;
        super.addView(child);
        setUpChildAnimation();
    }

    private boolean canChildScrollUp() {
        if (mChildView == null) {
            return false;
        }


        return ViewCompat.canScrollVertically(mChildView, -1);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsRefreshing) {
            return true;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartY = ev.getY();
                mTouchCurY = mTouchStartY;
                break;
            case MotionEvent.ACTION_MOVE:
                float curY = ev.getY();
                float dy = curY - mTouchStartY;
                if (dy > 0 && !canChildScrollUp()) {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsRefreshing) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mTouchCurY = event.getY();
                float dy = mTouchCurY - mTouchStartY;
                dy = Math.min(mPullHeight * 2, dy);
                dy = Math.max(0, dy);


                if (mChildView != null) {
                    float offsetY = decelerateInterpolator.getInterpolation(dy / 2 / mPullHeight) * dy / 2;
                    mChildView.setTranslationY(offsetY);

                    mHeader.getLayoutParams().height = (int) offsetY;
                    mHeader.requestLayout();
                }


                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mChildView != null) {
                    if (mChildView.getTranslationY() >= mHeaderHeight) {
                        mUpBackAnimator.start();
                        mHeader.releaseDrag();
                        mIsRefreshing = true;
                        if (onCircleRefreshListener!=null) {
                            onCircleRefreshListener.refreshing();
                        }

                    } else {
                        float height = mChildView.getTranslationY();
                        ValueAnimator backTopAni = ValueAnimator.ofFloat(height, 0);
                        backTopAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float val = (float) animation.getAnimatedValue();
                                val = decelerateInterpolator.getInterpolation(val / mHeaderHeight) * val;
                                if (mChildView != null) {
                                    mChildView.setTranslationY(val);
                                }
                                mHeader.getLayoutParams().height = (int) val;
                                mHeader.requestLayout();
                            }
                        });
                        backTopAni.setDuration((long) (height * BACK_TOP_DUR / mHeaderHeight));
                        backTopAni.start();
                    }
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    public void finishRefreshing() {
        if (onCircleRefreshListener != null) {
            onCircleRefreshListener.completeRefresh();
        }
        mIsRefreshing = false;
        mHeader.setRefreshing(false);
    }

    private OnCircleRefreshListener onCircleRefreshListener;

    public void setOnRefreshListener(OnCircleRefreshListener onCircleRefreshListener) {
        this.onCircleRefreshListener = onCircleRefreshListener;
    }

    public interface OnCircleRefreshListener {
        void completeRefresh();

        void refreshing();
    }
}
