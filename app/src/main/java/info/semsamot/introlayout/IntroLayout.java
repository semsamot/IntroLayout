/**
 * Created by semsamot on 8/31/14.
 *
 * Copyright 2014 semsamot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.semsamot.introlayout;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class IntroLayout extends RelativeLayout {

    private static final String TAG = "info.semsamot.introlayout";
    private static final long DEFAULT_ANIMATION_DURATION = 500;
    private static final int DEFAULT_OVERLAY_COLOR = 0xc7000000;

    private ViewGroup contentLayout;
    private TextView txtContent;
    private Button btnNext, btnPrevious;

    private Rect targetRect;
    private Path arrowPath;

    private Paint mPaint;
    private Paint targetRectPaint;
    private Paint arrowPathPaint;
    private Paint debugDrawPaint;

    private double degrees;
    private float strokeWidth;
    private long animationSpeed;

    private int overlayColor = DEFAULT_OVERLAY_COLOR;

    private boolean isArrowCurve = true;
    private boolean isDebugDraw = false;

    int
            ptx1, pty1,
            ptx2, pty2,
            ptx3, pty3;

    public IntroLayout(Context context) {
        super(context);
    }

    public IntroLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntroLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initChildren()
    {
        this.contentLayout = (ViewGroup) findViewById(R.id.content_layout);
        this.txtContent = (TextView) findViewById(R.id.txt_content);
        this.btnNext = (Button) findViewById(R.id.btn_next);
        this.btnPrevious = (Button) findViewById(R.id.btn_previous);
    }

    private void initGfx()
    {
        this.mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);

        this.targetRectPaint = new Paint();
        targetRectPaint.setStyle(Paint.Style.STROKE);
        targetRectPaint.setStrokeWidth(4);
        targetRectPaint.setColor(Color.RED);

        this.arrowPathPaint = new Paint();
        arrowPathPaint.setAntiAlias(true);
        arrowPathPaint.setStyle(Paint.Style.STROKE);
        arrowPathPaint.setStrokeWidth(3);
        arrowPathPaint.setColor(Color.YELLOW);

        this.debugDrawPaint = new Paint();

        if (this.targetRect != null)
        {
            this.arrowPath = makeArrowPath();
            applyContentLayoutAlignment();
        }


        // setting layer type to software
        // because of Region.Op.DIFFERENCE is ignored on LAYER_TYPE_HARDWARE in API 11+
        if (Build.VERSION.SDK_INT >= 11)
            this.setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
    }

    private Path makeArrowPath()
    {
        Path arrow = new Path();

        int mRight = getWidth();
        int mBottom = getHeight();
        int mCenterX = mRight / 2;
        int mCenterY = mBottom / 2;

        /* drawing arrow line */

        ptx1 = mCenterX;
        pty1 = mBottom;

        int leftGap = targetRect.left;
        int rightGap = mRight - targetRect.right;

        int horizontalMargin = (int) getInPixel(20);

        if (leftGap > rightGap)
        {
            ptx2 = leftGap / 2;
            ptx3 = targetRect.left - horizontalMargin;
        } else {
            ptx2 = targetRect.right + (rightGap / 2);
            ptx3 = targetRect.right + horizontalMargin;
        }

        pty2 = (mBottom - targetRect.bottom) / 2;
        pty3 = targetRect.top + (targetRect.height()/2);


        if (isArrowCurve)
        {
            // Bezier Curve
            arrow.moveTo(mCenterX, mBottom);
            arrow.cubicTo(
                    ptx1, pty1,
                    ptx2, pty2,
                    ptx3, pty3);
        } else {
            // Straight Line
            arrow.moveTo(ptx2, mBottom);
            arrow.lineTo(ptx2, pty3);
            arrow.lineTo(ptx3, pty3);
        }

        /* --- --- --- */

        /* drawing arrow head */
        //                                                -->              <--
        int dx = (int) ((leftGap > rightGap) ? getInPixel(-15) : getInPixel(15));
        int dy = (int) getInPixel(15);

        if (isArrowCurve)
        {
            int distX = Math.abs(ptx3 - ptx2);
            int distY = /*Math.abs*/(pty2 - pty3);
            double angle = Math.atan2(distY, distX);
            degrees = angle * (180 / Math.PI);
        } else {
            degrees = 0;
        }

        double offsetAng1 = (leftGap > rightGap) ? 22.5 : -22.5;
        double offsetAng2 = (leftGap > rightGap) ? -22.5 : 22.5;

        arrow.moveTo(ptx3, pty3);
        arrow.lineTo(
                (float) (ptx3 + dx * Math.cos(Math.toRadians(degrees + offsetAng1))),
                (float) (pty3 + dy * Math.sin(Math.toRadians(degrees + offsetAng1)))
        );
        arrow.moveTo(ptx3, pty3);
        arrow.lineTo(
                (float) (ptx3 + dx * Math.cos(Math.toRadians(degrees + offsetAng2))),
                (float) (pty3 + dy * Math.sin(Math.toRadians(degrees + offsetAng2)))
        );

        /* --- --- --- */

        return arrow;
    }

    private void debugDraw(Canvas canvas, int dx, double degrees)
    {
        int mCenterX = getWidth() / 2;
        int mCenterY = getHeight() / 2;

        debugDrawPaint.setStrokeWidth(8 - strokeWidth);

        debugDrawPaint.setStyle(Paint.Style.STROKE);
        debugDrawPaint.setColor(Color.GREEN);
        canvas.drawPoint(mCenterX, mCenterY, debugDrawPaint);

        debugDrawPaint.setColor(Color.BLUE);
        canvas.drawLine(
                mCenterX, mCenterY,
                (float) (mCenterX + dx * Math.cos(Math.toRadians(degrees))),
                (float) (mCenterY + dx * Math.sin(Math.toRadians(degrees))),
                debugDrawPaint);

        canvas.drawCircle(mCenterX, mCenterY, Math.abs(dx) + 10, debugDrawPaint);

        debugDrawPaint.setStrokeWidth(strokeWidth * 1.5f);
        debugDrawPaint.setColor(Color.RED);
        canvas.drawPoint(ptx1, pty1, debugDrawPaint);
        canvas.drawPoint(ptx2, pty2, debugDrawPaint);
        canvas.drawPoint(ptx3, pty3, debugDrawPaint);

        debugDrawPaint.setStyle(Paint.Style.FILL);
        debugDrawPaint.setTextSize(30);
        canvas.drawText(String.valueOf(degrees), mCenterX - 100, mCenterY + 120, debugDrawPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (targetRect != null && mPaint != null)
        {
            targetRectPaint.setStrokeWidth(strokeWidth);
            canvas.drawRect(targetRect, targetRectPaint);

            canvas.clipRect(targetRect, Region.Op.DIFFERENCE);
            canvas.drawColor(overlayColor);

            canvas.drawPath(arrowPath, arrowPathPaint);

            if (isDebugDraw)
                debugDraw(canvas, 30, degrees);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initChildren();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        initGfx();
    }

    public void animateTargetRect()
    {
        animateTargetRect(DEFAULT_ANIMATION_DURATION);
    }

    public void animateTargetRect(long duration)
    {
        if (Build.VERSION.SDK_INT < 11) return;

        ObjectAnimator strokeAnimator = ObjectAnimator.ofFloat(this, "strokeWidth", 0.5f, 8);
        strokeAnimator.setRepeatMode(ValueAnimator.REVERSE);
        strokeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        strokeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                postInvalidate();
            }
        });
        strokeAnimator.setDuration(duration).start();
    }

    public boolean isArrowCurve() {
        return isArrowCurve;
    }

    public void setArrowCurve(boolean isArrowCurve) {
        this.isArrowCurve = isArrowCurve;
    }

    public boolean isDebugDraw() {
        return isDebugDraw;
    }

    public void setDebugDraw(boolean isDebugDraw) {
        this.isDebugDraw = isDebugDraw;
    }

    public ViewGroup getContentLayout() {
        return contentLayout;
    }

    public TextView getTxtContent() {
        return txtContent;
    }

    public Button getBtnNext() {
        return btnNext;
    }

    public Button getBtnPrevious() {
        return btnPrevious;
    }

    public Rect getTargetRect() {
        return targetRect;
    }

    public void setTargetRect(final View targetView) {
        setTargetRect(targetView, true);
    }

    public void setTargetRect(final View targetView, boolean waitForVisibleState) {

        if (waitForVisibleState && !targetView.isShown())
        {
            targetView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    setTargetRect(targetView);

                    if (Build.VERSION.SDK_INT < 16)
                        targetView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    else
                        targetView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        } else {
            Rect rect = new Rect();
            targetView.getGlobalVisibleRect(rect);
            setTargetRect(rect);
        }
    }

    public void setTargetRect(Rect targetRect) {
        this.targetRect = targetRect;

        if (getWidth() != 0 && getHeight() != 0)
        {
            this.arrowPath = makeArrowPath();
            applyContentLayoutAlignment();
        }



        postInvalidate();
    }

    private void applyContentLayoutAlignment()
    {
        Rect contentRect = new Rect();
        contentLayout.getGlobalVisibleRect(contentRect);
        LayoutParams lp = (LayoutParams) contentLayout.getLayoutParams();
        if (contentRect.intersect(targetRect))
        {
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
    }

    public int getOverlayColor() {
        return overlayColor;
    }

    public void setOverlayColor(int overlayColor) {
        this.overlayColor = overlayColor;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint paint) {
        this.mPaint = paint;
    }

    public Paint getTargetRectPaint() {
        return targetRectPaint;
    }

    public void setTargetRectPaint(Paint targetRectPaint) {
        this.targetRectPaint = targetRectPaint;
    }

    public Paint getArrowPathPaint() {
        return arrowPathPaint;
    }

    public void setArrowPathPaint(Paint arrowPathPaint) {
        this.arrowPathPaint = arrowPathPaint;
    }

    private float getInPixel(float dp)
    {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    /* just for animate */
    private float getStrokeWidth() {
        return strokeWidth;
    }

    private void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
    /* --- --- --- */
}
