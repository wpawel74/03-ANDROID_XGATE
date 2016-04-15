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
public class Speedo extends FrameLayout {

    public Speedo(Context context) {
        super(context);
    }

    public Speedo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Speedo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Speedo(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Transformation speed in km/h on angle of the needle
     *
     * @param current_speed - speed in km/h
     * @return angle for rotation the needle
     */
    public float transformSpeedToAngle(float current_speed) {
        if (current_speed < 0)
            current_speed = 0;
        if (current_speed > 130)
            current_speed = 130;

        if (current_speed < 50)
            return -MAX_ANGLE + (current_speed * (float) (88.0f / 50.0f));
        if (current_speed >= 50 && current_speed < 70)
            return -MAX_ANGLE + 88.f + ((current_speed - 50) * (float) ((MAX_ANGLE - 88.0f) / 20.f));

        return (current_speed - 70) * (float) (MAX_ANGLE / 60.f);
    }

    public void resetSpeedAnim() {
        startNeedleAnimation(transformSpeedToAngle(0), 0);
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

    ;

    private final float MAX_ANGLE = 140;
    private final int MAX_SPEED = 130;
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

        needle = (ImageView) findViewById(R.id.IV_SPEED_NEEDLE);
        needleShadow = (ImageView) findViewById(R.id.IV_SPEED_NEEDLE_SHADOW);

        needleShadow.startAnimation(shadowAnim);
        needle.startAnimation(needleAnim);

        currentAngle = toDegree;
    }

    public void showSpeedWithAnimation(float speed) {
        startNeedleAnimation(transformSpeedToAngle(speed), 2000);
    }
}
