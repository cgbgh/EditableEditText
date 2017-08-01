package com.app.cgb.edittextmanager;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * Created by cgb on 2017/7/26.
 */

public class EditTextManagerView extends FrameLayout implements EditableEditText.OnStateChange {
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 300;
    private static final int DEFAULT_BORDER_WIDTH = 4;
    private static final int DEFAULT_CIRCLE_SIZE = 36;
    private static final int DEFAULT_CORLOR = Color.BLUE;
    private static final int DEFAULT_TEXT_SIZE = 16;
    private int borderWidth;
    private int circleRadius;
    private int color;
    private int textSize;
    private int itemWidth;
    private int itemHeight;
    private final int touchSlop;
    private float downX;
    private float downY;
    private boolean isAllHided = true;

    public EditTextManagerView(@NonNull Context context) {
        this(context, null);
    }

    public EditTextManagerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextManagerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initAttrs(context,attrs,defStyleAttr);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EditTextManagerView, defStyleAttr, 0);
        borderWidth = ta.getDimensionPixelSize(
                R.styleable.EditTextManagerView_borderWidth, DEFAULT_BORDER_WIDTH);
        circleRadius = ta.getDimensionPixelSize(
                R.styleable.EditTextManagerView_controlCircleRadius, DEFAULT_CIRCLE_SIZE);
        textSize = ta.getDimensionPixelSize(
                R.styleable.EditTextManagerView_textSize, DEFAULT_TEXT_SIZE);
        itemWidth = ta.getDimensionPixelSize(
                R.styleable.EditTextManagerView_itemWidth, DEFAULT_WIDTH);
        itemHeight = ta.getDimensionPixelSize(
                R.styleable.EditTextManagerView_itemHeight, DEFAULT_HEIGHT);
        color = ta.getColor(R.styleable.EditableEditText_color, DEFAULT_CORLOR);
        ta.recycle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(downX - event.getX()) < touchSlop
                        && Math.abs(downY - event.getY()) < touchSlop) {
                    addOrHideView();
                }
                break;
        }
        return true;
    }

    private void addOrHideView() {
        if (isAllHided) {
            LayoutParams params = new LayoutParams(itemWidth, itemHeight);
            EditableEditText eet = new EditableEditText(getContext());
            eet.setColor(color);
            eet.setTextSize(textSize);
            eet.setCircleRadius(circleRadius);
            eet.setBorderWidth(borderWidth);
            addView(eet, params);
            setupTransition(eet);
            requestChildFocus(eet, getFocusedChild());
            eet.setOnStateChange(this);
            hideChild(eet);
        } else hideChild(null);
    }

    private void setupTransition(EditableEditText eet) {
        float translationX = downX - itemWidth / 2;
        float translationY = downY - itemHeight / 2;
        if (translationX < 0) translationX = 0;
        else if (translationX + itemWidth > getWidth())
            translationX = getWidth() - itemWidth;
        if (translationY < 0) translationY = 0;
        else if (translationY + itemHeight > getHeight())
            translationY = getHeight() - itemHeight;
        eet.setTranslationX(translationX);
        eet.setTranslationY(translationY);
    }

    private void hideChild(View view) {
        if(view == null) isAllHided = true;
        else isAllHided = false;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child != view && child instanceof EditableEditText) {
                ((EditableEditText) child).hideBorder();
            }
        }
    }

    @Override
    public void onFocus(EditableEditText view) {
        hideChild(view);
    }

    @Override
    public void onDelete() {
        isAllHided = true;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        invalidate();
    }

    public int getCircleRadius() {
        return circleRadius;
    }

    public void setCircleRadius(int circleRadius) {
        this.circleRadius = circleRadius;
        invalidate();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        invalidate();
    }

    public int getItemWidth() {
        return itemWidth;
    }

    public void setItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }
}
