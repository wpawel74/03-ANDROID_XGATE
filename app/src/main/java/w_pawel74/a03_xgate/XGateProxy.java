package w_pawel74.a03_xgate;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.os.Handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by wisniewskip on 2016-04-12.
 */
public class XGateProxy extends AsyncTask<Void, String, Void> {

    private Context m_context = null;
    private DataInputStream m_xGateIn = null;
    private DataOutputStream m_xGateOut = null;
    private Socket m_socket;
    private IxGateOnDataListener m_xGateListener = null;
    private boolean m_sentInitData = false;
    private final String TAG = "XGateProxy";

    XGateProxy( Context context ){
        m_context = context;
    }

    private void tryConnectToXGate() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( m_context );

        Log.d(TAG, "---> CONNECTING ...");
        try {
            m_socket = new Socket(  sharedPrefs.getString("xgate_ip", "192.168.4.1"),
                                    Integer.valueOf(sharedPrefs.getString("xgate_port", "80")));
            m_xGateIn = new DataInputStream(m_socket.getInputStream());
            m_xGateOut = new DataOutputStream(m_socket.getOutputStream());

            ((Activity) m_context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((DashBoardActivity) m_context).showPopup((Activity) m_context, R.string.POPUP_NETWORK_INFO, R.string.POPUP_XGATE_CONNECTED);
                    new Handler().postDelayed(new Runnable() {

                        public void run() {
                            ((DashBoardActivity) m_context).hidePopup();
                        }
                    }, m_context.getResources().getInteger(R.integer.popup_hide_timeout));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            closeXGateConnection();
        }
        Log.d(TAG, "---> CONNECTING ... END");
    }

    private void closeXGateConnection(){
        Log.d(TAG, "---> CLOSE CONNECTION ...");
        try {
            if( m_xGateIn != null )
                m_xGateIn.close();
            m_xGateOut = null;
            if( m_xGateOut != null )
                m_xGateOut.close();
            m_xGateOut = null;
            if( m_socket != null )
                m_socket.close();
            m_socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        m_sentInitData = false;
    }

    @Override
    protected Void doInBackground(Void... params) {

        Log.d(TAG, "---> doInBackground ... running");

        while( isCancelled() == false ){

            Log.d(TAG, "---> NEXT LOOP");
            if( m_socket != null && m_socket.isConnected() == false )
                closeXGateConnection();
            if( m_socket == null || m_xGateIn == null || m_xGateOut == null ) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                tryConnectToXGate();
                continue;
            }
            String msg = null;
            try {
                if( m_sentInitData == false ) {
                    String conf = new String( "CONNECTED\n" );
                    //
                    // TODO: --- Do something !! ---
                    //

                    m_xGateOut.writeBytes(conf);
                    m_sentInitData = true;
                }
                Log.d(TAG, "---> TRY TO READ DATA...");
                msg = m_xGateIn.readLine();
                if( msg == null )
                    closeXGateConnection();
                Log.d(TAG, "---> READ MESSAGE: " + msg);
                publishProgress( msg );
            } catch (IOException e) {
                e.printStackTrace();
                ((Activity)m_context).runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        ((DashBoardActivity)m_context).showPopup((Activity) m_context, R.string.POPUP_NETWORK_ISSUE, R.string.POPUP_XGATE_NOT_FOUND);
                    }
                });
                closeXGateConnection();
            }
        }
        closeXGateConnection();
        Log.d(TAG, "---> END OF ASYNC TASK");
        return null;
    }

    /**
     * The update need to be done in UiThread
     * @param msg               received message (with \n at the end)
     */
    @Override
    protected void onProgressUpdate(String... msg) {
        if (m_xGateListener != null)
            m_xGateListener.onData(msg[0]);
    }

    @Override
    protected void onPostExecute(Void a) {
        Log.d(TAG, "---> onPostExecute");
    }

    public interface IxGateOnDataListener {
        void onData( String data );
    }

    public void setXGateOnDataListener(IxGateOnDataListener onDataListener) {
        this.m_xGateListener = onDataListener;
    }
}
