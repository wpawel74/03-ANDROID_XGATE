package w_pawel74.a03_xgate;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by wisniewskip on 2016-04-07.
 */
public class Odometer extends LinearLayout {
    public Odometer(Context context) {
        super(context);
    }

    public Odometer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Odometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Odometer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOdometer( int value ){
        String val = new String( String.valueOf(value) );
        if( findViewById( R.id.ODO_Nxxxx ) != null )
            ((OdometerDisc)findViewById( R.id.ODO_Nxxxx )).setDigitWithAnimation( (val.length() < 5) ? 0: val.charAt( val.length() - 5 ) - '0' );
        if( findViewById( R.id.ODO_xNxxx ) != null )
            ((OdometerDisc)findViewById( R.id.ODO_xNxxx )).setDigitWithAnimation( (val.length() < 4) ? 0: val.charAt( val.length() - 4 ) - '0' );
        if( findViewById( R.id.ODO_xxNxx ) != null )
         ((OdometerDisc)findViewById( R.id.ODO_xxNxx )).setDigitWithAnimation( (val.length() < 3) ? 0: val.charAt( val.length() - 3 ) - '0' );
        if( findViewById( R.id.ODO_xxxNx ) != null )
            ((OdometerDisc)findViewById( R.id.ODO_xxxNx )).setDigitWithAnimation( (val.length() < 2) ? 0: val.charAt( val.length() - 2 ) - '0' );
        if( findViewById( R.id.ODO_xxxxN ) != null )
            ((OdometerDisc)findViewById( R.id.ODO_xxxxN )).setDigitWithAnimation( (val.length() < 1) ? 0: val.charAt( val.length() - 1 ) - '0' );
    }
}
