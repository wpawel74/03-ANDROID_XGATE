package w_pawel74.a03_xgate;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import java.util.Random;

/**
 * Created by wisniewskip on 2016-04-07.
 * Class to show single odometer disc with digit. Implementation with animation when digit is changing on the disc
 * TODO: isn't possible to stop animation before is finished
 */
public class OdometerDisc extends FrameLayout implements  Animation.AnimationListener {

    private static final String TAG = "ODO_Disc";

    public OdometerDisc(Context context) {
        super(context);
    }

    public OdometerDisc(Context context, AttributeSet attrs) {
        super(context, attrs);
        getInvertColorsAttr(context, attrs);
    }

    public OdometerDisc(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getInvertColorsAttr(context, attrs);
    }

    public OdometerDisc(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getInvertColorsAttr(context, attrs);
    }

    private ImageSwitcher m_switcher = null;
    Handler handler = new Handler();

    private static Animation inFromUpAnimation(int duration) {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
                Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,   0.0f
        );
        inFromRight.setDuration(duration);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    private static Animation outToDownAnimation(int duration) {
        Animation outtoLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f
        );
        outtoLeft.setDuration(duration);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // prepare ImageSwitcher to work ...
        m_switcher = (ImageSwitcher) this.findViewById(R.id.IS_DISC_SWITCHER);
        m_switcher.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // Create a new ImageView set it's properties
                ImageView imageView = new ImageView(getContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
                return imageView;
            }
        });

        // initial digit on disc
        setImageDrawable(R.drawable.nf0);

        /**
         * For some reason this solution doesn't work... :(
         */
//        m_switcher.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.odo_disc_in));
//        m_switcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.odo_disc_out));
        m_switcher.setInAnimation(inFromUpAnimation(getResources().getInteger(R.integer.odo_dial_animation_duration)));
        m_switcher.setOutAnimation(outToDownAnimation(getResources().getInteger(R.integer.odo_dial_animation_duration)));
        m_switcher.getInAnimation().setAnimationListener(this);
    }

    /*-------------------------------------------------------
     *                 Custom Methods
     *-------------------------------------------------------*/
    private int m_reqDigit;                         // requested digit on disc
    private boolean m_invertColors = false;         // invert colors in drawing resource image

    /**
     * Do use in all three constructors to get invertColors attribute from xml layout resource
     * @param context
     * @param attrs
     * @return invertColors attribute as boolean
     */
    private boolean getInvertColorsAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.OdometerDisc,
                0, 0);

        try {
            m_invertColors = a.getBoolean(R.styleable.OdometerDisc_invertColors, false);
        } finally {
            a.recycle();
        }
        return m_invertColors;
    }

    /**
     * set drawable form for ImageSwitcher based on invertColors attribute
     * @param resId
     */
    private void setImageDrawable( int resId ){
        /**
         * TODO: optymalization for setTag()
         *     is really necessary to create new instance of Integer???
         */
        m_switcher.setTag(new Integer(resId));

        if(m_invertColors) {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), resId);
            m_switcher.setImageDrawable(new BitmapDrawable(getResources(), createInvertedBitmap(bm)));
        } else
            m_switcher.setImageResource(((Integer) m_switcher.getTag()).intValue());
    }

    /**
     * transform digit to resource id
     * @param digit         digit [0..9]
     * @return              drawable resource id
     * @throws Exception    parameter out of range
     */
    private int digitToRId( int digit ) throws Exception {
        switch( digit ){
            case 0: return R.drawable.nf0;
            case 1: return R.drawable.nf1;
            case 2: return R.drawable.nf2;
            case 3: return R.drawable.nf3;
            case 4: return R.drawable.nf4;
            case 5: return R.drawable.nf5;
            case 6: return R.drawable.nf6;
            case 7: return R.drawable.nf7;
            case 8: return R.drawable.nf8;
            case 9: return R.drawable.nf9;
        }
        throw new Exception("digit to convert is outside the range!");
    }

    /**
     * transform identifier to digit
     * @param id                drawable resource identifier
     * @return                  digit [0..9]
     * @throws Exception        resource identifier is not correct
     */
    private int rIdToDigit( int id ) throws Exception {
        switch( id ){
            case R.drawable.nf0: return 0;
            case R.drawable.nf1: return 1;
            case R.drawable.nf2: return 2;
            case R.drawable.nf3: return 3;
            case R.drawable.nf4: return 4;
            case R.drawable.nf5: return 5;
            case R.drawable.nf6: return 6;
            case R.drawable.nf7: return 7;
            case R.drawable.nf8: return 8;
            case R.drawable.nf9: return 9;
        }
        throw new Exception("unknown resource id!");
    }

    /**
     * set requested digit value on disc and start animation to change digit on disc
     * @param digit                 request digit on view
     */
    public void setDigitWithAnimation(int digit) {
        m_reqDigit = digit;
        if( isDigitVisible(m_reqDigit) == false )
            showNextDigit();
    }

    /**
     * check if digit is already visible on screen
     * @param digit                 check for digit
     * @return                      true digit already visible
     */
    private boolean isDigitVisible(int digit){
        boolean retvalue = false;
        try {
            retvalue = rIdToDigit( (((Integer) m_switcher.getTag())).intValue() ) == digit ? true: false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retvalue;
    }

    /**
     * show next digit with animation on view
     */
    private void showNextDigit() {
        try {
            Integer resId = (Integer) m_switcher.getTag();
            resId = digitToRId(
                    (rIdToDigit(((Integer) m_switcher.getTag()).intValue()) + 1) % 10
            );
            setImageDrawable( resId );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * NOTE: deprecated
     * use if you want to have some random delays effect on disc
     */
    private void nextDigitAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if( isDigitVisible(m_reqDigit) == false )
                    showNextDigit();
            }
        }, (new Random()).nextInt()%300 );
    }

    /**
     * Create a inverted bitmap. Use with care!
     * @param src           base bitmap
     * @return Bitmap       inverted bitmap
     */
    private Bitmap createInvertedBitmap(Bitmap src) {
        ColorMatrix colorMatrix_Inverted =
                new ColorMatrix(new float[] {
                        -1,  0,  0,  0, 255,
                        0, -1,  0,  0, 255,
                        0,  0, -1,  0, 255,
                        0,  0,  0,  1,   0});

        ColorFilter ColorFilter_Sepia = new ColorMatrixColorFilter(
                colorMatrix_Inverted);

        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        paint.setColorFilter(ColorFilter_Sepia);
        canvas.drawBitmap(src, 0, 0, paint);

        return bitmap;
    }

    /*-------------------------------------------------------
     * callback from animation.
     *   We need to start animation if it is necessary...
     *-------------------------------------------------------*/
    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if( isDigitVisible(m_reqDigit) == false )
            showNextDigit();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
