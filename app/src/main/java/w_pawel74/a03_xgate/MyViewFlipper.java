package w_pawel74.a03_xgate;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

/**
 * Created by wisniewskip on 2016-04-18.
 */
public class MyViewFlipper extends ViewFlipper {

    public MyViewFlipper(Context context) {
        super(context);
    }

    public MyViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent){
        if( findViewById(R.id.B_SETTINGS).isShown() == true )
            return super.dispatchTouchEvent(motionEvent);
        return false;
    }
}
