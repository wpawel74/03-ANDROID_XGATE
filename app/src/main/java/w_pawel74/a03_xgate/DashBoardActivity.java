package w_pawel74.a03_xgate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by wisniewskip on 2016-04-04.
 */
public class DashBoardActivity extends Activity implements View.OnClickListener, XGateProxy.IxGateOnDataListener {

    private static final int SWIPE_MIN_DISTANCE = 60;
    private static final int SWIPE_THRESHOLD_VELOCITY = 50;

    private static final String TAG = "DASHBOARD";
    private XGateProxy m_xGateProxy = null;
    private Activity m_activity = null;
    private PopupWindow m_popup = null;
    private View m_popupLayout = null;

    class MySimpleAdapter extends SimpleAdapter {
        /**
         * Constructor
         *
         * @param context  The context where the View associated with this SimpleAdapter is running
         * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
         *                 Maps contain the data for each row, and should include all the entries specified in
         *                 "from"
         * @param resource Resource identifier of a view layout that defines the views for this list
         *                 item. The layout file should include at least those named views defined in "to"
         * @param from     A list of column names that will be added to the Map associated with each
         *                 item.
         * @param to       The views that should display column in the "from" parameter. These should all be
         *                 TextViews. The first N views in this list are given the values of the first N columns
         */
        public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }
    private MySimpleAdapter m_simpleAdapter = null;

    private boolean m_ignition = false;
    private int m_odometer = 0;

    private GestureDetector m_detector = null;
    private ViewFlipper m_V_FLIPPER = null;

    private Tours m_tours = null;

    private int m_TourStartDistance = 0;
    private long m_TourStartTime = 0;
    private int m_TourID = -1;

    @Override
    public void onCreate(Bundle saveInstanceState) {

        super.onCreate(saveInstanceState);
        setContentView(R.layout.dashboard);

        m_activity = this;

        m_V_FLIPPER = (ViewFlipper) findViewById(R.id.V_FLIPPER);
        m_LL_BATTERY_MFD1 = (LinearLayout)findViewById(R.id.MFD1).findViewById(R.id.LL_BATTERY);
        m_LL_BATTERY_MFD2 = (LinearLayout)findViewById(R.id.MFD2).findViewById(R.id.LL_BATTERY);
        m_LL_TEMPERATURE_MFD1 = (LinearLayout)findViewById(R.id.MFD1).findViewById(R.id.LL_TEMPERATURE);
        m_LL_TEMPERATURE_MFD2 = (LinearLayout)findViewById(R.id.MFD2).findViewById(R.id.LL_TEMPERATURE);

        // TEST TEST TEST
        ((Button) findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Speedo) findViewById(R.id.SPEEDO)).showSpeedWithAnimation((new Random()).nextInt() % 130);
                ((Tacho) findViewById(R.id.TACHOMETER)).showRpmWithAnimation((new Random()).nextInt() % 13000);
                ((Odometer) findViewById(R.id.ODOMETER)).setOdometer((new Random()).nextInt());
                setTemperature((float) 34.5);
                setIcon(Icon.ICON_BATTERY, true);

                // store current tour ID
                m_tours.add( new SimpleDateFormat( "MM.dd HH:mm" ).format( new Date(System.currentTimeMillis() )),
                            TimeUtils.millisToShortDHMS(System.currentTimeMillis() - m_TourStartTime),
                            Integer.toString(m_odometer - m_TourStartDistance),
                            Integer.toString((int)((Speedo) findViewById(R.id.SPEEDO)).m_averageSpeed.getAverageValue()));

                // start new tour
                m_TourStartTime = System.currentTimeMillis();
                m_TourStartDistance = m_odometer;
                m_simpleAdapter.notifyDataSetChanged();
            }
        });

        // connect to onClick for B_SETTINGS buttons (MFD1 MFD2)
        ((Button)(findViewById(R.id.MFD1).findViewById(R.id.B_SETTINGS))).setOnClickListener(this);
        ((Button)(findViewById(R.id.MFD2).findViewById(R.id.B_SETTINGS))).setOnClickListener(this);
        m_V_FLIPPER.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.coin_fade_in));
        m_V_FLIPPER.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.coin_fade_out));
        ((ViewFlipper)(findViewById(R.id.MFD1)).findViewById(R.id.VF_MFD)).setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.mfd_down_in));
        ((ViewFlipper)(findViewById(R.id.MFD1)).findViewById(R.id.VF_MFD)).setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.mfd_down_out));
        ((ViewFlipper)(findViewById(R.id.MFD2)).findViewById(R.id.VF_MFD)).setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.mfd_down_in));
        ((ViewFlipper)(findViewById(R.id.MFD2)).findViewById(R.id.VF_MFD)).setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.mfd_down_out));

        ((Speedo) findViewById(R.id.SPEEDO)).resetSpeedAnim();
        ((Speedo) findViewById(R.id.SPEEDO)).m_averageSpeed.reset();
        ((Tacho) findViewById(R.id.TACHOMETER)).resetRpmAnim();
        ((VoltGauge)m_LL_BATTERY_MFD1.findViewById(R.id.VOLT_MULTIMETER)).resetVoltAnim();
        ((VoltGauge)m_LL_BATTERY_MFD2.findViewById(R.id.VOLT_MULTIMETER)).resetVoltAnim();

        m_TourStartTime = System.currentTimeMillis();

        m_tours = new Tours();
        m_tours.load(this);

        m_simpleAdapter = new MySimpleAdapter(this, m_tours.getList(), R.layout.tour_item, m_tours.from(), m_tours.to() );
        ((ListView)(findViewById(R.id.MFD1).findViewById(R.id.LV_TOURS))).setAdapter( m_simpleAdapter );
        ((ListView)(findViewById(R.id.MFD2).findViewById(R.id.LV_TOURS))).setAdapter( m_simpleAdapter );


        m_detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Log.d(TAG, "SWIPE_DRIECTION_LEFT");
                    m_V_FLIPPER.showNext();
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Log.d(TAG, "SWIPE_DRIECTION_RIGHT");
                    m_V_FLIPPER.showPrevious();
                    return true;
                } else {
                    if (m_V_FLIPPER.getCurrentView().getId() == R.id.MFD1 || m_V_FLIPPER.getCurrentView().getId() == R.id.MFD2) {
                        if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                            Log.d(TAG, "SWIPE_DRIECTION_UP");
                            ((ViewFlipper)(m_V_FLIPPER.getCurrentView().findViewById(R.id.VF_MFD))).setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.mfd_up_in));
                            ((ViewFlipper)(m_V_FLIPPER.getCurrentView().findViewById(R.id.VF_MFD))).setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.mfd_up_out));
                            ((ViewFlipper) m_V_FLIPPER.getCurrentView().findViewById(R.id.VF_MFD)).showNext();
                            return true;
                        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                            Log.d(TAG, "SWIPE_DRIECTION_DOWN");
                            ((ViewFlipper)(m_V_FLIPPER.getCurrentView().findViewById(R.id.VF_MFD))).setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.mfd_down_in));
                            ((ViewFlipper)(m_V_FLIPPER.getCurrentView().findViewById(R.id.VF_MFD))).setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.mfd_down_out));
                            ((ViewFlipper) m_V_FLIPPER.getCurrentView().findViewById(R.id.VF_MFD)).showPrevious();
                            return true;
                        }
                    }
                }
                return false;
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        m_xGateProxy = new XGateProxy( this );
        m_xGateProxy.setXGateOnDataListener( this );
        m_xGateProxy.execute();
    }

    @Override
    public void onStop() {
        super.onStop();

        m_xGateProxy.cancel(true);
        m_xGateProxy.setXGateOnDataListener( null );
        m_xGateProxy = null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        // store tours
        m_tours.store(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        dashBoardViewConfiguration();

        if( m_xGateProxy == null ) {
            // Just like threads, AsyncTasks can't be reused.
            m_xGateProxy = new XGateProxy(this);
            m_xGateProxy.setXGateOnDataListener(this);
            m_xGateProxy.execute();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        return super.onTouchEvent(event);
        return m_detector.onTouchEvent(event);
    }

    /**
     * start PreferenceActivity when onClick event in R.id.B_SETTING widgets
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.isShown() == true) {
            Intent intent = new Intent(DashBoardActivity.this,
                    PrefsActivity.class);
            startActivity(intent);
        }
    }

    // The method that displays the popup.
    public void showPopup(final Activity context, int rid_title, int rid_message) {
        int popupWidth;
        int popupHeight;

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.LL_POPUP);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_popupLayout = layoutInflater.inflate(R.layout.popup, viewGroup);

        ((TextView)(m_popupLayout.findViewById(R.id.TV_POPUP_TITLE))).setText(rid_title);
        ((TextView)(m_popupLayout.findViewById(R.id.TV_POPUP_INFORMATION))).setText(rid_message);
        m_popupLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        popupHeight = m_popupLayout.getMeasuredHeight();
        popupWidth = m_popupLayout.getMeasuredWidth();

        hidePopup();

        // Creating the PopupWindow
        m_popup = new PopupWindow(m_popupLayout, popupWidth, popupHeight, true );

        // hide popup onClick inside
        m_popup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if( m_popup != null )
                    m_popup.dismiss();
                return false;
            }
        });

        // Clear the default translucent background
        m_popup.setBackgroundDrawable(new BitmapDrawable());
        m_popup.setAnimationStyle(R.style.PopupAnimation);

        new Handler().postDelayed(new Runnable() {

            public void run() {
                m_popup.showAtLocation(m_popupLayout, Gravity.CENTER, 0, 0);
            }

        }, 100L);
    }

    /**
     * hide popup
     */
    public void hidePopup(){
        if( m_popup != null && m_popup.isShowing() == true )
            m_popup.dismiss();
    }

    /*----------------------------------------------------------------------
     *                     DASHBOARD CONFIGURATION
     *----------------------------------------------------------------------*/
    LinearLayout m_LL_TEMPERATURE_MFD1 = null;
    LinearLayout m_LL_TEMPERATURE_MFD2 = null;
    LinearLayout m_LL_BATTERY_MFD1 = null;
    LinearLayout m_LL_BATTERY_MFD2 = null;

    /**
     * Add or remove view from view flipper linked with MFD view
     * NOTE: Please check LayoutParams if you modify the implementation
     * @param v                         view object
     * @param f                         handler to flipper
     * @param visible                   requested visible or not
     */
    private void setDashBoardView( View v, ViewFlipper f, boolean visible ) {
        if ( visible == true ) {
            if( f.findViewById(v.getId()) == null )
                f.addView(v, new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT) );
            v.setVisibility(View.INVISIBLE);
        } else if (visible == false){
            f.removeView(v);
            v.setVisibility(View.GONE);
        }
    }

    /**
     * configuration for dashboard views
     */
    private void dashBoardViewConfiguration() {
        // widget custom configuration
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        findViewById(R.id.IV_BATTERY).setVisibility(sharedPrefs.getBoolean("ico_battery_visible", false)? View.VISIBLE: View.GONE);
        findViewById(R.id.IV_ENGINE).setVisibility(sharedPrefs.getBoolean("ico_engine_visible", false)? View.VISIBLE: View.GONE);
        findViewById(R.id.IV_TEMP).setVisibility(sharedPrefs.getBoolean("ico_temperature_visible", false)? View.VISIBLE: View.GONE);
        findViewById(R.id.IV_FUEL).setVisibility(sharedPrefs.getBoolean("ico_fuel_visible", false)? View.VISIBLE: View.GONE);
        findViewById(R.id.IV_HIGH_BEAM).setVisibility(sharedPrefs.getBoolean("ico_high_beam_visible", false)? View.VISIBLE: View.GONE);
        findViewById(R.id.IV_TURNS).setVisibility(sharedPrefs.getBoolean("ico_engine_visible", false)? View.VISIBLE: View.GONE);
        findViewById(R.id.IV_DRIVING_BEAM).setVisibility(sharedPrefs.getBoolean("ico_driving_beam_visible", false) ? View.VISIBLE : View.GONE);
        findViewById(R.id.IV_ALERT).setVisibility(sharedPrefs.getBoolean("ico_alert_visible", false) ? View.VISIBLE : View.GONE);

        setDashBoardView( m_LL_BATTERY_MFD1, (ViewFlipper)findViewById(R.id.MFD1).findViewById(R.id.VF_MFD), sharedPrefs.getBoolean("LL_BATTERY", false));
        setDashBoardView( m_LL_BATTERY_MFD2, (ViewFlipper)findViewById(R.id.MFD2).findViewById(R.id.VF_MFD), sharedPrefs.getBoolean("LL_BATTERY", false));
        setDashBoardView( m_LL_TEMPERATURE_MFD1, (ViewFlipper)findViewById(R.id.MFD1).findViewById(R.id.VF_MFD), sharedPrefs.getBoolean("LL_TEMPERATURE", false));
        setDashBoardView( m_LL_TEMPERATURE_MFD2, (ViewFlipper)findViewById(R.id.MFD2).findViewById(R.id.VF_MFD), sharedPrefs.getBoolean("LL_TEMPERATURE", false));
    }

    /*----------------------------------------------------------------------
     *                   INCOMING DATA FROM PROXY
     *----------------------------------------------------------------------*/
    private static final int FLAG_INPUT_GPIO_HIGH_BEAM  = 0x01;
    private static final int FLAG_INPUT_GPIO_LOW_BEAM = 0x02;
    private static final int FLAG_INPUT_GPIO_TURN_LIGHT	= 0x04;
    private static final int FLAG_INPUT_GPIO_ALERT = 0x08;
    private static final int FLAG_INPUT_GPIO_ENGINE_WARNING	= 0x10;
    private static final int FLAG_INPUT_GPIO_FUEL_WARNING = 0x20;
    private static final int FLAG_INPUT_GPIO_TEMP_WARNING = 0x40;
    private static final int FLAG_INPUT_GPIO_BATTERY_WARNING = 0x80;

    /**
     * xGate message parser
     * @param data              message from xGate (one line terminate with new line char)
     */
    @Override
    public void onData(String data) {
        String[] words = data.split(" ");
        if( words[0].startsWith("VOLTAGE") ){
            setVoltage(Integer.valueOf(words[1]));
        } else if ( words[0].startsWith("TEMPERATURE") ){
            setTemperature(Float.valueOf(words[2]));
        } else if ( words[0].startsWith("SPEED") ){
            ((Tacho) findViewById(R.id.SPEEDO)).showRpmWithAnimation(Integer.valueOf(words[1]));
        } else if( words[0].startsWith("ODOMETER") ){
            m_odometer = Integer.valueOf(words[1]);
            ((Odometer) findViewById(R.id.ODOMETER)).setOdometer(Integer.valueOf(words[1]));
        } else if( words[0].startsWith("DAILY_ODOMETER") ){
            setDailyOdometer(Integer.valueOf(words[1]));
        } else if( words[0].startsWith("INPUTS") ){
            int ico = Integer.valueOf(words[1]);
            setIcon(Icon.ICON_HIGH_BEAM, (ico & FLAG_INPUT_GPIO_HIGH_BEAM) == FLAG_INPUT_GPIO_HIGH_BEAM ? true: false );
            setIcon(Icon.ICON_DRIVING_BEAM, (ico & FLAG_INPUT_GPIO_LOW_BEAM) == FLAG_INPUT_GPIO_LOW_BEAM ? true: false );
            setIcon(Icon.ICON_TURNS, (ico & FLAG_INPUT_GPIO_TURN_LIGHT) == FLAG_INPUT_GPIO_TURN_LIGHT ? true: false );
            setIcon(Icon.ICON_ALERT, (ico & FLAG_INPUT_GPIO_ALERT) == FLAG_INPUT_GPIO_ALERT ? true: false );
            setIcon(Icon.ICON_ENGINE, (ico & FLAG_INPUT_GPIO_ENGINE_WARNING) == FLAG_INPUT_GPIO_ENGINE_WARNING ? true : false);
            setIcon(Icon.ICON_FUEL, (ico & FLAG_INPUT_GPIO_FUEL_WARNING) == FLAG_INPUT_GPIO_FUEL_WARNING ? true: false );
            setIcon(Icon.ICON_TEMPERATURE, (ico & FLAG_INPUT_GPIO_TEMP_WARNING) == FLAG_INPUT_GPIO_TEMP_WARNING ? true: false );
            setIcon(Icon.ICON_BATTERY, (ico & FLAG_INPUT_GPIO_BATTERY_WARNING) == FLAG_INPUT_GPIO_BATTERY_WARNING ? true: false );
        } else if( words[0].startsWith("IGNITION") ) {
            if( m_ignition != (Integer.valueOf(words[1]) == 1 ? true: false) ) {
                showPopup(m_activity, R.string.POPUP_INFO, !m_ignition ?
                                            R.string.POPUP_INFO_IGNITION_ON :
                                            R.string.POPUP_INFO_IGNITION_OFF);
                new Handler().postDelayed(new Runnable() {

                    public void run() {
                        ((DashBoardActivity) m_activity).hidePopup();
                    }
                }, getResources().getInteger(R.integer.popup_hide_timeout));
            }
            m_ignition = Integer.valueOf(words[1]) == 1 ? true: false;
            setIcon(Icon.ICON_ENGINE, !m_ignition ? true : false);
        } else if( words[0].startsWith("IGNITION_COUNTER") ){
            if( m_TourID != Integer.valueOf(words[1]) ){
                // store current tour ID
                m_tours.add( new SimpleDateFormat( "MM.dd HH:mm" ).format( new Date(System.currentTimeMillis() )),
                            TimeUtils.millisToLongDHMS(System.currentTimeMillis() - m_TourStartTime),
                            Integer.toString(m_odometer - m_TourStartDistance),
                            Integer.toString((int)((Speedo) findViewById(R.id.SPEEDO)).m_averageSpeed.getAverageValue()));

                // start new tour
                m_TourStartTime = System.currentTimeMillis();
                m_TourStartDistance = m_odometer;
                m_TourID = Integer.valueOf(words[1]);
                m_simpleAdapter.notifyDataSetChanged();
            }
        }
    }

    /*----------------------------------------------------------------------
     *                         DASHBOARD ICONs
     *----------------------------------------------------------------------*/
    public enum Icon {
        ICON_BATTERY, ICON_ALERT, ICON_DRIVING_BEAM, ICON_HIGH_BEAM, ICON_ENGINE, ICON_TEMPERATURE, ICON_TURNS, ICON_FUEL
    }

    /**
     * get view id for icon
     * @param icon                  see enum Icon
     * @return resource id
     */
    private int getViewIdByIcon( Icon icon ) {
        switch (icon) {
            case ICON_BATTERY:
                return R.id.IV_BATTERY;
            case ICON_ALERT:
                return R.id.IV_ALERT;
            case ICON_DRIVING_BEAM:
                return R.id.IV_DRIVING_BEAM;
            case ICON_HIGH_BEAM:
                return R.id.IV_HIGH_BEAM;
            case ICON_ENGINE:
                return R.id.IV_ENGINE;
            case ICON_TEMPERATURE:
                return R.id.IV_TEMP;
            case ICON_TURNS:
                return R.id.IV_TURNS;
            case ICON_FUEL:
                return R.id.IV_FUEL;
        }
        return 0;
    }

    /**
     * enable/disable icon on dashboard
     * @param icon              icon on dashboard (check Icon enum above)
     * @param active            requested status
     */
    public void setIcon( Icon icon, boolean active ) {
        if( findViewById(getViewIdByIcon(icon)) != null )
            ((ToggleButton) findViewById(getViewIdByIcon(icon))).setChecked(active);
    }

    /**
     * set temperature in celcius on widget
     * @param temperature     temperature in celcius
     */
    public void setTemperature( float temperature ) {
        ((TextView)m_LL_TEMPERATURE_MFD1.findViewById(R.id.TV_TEMPERATURE)).setText( Float.toString(temperature) );
        ((TextView)m_LL_TEMPERATURE_MFD2.findViewById(R.id.TV_TEMPERATURE)).setText( Float.toString(temperature) );
    }

    /**
     * set voltage on guage
     * @param voltage           voltage in milivolts unit, eg value 1000 mean 1V
     */
    public void setVoltage( int voltage ) {
        ((VoltGauge)m_LL_BATTERY_MFD1.findViewById(R.id.VOLT_MULTIMETER)).showVoltageWithAnimation(voltage);
        ((VoltGauge)m_LL_BATTERY_MFD2.findViewById(R.id.VOLT_MULTIMETER)).showVoltageWithAnimation(voltage);
    }

    public void setDailyOdometer( int odometer ) {
        if( findViewById(R.id.MFD1).findViewById(R.id.LV_DAILY_ODOMETER) != null )
            ((Odometer)findViewById(R.id.MFD1).findViewById(R.id.LV_DAILY_ODOMETER)).setOdometer(odometer);
        if( findViewById(R.id.MFD2).findViewById(R.id.LV_DAILY_ODOMETER) != null )
            ((Odometer)findViewById(R.id.MFD2).findViewById(R.id.LV_DAILY_ODOMETER)).setOdometer(odometer);
    }

}