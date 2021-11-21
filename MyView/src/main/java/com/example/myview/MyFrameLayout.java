package com.example.myview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MyFrameLayout extends FrameLayout {

    private static final String TAG = "MyFrameLayout";

    private String id = "";

    public MyFrameLayout(@NonNull Context context) {
        super(context);
    }

    public MyFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        id = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "id");
    }

    public MyFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    private final ArrayList<View> mMatchParentChildren = new ArrayList<>(1);

    boolean mMeasureAllChildren = false;

    int mHorizontalSpacing = 5;
    int mVerticalSpacing = 2;

    private List<List<View>> allLines = new ArrayList(); // 记录所有行 一行一行储存
    List<Integer> lineHeights = new ArrayList<>(); // 记录每一行行高


    private void init() {
        allLines.clear();
        lineHeights.clear();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        init();
        //先度量孩子
        int childCount = getChildCount();
        int paddingL = getPaddingLeft();
        int paddingR = getPaddingRight();
        int paddingT = getPaddingTop();
        int paddingB = getPaddingBottom();

        // 解析父View给的参考值
        int selfWidth = MeasureSpec.getSize(widthMeasureSpec);
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec);
        int selfMode = MeasureSpec.getMode(widthMeasureSpec);

        int parentNeedHeight = 0;
        int parentNeedWidth = 0;

        List<View> listViews = new ArrayList<View>();
        int lineWidth = 0;
        int lineHeight = 0;

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() == View.GONE) continue;
            // TODO 读一下getChildMeasureSpec
            // 一下为根据child的值推算
            // 1、 如果自己确定大小 则为确定大小 值为确认值
            // 2、 如果Match_Parent 要填满父view
            //      a.父为确定大小，则自己为确定大小
            //      b.父为不确定但有最大值，则自己为不确定但有最大值
            //      c.父为不确定，则自己为不确定
            // 3、 如果Wrap_Parent 根据孩子大小判断
            //      a.父为确定大小 则自己为不确定但有最大值
            //      b.父为不确定但有最大追 则自己为不确定但有最大值
            //      c.父为不确定 则自己为不确定
            ViewGroup.LayoutParams aa = childView.getLayoutParams();
            int childWidthMeasureSpec = getChildMeasureSpec_1(widthMeasureSpec, paddingR + paddingL, aa.width);
            int childHeightMeasureSpec = getChildMeasureSpec_1(heightMeasureSpec, paddingT + paddingB, aa.height);
            childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            int mode = MeasureSpec.getMode(childWidthMeasureSpec);
            int value = MeasureSpec.getSize(childWidthMeasureSpec);

//            String aaaa = "";
//            switch (mode) {
//                case MeasureSpec.AT_MOST:
//                    aaaa = "AT_MOST";
//                    break;
//                case MeasureSpec.EXACTLY:
//                    aaaa = "EXACTLY";
//                    break;
//                case MeasureSpec.UNSPECIFIED:
//                    aaaa = "UNSPECIFIED";
//                    break;
//            }

            Log.d(TAG, "onMeasure: childWidthMeasureSpec id = " + id  + " childMode = " + get(mode) + " childValue = " + value
                    + " selfWidth = " + selfWidth + " selfMode = " + get(selfMode) + " childView.getWidth() = " +  aa.width + " childView = " + childView);

            int measuredWidth = childView.getMeasuredWidth();
            int measuredHeight =childView.getMeasuredHeight();

            if (lineWidth + measuredWidth + mHorizontalSpacing > selfWidth) {
                allLines.add(listViews);
                lineHeights.add(lineHeight);
                parentNeedHeight = parentNeedHeight + lineHeight + mVerticalSpacing;
                parentNeedWidth = Math.max(parentNeedWidth, lineWidth + mHorizontalSpacing);
                listViews = new ArrayList<>();
                lineHeight = 0;
                lineWidth = 0;
            }

            listViews.add(childView);
            lineHeight = Math.max(lineHeight, measuredHeight);
            lineWidth = lineWidth + measuredWidth + mHorizontalSpacing;

            if (i == childCount - 1) {
                allLines.add(listViews);
                lineHeights.add(lineHeight);
            }
        }
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // TODO why
        // 如果为EXACTLY 则可确认 自己为设置固定大小或者填满父View（父View为固定大小） 则直接设置为固定大小，忽略计算得出的数据
        int realWidth = (widthMode == MeasureSpec.EXACTLY) ? selfWidth : parentNeedWidth;
        int realHeight = (heightMode == MeasureSpec.EXACTLY) ? selfHeight : parentNeedHeight;


    }


    public String get(int mode) {
        String aaaa = "";
        switch (mode) {
            case MeasureSpec.AT_MOST:
                aaaa = "AT_MOST";
                break;
            case MeasureSpec.EXACTLY:
                aaaa = "EXACTLY";
                break;
            case MeasureSpec.UNSPECIFIED:
                aaaa = "UNSPECIFIED";
                break;
        }
        return aaaa;
    }

    public static int getChildMeasureSpec_1(int spec, int padding, int childDimension) {
        int specMode = MeasureSpec.getMode(spec);
        int specSize = MeasureSpec.getSize(spec);

        int size = Math.max(0, specSize - padding);

        int resultSize = 0;
        int resultMode = 0;

        switch (specMode) {
            // Parent has imposed an exact size on us
            case MeasureSpec.EXACTLY:
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == ViewGroup.LayoutParams.MATCH_PARENT) {
                    // Child wants to be our size. So be it.
                    resultSize = size;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    // Child wants to determine its own size. It can't be
                    // bigger than us.
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                }
                break;

            // Parent has imposed a maximum size on us
            case MeasureSpec.AT_MOST:
                if (childDimension >= 0) {
                    // Child wants a specific size... so be it
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == ViewGroup.LayoutParams.MATCH_PARENT) {
                    // Child wants to be our size, but our size is not fixed.
                    // Constrain child to not be bigger than us.
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                } else if (childDimension == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    // Child wants to determine its own size. It can't be
                    // bigger than us.
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                }
                break;

            // Parent asked to see how big we want to be
            case MeasureSpec.UNSPECIFIED:
                if (childDimension >= 0) {
                    // Child wants a specific size... let them have it
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == ViewGroup.LayoutParams.MATCH_PARENT) {
                    // Child wants to be our size... find out how big it should
                    // be
                    resultSize = 0;
                    resultMode = MeasureSpec.UNSPECIFIED;
                } else if (childDimension == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    // Child wants to determine its own size.... find out how
                    // big it should be
                    resultSize = 0;
                    resultMode = MeasureSpec.UNSPECIFIED;
                }
                break;
        }
        //noinspection ResourceType
        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }




}
