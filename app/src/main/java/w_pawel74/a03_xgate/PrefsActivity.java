package w_pawel74.a03_xgate;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by wisniewskip on 2016-04-12.
 */
public class PrefsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
}
