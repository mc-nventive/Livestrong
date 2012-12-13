package com.demandmedia.livestrong.android;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.DataHelperDelegate;
import com.demandmedia.livestrong.android.back.db.DatabaseHelper;
import com.demandmedia.livestrong.android.back.models.FoodDiaryEntry.TimeOfDay;
import com.demandmedia.livestrong.android.utilities.SimpleDate;
import com.flurry.android.FlurryAgent;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.demandmedia.livestrong.android.R;

@ReportsCrashes(formKey = "dEx3eE1zenRWcG5NR1lTaW5td1Jvdmc6MQ")
public class MyPlateApplication extends Application {
	private static Context context;

	// Values that relate to the current session.
	// Easier to keep them here than to have to put them in intent bundles everywhere!
	private static SimpleDate workingDateStamp = new SimpleDate(new Date());
	private static TimeOfDay workingTimeOfDay = TimeOfDay.BREAKFAST;

	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("d MMMM, yyyy");

	// Detect app backgrounding
	Handler timingHandler = new Handler();
	private boolean backgrounded = true;
	private static Stack<Activity> activityStack = new Stack<Activity>();

	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
        ACRA.init(this);

        super.onCreate();
		context = this;
		DataHelper.initialize(context);
		setWorkingDateStamp(new Date());
		
		FlurryAgent.setCaptureUncaughtExceptions(false);
	}


	public static void setWorkingDateStamp(Date workingDateStamp) {
		if (workingDateStamp == null){
			MyPlateApplication.workingDateStamp = null;
		} else {
			MyPlateApplication.workingDateStamp = new SimpleDate(workingDateStamp);
		}		
	}

	public static void setWorkingTimeOfDay(TimeOfDay workingTimeOfDay) {
		MyPlateApplication.workingTimeOfDay = workingTimeOfDay;
	}
	
	@Override
	public void onTerminate() {
		Log.d("#TADA", "end");
	}

	public static Context getContext() {
		return context;
	}

	public static SimpleDate getWorkingDateStamp() {
		if (workingDateStamp == null){
			return new SimpleDate();
		}
		return workingDateStamp;
	}

	public static TimeOfDay getWorkingTimeOfDay() {
		return workingTimeOfDay;
	}

	// TODO Move this function and make it use the strings.xml resource file
	public static String getWorkingTimeOfDayString(){
        TimeOfDay timeOfDay = getWorkingTimeOfDay(); 
        switch (timeOfDay){
        	case BREAKFAST:
        		return "Breakfast";        	
        	case LUNCH:
        		return "Lunch";
        	case DINNER:
        		return "Dinner";
        	case SNACKS:
        		return "Snacks";
        }
        return "";
	}
	
	public static Context getFrontMostActivity() {
		if (activityStack.size() == 0) {
			return getContext();
		}
		return activityStack.lastElement();
	}

	// The following methods are use to check if the app is backgrounded, and sync the diary when it is.
	public void plusActivity(Activity activity) {
		activityStack.push(activity);
	}

	public void minusActivity() {
		activityStack.pop();
		timingHandler.removeCallbacks(checkBackgroundTask);
		timingHandler.postDelayed(checkBackgroundTask, 2000);
	}

	private void checkIfBackgrounded() {
		if (activityStack.size() == 0) {
			this.backgrounded = true;
			
			DataHelper.setDatabaseHelper(OpenHelperManager.getHelper(context, DatabaseHelper.class));
			DataHelper.refreshData(new DataHelperDelegate () {
				
				private int callCounter = 0;
				
				@Override
				public void dataReceivedThreaded(Method methodCalled, Object data) {
					callCounter++;
					if (callCounter >= 3){ // we only want to set the database helper to null after all refresh data callbacks have been made
						DataHelper.setDatabaseHelper(null);
						MyPlateApplication.setWorkingDateStamp(null);
					}											
				}

				@Override
				public boolean errorOccurredThreaded(Method methodCalled, Exception error, String errorMessage) {
					return false;
				}
			});			
		}
	}

	public boolean wasInBackground() {
		return this.backgrounded;
	}

	public void hasBeenLoaded() {
		this.backgrounded = false;
	}

	private Runnable checkBackgroundTask = new Runnable() {
		@Override
		public void run() {
			checkIfBackgrounded();
		}
	};
	
	public static String getPrettyWorkingDate(){
		return MyPlateApplication.getPrettyDate(MyPlateApplication.getWorkingDateStamp());
	}
	
	public static String getPrettyDate(Date date){
		if (isToday(date)) {
			return  MyPlateApplication.getContext().getString(R.string.today);
		} else if (isYesterday(date)) {
			return MyPlateApplication.getContext().getString(R.string.yesterday);
		} else { 
			return dateFormatter.format(date);
		}
	}
	
	public static boolean isToday(Date date) {
		return new SimpleDate().equals(new SimpleDate(date));
	}
	
	public static boolean isYesterday(Date date) {
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DATE, -1);
		return new SimpleDate(yesterday.getTime()).equals(new SimpleDate(date));
	}
}
