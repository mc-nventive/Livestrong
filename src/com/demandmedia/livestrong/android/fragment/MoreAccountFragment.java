package com.demandmedia.livestrong.android.fragment;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.demandmedia.livestrong.android.R;
import com.demandmedia.livestrong.android.activity.LoginActivity;
import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.DataHelper.DistanceUnits;
import com.demandmedia.livestrong.android.back.DataHelper.WaterUnits;
import com.demandmedia.livestrong.android.back.DataHelper.WeightUnits;
import com.demandmedia.livestrong.android.back.models.UserProfile;
import com.demandmedia.livestrong.android.utilities.NotificationReceiver;

public class MoreAccountFragment extends FragmentDataHelperDelegate {
	
	LinearLayout view, reminderTimeContainer, connectContainer, syncContainer;
	CheckBox dailyReminderCheckBox;
	EditText reminderTimeEditText;
	Spinner weightSpinner, distancesSpinner, waterSpinner;
	ScrollView scrollView;
	Dialog dialog;
	Date reminderTime;
	Button syncButton;
	ProgressBar syncProgressBar;
	TextView lastSyncTextView;
	int rowHeight = 0;
	
	private class UserRefreshTask  extends AsyncTask<Void, Void, Void>
	{		
		@Override
		protected Void doInBackground(Void... params) 
		{
			DataHelper.refreshData(MoreAccountFragment.this);
			
			return null;
		}

	};
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
	        // We have different layouts, and in one of them this
	        // fragment's containing frame doesn't exist.  The fragment
	        // may still be created from its saved state, but there is
	        // no reason to try to create its view hierarchy because it
	        // won't be displayed.  Note this is not needed -- we could
	        // just run the code below, where we would create and return
	        // the view hierarchy; it would just never be used.
	        return null;
	    }
		
		// Hook up outlets
		this.view = (LinearLayout) inflater.inflate(R.layout.fragment_more_account, container, false);
		this.connectContainer 		= (LinearLayout) this.view.findViewById(R.id.connectContainer);
		this.dailyReminderCheckBox 	= (CheckBox) this.view.findViewById(R.id.dailyReminderCheckBox);
		this.reminderTimeEditText 	= (EditText) this.view.findViewById(R.id.reminderTimeEditText);
		this.weightSpinner 			= (Spinner) this.view.findViewById(R.id.weightSpinner);
		this.distancesSpinner		= (Spinner) this.view.findViewById(R.id.distancesSpinner);
		this.waterSpinner			= (Spinner) this.view.findViewById(R.id.waterSpinner);
		this.reminderTimeContainer	= (LinearLayout) this.view.findViewById(R.id.reminderTimeContainer);
		this.scrollView				= (ScrollView) this.view.findViewById(R.id.scrollView);
		this.syncButton 			= (Button) this.view.findViewById(R.id.syncNowButton);
		this.syncContainer			= (LinearLayout) this.view.findViewById(R.id.syncContainer);
		this.lastSyncTextView		= (TextView) this.view.findViewById(R.id.lastSyncTextView);
		this.syncProgressBar		= (ProgressBar) this.view.findViewById(R.id.syncProgressBar);
		this.syncProgressBar.setVisibility(View.INVISIBLE);
		
		long savedTime = DataHelper.getPref(DataHelper.PREFS_DAILY_REMINDER_TIME, (long) 0);
		if (savedTime != 0){
			this.reminderTime = new Date(savedTime);
		} else {
			this.reminderTime = new Date();	
		}		
		
		this.refreshReminderEditText();
		this.initializeEditTexts();
		this.initializeButtons();
		this.initializeSpinners();
		this.initalizeCheckBox();
		this.refreshSyncTextView();
		this.refreshLayout();
		
		return this.view;
	}
	
	private void refreshLayout(){
		if (rowHeight == 0){
			rowHeight = this.connectContainer.getLayoutParams().height;
		}
		if (DataHelper.isLoggedIn()){
			this.connectContainer.getLayoutParams().height = 0;
			this.syncContainer.getLayoutParams().height = rowHeight;
		} else {
			this.connectContainer.getLayoutParams().height = rowHeight;
			this.syncContainer.getLayoutParams().height = 0;
		}
		this.connectContainer.requestLayout();
		this.syncContainer.requestLayout();
	}
	
	private void initializeButtons(){
		Button connectButton = (Button)this.view.findViewById(R.id.connectButton);
		connectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), LoginActivity.class);
				intent.putExtra(LoginActivity.INTENT_APP_OFFLINE_MODE, true);
				startActivityForResult(intent, 1);
			}
		});
		
		this.syncButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				syncButton.setEnabled(false);
				syncProgressBar.setVisibility(View.VISIBLE);
				new UserRefreshTask().execute(new Void[]{});
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Activity.RESULT_OK){
			this.refreshLayout();
		}
	}
	
	private void initializeSpinners(){
		ArrayAdapter<CharSequence> weightAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.weightUnits, R.layout.spinner_item);
		weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.weightSpinner.setAdapter(weightAdapter);
		WeightUnits weightUnits = WeightUnits.valueOf((String)DataHelper.getPref(DataHelper.PREFS_WEIGHT_UNITS, (String) null));
		if (weightUnits == WeightUnits.KILOGRAMS){
			this.weightSpinner.setSelection(0);
		} else if (weightUnits == WeightUnits.POUNDS) {
			this.weightSpinner.setSelection(1);
		} else {
			this.weightSpinner.setSelection(2);
		}
		
		ArrayAdapter<CharSequence> distancesAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.distanceUnits, R.layout.spinner_item);
		distancesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.distancesSpinner.setAdapter(distancesAdapter);
		DistanceUnits distanceUnits = DistanceUnits.valueOf((String) DataHelper.getPref(DataHelper.PREFS_DISTANCE_UNITS, (DistanceUnits) null));
		if (distanceUnits == DistanceUnits.METERS){
			this.distancesSpinner.setSelection(0);
		} else if (distanceUnits == DistanceUnits.MILES){
			this.distancesSpinner.setSelection(1);
		}
		
		ArrayAdapter<CharSequence> waterAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.waterUnits, R.layout.spinner_item);
		waterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.waterSpinner.setAdapter(waterAdapter);
		WaterUnits waterUnits = WaterUnits.valueOf((String) DataHelper.getPref(DataHelper.PREFS_WATER_UNITS, (WaterUnits) null));
		if (waterUnits == WaterUnits.MILLILITERS){
			this.waterSpinner.setSelection(0);
		} else {
			this.waterSpinner.setSelection(1);
		}
		
	}
	
	private void initalizeCheckBox(){		
		Boolean dailyReminder = (Boolean) DataHelper.getPref(DataHelper.PREFS_DAILY_REMINDER, (Boolean) null);
		this.dailyReminderCheckBox.setChecked(dailyReminder);		
		if (!dailyReminder){
			reminderTimeContainer.getLayoutParams().height = 0;
		}
		
		this.dailyReminderCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked){
					reminderTimeContainer.getLayoutParams().height = 0;
					cancelReminder();
				} else {
					reminderTimeContainer.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
					setDailyReminder();
				}
				reminderTimeContainer.requestLayout();
				
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
				  @Override
				  public void run() {
					  scrollView.fullScroll(ScrollView.FOCUS_DOWN);
				  }
				}, 100);
			}
		});
	}
	
	private void initializeEditTexts(){
		reminderTimeEditText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (dialog == null){
					dialog = new Dialog(getActivity());
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog.setContentView(R.layout.dialog_time); 
					final TimePicker timePicker = (TimePicker) dialog.findViewById(R.id.timePicker1);
					timePicker.setCurrentHour(reminderTime.getHours());
					timePicker.setCurrentMinute(reminderTime.getMinutes());
					
					Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
					cancelButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
				            dialog.cancel();
						}
					});	
										
					Button doneButton = (Button) dialog.findViewById(R.id.doneButton);
					doneButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							reminderTime.setHours(timePicker.getCurrentHour());
							reminderTime.setMinutes(timePicker.getCurrentMinute());
							
							DataHelper.setPref(DataHelper.PREFS_DAILY_REMINDER_TIME, reminderTime.getTime());
							
							refreshReminderEditText();
							
							setDailyReminder();
							
							dialog.dismiss();
						}
					});
				}
				
				if (!dialog.isShowing()){
					dialog.show();	
				}
				
				return true;
			}
		});
	}
	
	private void refreshReminderEditText(){
		SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
		this.reminderTimeEditText.setText(formatter.format(this.reminderTime));
	}
	
	protected void saveProfile(){
		if (this.weightSpinner == null || this.distancesSpinner == null || this.waterSpinner == null || this.dailyReminderCheckBox == null){
			return;
		}
		
		// Save Weight Units
		int position = this.weightSpinner.getSelectedItemPosition();
		if (position == 0){ // Kilograms selected
			DataHelper.setPref(DataHelper.PREFS_WEIGHT_UNITS, WeightUnits.KILOGRAMS.toString()); 
		} else if (position == 1){ // Pounds selected
			DataHelper.setPref(DataHelper.PREFS_WEIGHT_UNITS, WeightUnits.POUNDS.toString());
		} else { // Stones selected
			DataHelper.setPref(DataHelper.PREFS_WEIGHT_UNITS, WeightUnits.STONES.toString());
		}
		
		// Save Distance Units
		position = this.distancesSpinner.getSelectedItemPosition();
		if (position == 0){ // Kilograms selected
			DataHelper.setPref(DataHelper.PREFS_DISTANCE_UNITS, DistanceUnits.METERS.toString()); 
		} else if (position == 1){ // Pounds selected
			DataHelper.setPref(DataHelper.PREFS_DISTANCE_UNITS, DistanceUnits.MILES.toString());
		} 
		
		// Save Water Units
		position = this.waterSpinner.getSelectedItemPosition();
		if (position == 0){ // Kilograms selected
			DataHelper.setPref(DataHelper.PREFS_WATER_UNITS, WaterUnits.MILLILITERS.toString()); 
		} else if (position == 1){ // Pounds selected
			DataHelper.setPref(DataHelper.PREFS_WATER_UNITS, WaterUnits.ONCES.toString());
		}		
		
		// Save Dialy Reminder status
		if (this.dailyReminderCheckBox.isChecked()){
			DataHelper.setPref(DataHelper.PREFS_DAILY_REMINDER, true);
		} else {
			DataHelper.setPref(DataHelper.PREFS_DAILY_REMINDER, false);
		}
	}
	
	@Override
	public void onPause() {
		// Save data
		this.saveProfile();	
		
		super.onPause();
	}

	@Override
	public void dataReceived(Method methodCalled, Object data) {
		if (methodCalled.equals(DataHelper.METHOD_SYNC_DIARY)) {
			this.syncProgressBar.setVisibility(View.INVISIBLE);
			this.syncButton.setEnabled(true);
			this.refreshSyncTextView();
		}
	}

	private void refreshSyncTextView(){
		long time = DataHelper.getPref(DataHelper.PREFS_LAST_SYNC, (long) 0);
		if (time != 0){
			Date lastSync = new Date(time);
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
			this.lastSyncTextView.setText("Last Sync: " + dateFormat.format(lastSync));
		} else {
			this.lastSyncTextView.setText("Last Sync: ");
		}
	}
	
	private void setDailyReminder(){		
        //---use the AlarmManager to trigger an alarm---
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Activity.ALARM_SERVICE);                 

        //---get current date and time---
        Calendar calendar = Calendar.getInstance();       
        
        //---sets the time for the alarm to trigger---
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, reminderTime.getHours());
        calendar.set(Calendar.MINUTE, reminderTime.getMinutes());
        calendar.set(Calendar.SECOND, 0);

        //---PendingIntent to launch activity when the alarm triggers-
        PendingIntent pendingIntent = getDailyReminderIntent();

        //---sets the alarm to trigger---
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
	}
	
	private void cancelReminder(){
		AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Activity.ALARM_SERVICE);
		alarmManager.cancel(getDailyReminderIntent());
	}
	
	private PendingIntent getDailyReminderIntent(){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            	getActivity().getBaseContext(), 0, 
                new Intent(getActivity(), NotificationReceiver.class), 
                0); 
        return pendingIntent;
	}
}
