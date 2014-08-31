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
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

public class IntroLayout extends RelativeLayout {

    private static final String TAG = "info.semsamot.introlayout";
    private static final long DEFAULT_ANIMATION_DURATION = 500;

    private Rect targetRect;
    private Paint mPaint;
    private float strokeWidth;
    private boolean isFullUpdateNeeded = false;

    public IntroLayout(Context context) {
        super(context);
    }

    public IntroLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntroLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init()
    {
        this.mPaint = new Paint();

        // setting layer type to software
        // because of Region.Op.DIFFERENCE is ignored on LAYER_TYPE_HARDWARE in API 11+
        if (Build.VERSION.SDK_INT >= 11)
            this.setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (targetRect != null && mPaint != null)
        {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(strokeWidth);
            mPaint.setColor(Color.RED);
            canvas.drawRect(targetRect, mPaint);

            if (isFullUpdateNeeded)
            {
//                isFullUpdateNeeded = false;
//                canvas.drawColor(Color.TRANSPARENT);

                mPaint.setStyle(Paint.Style.FILL);
                canvas.clipRect(targetRect, Region.Op.DIFFERENCE);
                canvas.drawColor(0xc7000000);

                Log.d(TAG, "IntroLayout draw updated...");
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        init();
    }

    public void animateTargetRect()
    {
        animateTargetRect(DEFAULT_ANIMATION_DURATION);
    }

    public void animateTargetRect(long duration)
    {
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
        this.isFullUpdateNeeded = true;
        postInvalidate();
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
}
