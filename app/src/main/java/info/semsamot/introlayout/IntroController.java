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

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class IntroController {

    private Activity mActivity;
    private int layoutResId;
    private ViewGroup rootView;
    private View[] targets;
    private String[] messages;
    private OnShowChangeListener onShowChangeListener;
    private int currentShowIndex = -1;

    public IntroController(Activity mActivity) {
        this(mActivity, R.layout.intro_layout);
    }

    public IntroController(Activity mActivity, int layoutResId) {
        this.mActivity = mActivity;
        this.layoutResId = layoutResId;
        this.rootView = (ViewGroup) mActivity.getWindow().getDecorView().getRootView();
    }

    public void startShow()
    {
        final IntroLayout introLayout =
                (IntroLayout) View.inflate(mActivity, layoutResId, null);

        introLayout.animateTargetRect();

        introLayout.findViewById(R.id.btn_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextShow(introLayout);
                    }
                }
        );

        nextShow(introLayout);

        rootView.addView(introLayout);
    }

    public void nextShow(IntroLayout introLayout)
    {
        currentShowIndex++;

        if (onShowChangeListener != null)
            onShowChangeListener.beforeShowChanged(currentShowIndex);

        if (currentShowIndex >= targets.length)
        {
            rootView.removeView(introLayout);
            return;
        }

        if (currentShowIndex < messages.length)
            ((TextView) introLayout.findViewById(R.id.txt_content)).setText(messages[currentShowIndex]);

        introLayout.setTargetRect(targets[currentShowIndex]);

        if (onShowChangeListener != null)
            onShowChangeListener.afterShowChanged(currentShowIndex);
    }

    public void previousShow(IntroLayout introLayout)
    {
        currentShowIndex--;

        if (onShowChangeListener != null)
            onShowChangeListener.beforeShowChanged(currentShowIndex);

        if (currentShowIndex < 0)
        {
            currentShowIndex = 0;
            // todo set btn_prev as disabled
            return;
        }

        if (currentShowIndex < messages.length)
            ((TextView) introLayout.findViewById(R.id.txt_content)).setText(messages[currentShowIndex]);

        introLayout.setTargetRect(targets[currentShowIndex]);

        if (onShowChangeListener != null)
            onShowChangeListener.afterShowChanged(currentShowIndex);
    }

    public View[] getTargets() {
        return targets;
    }

    public void setTargets(View[] targets) {
        this.targets = targets;
    }

    public String[] getMessages() {
        return messages;
    }

    public void setMessages(String[] messages) {
        this.messages = messages;
    }

    public OnShowChangeListener getOnShowChangeListener() {
        return onShowChangeListener;
    }

    public void setOnShowChangeListener(OnShowChangeListener onShowChangeListener) {
        this.onShowChangeListener = onShowChangeListener;
    }

    public interface OnShowChangeListener
    {
        public void beforeShowChanged(int showIndex);
        public void afterShowChanged(int showIndex);
    }
}
