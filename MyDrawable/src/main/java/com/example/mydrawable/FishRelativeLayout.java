package com.example.mydrawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class FishRelativeLayout extends RelativeLayout {


    private Paint mPaint;
    private ImageView ivFish;
    private MyDrawable fishDrawable;
    private float touchX;
    private float touchY;
    private float ripple;
    private int alpha;


    public FishRelativeLayout(Context context) {
        this(context, null);
    }

    public FishRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FishRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(8);

        ivFish = new ImageView(context);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        ivFish.setLayoutParams(layoutParams);
        fishDrawable = new MyDrawable();
        ivFish.setImageDrawable(fishDrawable);
        addView(ivFish);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setAlpha(alpha);
        canvas.drawCircle(touchX, touchY, ripple * 150, mPaint);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchX = event.getX();
        touchY = event.getY();

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "ripple", 0, 1f).setDuration(1000);
        objectAnimator.start();

        makeTrail();

        return super.onTouchEvent(event);

    }

    private void makeTrail() {

        //鱼的重心
        PointF fishRelativeMiddle = fishDrawable.getMiddlePoint();
        // 鱼的重心： 绝对坐标 起始点
        PointF fishMiddle = new PointF(ivFish.getX() + fishRelativeMiddle.x, ivFish.getY() + fishRelativeMiddle.y);
        // 鱼的圆心的坐标 -- 控制点1
        PointF fishHead = new PointF(ivFish.getX() + fishDrawable.getHeadPoint().x, ivFish.getY() + fishDrawable.getHeadPoint().y);
        // 点击坐标 -- 结束点
        PointF touch = new PointF(touchX, touchY);

        // 控制点2的坐标
        float angle =includeAngle(fishMiddle, fishHead, touch);
        float delta = includeAngle(fishMiddle, new PointF(fishMiddle.x + 1, fishMiddle.y), fishHead);
        PointF controlPoint = fishDrawable.calculatePoint(fishMiddle,
                fishDrawable.getHEAD_RADIUS() * 1.6f, angle + delta);

        Path path = new Path();
        path.moveTo(fishMiddle.x - fishRelativeMiddle.x, fishMiddle.y - fishRelativeMiddle.y);
        path.cubicTo(fishHead.x - fishRelativeMiddle.x, fishHead.y - fishRelativeMiddle.x,
                controlPoint.x - fishRelativeMiddle.x, controlPoint.y - fishRelativeMiddle.x,
                touchX - fishRelativeMiddle.x, touchY - fishRelativeMiddle.x);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(ivFish, "x", "y", path);
        objectAnimator.setDuration(2000);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fishDrawable.setFrequence(1f);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                fishDrawable.setFrequence(2f);
            }
        });

        PathMeasure pathMeasure = new PathMeasure(path, false);
        float[] tan = new float[2];
        objectAnimator.addUpdateListener( a -> {
            float fraction = a.getAnimatedFraction();
            pathMeasure.getPosTan(pathMeasure.getLength() * fraction, null, tan);
            float angle_1 = (float) Math.toDegrees(Math.atan2(-tan[1], tan[0]));
            fishDrawable.setFishMainAngle(angle_1);
        });

        objectAnimator.start();
    }

    public float includeAngle(PointF O, PointF A, PointF B) {
        // cosAOB
        // OA*OB = (Ax - Ox)(Bx - Dx) + (Ay - Oy)*(By - Oy)

        float AOB = (A.x - O.x) * (B.x - O.x) + (A.y - O.y) * (B.y - O.y);
        float OALength = (float) Math.sqrt((A.x - O.x) * (A.x - O.x) + (A.y - O.y) * (A.y - O.y));
        float OBLength = (float) Math.sqrt((B.x - O.x) * (B.x - O.x) + (B.y - O.y) * (B.y - O.y));
        float cosAOB = AOB / (OALength * OBLength);

        //反余弦
        float angleAOB = (float) Math.toDegrees(Math.cos(cosAOB));

        //AB连线与x轴的夹角的tan值 - OB与x轴夹角的tan值
        float direction = (A.y - B.y) / (A.x - B.x) - (O.y - B.y) / (O.x - B.x);
        if (direction == 0) {
            if (AOB >= 0) {
                return 0;
            } else {
                return 180;
            }
        } else {
            if (direction > 0) {
                return -angleAOB;
            } else {
                return angleAOB;
            }
        }


    }

    public float getTouchX() {
        return touchX;
    }

    public void setTouchX(float touchX) {
        this.touchX = touchX;
    }

    public float getTouchY() {
        return touchY;
    }

    public void setTouchY(float touchY) {
        this.touchY = touchY;
    }

    public float getRipple() {
        return ripple;
    }

    public void setRipple(float ripple) {
        alpha = (int) (100* (1 - ripple));
        this.ripple = ripple;
    }
}







