package com.example.mydrawable;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyDrawable extends Drawable {

    private static final String TAG = "MyDrawable";

    private Path mPath;
    private Paint mPaint;



    //鱼的重心
    private PointF middlePoint;

    //鱼的主要朝向角度
    private float fishMainAngle = 90;

    /**
     * 鱼的长度值
     */
    private int OTHER_ALPHA = 110;

    //鱼头部半径
    private float HEAD_RADIUS = 100;
    // 鱼身长度
    private float BODY_LENGTH = HEAD_RADIUS * 3.2f;
    //寻找鱼鳍起始点的线长
    private float FIND_FINS_LENGTH = 0.9f * HEAD_RADIUS;
    //鱼鳍的长度
    private float FINS_LENGTH = 1.3f * HEAD_RADIUS;
    //大圆的半径
    private float BIG_RADIUS = 0.7f * HEAD_RADIUS;
    //中圆半径
    private float MIDDLE_RADIUS = 0.6f * BIG_RADIUS;
    //小圆半径
    private float SMALL_RADIUS = 0.4f * MIDDLE_RADIUS;
    // 寻找中原圆心的线长
    private float FIND_MIDDLE_CIRCLE_LENGTH = BIG_RADIUS * (0.6f + 1);
    // 寻找小圆圆心的线长
    private float FIND_SMALL_CIRCLE_LENGTH = MIDDLE_RADIUS * (0.4f + 2.7f);
    // 寻找三角形底边中心点的线长
    private float FIND_TRIANGLE_LENGTH = MIDDLE_RADIUS * 2.7f;

    float currentValue = 0f;

    private PointF headPoint;


    public MyDrawable() {
        init();
    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setARGB(OTHER_ALPHA, 244, 92, 71);

        middlePoint = new PointF(4.19f * HEAD_RADIUS, 4.19f * HEAD_RADIUS);

        // 属性动画
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 720f);
        // 动画周期
        valueAnimator.setDuration(2000);
        // 重复模式 ： 重新开始
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        // 重复次数
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        //差值器
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            currentValue = (float) animation.getAnimatedValue();
            invalidateSelf();
        });
        valueAnimator.start();
    }

    private float frequence = 1f;

    @Override
    public void draw(@NonNull Canvas canvas) {
        float fishAngle = (float) (fishMainAngle + Math.sin(Math.toRadians(currentValue * frequence)) * 10);
        //鱼头的圆心坐标
        headPoint = calculatePoint(middlePoint, BODY_LENGTH / 2, fishAngle);
        canvas.drawCircle(headPoint.x, headPoint.y, HEAD_RADIUS, mPaint);

        //右鱼鳍
        PointF rightFinsPoint = calculatePoint(headPoint, FIND_FINS_LENGTH, fishAngle - 110);
        makeFins(canvas, rightFinsPoint, fishAngle, true);

        //左鱼鳍
        PointF leftFinsPoint = calculatePoint(headPoint, FIND_FINS_LENGTH, fishAngle + 110);
        makeFins(canvas, leftFinsPoint, fishAngle, false);

        PointF bodyBottomCenterPoint = calculatePoint(headPoint, BODY_LENGTH, fishAngle - 180);
        //节肢体
        PointF  middleBottomCenterPoint = makeSegment(canvas, bodyBottomCenterPoint,BIG_RADIUS,MIDDLE_RADIUS,FIND_MIDDLE_CIRCLE_LENGTH,true, fishAngle);

        //节制2
        makeSegment(canvas, middleBottomCenterPoint,MIDDLE_RADIUS,SMALL_RADIUS,FIND_SMALL_CIRCLE_LENGTH,false, fishAngle);

        float findEdgeLength = (float) Math.abs(Math.sin(Math.toRadians(currentValue * 1.5 * frequence)) * BIG_RADIUS);
        //尾巴
        makeTriangle(canvas, middleBottomCenterPoint, FIND_TRIANGLE_LENGTH,findEdgeLength, fishAngle);
        makeTriangle(canvas, middleBottomCenterPoint, FIND_TRIANGLE_LENGTH - 20,findEdgeLength - 30, fishAngle);

        // 身体
        makeBody(canvas, headPoint, bodyBottomCenterPoint, fishAngle);
    }

    private void makeBody(Canvas canvas, PointF headPoint, PointF bodyBottomCenterPoint, float fishAngle) {
        // 身体的坐标
        PointF topLeftPoint = calculatePoint(headPoint, HEAD_RADIUS, fishAngle + 90);
        PointF topRightPoint = calculatePoint(headPoint, HEAD_RADIUS, fishAngle - 90);
        PointF bottomLeftPoint = calculatePoint(bodyBottomCenterPoint, BIG_RADIUS, fishAngle + 90);
        PointF bottomRightPoint = calculatePoint(bodyBottomCenterPoint, BIG_RADIUS, fishAngle - 90);

        //二阶贝塞尔曲线
        PointF controlLeft = calculatePoint(headPoint, BODY_LENGTH * 0.56f, fishAngle + 130);
        PointF controlRight= calculatePoint(headPoint, BODY_LENGTH * 0.56f, fishAngle - 130);

        //回执
        mPath.reset();
        mPath.moveTo(topLeftPoint.x, topLeftPoint.y);
        mPath.quadTo(controlLeft.x, controlLeft.y, bottomLeftPoint.x, bottomLeftPoint.y);
        mPath.lineTo(bottomRightPoint.x, bottomRightPoint.y);
        mPath.quadTo(controlRight.x, controlRight.y, topRightPoint.x, topRightPoint.y);
        mPaint.setAlpha(160);
        canvas.drawPath(mPath, mPaint);

    }

    private void makeTriangle(Canvas canvas, PointF startPoint,float findCenterLength, float findEdgeLength, float fishAngle) {
        float triangleAngle = (float) (fishAngle + Math.sin(Math.toRadians(currentValue * 1.5 * frequence)) * 35);

        //三角形底边的中心坐标
        PointF centerPoint = calculatePoint(startPoint, findCenterLength, triangleAngle - 180);
        //三角形底边两个点
        PointF leftPoint = calculatePoint(centerPoint, findEdgeLength, triangleAngle + 90);
        PointF rightPoint = calculatePoint(centerPoint, findEdgeLength, triangleAngle - 90);

        mPath.reset();
        mPath.moveTo(startPoint.x, startPoint.y);
        mPath.lineTo(leftPoint.x, leftPoint.y);
        mPath.lineTo(rightPoint.x, rightPoint.y);
        canvas.drawPath(mPath, mPaint);
    }

    private PointF makeSegment(Canvas canvas, PointF bottomCenterPoint,
                             float bigRadius,float smallRadius, float findSmallCircleLength, boolean hasBig
            , float fishAngle) {

        float segmentAngle;
        if (hasBig) {
            //节制1
            segmentAngle = (float) (fishAngle + Math.cos(Math.toRadians(currentValue * 1.5 * frequence)) * 15);
        } else {
            segmentAngle = (float) (fishAngle + Math.sin(Math.toRadians(currentValue * 1.5 * frequence)) * 35);
        }


        //梯形上帝的圆心
        PointF upperCenterPoint = calculatePoint(bottomCenterPoint, findSmallCircleLength,
                segmentAngle - 180);
        // 梯形的四个点
        PointF bottomLeft = calculatePoint(bottomCenterPoint, bigRadius, segmentAngle + 90);
        PointF bottomRight = calculatePoint(bottomCenterPoint, bigRadius, segmentAngle - 90);
        PointF upperLeft = calculatePoint(upperCenterPoint, smallRadius, segmentAngle + 90);
        PointF upperRight = calculatePoint(upperCenterPoint, smallRadius, segmentAngle - 90);


        if (hasBig) {
            //画大圆
            canvas.drawCircle(bottomCenterPoint.x, bottomCenterPoint.y, bigRadius, mPaint);
        }

        //画小圆
        canvas.drawCircle(upperCenterPoint.x, upperCenterPoint.y, smallRadius, mPaint);

        //画梯形
        mPath.reset();
        mPath.moveTo(upperLeft.x, upperLeft.y);
        mPath.lineTo(upperRight.x, upperRight.y);
        mPath.lineTo(bottomRight.x, bottomRight.y);
        mPath.lineTo(bottomLeft.x, bottomLeft.y);
        canvas.drawPath(mPath, mPaint);
        return upperCenterPoint;
    }

    private void makeFins(Canvas canvas, PointF startPoint, float fishAngle, boolean isRight) {
        Log.d(TAG, "makeFins: ");
        float controlAngle = 115;

        // 鱼鳍的终点 -- 二阶贝塞尔曲线终点
        PointF endPoint = calculatePoint(startPoint, FINS_LENGTH, fishAngle - 180);
        PointF controlPoint = calculatePoint(startPoint, FINS_LENGTH * 1.8f,
                isRight ? fishAngle - controlAngle : fishAngle + controlAngle);
        //绘制
        mPath.reset();
        mPath.moveTo(startPoint.x, startPoint.y);
        // 二阶贝斯尔曲线
        Log.d(TAG, "makeFins: " + controlPoint.x + " " + controlPoint.y + " " + endPoint.x + " " + endPoint.y);
        mPath.quadTo(controlPoint.x, controlPoint.y, endPoint.x, endPoint.y);
        canvas.drawPath(mPath, mPaint);
    }

    /**
     * 根据角度和斜边长度 求坐标值
     * @param startPoint 起始点坐标
     * @param length 要求的点到起始点的直线距离 -- 线长
     * @param angle 鱼当前的朝向角度
     * @return
     */
    public PointF calculatePoint(PointF startPoint, float length, float angle) {
        // x坐标
        float deltaX = (float) (Math.cos(Math.toRadians(angle)) * length);
        // y坐标
        float deltaY = (float) (Math.sin(Math.toRadians(angle - 180)) * length);
        return new PointF(startPoint.x + deltaX, startPoint.y + deltaY);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicHeight() {
        return (int)(8.38f * HEAD_RADIUS);
    }

    @Override
    public int getIntrinsicWidth() {
        return (int)(8.38f * HEAD_RADIUS);
    }

    public PointF getMiddlePoint() {
        return middlePoint;
    }

    public PointF getHeadPoint() {
        return headPoint;
    }

    public static String getTAG() {
        return TAG;
    }

    public Path getmPath() {
        return mPath;
    }

    public Paint getmPaint() {
        return mPaint;
    }

    public float getFishMainAngle() {
        return fishMainAngle;
    }

    public int getOTHER_ALPHA() {
        return OTHER_ALPHA;
    }

    public float getHEAD_RADIUS() {
        return HEAD_RADIUS;
    }

    public float getBODY_LENGTH() {
        return BODY_LENGTH;
    }

    public float getFIND_FINS_LENGTH() {
        return FIND_FINS_LENGTH;
    }

    public float getFINS_LENGTH() {
        return FINS_LENGTH;
    }

    public float getBIG_RADIUS() {
        return BIG_RADIUS;
    }

    public float getMIDDLE_RADIUS() {
        return MIDDLE_RADIUS;
    }

    public float getSMALL_RADIUS() {
        return SMALL_RADIUS;
    }

    public float getFIND_MIDDLE_CIRCLE_LENGTH() {
        return FIND_MIDDLE_CIRCLE_LENGTH;
    }

    public float getFIND_SMALL_CIRCLE_LENGTH() {
        return FIND_SMALL_CIRCLE_LENGTH;
    }

    public float getFIND_TRIANGLE_LENGTH() {
        return FIND_TRIANGLE_LENGTH;
    }

    public float getCurrentValue() {
        return currentValue;
    }

    public float getFrequence() {
        return frequence;
    }

    public void setFrequence(float frequence) {
        this.frequence = frequence;
    }

    public void setFishMainAngle(float fishMainAngle) {
        this.fishMainAngle = fishMainAngle;
    }
}
