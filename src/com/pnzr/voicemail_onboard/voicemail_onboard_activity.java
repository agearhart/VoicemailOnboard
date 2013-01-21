package com.pnzr.voicemail_onboard;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;

public class voicemail_onboard_activity extends ListActivity{

	public static final int PREF_MENU = Menu.FIRST;
	public static final int DELETE_ID=1;
	public static final int PLAY_ID=0;
	String[] theDates;
	String[] mFiles;
	Uri[] mUris;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            loadMail();
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, PREF_MENU, 0, R.string.pref);
        return result;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case PREF_MENU:
        	Intent prefIntent = new Intent();
        	prefIntent.setClassName("com.pnzr.voicemail_onboard", "com.pnzr.voicemail_onboard.voicemail_onboard_pref_activity");
        	startActivity(prefIntent);
            break;
        }

        return super.onOptionsItemSelected(item);
    }

	private void loadMail() {
		FilenameFilter filter= new FilenameFilter(){

			public boolean accept(File dir, String name) {
				return (name.endsWith(".3gp"));
			}
		};

		File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/voicemailonboard/");
		if(path.listFiles(filter)==null)
		{
			return;
		}
		else
		{
			File[] files=path.listFiles(filter);
			mFiles=new String[files.length];
			mUris=new Uri[mFiles.length];
			theDates=new String[files.length];
			String file="";
			long mils=0;
			int start=0;
			int end=0;
			DateFormat df=DateFormat.getInstance();
			Date date=new Date();

			for(int i=0;i<files.length;i++)
			{
				mFiles[i]=files[i].getAbsolutePath();
				mUris[i]=Uri.parse(mFiles[i]);
				file=files[i].getName();
				end=file.indexOf("_");
				mils=Long.parseLong(file.substring(start,end));
				date.setTime(mils);
				theDates[i]=df.format(date);
			}
		}

		if(theDates.length>0)
        {
        	setListAdapter(new ArrayAdapter<String>(this,R.layout.vm_row,theDates));
        	ListView lView=(ListView)findViewById(R.layout.main);
        	registerForContextMenu(lView);
        }
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
	    AdapterView.AdapterContextMenuInfo info=(AdapterContextMenuInfo) menuInfo;
    	menu.setHeaderTitle(theDates[info.position]);

    	String[] menuItems=getResources().getStringArray(R.array.contextMenu);
    	for(int i=0;i<menuItems.length;i++)
    	{
    		menu.add(Menu.NONE,i,i,menuItems[i]);
    	}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch(item.getItemId()) {
	    case DELETE_ID:
	        deleteVoicemail((int)info.id);
	        loadMail();
	        return true;
		case PLAY_ID:
	        playVoicemail((int)info.id);
	        loadMail();
	    	return true;
	    }
	    return super.onContextItemSelected(item);
	}


	private void playVoicemail(int id) {

	}

	private void deleteVoicemail(int id)
	{
		File vm=new File(mFiles[id]);
		vm.delete();
	}
}
