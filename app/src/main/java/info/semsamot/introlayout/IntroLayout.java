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
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class IntroLayout extends RelativeLayout {

    private static final String TAG = "info.semsamot.introlayout";

    public enum ShapeType {SHAPE_RECTANGLE, SHAPE_CIRCLE, SHAPE_HEXAGON}
    public enum ContentLayoutPosition {TOP, BOTTOM, FLOAT}

    private static final long DEFAULT_ANIMATION_DURATION = 500;
    private static final int DEFAULT_OVERLAY_COLOR = 0xc7000000;

    private ViewGroup contentLayout;
    private TextView txtContent;
    private Button btnNext, btnPrevious;

    private ContentLayoutPosition contentLayoutDefaultPosition = ContentLayoutPosition.BOTTOM;
    private ContentLayoutPosition contentLayoutCurrentPosition;

    private IntroTarget introTarget;

    private Path targetPath;
    private Rect targetRect;
    private Path arrowPath;

    private Paint mPaint;
    private Paint targetShapePaint;
    private Paint targetHighlightPaint;
    private Paint arrowPathPaint;
    private Paint debugDrawPaint;

    private ShapeType targetShapeType;

    private int overlayColor;
    private int targetHighlightColor;
    private int targetShapeBorderColor;
    private int arrowColor;
    private int arrowStrokeWidth;

    private double degrees;
    private float strokeWidth;
    private int highlightAlpha;
    private long animationSpeed;

    private boolean isArrowCurve = true;
    private boolean isDebugDraw = false;

    int
            ptx1, pty1,
            ptx2, pty2,
            ptx3, pty3;

    public IntroLayout(Context context) {
        this(context, null, 0);
    }

    public IntroLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IntroLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IntroLayout, defStyle, 0);

        this.overlayColor =
                a.getColor(R.styleable.IntroLayout_overlay_color, DEFAULT_OVERLAY_COLOR);
        this.targetHighlightColor =
                a.getColor(R.styleable.IntroLayout_target_highlight_color, Color.WHITE);
        this.targetShapeBorderColor =
                a.getColor(R.styleable.IntroLayout_target_shape_border_color, Color.RED);
        this.targetShapeType = ShapeType.values()[
                a.getInt(R.styleable.IntroLayout_target_highlight_shape, 0)];
        this.arrowColor =
                a.getColor(R.styleable.IntroLayout_arrow_color, Color.YELLOW);
        this.arrowStrokeWidth =
                a.getInt(R.styleable.IntroLayout_arrow_stroke_width, 3);

        a.recycle();
    }

    private void initChildren()
    {
        this.contentLayout = (ViewGroup) findViewById(R.id.content_layout);
        this.txtContent = (TextView) findViewById(R.id.txt_content);
        this.btnNext = (Button) findViewById(R.id.btn_next);
        this.btnPrevious = (Button) findViewById(R.id.btn_previous);
    }

    boolean isGfxInitiated;
    private void initGfx()
    {
        if (isGfxInitiated)
            return;

        Log.d(TAG, "initGfx called.");

        this.mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);

        this.targetShapePaint = new Paint();
        targetShapePaint.setAntiAlias(true);
        targetShapePaint.setStyle(Paint.Style.STROKE);
        targetShapePaint.setStrokeWidth(4);
        targetShapePaint.setColor(targetShapeBorderColor);

        this.targetHighlightPaint = new Paint();
        targetHighlightPaint.setStyle(Paint.Style.FILL);
        targetHighlightPaint.setColor(targetHighlightColor);
        targetHighlightPaint.setAlpha(highlightAlpha);

        this.arrowPathPaint = new Paint();
        arrowPathPaint.setAntiAlias(true);
        arrowPathPaint.setStyle(Paint.Style.STROKE);
        arrowPathPaint.setStrokeWidth(arrowStrokeWidth);
        arrowPathPaint.setColor(arrowColor);

        this.debugDrawPaint = new Paint();

        if (this.targetRect != null)
        {
            applyContentLayoutAlignment();
            this.arrowPath = makeArrowPath();
        }


        // setting layer type to software
        // because of Region.Op.DIFFERENCE is ignored on LAYER_TYPE_HARDWARE in API 11+
        if (Build.VERSION.SDK_INT >= 11)
            this.setLayerType(LAYER_TYPE_SOFTWARE, mPaint);

        isGfxInitiated = true;
    }

    private Path makeArrowPath()
    {
        Path arrow = new Path();

        int mContentLayoutEdge = contentLayoutCurrentPosition == ContentLayoutPosition.TOP ? 0 : getHeight();
        int mRight = getWidth();
        int mCenterX = mRight / 2;
        int mCenterY = mContentLayoutEdge / 2;

        /* drawing arrow line */

        ptx1 = mCenterX;
        pty1 = mContentLayoutEdge;

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

        int targetRectBottom = contentLayoutCurrentPosition == ContentLayoutPosition.TOP ?
                targetRect.bottom : -targetRect.bottom;
        pty2 = (mContentLayoutEdge + targetRectBottom) / 2;
        pty3 = targetRect.top + (targetRect.height()/2);


        if (isArrowCurve)
        {
            // Bezier Curve
            arrow.moveTo(mCenterX, mContentLayoutEdge);
            arrow.cubicTo(
                    ptx1, pty1,
                    ptx2, pty2,
                    ptx3, pty3);
        } else {
            // Straight Line
            arrow.moveTo(ptx2, mContentLayoutEdge);
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
            targetHighlightPaint.setAlpha(highlightAlpha);
            canvas.drawRect(targetRect, targetHighlightPaint);

            targetShapePaint.setStrokeWidth(strokeWidth);
            canvas.drawPath(targetPath, targetShapePaint);

            canvas.clipPath(targetPath, Region.Op.DIFFERENCE);

            canvas.drawColor(overlayColor);

            if (arrowPath != null)
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

    public void animateTargetRect(long repeatDuration)
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
        strokeAnimator.setDuration(repeatDuration).start();

        ObjectAnimator alphaAnimator = ObjectAnimator.ofInt(this, "highlightAlpha", 0, 50);
        alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                postInvalidate();
            }
        });
        alphaAnimator.setDuration(repeatDuration).start();
    }

    public void disableContentLayoutBackground() {
        contentLayout.setBackgroundColor(0x00000000);
        txtContent.setTextColor(Color.WHITE);
    }

    public IntroTarget getIntroTarget() {
        return introTarget;
    }

    public void setIntroTarget(IntroTarget introTarget) {
        this.introTarget = introTarget;

        if (introTarget.arrowColor != -1)
            setArrowColor(introTarget.arrowColor);
        if (introTarget.arrowStrokeWidth != -1)
            setArrowStrokeWidth(introTarget.arrowStrokeWidth);
        if (introTarget.highlightColor != -1)
            setTargetHighlightColor(introTarget.highlightColor);
        if (introTarget.shapeBorderColor != -1)
            setTargetShapeBorderColor(introTarget.shapeBorderColor);
        if (introTarget.shapeType != null)
            setTargetShapeType(introTarget.shapeType);

        setTargetView(introTarget.view);
    }

    public void setTargetView(final View targetView) {
        setTargetView(targetView, true);
    }

    @SuppressLint("NewApi")
    public void setTargetView(final View targetView, boolean waitForVisibleState)
    {
        if (targetView == null) return;

        if (waitForVisibleState && !targetView.isShown())
        {
            targetView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    setTargetView(targetView);

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

    public Rect getTargetRect() {
        return targetRect;
    }

    public void setTargetRect(Rect targetRect) {
        this.targetRect = targetRect;
        setTargetPath();

        if (getWidth() != 0 && getHeight() != 0)
        {
            if (contentLayoutDefaultPosition == ContentLayoutPosition.FLOAT) {
                Rect screenLeft = new Rect();
                Rect screenRight = new Rect();
                Rect screenTop = new Rect();
                Rect screenBottom = new Rect();

                getGlobalVisibleRect(screenLeft);
                getGlobalVisibleRect(screenRight);
                getGlobalVisibleRect(screenTop);
                getGlobalVisibleRect(screenBottom);

                screenLeft.right -= screenLeft.width() / 2;
                screenRight.left = screenRight.width() / 2;
                screenTop.bottom -= screenTop.height() / 2;
                screenBottom.top = screenBottom.height() / 2;

                boolean isInLeft, isInRight, isInTop, isInBottom;

                isInLeft = screenLeft.intersect(targetRect);
                isInRight = screenRight.intersect(targetRect);
                isInTop = screenTop.intersect(targetRect);
                isInBottom = screenBottom.intersect(targetRect);


                if (isInLeft && isInRight && isInTop) {
                    contentLayout.getLayoutParams().width = getWidth();
                    contentLayout.setX(0);
                    contentLayout.setY(targetRect.bottom);
                } else if (isInLeft && isInRight && isInBottom) {
                    contentLayout.getLayoutParams().width = getWidth();
                    contentLayout.setX(0);
                    contentLayout.setY(targetRect.top - contentLayout.getHeight());
                } else if (isInLeft) {
                    contentLayout.getLayoutParams().width = getWidth() - (targetRect.right + 50);
                    contentLayout.setX(targetRect.right + 50);
                    contentLayout.setY(targetRect.top);
                } else if (isInRight) {
                    contentLayout.getLayoutParams().width = getWidth() - (getWidth() - (targetRect.left - 50));
                    contentLayout.setX(0);
                    contentLayout.setY(targetRect.top);
                }

                disableContentLayoutBackground();
            } else {
                applyContentLayoutAlignment();
                this.arrowPath = makeArrowPath();
            }
        }

        postInvalidate();
    }

    public Path getTargetPath() {
        return targetPath;
    }

    public void setTargetPath() {
        targetPath = new Path();

        int targetLeft = targetRect.left;
        int targetRight = targetRect.right;

        switch (targetShapeType)
        {
            case SHAPE_RECTANGLE:
                targetPath.addRect(new RectF(targetRect), Path.Direction.CW);
                break;
            case SHAPE_CIRCLE:
                int cx = targetLeft + (targetRect.width() / 2);
                int cy = targetRect.top + (targetRect.height() / 2);
                int radius = (targetRect.width() > targetRect.height())
                        ? targetRect.width() / 2 : targetRect.height() / 2;
                targetPath.addCircle(cx, cy, radius, Path.Direction.CW);
                break;
            case SHAPE_HEXAGON:
                /*int edge = (targetRect.width() > targetRect.height())
                        ? targetRect.width() / 2 : targetRect.height() / 2;*/
                int edge = targetRect.height() / 2;
                int pcy = targetRect.top + targetRect.height() / 2;

                targetLeft += edge / 2;
                targetRight -= edge / 2;

                targetPath.moveTo(targetLeft, pcy - edge);

                //      -----
                targetPath.lineTo(targetRight, pcy - edge);
                //            \
                targetPath.lineTo(targetRight + edge / 2, pcy);
                //            /
                targetPath.lineTo(targetRight, pcy + edge);
                //      -----
                targetPath.lineTo(targetLeft, pcy + edge);
                //     \
                targetPath.lineTo(targetLeft - edge / 2, pcy);
                //  /
                targetPath.lineTo(targetLeft, pcy - edge);

                break;
        }
    }

    public void setTargetPath(Path targetPath) {
        this.targetPath = targetPath;
    }

    private void applyContentLayoutAlignment()
    {
        Rect contentRect = new Rect();
        contentLayout.getGlobalVisibleRect(contentRect);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();

        if (contentRect.intersect(targetRect))
        {
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            contentLayoutCurrentPosition = ContentLayoutPosition.TOP;
        } else {
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            contentLayoutCurrentPosition = ContentLayoutPosition.BOTTOM;
        }

        contentLayout.setLayoutParams(lp);
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

    public int getOverlayColor() {
        return overlayColor;
    }

    public void setOverlayColor(int overlayColor) {
        this.overlayColor = overlayColor;
    }

    public ShapeType getTargetShapeType() {
        return targetShapeType;
    }

    public void setTargetShapeType(ShapeType targetShapeType) {
        this.targetShapeType = targetShapeType;
    }

    public int getTargetHighlightColor() {
        return targetHighlightColor;
    }

    public void setTargetHighlightColor(int targetHighlightColor) {
        this.targetHighlightColor = targetHighlightColor;
    }

    public int getTargetShapeBorderColor() {
        return targetShapeBorderColor;
    }

    public void setTargetShapeBorderColor(int targetShapeBorderColor) {
        this.targetShapeBorderColor = targetShapeBorderColor;
    }

    public int getArrowColor() {
        return arrowColor;
    }

    public void setArrowColor(int arrowColor) {
        this.arrowColor = arrowColor;
    }

    public int getArrowStrokeWidth() {
        return arrowStrokeWidth;
    }

    public void setArrowStrokeWidth(int arrowStrokeWidth) {
        this.arrowStrokeWidth = arrowStrokeWidth;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint paint) {
        this.mPaint = paint;
    }

    public Paint getTargetShapePaint() {
        return targetShapePaint;
    }

    public void setTargetShapePaint(Paint targetShapePaint) {
        this.targetShapePaint = targetShapePaint;
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

    public int getHighlightAlpha() {
        return highlightAlpha;
    }

    public void setHighlightAlpha(int highlightAlpha) {
        this.highlightAlpha = highlightAlpha;
    }
    /* --- --- --- */
}
