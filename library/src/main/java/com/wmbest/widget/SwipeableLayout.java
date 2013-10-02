package com.wmbest.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.*;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.nineoldandroids.animation.*;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class SwipeableLayout extends FrameLayout {
    protected static final String TAG = SwipeableLayout.class.getSimpleName();

    protected int mWidth;
    protected View mBackChild;
    protected View mFrontChild;
    protected View mTabChild;
    protected ViewGroup mFrontContainer;
    protected ViewGroup mBackContainer;

    private int mFrontId = -1;
    private int mBackId  = -1;
    private int mTabId   = -1;

    protected int mFrontNaturalHeight;
    protected int mBackNaturalHeight;

    protected boolean mIsOpen = false;
    protected boolean mIsAnimating = false;
    private boolean mSwipeable = true;

    protected Direction mDirection = Direction.LEFT;
    protected MotionGrabber mMotionGrabber;

    protected int mSwipeThresh;
    protected int mFrameGravity;
    protected int mGrabSize;
    protected int mPeekSize;

    private boolean mFromBind;
    private boolean mFromIntercept;

    private static String ISOPEN = "isopen";
    private static String FRONTNAT = "frontheight";
    private static String BACKNAT = "backheight";

    private OnOpenListener mListener;

    public SwipeableLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);

        mWidth = context.getResources().getDisplayMetrics().widthPixels;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeableLayout); 
         
        mFrontId = a.getResourceId(R.styleable.SwipeableLayout_frontView, -1);
        mBackId = a.getResourceId(R.styleable.SwipeableLayout_backView, -1);
        mTabId = a.getResourceId(R.styleable.SwipeableLayout_tabView, -1);

        mPeekSize = a.getDimensionPixelSize(R.styleable.SwipeableLayout_peekSize, 48);
        mGrabSize = a.getDimensionPixelSize(R.styleable.SwipeableLayout_grabSize, 48);
        mSwipeThresh = a.getDimensionPixelSize(R.styleable.SwipeableLayout_swipeThreshold, 20);
        mFrameGravity = a.getInt(R.styleable.SwipeableLayout_android_layout_gravity, 0);

        int dir = a.getInt(R.styleable.SwipeableLayout_direction, 0);
        mDirection = Direction.values()[dir];
        updateGrabber();

        a.recycle();

        if (mFrontId == -1 || mBackId == -1) {
            throw new RuntimeException("SwipableLayout requires a frontView and backView attribute to be set");
        }

    }

    public View getFrontView() {
        return mFrontContainer;
    }

    public void saveInstanceState(Bundle outState){
        outState.putBoolean(ISOPEN, mIsOpen);
        outState.putInt(FRONTNAT, mFrontNaturalHeight);
        outState.putInt(BACKNAT, mBackNaturalHeight);
    }

    public void restoreInstanceState(final Bundle saveState){
        if(saveState != null){
            post(new Runnable() {
                @Override
                public void run() {
                    mFrontNaturalHeight = saveState.getInt(FRONTNAT);
                    mBackNaturalHeight = saveState.getInt(BACKNAT);
                    if (saveState.getBoolean(ISOPEN)) {
                        open(false);
                    } else {
                        close(false);
                    }
                }
            });
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mBackChild =  findViewById(mBackId);
        mFrontChild = findViewById(mFrontId);

        if (mTabId != -1) {
            mTabChild = findViewById(mTabId);
        }

        mFrontContainer = wrapChild(mFrontChild);
        mBackContainer = wrapChild(mBackChild);

        mBackContainer.setVisibility(View.GONE);
        mFrontContainer.getLayoutParams().height  = LayoutParams.WRAP_CONTENT;
        mFrontContainer.setVisibility(View.VISIBLE);
    }

    private ViewGroup wrapChild(View aChild) {
        int index = indexOfChild(aChild);
        FrameLayout f = new FrameLayout(getContext());
        removeView(aChild);

        f.setBackgroundDrawable(aChild.getBackground());
        aChild.setBackgroundDrawable(null);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(f, index, params);
        f.addView(aChild, 0, aChild.getLayoutParams());

        return f;
    }

    public void setSwipeable(boolean aIsSwipeable) {
        mSwipeable = aIsSwipeable;
    }

    public void setOpen(boolean isOpen) {
        mIsOpen = isOpen;

        if (mTabChild != null) {
            mTabChild.setVisibility(mIsOpen ? View.GONE : View.VISIBLE);
        }

        float margin = mPeekSize - mWidth;
        if (mDirection == Direction.RIGHT) {
            margin = -margin;
        }

        if (mIsOpen) {
            ViewHelper.setX(mFrontContainer, margin);
            mBackContainer.setVisibility(View.VISIBLE);
        } else {
            ViewHelper.setX(mFrontContainer, 0);
            mBackContainer.setVisibility(View.GONE);
            mFrontContainer.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
            resetForContainer(mFrontContainer, mFrontChild);
        }
    }

    public void open(boolean animate) {
        mBackContainer.setVisibility(View.VISIBLE);

        int time = animate ? 300 : 0;

        android.util.Log.d("SwipeableLayout", "HEIGHT: " + getLayoutParams().height);

        float x = ViewHelper.getX(mFrontContainer);
        float margin = mPeekSize - mWidth;
        if (mDirection == Direction.RIGHT) {
            margin = -margin;
        }
        int height = (getLayoutParams().height < 0) ? mBackNaturalHeight : getLayoutParams().height;
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
            ObjectAnimator.ofFloat(mFrontContainer, "x", x, margin),
            getHeightAnimator(height, mBackNaturalHeight)
        );
        set.setDuration(time);
        set.setInterpolator(new OvershootInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator aAnimator) {
                mIsAnimating = false;
                mIsOpen = true;
                updateOpenState();
                getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                resetForContainer(mBackContainer, mBackChild);
            }
        });
        set.start();
    }

    public void close(boolean animate) {
        int time = animate ? 300 : 0;

        int height = (getLayoutParams().height < 0) ? mFrontNaturalHeight : getLayoutParams().height;

        float x = ViewHelper.getX(mFrontContainer);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
            ObjectAnimator.ofFloat(mFrontContainer, "x", x, 0f),
            getHeightAnimator(height, mFrontNaturalHeight)
        );
        set.setDuration(time);
        set.setInterpolator(new SlamInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator aAnimator) {
                mIsAnimating = false;
                mIsOpen = false;
                updateOpenState();

                resetForContainer(mBackContainer, mBackChild);
                mBackContainer.setVisibility(View.GONE);
                if (mTabChild != null) {
                    mTabChild.setVisibility(View.VISIBLE);
                }
                resetForContainer(mFrontContainer, mFrontChild);
                getLayoutParams().height = LayoutParams.WRAP_CONTENT;
            }
        });
        set.start();
    }
    
    private Animator getHeightAnimator(int start, int finish) {
        mIsAnimating = true;
        ValueAnimator va = ValueAnimator.ofInt(start, finish);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();

                int diff = Math.abs(getLayoutParams().height - value);
                if (diff > 0) {
                    getLayoutParams().height = value;
                    mFrontContainer.getLayoutParams().height = value;
                    mBackContainer.getLayoutParams().height = value;
                    requestLayout();
                }
            }
        });
        return va;
    }

    private void updateGrabber() {
        if (mDirection == Direction.LEFT) {
            mMotionGrabber = new LeftMotionGrabber();
        } else {
            mMotionGrabber = new RightMotionGrabber();
        }
    }

    public void setOnOpenListener(OnOpenListener aListener) {
        mListener = aListener;
    }

    protected void updateOpenState() {
        if (mListener != null) {
            mListener.onOpen(mIsOpen);
        }
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (!mIsAnimating) {
            mBackContainer.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
            mBackNaturalHeight = mBackContainer.getMeasuredHeight();

            if (mIsOpen) {
                mFrontContainer.getLayoutParams().height=mBackNaturalHeight;
            } else {
                mFrontContainer.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
                mFrontNaturalHeight = mFrontContainer.getMeasuredHeight();
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent aEvent) {

        if(!mSwipeable) return false;

        int action = aEvent.getActionMasked();
        final int x = (int) aEvent.getRawX();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mMotionGrabber.setTouchDown(x);
                break;
            case MotionEvent.ACTION_MOVE:
                mFromIntercept = mMotionGrabber.handleInterceptMove(x) || super.onInterceptTouchEvent(aEvent);
                return mFromIntercept;
        }

        return super.onInterceptTouchEvent(aEvent);

    }


    @Override
    public boolean onTouchEvent(MotionEvent aEvent) {

        if(!mSwipeable) return false;

        int action = aEvent.getActionMasked();
        final int x = (int) aEvent.getRawX();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mMotionGrabber.setTouchDown(x);
                mFromIntercept = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!mFromIntercept && mMotionGrabber.handleInterceptMove(x)) mFromIntercept = true;
                if (mFromIntercept) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    if (mTabChild != null && mTabChild.isShown()) {
                        mTabChild.setVisibility(View.GONE);
                    }
                    mMotionGrabber.handleTouchMove(x);
                    return true;
                }
                return false;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mFromIntercept) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    mFromIntercept = false;
                    handleTouchUp(x);
                    return true;
                }
                return false;
        }
        return super.onTouchEvent(aEvent);

    }

    private void fixContainerContents() {
        fixForContainer(mFrontContainer);
        fixForContainer(mBackContainer);
    }

    private void fixForContainer(ViewGroup aContainer) {
        if (aContainer.getChildAt(0) instanceof ImageView) return;
        if(aContainer.getChildAt(0).getVisibility() == View.GONE) return;

        aContainer.getChildAt(0).setDrawingCacheEnabled(true);
        Bitmap dc = Bitmap.createBitmap(aContainer.getChildAt(0).getDrawingCache());
        aContainer.getChildAt(0).setDrawingCacheEnabled(false);

        ImageView cache = new ImageView(getContext());
        cache.setScaleType(ImageView.ScaleType.MATRIX);
        cache.setImageBitmap(dc);
        cache.setTag("CACHE");

        aContainer.removeViewAt(0);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.gravity = mFrameGravity;
        aContainer.addView(cache, 0, params);
    }

    private void resetForContainer(ViewGroup aContainer, View aChild) {
        if (aContainer.getChildAt(0).getTag() == null) return;

        ImageView cache = (ImageView) aContainer.getChildAt(0);
        if(cache.getDrawable() instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) cache.getDrawable()).getBitmap();
            bitmap.recycle();
        }

        aContainer.removeViewAt(0);
        aContainer.addView(aChild, 0, aChild.getLayoutParams());
        aContainer.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
        aContainer.requestLayout();
    }

    private void handleTouchUp(int aX) {
        mMotionGrabber.handleRelease(aX);
    }

    public interface OnOpenListener {
        public void onOpen(boolean aOpen);
    }

    public static enum Direction {
        LEFT, RIGHT;
    }

    public abstract class MotionGrabber {
        protected int mTouchDownX;
        protected int mDeltaHeight;

        public void setTouchDown(int aTouchDown) {
            mTouchDownX = aTouchDown;
        }
        
        public int getTouchDown() {
            return mTouchDownX;
        }

        public abstract boolean handleInterceptMove(int aX);
        public abstract int getMargin(int aDelta);
        public abstract boolean shouldOpen(int aX);

        public void handleTouchMove(int aX) {
            int deltaX = aX - mTouchDownX;

            int startHeight = mIsOpen ?  mBackNaturalHeight : mFrontNaturalHeight;
            int margin = getMargin(deltaX);

            float ratio = (float) Math.abs(deltaX) / mWidth;
            int height = startHeight - (int)(ratio * mDeltaHeight);

            mIsAnimating = true;
            getLayoutParams().height = height;
            mFrontContainer.getLayoutParams().height = height;
            mBackContainer.getLayoutParams().height = height;
            requestLayout();

            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                ObjectAnimator.ofFloat(mFrontContainer, "x", margin, margin)
            );
            set.start();

            if (!mBackContainer.isShown()) {
                mBackContainer.setVisibility(View.VISIBLE);
                mBackChild.setVisibility(View.VISIBLE);
            } else {
                fixContainerContents();
            }

        }

        public void handleRelease(int aX) {
            if (shouldOpen(aX)) {
                open(true);
            } else {
                close(true);
            }
        }
    }

    public class LeftMotionGrabber extends MotionGrabber {
        @Override
        public boolean handleInterceptMove(int aX) {
            int deltaX = aX - mTouchDownX;
            boolean isIntercept = false;
            if (Math.abs(deltaX) > mSwipeThresh) {
                if(mIsOpen){
                    isIntercept = mTouchDownX < mGrabSize;
                    mDeltaHeight = mBackNaturalHeight - mFrontNaturalHeight;
                }else{
                    isIntercept = deltaX < 0;
                    mDeltaHeight = mFrontNaturalHeight - mBackNaturalHeight;
                }

            }
            return isIntercept;
        }

        @Override
        public int getMargin(int aDeltaX) {
            int startHeight;
            int margin;
            if (mIsOpen) {
                if (aDeltaX >= 0) {
                    margin = ((mPeekSize - mWidth) + aDeltaX);
                } else {
                    margin = mPeekSize - mWidth;
                    aDeltaX = 0;
                }
            }
            else {
                if (aDeltaX <= 0)
                    margin = aDeltaX;
                else {
                    margin = 0;
                    aDeltaX = 0;
                }
            }
            return margin;
        }

        @Override
        public boolean shouldOpen(int aX) { 
            return (aX <= mWidth/2); 
        }
    }

    public class RightMotionGrabber extends MotionGrabber {
        @Override
        public boolean handleInterceptMove(int aX) {
            int deltaX = aX - mTouchDownX;
            boolean isIntercept = false;
            if (Math.abs(deltaX) > mSwipeThresh) {
                if (mIsOpen) {
                    isIntercept = mTouchDownX > (mWidth - mGrabSize);
                    mDeltaHeight = mBackNaturalHeight - mFrontNaturalHeight;
                } else {
                    isIntercept = deltaX > 0;
                    mDeltaHeight = mFrontNaturalHeight - mBackNaturalHeight;
                }

            }
            return isIntercept;
        }

        @Override
        public int getMargin(int aDeltaX) {
            int margin;
            if (mIsOpen) {
                if (aDeltaX <= 0)
                    margin = mWidth + aDeltaX;
                else {
                    margin = mWidth - mPeekSize;
                    aDeltaX = 0;
                }
            }
            else {
                if (aDeltaX >= 0)
                    margin = aDeltaX;
                else {
                    margin = 0;
                    aDeltaX = 0;
                }
            }
            return margin;
        }

        @Override
        public boolean shouldOpen(int aX) { 
            return (aX >= mWidth/2); 
        }
    }

    public class SlamInterpolator implements Interpolator {
        float mTension = 2.0f;
        public float getInterpolation(float t) {
            // _o(t) = t * t * ((tension + 1) * t + tension)
            // o(t) = _o(t - 1) + 1
            t -= 1.0f;
            float val = t * t * ((mTension + 1) * t + mTension) + 1.0f;
            if (val > 1f) {
                val += 1.5 * (1f - val);
            }
            return val;
        }
    }

}
