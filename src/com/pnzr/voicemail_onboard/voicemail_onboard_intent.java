package com.pnzr.voicemail_onboard;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

public class voicemail_onboard_intent extends IntentService{
	public static final String VMO="Voicemail_Onboard";
	public static boolean isRecording=true;

	public voicemail_onboard_intent() {
		super("voicemail_onboard_intent");
	}

	protected void onHandleIntent(Intent intent) {
		Context context = getBaseContext();
		SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(context);

		// Wait so many rings before answering
		if(!sPrefs.getString("rings", "1").equals("0"))
		{
			try {
				Thread.sleep(Integer.parseInt(sPrefs.getString("rings", "3"))*1000);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		//Did they hang up yet?
		TelephonyManager tMan = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if(tMan.getCallState()==TelephonyManager.CALL_STATE_RINGING)
		{
			Log.v(VMO,"Answering the call.");
			// Yep, still ringing.  Answer the phone.

			// Pretend you're a bluetooth headset and answer the call
            Intent press = new Intent(Intent.ACTION_MEDIA_BUTTON);
            press.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
            context.sendOrderedBroadcast(press, "android.permission.CALL_PRIVILEGED");

            // What goes down must come up
            Intent release = new Intent(Intent.ACTION_MEDIA_BUTTON);
            release.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
            context.sendOrderedBroadcast(release, "android.permission.CALL_PRIVILEGED");


			//Play answer message - per Google you cannot play audio through active call
			/*MediaPlayer mp = new MediaPlayer();//idle state

			try {
				mp.setDataSource(sPrefs.getString("greeting", "")); //initialized state
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			mp.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);

			//Once the greeting is finished playing record message and end call

			try {
				mp.prepare();//prepared state
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    mp.start();
		    mp.setOnCompletionListener(new OnCompletionListener(){

				public void onCompletion(MediaPlayer arg0) {
					arg0.release();

					//hang up the call
					android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.AIRPLANE_MODE_ON, 1);
		            Intent intent1 = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		            intent1.putExtra("state", 1);
		            sendBroadcast(new Intent("android.intent.action.AIRPLANE_MODE"));
		            sendBroadcast(intent1);

		            android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.AIRPLANE_MODE_ON, 0);
		            intent1.putExtra("state", 0);
		            sendBroadcast(new Intent("android.intent.action.AIRPLANE_MODE"));
		            sendBroadcast(intent1);
				}

			});
			*/

		    //Record Voicemail
            AudioManager am = (AudioManager) context.getSystemService(AUDIO_SERVICE);
            am.setMicrophoneMute(true);

			String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/voicemailonboard/"+System.currentTimeMillis() +"_" + (int)(Math.random() * 1000)+ ".3gp";

		    String state = android.os.Environment.getExternalStorageState();
		    if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  {
		        try {
					throw new IOException("SD Card is not mounted.  It is " + state + ".");
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }

		    // make sure the directory we plan to store the recording in exists
		    File directory = new File(path).getParentFile();
		    if (!directory.exists() && !directory.mkdirs()) {
		      try {
				throw new IOException("Path to file could not be created.");
		      } catch (IOException e) {
				e.printStackTrace();
		      }
		    }

		    File file=new File(path);
		    int frequency=8000;//44100 supported by device, 8000 by emulator
		    int channelConfig=AudioFormat.CHANNEL_CONFIGURATION_MONO;
		    int audioEncoding=AudioFormat.ENCODING_PCM_16BIT;
		    int bufferSize=AudioRecord.getMinBufferSize(frequency, channelConfig, audioEncoding);
		    int length=Integer.parseInt(sPrefs.getString("vmLength", "5"));//set in preferences

		    AudioRecord voicemail=null;
		    try{
		    	voicemail = new AudioRecord(MediaRecorder.AudioSource.VOICE_CALL, frequency, channelConfig, audioEncoding, bufferSize);
		    }catch(IllegalArgumentException iae)
		    {
		    	iae.printStackTrace();
		    }

		    if(voicemail==null)
		    {
		    	return;
		    }

		    voicemail.setNotificationMarkerPosition(length);
		    voicemail.setRecordPositionUpdateListener(new OnRecordPositionUpdateListener()
		    {
				public void onMarkerReached(AudioRecord arg0) {
					isRecording=false;
				}

				public void onPeriodicNotification(AudioRecord recorder) {
				}
		    });

		    OutputStream os = null;
			try {
				os = new FileOutputStream(file);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		    BufferedOutputStream bos = new BufferedOutputStream(os);
		    DataOutputStream dos = new DataOutputStream(bos);

		    short[] buffer=new short[bufferSize];

		    voicemail.startRecording();
		    isRecording=true;

		    while(isRecording)
		    {
		    	int bufferReadResult=voicemail.read(buffer,0,bufferSize);
		    	for (int i = 0; i < bufferReadResult; i++)
		    	{
		    		try {
						dos.writeShort(buffer[i]);
					} catch (IOException e) {
						e.printStackTrace();
					}
		    	}
		    }

		    voicemail.stop();
		    voicemail.release();
		    am.setMicrophoneMute(false);
		}
		killCall();
		setNotification();
		return;
	}

	private void killCall()
	{
		//hang up the call
		android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.AIRPLANE_MODE_ON, 1);
        Intent intent1 = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent1.putExtra("state", 1);
        sendBroadcast(new Intent("android.intent.action.AIRPLANE_MODE"));
        sendBroadcast(intent1);

        android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.AIRPLANE_MODE_ON, 0);
        intent1.putExtra("state", 0);
        sendBroadcast(new Intent("android.intent.action.AIRPLANE_MODE"));
        sendBroadcast(intent1);
	}

	private void setNotification()
	{
		//Set notification icon
	}

}
