package com.zhhz.reader.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecycleViewDivider extends RecyclerView.ItemDecoration {
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    private final int mOrientation;//列表的方向：LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
    private Paint mPaint;
    private Drawable mDivider;
    private int mDividerHeight = 2;//分割线高度，默认为1px

    /**
     * 默认分割线：高度为2px，颜色为灰色
     *
     * @param orientation 列表方向
     */
    public RecycleViewDivider(Context context, int orientation) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请输入正确的参数！");
        }
        mOrientation = orientation;

        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    /**
     * 自定义分割线
     *
     * @param orientation 列表方向
     * @param drawableId  分割线图片
     */
    public RecycleViewDivider(Context context, int orientation, int drawableId) {
        this(context, orientation);
        mDivider = ContextCompat.getDrawable(context, drawableId);
        assert mDivider != null;
        mDividerHeight = mDivider.getIntrinsicHeight();
    }

    /**
     * 自定义分割线
     *
     * @param orientation   列表方向
     * @param dividerHeight 分割线高度
     * @param dividerColor  分割线颜色
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight, int dividerColor) {
        this(context, orientation);
        mDividerHeight = dividerHeight;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.FILL);
    }


    //获取分割线尺寸
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, mDividerHeight);
        } else {
            outRect.set(0, 0, mDividerHeight, 0);
        }
    }

    //绘制分割线
    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    /**
     * 绘制纵向列表时的分隔线  这时分隔线是横着的
     * 每次 left相同，top根据child变化，right相同，bottom也变化
     *
     */
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mDividerHeight;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    /**
     * 绘制横向列表时的分隔线  这时分隔线是竖着的
     * l、r 变化； t、b 不变
     *
     */
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mDividerHeight;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }
}
