
package com.example.gaara.trianglepagerindicator;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Lightweight implementation of ViewPager tabs. This looks similar to traditional actionBar tabs,
 * but allows for the view containing the tabs to be placed anywhere on screen. Text-related
 * attributes can also be assigned in XML - these will get propogated to the child TextViews
 * automatically.
 */
public class MainTabs extends HorizontalScrollView implements ViewPager.OnPageChangeListener {

    private static final String TAG = "MainTabs";
    ViewPager mPager;
    private MainTabStrip mTabStrip;

    /**
     * Linearlayout that will contain the TextViews serving as tabs. This is the only child
     * of the parent HorizontalScrollView.
     */
    final int mTextStyle;
    final ColorStateList mTextColor;
    final int mTextSize;
    final boolean mTextAllCaps;
    int mPrevSelected = -1;
    int mSidePadding;
    int mUnderlineColor;
    int mUnderlineThickness;
    int mUnderlineWidth;
    int mSpecialWidth;
    int mMarginBottom;
    private int backgroundColor;

    private static final int TAB_SIDE_PADDING_IN_DPS = 13;
    private int weightType = 1;
    private boolean mNeedScale;
    private boolean mNeedBold;
    private float mScale;

    /**
     * Simulates actionbar tab behavior by showing a toast with the tab title when long clicked.
     */
    private class OnTabLongClickListener implements View.OnLongClickListener {
        final int mPosition;

        public OnTabLongClickListener(int position) {
            mPosition = position;
        }

        @Override
        public boolean onLongClick(View v) {
            final int[] screenPos = new int[2];
            getLocationOnScreen(screenPos);

            final Context context = getContext();
            final int width = getWidth();
            final int height = getHeight();
            final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

            Toast toast = Toast.makeText(context, mPager.getAdapter().getPageTitle(mPosition), Toast.LENGTH_SHORT);

            // Show the toast under the tab
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, (screenPos[0] + width / 2) - screenWidth / 2, screenPos[1] + height);

            toast.show();
            return true;
        }
    }

    public MainTabs(Context context) {
        this(context, null);
    }

    public MainTabs(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainTabs(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFillViewport(true);

        mSidePadding = (int) (getResources().getDisplayMetrics().density * TAB_SIDE_PADDING_IN_DPS);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MainTabs, defStyle, 0);
        mTextSize = a.getDimensionPixelSize(R.styleable.MainTabs_textSize_main, 0);
        mTextStyle = a.getInt(R.styleable.MainTabs_textStyle_main, 0);
        mTextColor = a.getColorStateList(R.styleable.MainTabs_textColor_main);
        mTextAllCaps = a.getBoolean(R.styleable.MainTabs_textAllCap_main, false);
        mUnderlineColor = a.getColor(R.styleable.MainTabs_underlineColor_main, 0);
        backgroundColor = a.getColor(R.styleable.MainTabs_backgroundColor_main, getResources().getColor(R.color.color_1));
        mUnderlineThickness = a.getDimensionPixelOffset(R.styleable.MainTabs_underlineThickness_main, dp2px(2f));
        mUnderlineWidth = a.getDimensionPixelOffset(R.styleable.MainTabs_underlinewidth_main, 0);
        mSpecialWidth = a.getDimensionPixelOffset(R.styleable.MainTabs_specialwidth_main, 0);
        mMarginBottom = a.getDimensionPixelOffset(R.styleable.MainTabs_marginBottom_main, 0);
        mNeedScale = a.getBoolean(R.styleable.MainTabs_needScale_main, false);
        mNeedBold = a.getBoolean(R.styleable.MainTabs_needBold_main, false);
        mScale = a.getFloat(R.styleable.MainTabs_scale_main, 1.0f);

        mTabStrip = new MainTabStrip(context);
        mTabStrip.setUnderlineColor(mUnderlineColor);
        mTabStrip.setSelectedUnderlineThickness(mUnderlineThickness);
        mTabStrip.setmMarginBottom(mMarginBottom);
        mTabStrip.setBackgroundColor(backgroundColor);
        mTabStrip.setNeedScale(mNeedScale);
        mTabStrip.setScale(mScale);
        mTabStrip.setUnderlineWidth(mUnderlineWidth);
        mTabStrip.setSpecialWidth(mSpecialWidth);

        addView(mTabStrip, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT));
        a.recycle();
    }

    public void setViewPager(ViewPager viewPager) {
        mPager = viewPager;
        addTabs(mPager.getAdapter());
        //高版本v4支持
        //mPager.addOnPageChangeListener(this);
        mPager.setOnPageChangeListener(this);
    }

    private void addTabs(PagerAdapter adapter) {
        mTabStrip.removeAllViews();

        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            addTab(adapter.getPageTitle(i), i);
        }
    }

    private void addTab(CharSequence tabTitle, final int position) {
        final TextView textView = new TextView(getContext());
        textView.setText(tabTitle);
//        textView.setBackgroundResource(R.drawable.view_pager_tab_background);
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onTabClickListener) {
                    View childAt = mTabStrip.getChildAt(position);
                    final int[] screenPos = new int[2];
                    childAt.getLocationOnScreen(screenPos);
                    if (mPrevSelected >= 0 && mPrevSelected != position && mNeedScale) {
                        ScaleAnimation outAnim = new ScaleAnimation(mScale, 1.0f, mScale, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        outAnim.setDuration(200);
                        outAnim.setFillAfter(true);
                        childAt.clearAnimation();
                        childAt.startAnimation(outAnim);

                        View childAt1 = mTabStrip.getChildAt(mPrevSelected);
                        ScaleAnimation inAnim = new ScaleAnimation(1.0f / mScale, 1.0f, 1.0f / mScale, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        inAnim.setDuration(200);
                        inAnim.setFillAfter(true);
                        childAt1.clearAnimation();
                        childAt1.startAnimation(inAnim);
                    }
                    onTabClickListener.onTabClick(position, screenPos[0], childAt.getWidth());
                } else {
                    mPager.setCurrentItem(getRtlPosition(position));
                }
            }
        });

        //textView.setOnLongClickListener(new OnTabLongClickListener(position));

        // Assign various text appearance related attributes to child views.
        if (mTextStyle > 0) {
            textView.setTypeface(textView.getTypeface(), mTextStyle);
        }
        if (mTextSize > 0) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        }
        if (mTextColor != null) {
            textView.setTextColor(mTextColor);
        }
        textView.setAllCaps(mTextAllCaps);
        textView.setPadding(mSidePadding, 0, mSidePadding, 0);
        mTabStrip.addView(textView, new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT, weightType));
        // Default to the first child being selected
        if (position == 0) {
            mPrevSelected = 0;
//            textView.setSelected(true);
            setSelected(mPrevSelected, textView, true);
        } else {
            if (mNeedScale) {
                textView.setScaleX(mScale);
                textView.setScaleY(mScale);
            }
            if (mNeedBold) {
                textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        position = getRtlPosition(position);
        int tabStripChildCount = mTabStrip.getChildCount();
        if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
            return;
        }
        mTabStrip.onPageScrolled(position, positionOffset, positionOffsetPixels);
        if (null != onPageChangeListener) {
            onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        position = getRtlPosition(position);
        if (mPrevSelected >= 0) {
//            mTabStrip.getChildAt(mPrevSelected).setSelected(false);
            setSelected(mPrevSelected, (TextView) mTabStrip.getChildAt(mPrevSelected), false);
        }
        final View selectedChild = mTabStrip.getChildAt(position);
//        selectedChild.setSelected(true);
        setSelected(position, (TextView) selectedChild, true);

        // Update scroll position
        final int scrollPos = selectedChild.getLeft() - (getWidth() - selectedChild.getWidth()) / 2;
        smoothScrollTo(scrollPos, 0);
        mPrevSelected = position;
        if (null != onPageChangeListener) {
            onPageChangeListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (null != onPageChangeListener) {
            onPageChangeListener.onPageScrollStateChanged(state);
        }
    }

    private int getRtlPosition(int position) {
        if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            return mTabStrip.getChildCount() - 1 - position;
        }
        return position;
    }

    private int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getContext().getResources().getDisplayMetrics());
    }

    private void setSelected(int position, TextView tView, boolean selected) {
        tView.setSelected(selected);
        if (mNeedScale) {
            if (selected) {
                tView.setScaleX(1.0f);
                tView.setScaleY(1.0f);
            } else {
                tView.setScaleX(mScale);
                tView.setScaleY(mScale);
            }
        }
        if (mNeedBold) {
            if (selected) {
                tView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            } else {
                tView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }
        }
    }

    public void setSpecialIndex(int specialIndex) {
        mTabStrip.setSpecialIndex(specialIndex);
    }

    /**
     * 下划线类型
     *
     * @param type 0:默认,和tab等长;1:和文字等长
     */
    public void setType(int type) {
        mTabStrip.setType(type);
    }

    /**
     * 设置宽度类型
     *
     * @param weightType 权重值,默认为1
     */
    public void setWeightType(int weightType) {
        this.weightType = weightType;
    }

    public void setSelectedText(String txt, int position) {
        if (!TextUtils.isEmpty(txt) && position >= 0) {
            ((TextView) mTabStrip.getChildAt(position)).setText(txt);
        }
    }

    public TextView getView(int position) {
        if (position >= 0 && position < mTabStrip.getChildCount()) {
            TextView childAt = (TextView) mTabStrip.getChildAt(position);
            return childAt;
        } else {
            return null;
        }
    }

    private OnPageChangeListener onPageChangeListener;

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    public interface OnPageChangeListener {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        public void onPageSelected(int position);

        public void onPageScrollStateChanged(int state);
    }

    private OnTabClickListener onTabClickListener;

    public void setOnTabClickListener(OnTabClickListener onTabClickListener) {
        this.onTabClickListener = onTabClickListener;
    }

    /**
     * item点击监听,不设置默认平滑翻页
     */
    public interface OnTabClickListener {
        /**
         * item点击事件
         *
         * @param position item位置角标
         * @param left     item左侧距离(距离屏幕)
         * @param dX       item宽度
         */
        public void onTabClick(int position, int left, int dX);
    }

    public void setSidePadding(int paddingDP) {
        this.mSidePadding = (int) (getResources().getDisplayMetrics().density * paddingDP);
    }

    /**
     * 计算宽度
     *
     * @param textView
     * @param text
     * @return
     */
    private float getTextViewLength(TextView textView, String text) {
        TextPaint paint = textView.getPaint();
        float textLength = paint.measureText(text);
        return textLength;
    }

    /**
     * 数量>=5的时候,保证第五个textview显示一半
     *
     * @param selectedPage
     * @param maxWidth
     * @param content
     */
    public void fitGap(int selectedPage, int maxWidth, ArrayList<String> content) {
        TextView textView = new TextView(getContext());
        if (mTextSize > 0) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        }
        switch (selectedPage) {
            case 0:
            case 1:
                String ahead = "";
                for (int i = 0; i < 4; i++) {
                    ahead += content.get(i);
                }
                float textLength = getTextViewLength(textView, ahead);
                float fifLength = getTextViewLength(textView, content.get(4)) / 2;
                int gap = Math.round((maxWidth * 1.0f - (textLength + fifLength)) / 9);
                if (gap >= 0) {
                    this.mSidePadding = gap;
                }
                break;
            case 2:
                float thirdLength = getTextViewLength(textView, content.get(2)) / 2;
                float fourthLength = getTextViewLength(textView, content.get(3));
                float fifthLength = getTextViewLength(textView, content.get(4)) / 2;
                int gap2 = Math.round((maxWidth * 1.0f / 2 - thirdLength - fourthLength - fifthLength) / 4);
                if (gap2 >= 0) {
                    this.mSidePadding = gap2;
                }
                break;
            default:
                break;
        }
    }
}

