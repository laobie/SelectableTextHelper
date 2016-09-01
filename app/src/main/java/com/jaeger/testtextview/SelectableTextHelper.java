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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Jaeger on 16/8/30.
 *
 * Email: chjie.jaeger@gamil.com
 * GitHub: https://github.com/laobie
 */
public class SelectableTextHelper {

    private final static String TAG = SelectableTextHelper.class.getSimpleName();

    private final static int DEFAULT_SELECT_LENGTH = 1;

    private TextView mTextView;

    private CursorHandle mStartHandle;
    private CursorHandle mEndHandle;

    private OperateWindow mOperateWindow;
    private Context mContext;
    private int mTouchX;
    private int mTouchY;
    private int mSelectedColor = 0xFFAFE1F4;
    private SelectionInfo mSelectionInfo = new SelectionInfo();

    private OnSelectListener mSelectListener;

    private Spannable mSpannable;
    private BackgroundColorSpan mSpan;

    public SelectableTextHelper(TextView textView) {
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
        //mTextView.getViewTreeObserver().
        mTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    dismiss();
                }
            }
        });
        mTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //if (event.getAction() == MotionEvent.ACTION_MOVE) {
                //    dismiss();
                //}
                //if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                //    showCursor(mStartHandle, mSelectionInfo.mStart);
                //    showCursor(mEndHandle, mSelectionInfo.mEnd);
                //}
                mTouchX = (int) event.getX();
                mTouchY = (int) event.getY();
                Log.d(TAG, "touch x is " + mTouchX + " touch y is " + mTouchY);
                return false;
            }
        });

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSelect();
                if (mStartHandle != null && mEndHandle != null) {
                    dismiss();
                }
            }
        });

        mOperateWindow = new OperateWindow(mContext);
    }

    private void dismiss() {
        if (mStartHandle != null && mStartHandle.isShowing()) {
            mStartHandle.dismiss();
        }
        if (mEndHandle != null && mEndHandle.isShowing()) {
            mEndHandle.dismiss();
        }
        if (mOperateWindow != null && mOperateWindow.isShowing()) {
            mOperateWindow.dismiss();
        }
    }

    private void removeSelect() {
        if (mSpannable != null && mSpan != null) {
            mSpannable.removeSpan(mSpan);
            mSpan = null;
        }
    }

    private void show(int x, int y) {
        if (mStartHandle == null) mStartHandle = new CursorHandle(mTextView, true);
        if (mEndHandle == null) mEndHandle = new CursorHandle(mTextView, false);

        dismiss();
        removeSelect();

        int startOffset = SelectUtil.getPreciseOffset(mTextView, x, y);
        int endOffset = startOffset + DEFAULT_SELECT_LENGTH;

        showCursor(mStartHandle, startOffset, endOffset);
        showCursor(mEndHandle, startOffset, endOffset);

        if (mTextView.getText() instanceof Spannable) {
            mSpannable = (Spannable) mTextView.getText();
        }

        selectText(startOffset, endOffset);

        mOperateWindow.show();
    }

    private void showCursor(CursorHandle cursorHandle, int startOffset, int endOffset) {
        Layout layout = mTextView.getLayout();
        int offset = cursorHandle.isLeft ? startOffset : endOffset;
        cursorHandle.show((int) layout.getPrimaryHorizontal(offset), layout.getLineBottom(layout.getLineForOffset(offset)));
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
                mSpan = new BackgroundColorSpan(mSelectedColor);
            }
            mSelectionInfo.mSelectionContent = mSpannable.subSequence(mSelectionInfo.mStart, mSelectionInfo.mEnd).toString();
            mSpannable.setSpan(mSpan, mSelectionInfo.mStart, mSelectionInfo.mEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            if (mSelectListener != null) {
                //mSelectListener.onTextSelected(mSpannable.subSequence(startPos, endPos));
            }
        }
    }

    public void setSelectListener(OnSelectListener selectListener) {
        mSelectListener = selectListener;
    }

    public void destroy() {
        removeSelect();
        dismiss();
        mStartHandle = null;
        mEndHandle = null;
    }

    /**
     * Operate windows : copy, select all
     */
    class OperateWindow {

        private PopupWindow mWindow;
        private int[] mTempCoors = new int[2];

        private int mWidth;
        private int mHeight;

        public OperateWindow(final Context context) {
            View contentView = LayoutInflater.from(context).inflate(R.layout.layout_operate_windows, null);
            contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            mWidth = contentView.getMeasuredWidth();
            mHeight = contentView.getMeasuredHeight();
            mWindow =
                new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
            mWindow.setClippingEnabled(false);

            contentView.findViewById(R.id.tv_copy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, mSelectionInfo.mSelectionContent, Toast.LENGTH_SHORT).show();
                    SelectableTextHelper.this.removeSelect();
                    SelectableTextHelper.this.dismiss();
                }
            });
            contentView.findViewById(R.id.tv_select_all).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectText(0, mTextView.getText().length() - 1);
                }
            });
        }

        public void show() {
            mTextView.getLocationInWindow(mTempCoors);
            Layout layout = mTextView.getLayout();
            int posX = (int) layout.getPrimaryHorizontal(mSelectionInfo.mStart) + mTempCoors[0];
            int posY = layout.getLineTop(layout.getLineForOffset(mSelectionInfo.mStart)) + mTempCoors[1] - mHeight - 16;
            L.d("fuck" + "pos x is " + posX);
            L.d("fuck" + "posY is " + posY);
            if (posX <= 0) posX = 16;
            if (posY < 0) posY = 16;
            if (posX + mWidth > SelectUtil.getScreenWidth(mContext)) {
                posX = SelectUtil.getScreenWidth(mContext) - mWidth - 16;
            }
            mWindow.showAtLocation(mTextView, Gravity.NO_GRAVITY, posX, posY);
        }

        public void dismiss() {
            if (mWindow.isShowing()) {
                mWindow.dismiss();
            }
        }

        public boolean isShowing() {
            return mWindow.isShowing();
        }
    }

    class CursorHandle extends View {

        private TextView mTextView;
        private PopupWindow mPopupWindow;
        private int mCircleSize = 35;
        private int mWidth = mCircleSize * 2;
        private int mHeight = mCircleSize * 2;
        private int mPadding = 25;
        private boolean isLeft;

        Path mPath = new Path();
        private Paint mPaint;

        public CursorHandle(TextView textView, boolean isLeft) {

            super(textView.getContext());
            this.isLeft = isLeft;
            mTextView = textView;
            mPaint = new Paint();
            mPaint.setColor(0xFF1379D6);

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
            canvas.drawCircle(mCircleSize + mPadding, mCircleSize, mCircleSize, mPaint);
            if (isLeft) {
                canvas.drawRect(mCircleSize + mPadding, 0, mCircleSize * 2 + mPadding, mCircleSize, mPaint);
            } else {
                canvas.drawRect(mPadding, 0, mCircleSize + mPadding, mCircleSize, mPaint);
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

        private int mBeforeDragStart;
        private int mBeforeDragEnd;

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mBeforeDragStart = mSelectionInfo.mStart;
                    mBeforeDragEnd = mSelectionInfo.mEnd;
                    mAdjustX = (int) event.getX();
                    mAdjustY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mOperateWindow.show();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mOperateWindow.dismiss();
                    int rawX = (int) event.getRawX();
                    int rawY = (int) event.getRawY();
                    L.d("Action move raw y is " + rawY);
                    update(rawX + mAdjustX - mWidth, rawY + mAdjustY - mHeight);
                    break;
            }
            return true;
        }

        private void change() {
            isLeft = !isLeft;
            invalidate();
        }

        public void dismiss() {
            mPopupWindow.dismiss();
        }

        private int[] mTempCoors = new int[2];

        public void update(int x, int y) {
            mTextView.getLocationInWindow(mTempCoors);
            int oldOffset;
            if (isLeft) {
                oldOffset = mSelectionInfo.mStart;
            } else {
                oldOffset = mSelectionInfo.mEnd;
            }

            //x += mTempCoors[0] - mTextView.getPaddingLeft();
            y = y - mTempCoors[1];

            int offset = SelectUtil.getHysteresisOffset(mTextView, x, y, oldOffset);

            if (offset != oldOffset) {
                removeSelect();
                if (isLeft) {
                    if (offset > mBeforeDragEnd) {
                        CursorHandle handle = getCursorHandle(false);
                        change();
                        handle.change();
                        mBeforeDragStart = mBeforeDragEnd;
                        selectText(mBeforeDragEnd, offset);

                        handle.updateCursor();
                    } else {
                        selectText(offset, -1);
                    }
                    updateCursor();
                } else {
                    if (offset < mBeforeDragStart) {
                        CursorHandle handle = getCursorHandle(true);
                        handle.change();
                        change();
                        mBeforeDragEnd = mBeforeDragStart;
                        selectText(offset, mBeforeDragStart);
                        handle.updateCursor();
                    } else {
                        selectText(mBeforeDragStart, offset);
                    }
                    updateCursor();
                }
            }
        }

        private void updateCursor() {

            mTextView.getLocationInWindow(mTempCoors);
            Layout layout = mTextView.getLayout();
            if (isLeft) {
                mPopupWindow.update((int) layout.getPrimaryHorizontal(mSelectionInfo.mStart) - mWidth + getExtraX(),
                    layout.getLineBottom(layout.getLineForOffset(mSelectionInfo.mStart)) + getExtraY(), -1, -1);
            } else {
                mPopupWindow.update((int) layout.getPrimaryHorizontal(mSelectionInfo.mEnd) + getExtraX(),
                    layout.getLineBottom(layout.getLineForOffset(mSelectionInfo.mEnd)) + getExtraY(), -1, -1);
            }
        }

        public void show(int x, int y) {
            L.d("show x is " + x + " show y is " + y);
            mTextView.getLocationInWindow(mTempCoors);
            int offset = isLeft ? mWidth : 0;
            mPopupWindow.showAtLocation(mTextView, Gravity.NO_GRAVITY, +x - offset + getExtraX(), y + getExtraY());
        }

        public int getExtraX() {
            return mTempCoors[0] - mPadding + mTextView.getPaddingLeft();
        }

        public int getExtraY() {
            return mTempCoors[1] + mTextView.getPaddingTop();
        }

        public boolean isShowing() {
            return mPopupWindow.isShowing();
        }
    }

    private CursorHandle getCursorHandle(boolean isLeft) {
        if (mStartHandle.isLeft == isLeft) {
            return mStartHandle;
        } else {
            return mEndHandle;
        }
    }
}


