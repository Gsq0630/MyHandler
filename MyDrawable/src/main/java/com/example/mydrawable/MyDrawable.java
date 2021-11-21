package com.example.mydrawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyDrawable extends Drawable {

    private static final String TAG = "MyDrawable";

    private Path mPath;
    private Paint mPaint;



    //鱼的重心
    private PointF middlePoint;

    //鱼的主要朝向角度
    private float fishMainAngle = 0;

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
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        float fishAngle = fishMainAngle;
        //鱼头的圆心坐标
        PointF headPoint = calculatePoint(middlePoint, BODY_LENGTH / 2, fishAngle);
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

        //尾巴
        makeTriangle(canvas, middleBottomCenterPoint, FIND_TRIANGLE_LENGTH,BIG_RADIUS, fishAngle);
        makeTriangle(canvas, middleBottomCenterPoint, FIND_TRIANGLE_LENGTH - 20,BIG_RADIUS - 30, fishAngle);

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
        //三角形底边的中心坐标
        PointF centerPoint = calculatePoint(startPoint, findCenterLength, fishAngle - 180);
        //三角形底边两个点
        PointF leftPoint = calculatePoint(centerPoint, findEdgeLength, fishAngle + 90);
        PointF rightPoint = calculatePoint(centerPoint, findEdgeLength, fishAngle - 90);

        mPath.reset();
        mPath.moveTo(startPoint.x, startPoint.y);
        mPath.lineTo(leftPoint.x, leftPoint.y);
        mPath.lineTo(rightPoint.x, rightPoint.y);
        canvas.drawPath(mPath, mPaint);
    }

    private PointF makeSegment(Canvas canvas, PointF bottomCenterPoint,
                             float bigRadius,float smallRadius, float findSmallCircleLength, boolean hasBig
            , float fishAngle) {
        //梯形上帝的圆心
        PointF upperCenterPoint = calculatePoint(bottomCenterPoint, findSmallCircleLength,
                fishAngle - 180);
        // 梯形的四个点
        PointF bottomLeft = calculatePoint(bottomCenterPoint, bigRadius, fishAngle + 90);
        PointF bottomRight = calculatePoint(bottomCenterPoint, bigRadius, fishAngle - 90);
        PointF upperLeft = calculatePoint(upperCenterPoint, smallRadius, fishAngle + 90);
        PointF upperRight = calculatePoint(upperCenterPoint, smallRadius, fishAngle - 90);


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


}
