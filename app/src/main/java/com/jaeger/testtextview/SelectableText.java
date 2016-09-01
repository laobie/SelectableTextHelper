package com.jaeger.testtextview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Created by Jaeger on 16/8/30.
 *
 * Email: chjie.jaeger@gamil.com
 * GitHub: https://github.com/laobie
 */
public class SelectableText {

    private final static String TAG = SelectableText.class.getSimpleName();

    private final static int DEFAULT_SELECT_LENGTH = 6;

    private TextView mTextView;

    private CursorHandle mStartHandle;
    private CursorHandle mEndHandle;
    private Context mContext;
    private int mTouchX;
    private int mTouchY;

    private SelectionInfo mSelectionInfo = new SelectionInfo();

    private OnSelectListener mSelectListener;

    private Spannable mSpannable;
    private BackgroundColorSpan mSpan;

    public SelectableText(TextView textView) {
        mTextView = textView;
        mContext = mTextView.getContext();
        init();
    }

    private void init() {
        mTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                show(mTouchX, mTouchY);
                return true;
            }
        });
        mTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTouchX = (int) event.getX();
                mTouchY = (int) event.getY();
                Log.d(TAG, "touch x is " + mTouchX + " touch y is " + mTouchY);
                return false;
            }
        });

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStartHandle != null && mEndHandle != null) {
                    dismiss();
                }
            }
        });
    }

    private void dismiss() {
        removeSelect();
        if (mStartHandle != null) {

            mStartHandle.dismiss();
        }
        if (mEndHandle != null) {
            mEndHandle.dismiss();
        }
    }

    private void removeSelect() {
        if (mSpannable != null && mSpan != null) {
            mSpannable.removeSpan(mSpan);
            mSpan = null;
        }
    }

    private void show(int x, int y) {
        if (mStartHandle == null) {
            mStartHandle = new CursorHandle(mTextView, CursorHandle.MODE_LEFT);
        }
        if (mEndHandle == null) {
            mEndHandle = new CursorHandle(mTextView, CursorHandle.MODE_RIGHT);
        }
        if (mStartHandle.isShown()) {
            mStartHandle.dismiss();
        }
        if (mEndHandle.isShown()) {
            mEndHandle.dismiss();
        }
        removeSelect();

        int startOffset = SelectUtil.getPreciseOffset(mTextView, x, y);

        int endOffset = startOffset + DEFAULT_SELECT_LENGTH;
        showCursor(mStartHandle, startOffset);
        showCursor(mEndHandle, endOffset);
        if (mTextView.getText() instanceof Spannable) {
            mSpannable = (Spannable) mTextView.getText();
        }

        selectText(startOffset, endOffset);
    }

    private void showCursor(CursorHandle cursorHandle, int startOffset) {
        Layout layout = mTextView.getLayout();
        cursorHandle.show((int) layout.getPrimaryHorizontal(startOffset),
            layout.getLineBottom(layout.getLineForOffset(startOffset)));
    }

    private void selectText(int startPos, int endPos) {
        if (startPos != -1) {
            mSelectionInfo.mStart = startPos;
        }
        if (endPos != -1) {
            mSelectionInfo.mEnd = endPos;
        }
        if (mSelectionInfo.mStart > mSelectionInfo.mEnd) {
            int temp = mSelectionInfo.mStart;
            mSelectionInfo.mStart = mSelectionInfo.mEnd;
            mSelectionInfo.mEnd = temp;
        }

        if (mSpannable != null) {
            if (mSpan == null) {
                mSpan = new BackgroundColorSpan(0x9900A9FF);
            }
            mSelectionInfo.mSelectionContent = mSpannable.subSequence(mSelectionInfo.mStart, mSelectionInfo.mEnd).toString();
            mSpannable.setSpan(mSpan, mSelectionInfo.mStart, mSelectionInfo.mEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            if (mSelectListener != null) {
                //mSelectListener.onTextSelected(mSpannable.subSequence(startPos, endPos));
            }
        }
    }

    public void setSelectListener(OnSelectListener selectListener) {
        mSelectListener = selectListener;
    }

    public void destroy() {
        dismiss();
        mStartHandle = null;
        mEndHandle = null;
    }

    class CursorHandle extends View {

        private TextView mTextView;
        private PopupWindow mPopupWindow;
        private int mWidth = 60;
        private int mHeight = 60;
        private int mPadding = 20;
        private int mMode;
        static final int MODE_LEFT = -1;
        static final int MODE_RIGHT = 1;

        Path mPath = new Path();
        private Paint mPaint;

        public CursorHandle(TextView textView, int mode) {

            super(textView.getContext());
            mMode = mode;
            mTextView = textView;
            mPaint = new Paint();
            mPaint.setColor(0xff1278BD);

            //mWidth = 120;
            //mHeight = 150;

            mPopupWindow = new PopupWindow(this);
            mPopupWindow.setClippingEnabled(false);
            mPopupWindow.setWidth(mWidth + mPadding * 2);
            mPopupWindow.setHeight(mHeight + mPadding / 2);
            invalidate();
            mPaint.setAntiAlias(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            mPath.reset();
            canvas.drawCircle(30 + mPadding, 30, 30, mPaint);
            if (mMode == MODE_LEFT) {
                canvas.drawRect(30 + mPadding, 0, 60 + mPadding, 30, mPaint);
            } else {
                canvas.drawRect( mPadding, 0, 30 + mPadding, 30, mPaint);
            }
            //int startX = 60;
            //int offsetX = 100;
            //int offsetY = 100;
            //mPath.moveTo(startX, 0);
            //mPath.cubicTo(startX - offsetX, offsetY, startX + offsetX, offsetY, startX, 0);
            //canvas.drawPath(mPath, mPaint);
        }

        private int mAdjustX;
        private int mAdjustY;

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mAdjustX = (int) event.getX();
                    mAdjustY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mTextView.getLocationInWindow(mTempCoors);
                    Layout layout = mTextView.getLayout();
                    mStartHandle.mPopupWindow.update(
                        (int) layout.getPrimaryHorizontal(mSelectionInfo.mStart) + mTempCoors[0] - mWidth - mPadding,
                        layout.getLineBottom(layout.getLineForOffset(mSelectionInfo.mStart)) + mTempCoors[1], -1, -1);
                    mEndHandle.mPopupWindow.update(
                        (int) layout.getPrimaryHorizontal(mSelectionInfo.mEnd) + mTempCoors[0] - mPadding,
                        layout.getLineBottom(layout.getLineForOffset(mSelectionInfo.mEnd)) + mTempCoors[1], -1, -1);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int rawX = (int) event.getRawX();
                    int rawY = (int) event.getRawY();
                    L.d("Action move raw y is " + rawY);
                    update(rawX + mAdjustX, rawY + mAdjustY);
                    break;
            }
            return true;
        }

        public void dismiss() {
            mPopupWindow.dismiss();
        }

        private int[] mTempCoors = new int[2];

        public void update(int x, int y) {
            mTextView.getLocationInWindow(mTempCoors);
            if (mMode == MODE_LEFT) {
                mPopupWindow.update(x - mWidth, y, -1, -1);
            } else {
                mPopupWindow.update(x - mWidth / 2, y, -1, -1);
            }
            //int curOffset = mMode == MODE_LEFT ? -mPadding * 2 + mWidth : mWidth;

            x += mTempCoors[0];
            y = y - mTempCoors[1];
            int offset = SelectUtil.getPreciseOffset(mTextView, x, y);

            removeSelect();
            if (mMode == MODE_LEFT) {
                selectText(offset, -1);
            } else {
                selectText(-1, offset);
            }
        }

        public void show(int x, int y) {
            L.d("show x is " + x + " show y is " + y);
            mTextView.getLocationInWindow(mTempCoors);
            int offset = mMode == MODE_LEFT ? mWidth : 0;
            mPopupWindow.showAtLocation(mTextView, Gravity.NO_GRAVITY, mTempCoors[0] + x - offset - mPadding, mTempCoors[1] + y);
        }
    }
}


