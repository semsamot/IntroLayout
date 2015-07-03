/**
 * Created by semsamot on 10/24/14.
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

import android.view.View;

public class IntroTarget {

    public View view;
    public IntroLayout.ShapeType shapeType;

    public int shapeBorderColor     = -1;
    public int highlightColor       = -1;
    public int arrowColor           = -1;
    public int arrowStrokeWidth     = -1;

    public IntroTarget(View view) {
        this.view = view;
    }

    public IntroTarget(View view, IntroLayout.ShapeType shapeType,
                       int shapeBorderColor, int highlightColor,
                       int arrowColor, int arrowStrokeWidth) {
        this.view = view;
        this.shapeType = shapeType;
        this.shapeBorderColor = shapeBorderColor;
        this.highlightColor = highlightColor;
        this.arrowColor = arrowColor;
        this.arrowStrokeWidth = arrowStrokeWidth;
    }
}
