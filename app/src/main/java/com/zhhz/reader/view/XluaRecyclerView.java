package com.zhhz.reader.view;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class XluaRecyclerView extends RecyclerView {

    String Tag = "XluaRecyclerView";

    public XluaRecyclerView(@NonNull Context context) {
        super(context);
    }

    public XluaRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public XluaRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //缩放比例因子
    public float mScaleFactor = 1.0f;
    //默认缩放比例因子
    public final float defaultScaleFactor = 1.0f;

    public float centerX;

    public float centerY;

    public float mPosX;

    public float mPosY;

    private final ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            XluaRecyclerView.this.mScaleFactor = XluaRecyclerView.this.mScaleFactor * scaleGestureDetector.getScaleFactor();
            XluaRecyclerView.this.centerX = scaleGestureDetector.getFocusX();
            XluaRecyclerView.this.centerY = scaleGestureDetector.getFocusY();
            //最大缩放3倍
            XluaRecyclerView.this.mScaleFactor = Math.max(XluaRecyclerView.this.defaultScaleFactor, Math.min(XluaRecyclerView.this.mScaleFactor, 3.0f));


            float w1 = centerX * (mScaleFactor - 1);
            float w2 = (centerX - getWidth()) * (mScaleFactor - 1);

            float h1 = centerY * (mScaleFactor - 1);
            float h2 = (centerY - getHeight()) * (mScaleFactor - 1);


            if (mPosX > w1) {
                mPosX = w1;
            } else if (mPosX < w2) {
                mPosX = w2;
            }

            if (mPosY > h1) {
                mPosY = h1;
            } else if (mPosY < h2) {
                mPosY = h2;
            }

            polydactylism = true;

            XluaRecyclerView.this.invalidate();
            return true;
        }
    }

    private float x;
    private float y;
    private float x1;
    private float y1;

    //是否多指触碰
    private boolean polydactylism = false;

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        int action = motionEvent.getActionMasked();
        //触摸个数
        int actions = motionEvent.getPointerCount();

        if (actions > 1) {
            polydactylism = true;
            this.mScaleDetector.onTouchEvent(motionEvent);
            return false;
        }
        if (polydactylism) {
            polydactylism = false;
            x = motionEvent.getX(0);
            y = motionEvent.getY(0);

        }

        if (action == ACTION_DOWN) {
            x = motionEvent.getX(0);
            y = motionEvent.getY(0);
        } else if (action == ACTION_MOVE) {
            if (this.mScaleFactor > this.defaultScaleFactor) {
                float w1 = centerX * (mScaleFactor - 1);
                float w2 = (centerX - getWidth()) * (mScaleFactor - 1);

                float h1 = centerY * (mScaleFactor - 1);
                float h2 = (centerY - getHeight()) * (mScaleFactor - 1);

                mPosX = motionEvent.getX(0) - x + x1;
                mPosY = motionEvent.getY(0) - y + y1;

                if (mPosX > w1) {
                    mPosX = w1;
                } else if (mPosX < w2) {
                    mPosX = w2;
                }

                if (mPosY > h1) {
                    mPosY = h1;
                } else if (mPosY < h2) {
                    mPosY = h2;
                }

            }
        } else if (action == ACTION_UP) {
            x1 = mPosX;
            y1 = mPosY;
        }

        return true;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        //return super.fling(velocityX, velocityY);
        return super.fling((int) (velocityY / mScaleFactor), (int) (velocityY / mScaleFactor));
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (this.mScaleFactor == this.defaultScaleFactor) {
            this.mPosX = 0.0f;
            this.mPosY = 0.0f;
        }

        canvas.save();
        canvas.translate(this.mPosX, this.mPosY);
        canvas.scale(this.mScaleFactor, this.mScaleFactor, this.centerX, this.centerY);
        super.dispatchDraw(canvas);
        canvas.restore();
        invalidate();
    }
}
