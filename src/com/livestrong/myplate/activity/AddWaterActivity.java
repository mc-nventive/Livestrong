package com.livestrong.myplate.activity;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.admarvel.android.ads.AdMarvelView;
import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.DataHelper.WaterUnits;
import com.livestrong.myplate.back.models.DiaryEntries;
import com.livestrong.myplate.back.models.WaterDiaryEntry;
import com.livestrong.myplate.constants.BuildValues;
import com.livestrong.myplate.utilities.AdvertisementHelper;
import com.livestrong.myplate.utilities.picker.NumberPicker;
import com.livestrong.myplatelite.R;
import com.sessionm.api.SessionM;

public class AddWaterActivity extends LiveStrongActivity {
	
	private WaterDiaryEntry diaryEntry;
	private NumberPicker waterPicker;
	WaterUnits unitsPreference;
	private AdvertisementHelper adHelper;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_add_water);
        
        this.initializePickers();

        // Fetch WaterDiaryEntry from Intent Extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
    		this.diaryEntry = (WaterDiaryEntry) extras.get(WaterDiaryEntry.class.getName());
            if (this.diaryEntry != null) {
        		setPickers(this.diaryEntry.getOnces());
        	}

        }

        Button iDrankThisButton = (Button) findViewById(R.id.iDrankThisButton);
        Button deleteButton = (Button) findViewById(R.id.deleteButton);

        if (this.diaryEntry != null) {
        	iDrankThisButton.setText("Update");
        	deleteButton.setVisibility(View.VISIBLE);
    	} else {
    		iDrankThisButton.setText("I Drank This");
        	deleteButton.setVisibility(View.GONE);
    	}

        TextView selectedDateTextView = (TextView) findViewById(R.id.timeOfDayTextView);
        selectedDateTextView.setText(MyPlateApplication.getPrettyWorkingDate());
        
        iDrankThisButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent resultIntent = new Intent();
				
				double ounces = getPickerOnces();
				
		    	// Not coming from the Diary; but maybe we already have a water diary entry for this day?
				DiaryEntries entries = DataHelper.getDailyDiaryEntries(MyPlateApplication.getWorkingDateStamp(), null);
				WaterDiaryEntry e = entries.getWaterEntry(MyPlateApplication.getWorkingDateStamp());
				if (e == null) {
					e = new WaterDiaryEntry(ounces, MyPlateApplication.getWorkingDateStamp());
					
					// Post SessionM event for first time tracking this day
					// Seems like this event was removed...
					// SessionM.getInstance().logAction("trackedWater");
				} else {
					if (diaryEntry != null){
						if (ounces == 0.0){ // Remove the entry if user enters 0 onces
							DataHelper.deleteDiaryEntry(e, AddWaterActivity.this);
						} else {
							e.setOnces(ounces);
						}
					} else {
						if (ounces > 0.0){
							e.addOnces(ounces);
						}
					}
				}
				
				setResult(Activity.RESULT_OK, resultIntent);

	            DataHelper.saveDiaryEntry(e, AddWaterActivity.this);
	            finish();
			}
		});

    	deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
	            DataHelper.deleteDiaryEntry(AddWaterActivity.this.diaryEntry, AddWaterActivity.this);
	            setResult(Activity.RESULT_OK);
	            finish();
			}
		});

		// Light version contains ads
 		if (BuildValues.IS_LIGHT) {
 			initializeAdvertisement();
 			this.adHelper.startAdvertising();
 		}
	}

	private void initializeAdvertisement() {
		// TODO Auto-generated method stub
		AdMarvelView admarvelView = (AdMarvelView) findViewById(R.id.ad);
		adHelper = new AdvertisementHelper();
		adHelper.initWithAdMarvelViewAndActivity(admarvelView, this);
	}

	private double getPickerOnces() {
		Integer waterAmount;
		int selectedIndex = this.waterPicker.getCurrent();
		switch (this.unitsPreference) {
			case MILLILITERS:
				waterAmount = (Integer) WaterDiaryEntry.metricWaterPickerValues.values().toArray()[selectedIndex];
				return (double) WaterDiaryEntry.getOnceWater(waterAmount);
			default:
				waterAmount = (Integer) WaterDiaryEntry.imperialWaterPickerValues.values().toArray()[selectedIndex];
				return (double) waterAmount;
		}
	}

	private void setPickers(double onces) {
		switch (this.unitsPreference) {
			case MILLILITERS:
				this.waterPicker.setCurrent(WaterDiaryEntry.getMetricWaterIndex(onces));
				break;
			default:
				this.waterPicker.setCurrent((int) Math.round(onces / WaterDiaryEntry.ONCES_PER_GLASS) - 1);
				break;
		}
	}

	private void initializePickers() {
		this.waterPicker = (NumberPicker) findViewById(R.id.waterPicker);
		
		TextView unitsTv = (TextView) findViewById(R.id.unitsTextView);

		String[] waterValues;
		this.unitsPreference = WaterUnits.valueOf((String)DataHelper.getPref(DataHelper.PREFS_WATER_UNITS, WaterUnits.MILLILITERS));
		switch (unitsPreference) {
			case MILLILITERS:
				waterValues = WaterDiaryEntry.metricWaterPickerValues.keySet().toArray(new String[WaterDiaryEntry.metricWaterPickerValues.size()]);
				unitsTv.setText("ml");
				break;
			default:
				waterValues = WaterDiaryEntry.imperialWaterPickerValues.keySet().toArray(new String[WaterDiaryEntry.imperialWaterPickerValues.size()]);
				unitsTv.setText("ounces");
				break;
		}
        this.waterPicker.setRange(0, waterValues.length - 1, waterValues);
        this.waterPicker.setFocusable(false);
	}
	
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        // -> onResume()
        this.adHelper.startAdvertising();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        this.adHelper.startAdvertising();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    	// Called before making the activity vulnerable to destruction; save your activity state in outState.
        // UI elements states are saved automatically by super.onSaveInstanceState()
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused"); commit unsaved changes to persistent data, etc.
        // -> onStop()
        this.adHelper.stopAdvertising();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        this.adHelper.stopAdvertising();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // The activity was stopped, and is about to be started again. It was not destroyed, so all members are intact.
        // -> onStart()
        this.adHelper.startAdvertising();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.adHelper.stopAdvertising();
        // The activity is about to be destroyed.
        if (isFinishing()) {
        	// Someone called finish()
        } else {
        	// System is temporarily destroying this instance of the activity to save space
        }
    }
}
