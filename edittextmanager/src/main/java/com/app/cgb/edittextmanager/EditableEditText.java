package com.app.cgb.edittextmanager;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.InputType;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

/**
 * Created by cgb on 2017/7/26.
 */

public class EditableEditText extends FrameLayout {
    private static final int STATE_NORMAL = 100;
    private static final int STATE_MOVE = 101;
    private static final int STATE_RESIZE = 102;
    private static final int STATE_DELETE = 103;
    private static final int DEFAULT_BORDER_WIDTH = 4;
    private static final int DEFAULT_CIRCLE_SIZE = 36;
    private static final int DEFAULT_CORLOR = Color.BLUE;
    private static final int DEFAULT_TEXT_SIZE = 16;
    private float lastY;
    private float lastX;
    private int state;
    private Paint borderPaint;
    private Paint circlePaint;
    private TextPaint textPaint;
    private int borderWidth;
    private int circleRadius;
    private int color;
    private int minWidth;
    private int minHeight;
    private int parentWidth;
    private int parentHeight;
    private float downX;
    private float downY;
    private int textSize;
    private int paddingBottom;
    private int paddingTop;
    private int paddingRight;
    private int paddingLeft;
    private EditText et;
    private boolean isBorderVisible = true;
    private int touchSlop;
    private OnStateChange onStateChange;

    public EditableEditText(Context context) {
        this(context, null);
    }

    public EditableEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setWillNotDraw(false);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initAttrs(context, attrs, defStyleAttr);
        setupEditTextStyle(context);
        getPaddings();
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        initPaint();
    }

    private void getPaddings() {
        paddingLeft = getPaddingLeft();
        paddingRight = getPaddingRight();
        paddingTop = getPaddingTop();
        paddingBottom = getPaddingBottom();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EditableEditText, defStyleAttr, 0);
        borderWidth = ta.getDimensionPixelSize(R.styleable.EditableEditText_borderWidth, DEFAULT_BORDER_WIDTH);
        circleRadius = ta.getDimensionPixelSize(R.styleable.EditableEditText_controlCircleRadius, DEFAULT_CIRCLE_SIZE);
        textSize = ta.getDimensionPixelSize(R.styleable.EditableEditText_textSize, DEFAULT_TEXT_SIZE);
        color = ta.getColor(R.styleable.EditableEditText_color, DEFAULT_CORLOR);
        ta.recycle();
    }

    private void setupEditTextStyle(Context context) {
        et = new EditText(context);
        et.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        et.setGravity(Gravity.TOP);
        et.setSingleLine(false);
        et.setHorizontallyScrolling(false);
        et.setBackgroundDrawable(null);
        et.setTextSize(textSize);
        et.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onActionDown(event);
                return false;
            }
        });
        addView(et);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        int insetSize = circleRadius * 2;
        super.setPadding(left + insetSize, top + insetSize, right + insetSize, bottom + insetSize);
    }

    private void initPaint() {
        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setAntiAlias(true);

        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.FILL);

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
    }


    private void reset() {
        state = STATE_NORMAL;
    }

    private void removeSelf() {
        if (!isBorderVisible) return;
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            if (onStateChange != null) onStateChange.onDelete();
            parent.removeView(this);
        }
    }

    private void actionByState(float x, float y) {
        if (!isBorderVisible) return;
        if (state == STATE_MOVE) {
            moveByTranslation(x, y);
//            moveByLayout((int)x,(int)y);
//            moveByParams(x,y);
//            moveByScroll(x,y);
        } else {
            resize((int) x, (int) y);
        }
    }

    private void resize(int x, int y) {
        ViewGroup.LayoutParams params = getLayoutParams();
        int width = getWidth() + x;
        int height = getHeight() + y;
        if (width < minWidth) {
            width = minWidth;
        } else if (width > parentWidth - getTranslationX()) {
            width = (int) (parentWidth - getTranslationX());
        }
        if (height < minHeight) {
            height = minHeight;
        } else if (height > parentHeight - getTranslationY()) {
            height = (int) (parentHeight - getTranslationY());
        }
        params.width = width;
        params.height = height;
        setLayoutParams(params);
    }

    private void setStateByPoint(float x, float y) {
        int range = circleRadius * 2;
        if (x > getWidth() - range && y > getHeight() - range) {
            state = STATE_RESIZE;

        } else if (x < range && y < range) {
            state = STATE_DELETE;
        } else {
            state = STATE_MOVE;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        getParentSize();
    }

    private void getParentSize() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parentWidth = parent.getWidth();
            parentHeight = parent.getHeight();
        }
    }

    private void moveByTranslation(float x, float y) {
        float translationX = getTranslationX() + x;
        float translationY = getTranslationY() + y;
        float viewX = getX();
        float viewY = getY();
        if (viewX + x < 0) {
            translationX = 0;
        }
        if (viewY + y < 0) {
            translationY = 0;
        }
        if (viewX + x > parentWidth - getWidth()) {
            translationX = parentWidth - getWidth();
        }
        if (viewY + y > parentHeight - getHeight()) {
            translationY = parentHeight - getHeight();
        }
        setTranslationX(translationX);
        setTranslationY(translationY);

    }

    /**
     * 用layout方法刷新会回到原位
     *
     * @param x
     * @param y
     */
    private void moveByLayout(int x, int y) {
        layout(getLeft() + x, getTop() + y, getRight() + x, getBottom() + y);
    }

    /**
     * 网上有提到部分手机不适用
     *
     * @param x
     * @param y
     */
    private void moveByParams(float x, float y) {
        ViewGroup.MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
        params.leftMargin += x;
        params.topMargin += y;
        setLayoutParams(params);
    }

    /**
     * 只能在控件内部滑动，超出范围会被遮挡
     *
     * @param x
     * @param y
     */
    private void moveByScroll(float x, float y) {
        scrollBy(-(int) x, -(int) y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        int width = getWidth();
        int height = getHeight();
        drawBorder(canvas, width, height);
        drawCircle(canvas, circleRadius, circleRadius);
        drawCircle(canvas, width - circleRadius, height - circleRadius);
        drawText(canvas);
    }

    private void setupMinSize() {
        minWidth = circleRadius * 4;
        minHeight = circleRadius * 4 ;
    }

    private void drawText(Canvas canvas) {
        textPaint.setTextSize(circleRadius * 1.5f);
        StaticLayout staticLayout = new StaticLayout("X", textPaint, circleRadius * 2, Layout.Alignment.ALIGN_CENTER, 1.0f, 1.0f, true);
        staticLayout.draw(canvas);
    }

    private void drawCircle(Canvas canvas, int cx, int cy) {
        circlePaint.setColor(color);
        canvas.drawCircle(cx, cy, circleRadius, circlePaint);
    }

    private void drawBorder(Canvas canvas, int width, int height) {
        Rect rect = new Rect(circleRadius, circleRadius, width - circleRadius, height - circleRadius);
        borderPaint.setColor(color);
        canvas.drawRect(rect, borderPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onActionDown(event);
                if (state == STATE_DELETE) {
                    removeSelf();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getRawX();
                float moveY = event.getRawY();
                float deltaX = moveX - lastX;
                float deltaY = moveY - lastY;
                actionByState(deltaX, deltaY);
                lastX = moveX;
                lastY = moveY;
                hideKeyBoard();
                break;
            case MotionEvent.ACTION_UP:
                reset();
                break;
        }
        return true;
    }

    private void onActionDown(MotionEvent event) {
        if (!isBorderVisible) {
            showBorder();
        }
        if (onStateChange != null) {
            onStateChange.onFocus(EditableEditText.this);
        }
        downX = lastX = event.getRawX();
        downY = lastY = event.getRawY();
        setStateByPoint(event.getX(), event.getY());
    }

    private void hideKeyBoard() {
        InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im.isActive()) {
            im.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = lastX = ev.getRawX();
                downY = lastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                return Math.abs(downX - ev.getRawX()) > touchSlop
                        || Math.abs(downY - ev.getRawY()) > touchSlop;
            case MotionEvent.ACTION_UP:
                return false;
        }
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        hideKeyBoard();
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
        setupMinSize();
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


    public void hideBorder() {
        if (et.getText() == null || et.getText().length() <= 0) {
            removeSelf();
            return;
        }
        isBorderVisible = false;
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        setWillNotDraw(true);
        invalidate();
    }

    public void showBorder() {
        isBorderVisible = true;
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        invalidate();

    }

    public void setOnStateChange(OnStateChange onStateChange) {
        this.onStateChange = onStateChange;
    }

    interface OnStateChange {
        void onFocus(EditableEditText view);

        void onDelete();
    }

}
