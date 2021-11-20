package com.example.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {

    //TODO 查一下各个构造函数
    //TODO 看一下各个布局的源码

    // new
    public FlowLayout(Context context) {
        super(context);
    }

    // 布局文件  inflate解析布局文件 反射
    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 主题style 自定义style
    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ImageView四个参数  自定义属性  TODO 去瞅瞅ImageView
    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    int mHorizontalSpacing = 5;
    int mVerticalSpacing = 2;

    private List<List<View>> allLines = new ArrayList(); // 记录所有行 一行一行储存
    List<Integer> lineHeights = new ArrayList<>(); // 记录每一行行高


    private void init() {
        allLines.clear();
        lineHeights.clear();
    }

    // 度量
    /*
    先计算孩子 再计算自己
    TODO 做根布局 参考值是多少  做子布局 参考值是多少
     */
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
            int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, paddingR + paddingL, childView.getWidth());
            int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, paddingT + paddingB, childView.getHeight());
            childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);

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

        // 度量自己 保存自己数据
        setMeasuredDimension(realWidth, realHeight);

    }

    //布局
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int curl = getPaddingLeft();
        int curt = getPaddingTop();
        int lineCount = allLines.size();
        for (int i = 0; i < lineCount; i++) {
            List<View> lineViews = allLines.get(i);
            for (int j = 0; j < lineViews.size(); j++) {
                View view = lineViews.get(i);
                int left = curl;
                int top = curt;
                // measure 之后有值
                // getWidth 和 getHeight 在 layout之后有值
                int right = left + view.getMeasuredWidth();
                int bottom = top + view.getMeasuredHeight();
                view.layout(left, top, right, bottom);
                curl = right + mHorizontalSpacing;
            }
            curt = curt + lineHeights.get(i) + mVerticalSpacing;
            curl = getPaddingLeft();
        }
    }


    // 传入自己的spec和padding和孩子的数值  获取孩子的spec
    public static int getChildMeasureSpec(int spec, int padding, int childDimension) {

        //自己的mode
        int specMode = MeasureSpec.getMode(spec);
        //自己的spec
        int specSize = MeasureSpec.getSize(spec);

        //孩子的最大可用size
        int size = Math.max(0, specSize - padding);

        int resultSize = 0;
        int resultMode = 0;

        switch (specMode) {
            // Parent has imposed an exact size on us
            //自己确定为100dp （自己为确认大小）
            case MeasureSpec.EXACTLY:
                if (childDimension >= 0) {
                    // 孩子确定为50dp 则孩子为确定大小 result为50
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    // 孩子确定为填满  则孩子为确定大小 result为100dp
                    // Child wants to be our size. So be it.
                    resultSize = size;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    //孩子需要根据自己的孩子确定大小 则孩子为有最大值待确认 则暂时给他一个最大值100dp 等他确定自己的值
                    // TODO 如果孙子view加起来大于100呢
                    // Child wants to determine its own size. It can't be
                    // bigger than us.
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                }
                break;

            // Parent has imposed a maximum size on us
            // 自己不确定大小  但是父view给了一个最大值 （自己为WRAP_CONTENT）
            case MeasureSpec.AT_MOST:
                if (childDimension >= 0) {
                    // 孩子确定自己为50dp 则孩子为确定大小 result为50dp
                    // Child wants a specific size... so be it
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    //孩子确定为填满 但是自身为最大值待确认  则让孩子跟自己一样  为最大值待确认 result为100dp
                    // Child wants to be our size, but our size is not fixed.
                    // Constrain child to not be bigger than us.
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    //孩子需要根据自己的孩子确定大小 则孩子为有最大值待确认 则暂时给他一个最大值100dp 等他确定自己的值
                    // Child wants to determine its own size. It can't be
                    // bigger than us.
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                }
                break;

            // Parent asked to see how big we want to be
            // 自己无确认数值，父view也未给加限制（父和自己都是WRAP_CONTENT）
            case MeasureSpec.UNSPECIFIED:
                if (childDimension >= 0) {
                    // Child wants a specific size... let them have it
                    // 孩子有自己确认50dp 则孩子为确定大小 result为50dp
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    // Child wants to be our size... find out how big it should
                    // be
                    // 孩子确定为填满 自己暂未确定 则孩子与自己一样
//                    resultSize = View.sUseZeroUnspecifiedMeasureSpec ? 0 : size;
                    resultMode = MeasureSpec.UNSPECIFIED;
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    // Child wants to determine its own size.... find out how
                    // big it should be
                    // 孩子需要根据子view来判断 则孩子跟自己一样
//                    resultSize = View.sUseZeroUnspecifiedMeasureSpec ? 0 : size;
                    resultMode = MeasureSpec.UNSPECIFIED;
                }
                break;
        }
        //noinspection ResourceType
        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }
}
