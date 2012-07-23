package com.livestrong.myplate.utilities;

import java.util.Date;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.livestrong.myplatelite.R;
import com.livestrong.myplate.activity.TabBarActivity;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.models.DiaryEntries;

public class NotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		//---get the notification ID for the notification; 
	    // passed in by the MainActivity---
	    //int notifID = getIntent().getExtras().getInt("NotifID");
	 
		
		// Check if user has tracked anything
		DiaryEntries diaryEntries = DataHelper.getDailyDiaryEntries(new Date(), null);
		if (diaryEntries != null && diaryEntries.hasEntries() == true){
			return;
		}
		
	    //---PendingIntent to launch activity if the user selects 
	    // the notification---
	    Intent intent = new Intent(context, TabBarActivity.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	 
	    PendingIntent detailsIntent = PendingIntent.getActivity(context, 0, intent, 0);
	    
	    
	    NotificationManager nm = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
	    Notification notification = new Notification(R.drawable.icon, "LIVESTRONG", System.currentTimeMillis());
	 
	    CharSequence from = "LIVESTRONG - Daily Reminder";
	    CharSequence message = "Nothing has been tracked today.";      
	    notification.vibrate = new long[] { 100, 250, 100, 500};   
	    notification.setLatestEventInfo(context, from, message, detailsIntent);
	    notification.flags = Notification.FLAG_AUTO_CANCEL;
	    
	    nm.notify((int) System.currentTimeMillis(), notification);
	}

}
