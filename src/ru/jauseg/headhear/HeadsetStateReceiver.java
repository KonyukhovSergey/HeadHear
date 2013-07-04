package ru.jauseg.headhear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class HeadsetStateReceiver extends BroadcastReceiver
{
	private static boolean isHead = false;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();

		if ((action.compareTo(Intent.ACTION_HEADSET_PLUG)) == 0)
		{
			int headSetState = intent.getIntExtra("state", 0);
			isHead = headSetState != 0;
		}
	}
	
	public static boolean isHeadset()
	{
		return isHead;		
	}
}
