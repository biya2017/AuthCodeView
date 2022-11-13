package com.lib;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;


import java.util.ArrayList;
import java.util.List;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

import android.view.inputmethod.InputMethodManager;
import android.view.MotionEvent;

import com.example.R;

import java.util.Timer;
import java.util.TimerTask;


/**
 * author : A
 * date :
 * desc:
 */
public class AuthCodeView extends EditText {
    private int borderColor;
    private float borderWidth;
    private float borderRadius;

    private int passwordLength = 4;
    private int passwordColor;
    private float passwordWidth;
    private float passwordRadius;

    private Paint passwordPaint = new Paint(ANTI_ALIAS_FLAG);
    private Paint borderPaint = new Paint(ANTI_ALIAS_FLAG);
    private Paint linePaint = new Paint(ANTI_ALIAS_FLAG);


    private final int defaultContMargin = 5;
    private final int defaultSplitLineWidth = 3;

    private TextChangeListener mTextChangeListener;
    private float mDefaultInputViewTextSize, mDefaultInputViewPadding, mDefaultInputTextSize;
    private float mCursorWidth;
    private int mCursorHeight;
    private float mDefalutMargin = 10;
    private boolean mPwdVisiable = true;
    private String mInputText;
    private List<RectF> rectList;
    private Context mContext;
    private int mSelectIndex = 0;
    private Handler mCursorHandler;
    static final int CURSOR_DELAY_TIME = 400;
    private TimerTask mCursorTimerTask;
    private Timer mCursorTimer;
    private boolean isCursorShowing;


    public AuthCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        final Resources res = getResources();
        final int defaultBorderColor = res.getColor(R.color.default_ev_border_color);
        final float defaultBorderWidth = res.getDimension(R.dimen.default_ev_border_width);
        final float defaultBorderRadius = res.getDimension(R.dimen.default_ev_border_radius);
        final int defaultPasswordLength = res.getInteger(R.integer.default_ev_password_length);
        final int defaultPasswordColor = res.getColor(R.color.default_ev_password_color);
        final float defaultPasswordWidth = res.getDimension(R.dimen.default_ev_password_width);
        final float defaultPasswordRadius = res.getDimension(R.dimen.default_ev_password_radius);
        final float defaultInputViewTextSize = res.getDimension(R.dimen.default_input_text_view_size);
        final float defaultInputViewPadding = res.getDimension(R.dimen.default_input_text_view_padding);
        final float defaultInputTextSize = res.getDimension(R.dimen.default_input_text_size);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PasswordInputView, 0, 0);
        try {
            borderColor = a.getColor(R.styleable.PasswordInputView_captchaBorderColor, defaultBorderColor);
            borderWidth = a.getDimension(R.styleable.PasswordInputView_captchaBorderWidth, defaultBorderWidth);
            borderRadius = a.getDimension(R.styleable.PasswordInputView_captchaBorderRadius, defaultBorderRadius);
            passwordLength = a.getInt(R.styleable.PasswordInputView_captchaLength, defaultPasswordLength);
            passwordColor = a.getColor(R.styleable.PasswordInputView_captchaColor, defaultPasswordColor);
            passwordWidth = a.getDimension(R.styleable.PasswordInputView_captchaWidth, defaultPasswordWidth);
            passwordRadius = a.getDimension(R.styleable.PasswordInputView_captchaRadius, defaultPasswordRadius);
            mDefaultInputViewTextSize = a.getDimension(R.styleable.PasswordInputView_captchaViewSize, defaultInputViewTextSize);
            mDefaultInputViewPadding = a.getDimension(R.styleable.PasswordInputView_captchaViewSize, defaultInputTextSize);
            mDefaultInputTextSize = a.getDimension(R.styleable.PasswordInputView_captchaTextSize, defaultInputViewPadding);
        } finally {
            a.recycle();
        }

        mCursorWidth = mContext.getResources().getDimension(R.dimen.captcha_cursor_width);
        mCursorHeight = (int) mContext.getResources().getDimension(R.dimen.captcha_cursor_height);

        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(borderColor);
        linePaint.setColor(getResources().getColor(R.color.select_border_color));
        linePaint.setStrokeWidth(mCursorWidth);        //绘制直线

        borderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        borderPaint.setAntiAlias(true);

        passwordPaint.setColor(passwordColor);
        passwordPaint.setTextSize(mDefaultInputTextSize);
        passwordPaint.setStyle(Paint.Style.FILL);
        rectList = new ArrayList<>();


        initCursorTimer();
        // setFocusableInTouchMode(true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = passwordLength * (int) mDefaultInputViewTextSize + (int) mDefaultInputViewPadding * 3 + (int) mDefalutMargin * 2;
        int height = (int) mDefaultInputViewTextSize + (int) mDefalutMargin * 2;
        setMeasuredDimension(width, height);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            setSelection(getText().length());
            showKeyBoard(getContext());
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        rectList.clear();
        //边框
        int left = (int) mDefalutMargin;
        int top = (int) mDefalutMargin;
        for (int i = 0; i < passwordLength; i++) {
            if (i < mSelectIndex) {
                //彩色边框
                borderPaint.setColor(getResources().getColor(R.color.select_border_color));
            } else if (i == mSelectIndex) {
                borderPaint.setColor(getResources().getColor(R.color.color_FAE4E6));
            } else {
                //灰色边框
                borderPaint.setColor(borderColor);
            }
            RectF rectF = new RectF(left, top, mDefaultInputViewTextSize + left, mDefaultInputViewTextSize + top);
            rectList.add(rectF);
            canvas.drawRoundRect(rectF, DisplayUtil.dip2px(mContext, 12), DisplayUtil.dip2px(mContext, 12), borderPaint);
            left += mDefaultInputViewPadding + mDefaultInputViewTextSize;
        }

        //内容,密码可见
        int textLeft = (int) mDefalutMargin + (int) mDefaultInputViewTextSize / 2;
        if (mPwdVisiable) {
            for (int i = 0; i < mInputText.length(); i++) {
                String text = mInputText.substring(i, i + 1);
                int textWidth = !TextUtils.isEmpty(text) ? getTextWidth(passwordPaint, text) / 2 : 0;
                canvas.drawText(text, textLeft - textWidth, mDefaultInputViewTextSize / 2 + mDefaultInputTextSize / 2, passwordPaint);
                textLeft += mDefaultInputViewPadding + mDefaultInputViewTextSize;
            }
        } else {
            for (int i = 0; i < mInputText.length(); i++) {
                String text = mInputText.substring(i, i + 1);
                int textWidth = !TextUtils.isEmpty(text) ? getTextWidth(passwordPaint, "*") / 2 : 0;
                canvas.drawText("*", textLeft - textWidth, mDefaultInputViewTextSize / 2 + mDefaultInputTextSize / 2 + 5, passwordPaint);
                textLeft += mDefaultInputViewPadding + mDefaultInputViewTextSize;
            }
        }

        //光标
        if (mSelectIndex < passwordLength && isCursorShowing) {
            int cursorLeft = (int) mDefalutMargin;
            // int cursorTop = (int) mDefalutMargin + ((int) mDefaultInputViewTextSize - mCursorHeight) / 2;
            int cursorTop = (int) mDefalutMargin + (int) mDefaultInputViewTextSize - DisplayUtil.dip2px(mContext, 9);
            //int cursorTop = DisplayUtil.dip2px(mContext,9) ;
            int startX = cursorLeft + (int) mDefaultInputViewTextSize / 2 + mSelectIndex * (int) mDefaultInputViewTextSize + mSelectIndex * (int) mDefaultInputViewPadding;
            int stopX = startX;
            int startY = cursorTop;
            int stopY = startY + mCursorHeight;
            canvas.drawLine(startX, startY, stopX, stopY, linePaint);
        }

    }

    public int getTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (mTextChangeListener != null) {
            mTextChangeListener.onTextChanged(text, start, lengthBefore, lengthAfter);
        }
        mInputText = text.toString();
        if (mInputText.length() > 0) {
            mSelectIndex = mInputText.length();
        } else {
            mSelectIndex = 0;
        }
        postInvalidate();
    }

    public void setPwdVisiable(boolean pwdVisiable) {
        this.mPwdVisiable = pwdVisiable;
    }

    public void setTextLength(int length) {
        this.passwordLength = length;
        postInvalidate();
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        borderPaint.setColor(borderColor);
        invalidate();
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        borderPaint.setStrokeWidth(borderWidth);
        invalidate();
    }

    public float getBorderRadius() {
        return borderRadius;
    }

    public void setBorderRadius(float borderRadius) {
        this.borderRadius = borderRadius;
        invalidate();
    }

    public int getPasswordLength() {
        return passwordLength;
    }

    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
        invalidate();
    }

    public int getPasswordColor() {
        return passwordColor;
    }

    public void setPasswordColor(int passwordColor) {
        this.passwordColor = passwordColor;
        passwordPaint.setColor(passwordColor);
        invalidate();
    }

    public float getPasswordWidth() {
        return passwordWidth;
    }

    public void setPasswordWidth(float passwordWidth) {
        this.passwordWidth = passwordWidth;
        passwordPaint.setStrokeWidth(passwordWidth);
        invalidate();
    }

    public float getPasswordRadius() {
        return passwordRadius;
    }

    public void setPasswordRadius(float passwordRadius) {
        this.passwordRadius = passwordRadius;
        invalidate();
    }

    public void setTextChangeListener(TextChangeListener mTextChangeListener) {
        this.mTextChangeListener = mTextChangeListener;
    }

    public interface TextChangeListener {
        void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter);
    }


    private void initCursorTimer() {
        mCursorTimerTask = new TimerTask() {
            @Override
            public void run() {
                // 通过光标间歇性显示实现闪烁效果
                isCursorShowing = !isCursorShowing;
                postInvalidate();
            }
        };
        mCursorTimer = new Timer();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // 启动定时任务，定时刷新实现光标闪烁
        mCursorTimer.scheduleAtFixedRate(mCursorTimerTask, 0, CURSOR_DELAY_TIME);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCursorTimer.cancel();
    }

    public void showKeyBoard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
    }

}
