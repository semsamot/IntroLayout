package info.semsamot.introlayout;

import android.view.View;

/**
 * Created by semsamot on 10/24/14.
 */
public class IntroTarget {

    private View view;
    private IntroLayout.ShapeType shapeType;

    private int shapeBorderColor;
    private int highlightColor;
    private int arrowColor;
    private int arrowStrokeWidth;

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
