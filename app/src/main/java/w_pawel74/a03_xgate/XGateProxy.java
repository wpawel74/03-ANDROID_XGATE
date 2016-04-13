package w_pawel74.a03_xgate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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

    private void connect() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( m_context );

        Log.d(TAG, "---> CONNECTING ...");
        try {
            m_socket = new Socket(  sharedPrefs.getString("xgate_ip", "192.168.4.1"),
                                    Integer.valueOf(sharedPrefs.getString("xgate_port", "80")));
            m_xGateIn = new DataInputStream(m_socket.getInputStream());
            m_xGateOut = new DataOutputStream(m_socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
        Log.d(TAG, "---> CONNECTING ... END");
    }

    private void close(){
        Log.d(TAG, "---> CLOSE CONNECTION ...");
        try {
            if( m_xGateIn != null )
                m_xGateIn.close();
            m_xGateOut = null;
            if( m_xGateOut != null )
                m_xGateOut.close();
            m_xGateOut = null;
            if( m_socket != null && m_socket.isClosed() == false )
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
                close();
            if( m_socket == null || m_xGateIn == null || m_xGateOut == null ) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connect();
                continue;
            }
            String msg = null;
            try {
                if( m_sentInitData == false ) {
                    Log.d(TAG, "---> WRITE 1 BYTE TO OUTPUT STREAM");
                    m_xGateOut.write(30);
                    m_sentInitData = true;
                }
                Log.d(TAG, "---> TRY TO READ DATA...");
                msg = m_xGateIn.readLine();
                Log.d(TAG, "---> READ MESSAGE: " + msg);
                publishProgress( msg );
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }
        close();
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
