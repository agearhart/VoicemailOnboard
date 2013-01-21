package com.pnzr.voicemail_onboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class voicemail_onboard_receiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING) && sPrefs.getBoolean("enabled", false))
		{
			context.startService(new Intent(context,voicemail_onboard_intent.class));
		}
	}
    
}

