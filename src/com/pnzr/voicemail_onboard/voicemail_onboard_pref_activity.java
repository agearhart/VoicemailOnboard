package com.pnzr.voicemail_onboard;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class voicemail_onboard_pref_activity extends PreferenceActivity{
	@Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
    }

}
