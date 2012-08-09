package com.livestrong.myplate.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.DataHelper.DistanceUnits;
import com.livestrong.myplate.back.DataHelper.WaterUnits;
import com.livestrong.myplate.back.DataHelper.WeightUnits;
import com.livestrong.myplate.constants.BuildValues;
import com.livestrong.myplate.utilities.NotificationReceiver;
import com.livestrong.myplatelite.R;
import com.sessionm.api.SessionM;

public class MoreAccountFragment extends FragmentDataHelperDelegate {
	
	LinearLayout view, reminderTimeContainer;
	CheckBox dailyReminderCheckBox;
	EditText reminderTimeEditText;
	Spinner weightSpinner, distancesSpinner, waterSpinner;
	ScrollView scrollView;
	Dialog dialog;
	Date reminderTime;
	int rowHeight = 0;
	
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
		this.dailyReminderCheckBox 	= (CheckBox) this.view.findViewById(R.id.dailyReminderCheckBox);
		this.reminderTimeEditText 	= (EditText) this.view.findViewById(R.id.reminderTimeEditText);
		this.weightSpinner 			= (Spinner) this.view.findViewById(R.id.weightSpinner);
		this.distancesSpinner		= (Spinner) this.view.findViewById(R.id.distancesSpinner);
		this.waterSpinner			= (Spinner) this.view.findViewById(R.id.waterSpinner);
		this.reminderTimeContainer	= (LinearLayout) this.view.findViewById(R.id.reminderTimeContainer);
		this.scrollView				= (ScrollView) this.view.findViewById(R.id.scrollView);
		
		long savedTime = DataHelper.getPref(DataHelper.PREFS_DAILY_REMINDER_TIME, (long) 0);
		if (savedTime != 0){
			this.reminderTime = new Date(savedTime);
		} else {
			this.reminderTime = new Date();
		}

		this.initializeButtons();
		this.refreshReminderEditText();
		this.initializeEditTexts();
		this.initializeSpinners();
		this.initalizeCheckBox();
		
		return this.view;
	}
	
	private void initializeButtons() {
		if (BuildValues.IS_LIGHT) {
			Button howItWorksButton = (Button) view.findViewById(R.id.howItWorksButton);
			howItWorksButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					SessionM.getInstance().presentActivity(SessionM.ActivityType.INTRODUCTION);
				}
			});
			
			Button achievemntsButton = (Button) view.findViewById(R.id.achievmentsButton);
			achievemntsButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					SessionM.getInstance().presentActivity(SessionM.ActivityType.PORTAL);
				}
			});
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
    public void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }

    @Override
    public void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused"); commit unsaved changes to persistent data, etc.
        // -> onStop()
    }
    
    @Override
    public void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    }
    
    @Override
    public void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
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
