package w_pawel74.a03_xgate;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wisniewskip on 2016-04-18.
 */
public class Tours {

    private final static int MAX_HISTORY_ITEMS = 4;

    private ArrayList<Map<String, String>> m_list = new ArrayList<Map<String, String>>();

    private HashMap<String, String> putData(String date, String duration, String speed, String distance) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("date", date);
        item.put("duration", duration);
        item.put("speed", speed);
        item.put("distance", distance);
        return item;
    }

    /**
     * add history to the list. NOTE: if list is full the last element will be deleted automatically!
     * @param date
     * @param duration
     * @param speed
     * @param distance
     */
    public void add( String date, String duration, String speed, String distance ){
        if( m_list.size() >= MAX_HISTORY_ITEMS )
            delFirstItem();
        m_list.add( putData( date, duration, speed, distance ));
    }

    public ArrayList<Map<String, String>> getList(){
        return m_list;
    }

    public String[] from(){
        String[] from = { "date", "duration", "distance", "speed" };
        return from;
    }

    public int[] to(){
        int[] to = { R.id.TV_HISTORY_ITEM_DATE, R.id.TV_HISTORY_ITEM_DURATION, R.id.TV_HISTORY_ITEM_DISTANCE, R.id.TV_HISTORY_ITEM_SPEED };
        return to;
    }

    /**
     * clear history list
     */
    public void clear(){
        m_list.clear();
    }

    /**
     * delete first item from list
     */
    public void  delFirstItem(){
        if( m_list.size() > 0 )
            m_list.remove(0);
    }
    /**
     * used for store history in preferences
     * @param context
     */
    public void store( Context context ) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        JSONArray arr = new JSONArray();
        for( Map<String, String> map: m_list ) {
            try {
                JSONObject item = new JSONObject();

                item.put( "date", map.get("date") );
                item.put( "duration", map.get("duration") );
                item.put( "distance", map.get("distance") );
                item.put( "speed", map.get("speed") );
                arr.put( item );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // need to by apply in Android Studio!
        // TODO: what about older version of android?
        prefs.edit().putString("history", arr.toString()).apply();
        prefs.edit().commit();
    }

    /**
     * used for load history in preferences
     * @param context
     */
    public void load( Context context ) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            JSONArray arr = new JSONArray(prefs.getString("history", "{}"));
            for( int it = 0; it < arr.length() && it < MAX_HISTORY_ITEMS; it++ ){
                JSONObject obj = arr.optJSONObject(it);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put( "date", obj.getString("date"));
                map.put( "duration", obj.getString("duration"));
                map.put( "distance", obj.getString("distance"));
                map.put( "speed", obj.getString("speed"));

                m_list.add( map );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
