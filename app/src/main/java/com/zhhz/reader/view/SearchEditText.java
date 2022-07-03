package com.zhhz.reader.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

import com.zhhz.reader.R;

import java.util.Objects;

public class SearchEditText extends AppCompatEditText implements

        View.OnFocusChangeListener, TextWatcher {


    private Drawable mClearDrawable;
    private Drawable mSearchDrawable;
    private Drawable mSearchingDrawable;
    private boolean hasFoucs;
    private Context context;
    private onSearchFocusListener onSearchFocusListener;

    public SearchEditText(Context context) {
        this(context, null);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }


    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {

        if (mClearDrawable == null) {
            mClearDrawable = getResources().getDrawable(R.drawable.search_clear, null);
        }

        mSearchDrawable = getResources().getDrawable(R.drawable.search_icon, null);
        mSearchingDrawable = getResources().getDrawable(R.drawable.search_icon, null);

        mClearDrawable.setBounds(-mClearDrawable.getMinimumWidth(), 0, 0, mClearDrawable.getMinimumWidth());
        //默认设置隐藏图标
        setClearIconVisible(false);
        //设置焦点改变的监听
        setOnFocusChangeListener(this);
        //设置输入框里面内容发生改变的监听
        addTextChangedListener(this);


    }

    public void changeSearchLogo(boolean isSearch) {
        Drawable drawable;
        if (isSearch) {
            drawable = mSearchingDrawable;
        } else {
            drawable = mSearchDrawable;
        }
        setCompoundDrawablePadding(drawable.getMinimumWidth());
        drawable.setBounds((int) (drawable.getMinimumWidth() * 0.75f), 0, (int) (drawable.getMinimumWidth() * 1.75f), drawable.getMinimumHeight());
        setCompoundDrawables(drawable, null, null, null);

    }

    /**
     * 因为我们不能直接给EditText设置点击事件，所以我们用记住我们按下的位置来模拟点击事件
     * 当我们按下的位置 在  EditText的宽度 - 图标到控件右边的间距 - 图标的宽度  和
     * EditText的宽度 - 图标到控件右边的间距之间我们就算点击了图标，竖直方向就没有考虑
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {

                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())
                        && (event.getX() < ((getWidth() - getPaddingRight())));

                if (touchable) {
                    this.setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }

    /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     */
    protected void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (hasFoucs) {
            setClearIconVisible(s.length() > 0);
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFoucs = hasFocus;
        if (hasFocus) {
            setClearIconVisible(Objects.requireNonNull(getText()).length() > 0);
        } else {
            setClearIconVisible(false);
        }
        onSearchFocusListener.onSearchFocusChange(v, hasFocus);
    }

    public void setOnSearchFocusListener(onSearchFocusListener onSearchFocusListener) {
        this.onSearchFocusListener = onSearchFocusListener;
    }


    public interface onSearchFocusListener {

        void onSearchFocusChange(View v, boolean hasFocus);

    }


}
