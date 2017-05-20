
package com.example.gaara.trianglepagerindicator;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class MainTabStrip extends LinearLayout {
    private int mSelectedUnderlineThickness;
    private int mUnderlineWidth;
    private Paint mSelectedUnderlinePaint;
    private Paint mSpecialUnderlinePaint;
    private Paint mTrianglePaint;

    private int mIndexForSelection;
    private float mSelectionOffset;
    private int mUnderlineColor;
    private int mBackgroundColor;
    private int mMarginBottom;
    private int specialIndex;
    private int type = 0;
    private int mNextIndexForSelection;
    private int specialWidth = 0;

    private boolean mNeedScale;
    private float mScale;

    public MainTabStrip(Context context) {
        this(context, null);
    }

    public MainTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);

        final Resources res = context.getResources();

        mSelectedUnderlineThickness = res.getDimensionPixelSize(R.dimen.dimens_dip_2);
        mUnderlineColor = res.getColor(R.color.color_2);
        mBackgroundColor = res.getColor(R.color.color_1);

        mSelectedUnderlinePaint = new Paint();
        mSelectedUnderlinePaint.setColor(mUnderlineColor);
        mSelectedUnderlinePaint.setAntiAlias(true);
        mSpecialUnderlinePaint = new Paint();
        mSpecialUnderlinePaint.setColor(mBackgroundColor);

        mTrianglePaint = new Paint();
        mTrianglePaint.setAntiAlias(true);
        mTrianglePaint.setColor(mUnderlineColor);
        mTrianglePaint.setStyle(Paint.Style.STROKE);
        mTrianglePaint.setStrokeCap(Paint.Cap.ROUND);
        mTrianglePaint.setStrokeWidth(mSelectedUnderlineThickness);

        setBackgroundColor(mBackgroundColor);
        setWillNotDraw(false);
        mMarginBottom = 0;
        specialIndex = -1;
        //部分机型clipPath在硬件加速状态下有bug
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        mBackgroundColor = color;
        mSpecialUnderlinePaint.setColor(mBackgroundColor);
    }

    public void setUnderlineColor(int underlineColor) {
        mUnderlineColor = underlineColor;
        mSelectedUnderlinePaint.setColor(mUnderlineColor);
        mTrianglePaint.setColor(mUnderlineColor);
    }

    public void setSelectedUnderlineThickness(int size) {
        mSelectedUnderlineThickness = size;
        mTrianglePaint.setStrokeWidth(mSelectedUnderlineThickness);
    }

    public void setmMarginBottom(int mMarginBottom) {
        this.mMarginBottom = mMarginBottom;
    }

    /**
     * Notifies this view that view pager has been scrolled. We save the tab index
     * and selection offset for interpolating the position and width of selection
     * underline.
     */
    void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mIndexForSelection = position;
        mSelectionOffset = positionOffset;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int childCount = getChildCount();
        View selectedTitle;
        View nextTitle = null;

        // Thick colored underline below the current selection
        if (childCount > 0) {
            selectedTitle = getChildAt(mIndexForSelection);
            int selectedLeft = selectedTitle.getLeft();
            int selectedRight = selectedTitle.getRight();

            if (mUnderlineWidth != 0) {
                int gap = (selectedTitle.getWidth() - mUnderlineWidth) / 2;
                selectedLeft += gap;
                selectedRight -= gap;
                if (mIndexForSelection == specialIndex && specialWidth != 0) {
                    int distance = (mUnderlineWidth - specialWidth) / 2;
                    selectedLeft += distance;
                    selectedRight -= distance;
                }
            }

            final boolean isRtl = isRtl();
            final boolean hasNextTab = isRtl ? mIndexForSelection > 0 : (mIndexForSelection < (getChildCount() - 1));
            if ((mSelectionOffset > 0.0f) && hasNextTab) {
                // Draw the selection partway between the tabs
                mNextIndexForSelection = mIndexForSelection + (isRtl ? -1 : 1);
                nextTitle = getChildAt(mNextIndexForSelection);
                int nextLeft = nextTitle.getLeft();
                int nextRight = nextTitle.getRight();
                if (mUnderlineWidth != 0) {
                    int nextGap = (nextTitle.getWidth() - mUnderlineWidth) / 2;
                    nextLeft += nextGap;
                    nextRight -= nextGap;
                    if (mNextIndexForSelection == specialIndex && specialWidth != 0) {
                        int distance = (mUnderlineWidth - specialWidth) / 2;
                        nextLeft += distance;
                        nextRight -= distance;
                    }
                }

                selectedLeft = (int) (mSelectionOffset * nextLeft + (1.0f - mSelectionOffset) * selectedLeft);
                selectedRight = (int) (mSelectionOffset * nextRight + (1.0f - mSelectionOffset) * selectedRight);
            }

            int height = getHeight() - mMarginBottom;
            canvas.save(Canvas.CLIP_SAVE_FLAG);
            RectF t1 = new RectF(selectedLeft, height - mSelectedUnderlineThickness, selectedRight, height);
            canvas.drawRoundRect(t1, mSelectedUnderlineThickness / 2, mSelectedUnderlineThickness / 2, mSelectedUnderlinePaint);
            if (mIndexForSelection == specialIndex || mNextIndexForSelection == specialIndex) {
                drawTriangle(canvas, selectedLeft, selectedRight, height);
            }
            if (mNeedScale) {
                selectedTitle.setScaleX(1.0f + (mScale - 1.0f) * mSelectionOffset);
                selectedTitle.setScaleY(1.0f + (mScale - 1.0f) * mSelectionOffset);

                if (nextTitle != null) {
                    nextTitle.setScaleX(mScale + (1.0f - mScale) * mSelectionOffset);
                    nextTitle.setScaleY(mScale + (1.0f - mScale) * mSelectionOffset);
                }
            }
            canvas.restore();
        }
    }

    private boolean isRtl() {
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public void setSpecialIndex(int specialIndex) {
        this.specialIndex = specialIndex;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSpecialWidth(int minimumWidth) {
        this.specialWidth = minimumWidth;
    }

    /**
     * 缩放字体
     *
     * @param mNeedScale
     */
    public void setNeedScale(boolean mNeedScale) {
        this.mNeedScale = mNeedScale;
    }

    public void setScale(float mScale) {
        this.mScale = mScale;
    }

    public void setUnderlineWidth(int underlineWidth) {
        this.mUnderlineWidth = underlineWidth;
    }

    private int dx2 = dip2px(getContext(), 3);
    private int dy1 = dip2px(getContext(), 3);
    private int dy2 = dip2px(getContext(), 1);
    private int roundX = dip2px(getContext(), 6);
    private int roundY = dip2px(getContext(), 9);

    /**
     * 绘制三角形
     *
     * @param canvas
     * @param selectedLeft
     * @param selectedRight
     * @param height
     */
    private void drawTriangle(Canvas canvas, int selectedLeft, int selectedRight, int height) {
        View specialTitle = getChildAt(specialIndex);
        if (specialTitle != null) {
            int specialGap = (specialTitle.getWidth() - specialWidth) / 2;
            int left = specialTitle.getLeft() + specialGap;
            int right = specialTitle.getRight() - specialGap;
            if (selectedRight <= left || selectedLeft >= right) {
                return;
            }
            int rise;
            if (selectedLeft <= left) {
                rise = Math.round((selectedRight - left) * 1.0f / specialWidth * (mSelectedUnderlineThickness + 1) / 2);
            } else {
                rise = Math.round((right - selectedLeft) * 1.0f / specialWidth * (mSelectedUnderlineThickness + 1) / 2);
            }
            selectedLeft -= rise;
            selectedRight += rise;
            Path path = new Path();
            path.addRoundRect(new RectF(selectedLeft, getHeight() - mMarginBottom - mSelectedUnderlineThickness * 3, selectedRight, getHeight()), roundX, roundY, Path.Direction.CCW);
            canvas.clipPath(path);
            canvas.drawRect(left, height - mSelectedUnderlineThickness, right, height, mSpecialUnderlinePaint);
            int startY = getHeight() - mMarginBottom - mSelectedUnderlineThickness / 2;
            int dx1 = (right - left - dx2) / 2;
            Path mPath = new Path();
            mPath.moveTo(left, startY);
            mPath.lineTo(left + dx1, startY + dy1);
            //设置贝塞尔曲线的控制点坐标和终点坐标
            mPath.quadTo(left + dx1 + dx2 / 2, startY + dy1 + dy2, left + dx1 + dx2, startY + dy1);
            mPath.lineTo(right, startY);
            canvas.drawPath(mPath, mTrianglePaint);
        }
    }

    public int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}