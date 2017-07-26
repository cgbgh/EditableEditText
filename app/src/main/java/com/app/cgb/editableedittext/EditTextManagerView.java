package com.app.cgb.editableedittext;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Created by cgb on 2017/7/26.
 */

public class EditTextManagerView extends FrameLayout implements EditableEditText.OnFocus {
    private final int touchSlop;
    private float downX;
    private float downY;

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

                    LayoutParams params = new LayoutParams(500, 300);
                    EditableEditText eet = new EditableEditText(getContext());
                    addView(eet, params);
                    eet.setTranslationX(downX);
                    eet.setTranslationY(downY);
                    requestChildFocus(eet, getFocusedChild());
                    eet.setOnFocus(this);
                    hideChild(eet);
                }
                break;
        }
        return true;
    }

    private void hideChild(View view) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof EditableEditText && child != view) {
                ((EditableEditText) child).hideBorder();
            }
        }
    }

    @Override
    public void onFocus(EditableEditText view) {
        hideChild(view);
    }
}
