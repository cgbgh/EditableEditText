package com.app.cgb.editableedittext;

import android.content.Context;
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
                    if (isAllHided) {
                        LayoutParams params = new LayoutParams(DEFAULT_WIDTH, DEFAULT_HEIGHT);
                        EditableEditText eet = new EditableEditText(getContext());
                        addView(eet, params);
                        setupTransition(eet);
                        requestChildFocus(eet, getFocusedChild());
                        eet.setOnStateChange(this);
                        hideChild(eet);
                    } else hideChild(null);
                }
                break;
        }
        return true;
    }

    private void setupTransition(EditableEditText eet) {
        float translationX = downX - DEFAULT_WIDTH / 2;
        float translationY = downY - DEFAULT_HEIGHT / 2;
        if (translationX < 0) translationX = 0;
        else if (translationX + DEFAULT_WIDTH > getWidth())
            translationX = getWidth() - DEFAULT_WIDTH;
        if (translationY < 0) translationY = 0;
        else if (translationY + DEFAULT_HEIGHT > getHeight())
            translationY = getHeight() - DEFAULT_HEIGHT;
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
}
