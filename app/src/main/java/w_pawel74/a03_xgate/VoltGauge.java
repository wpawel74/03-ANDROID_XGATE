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
public class VoltGauge extends FrameLayout {

    public VoltGauge(Context context) {
        super(context);
    }

    public VoltGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VoltGauge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VoltGauge(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Transformation speed in km/h on angle of the needle
     * @param current_voltage
     * @return angle for rotation the needle
     */
    public float transformVoltageToAngle(float current_voltage){
        if (current_voltage < MIN_VOLTAGE)
            current_voltage = MIN_VOLTAGE;
        if (current_voltage > MAX_VOLTAGE)
            current_voltage = MAX_VOLTAGE;

        return (current_voltage - 13000) * (float)(MAX_ANGLE / 4000.f);
    }

    public void resetVoltAnim() {
        startNeedleAnimation(transformVoltageToAngle(0), 0);
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

        public float getLastDegree()
        {
            return degrees;
        }
    };

    private final float         MAX_ANGLE       = 104;
    private final int           MIN_VOLTAGE     = 11000;
    private final int           MAX_VOLTAGE     = 15000;
    private float               currentAngle    = 0;
    ImageView                   needle          = null;
    LinearInterpolator          interpolator    = new LinearInterpolator();
    RotAnim                     needleAnim      = null;

    /**
     * animation for rotation the needle
     * @param toDegree - rotation needle
     * @param milisec - time animation
     */
    public void startNeedleAnimation(float toDegree, long milisec) {
        final float lastDegree;
        if (milisec == 0)
            lastDegree = currentAngle;
        else
            lastDegree = (this.needleAnim != null ? this.needleAnim.getLastDegree() : currentAngle);

        RotAnim needleAnim = new RotAnim(lastDegree, toDegree, RotateAnimation.RELATIVE_TO_SELF,
                (float)0.5, RotateAnimation.RELATIVE_TO_SELF, (float)1.0 );

        this.needleAnim = needleAnim;

        needleAnim.setInterpolator(interpolator);

        needleAnim.setDuration(milisec);

        needleAnim.setFillAfter(true);

        needle = (ImageView)findViewById(R.id.IV_VOLT_NEDLE);

        needle.startAnimation(needleAnim);

        currentAngle = toDegree;
    }

    public void showVoltageWithAnimation(float voltage){
        startNeedleAnimation( transformVoltageToAngle(voltage), 2000 );
    }
}
