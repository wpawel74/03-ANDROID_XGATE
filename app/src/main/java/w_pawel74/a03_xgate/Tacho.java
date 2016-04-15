package w_pawel74.a03_xgate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by wisniewskip on 2016-04-06.
 */
public class Tacho extends FrameLayout {

    public Tacho(Context context) {
        super(context);
    }

    public Tacho(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Tacho(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Tacho(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Transformation RPM on angle of the needle
     *
     * @param current_rpm - speed in km/h
     * @return angle for rotation the needle
     */
    public float transformRpmToAngle(int current_rpm) {
        if (current_rpm < 0)
            current_rpm = 0;
        if (current_rpm > MAX_RPM)
            current_rpm = MAX_RPM;

        if (current_rpm < 5000)
            return -MAX_ANGLE + (current_rpm * (float) (88.0f / 5000.0f));
        if (current_rpm >= 5000 && current_rpm < 7000)
            return -MAX_ANGLE + 88.f + ((current_rpm - 5000) * (float) ((MAX_ANGLE - 88.0f) / 2000.f));

        return (current_rpm - 7000) * (float) (MAX_ANGLE / 6000.f);
    }

    public void resetRpmAnim() {
        startNeedleAnimation(transformRpmToAngle(0), 0);
    }

    /**
     * A modified version of RotateAnimation which allows to check the progress of active
     * animation (last calculated degree)
     */
    private class RotAnim extends RotateAnimation {
        private float from;
        private float to;
        private float degrees;

        public RotAnim(float fromDegrees, float toDegrees, int pivotXType, float pivotXValue,
                       int pivotYType, float pivotYValue) {
            super(fromDegrees, toDegrees, pivotXType, pivotXValue, pivotYType, pivotYValue);
            from = fromDegrees;
            to = toDegrees;
            degrees = from;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            degrees = from + ((to - from) * interpolatedTime);
        }

        public float getLastDegree() {
            return degrees;
        }
    }

    private final float MAX_ANGLE = 140;
    private final int MAX_RPM = 13000;
    private float currentAngle = 0;
    ImageView needle = null;
    ImageView needleShadow = null;
    LinearInterpolator interpolator = new LinearInterpolator();
    RotAnim needleAnim = null;

    /**
     * animation for rotation the needle
     *
     * @param toDegree - rotation needle
     * @param milisec  - time animation
     */
    public void startNeedleAnimation(float toDegree, long milisec) {
        final float lastDegree;
        if (milisec == 0)
            lastDegree = currentAngle;
        else
            lastDegree = (this.needleAnim != null ? this.needleAnim.getLastDegree() : currentAngle);

        RotateAnimation shadowAnim = new RotateAnimation(lastDegree, toDegree,
                RotateAnimation.RELATIVE_TO_SELF, (float) 0.5,
                RotateAnimation.RELATIVE_TO_SELF, (float) 0.5);

        RotAnim needleAnim = new RotAnim(lastDegree, toDegree, RotateAnimation.RELATIVE_TO_SELF,
                (float) 0.5, RotateAnimation.RELATIVE_TO_SELF, (float) 0.5);

        this.needleAnim = needleAnim;

        shadowAnim.setInterpolator(interpolator);
        needleAnim.setInterpolator(interpolator);

        shadowAnim.setDuration(milisec);
        needleAnim.setDuration(milisec);

        shadowAnim.setFillAfter(true);
        needleAnim.setFillAfter(true);

        needle = (ImageView) findViewById(R.id.IV_TACHOMETER_NEEDLE);
        needleShadow = (ImageView) findViewById(R.id.IV_TACHOMETER_NEEDLE_SHADOW);

        needleShadow.startAnimation(shadowAnim);
        needle.startAnimation(needleAnim);

        currentAngle = toDegree;
    }

    public void showRpmWithAnimation(int rpm) {
        startNeedleAnimation( transformRpmToAngle(rpm), 2000);
    }
}
