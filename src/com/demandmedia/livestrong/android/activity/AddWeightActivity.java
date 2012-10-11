package com.demandmedia.livestrong.android.activity;

import java.text.DecimalFormat;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.demandmedia.livestrong.android.Constants;
import com.demandmedia.livestrong.android.MyPlateApplication;
import com.demandmedia.livestrong.android.R;
import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.DataHelper.WaterUnits;
import com.demandmedia.livestrong.android.back.DataHelper.WeightUnits;
import com.demandmedia.livestrong.android.back.models.DiaryEntries;
import com.demandmedia.livestrong.android.back.models.WeightDiaryEntry;
import com.flurry.android.FlurryAgent;

public class AddWeightActivity extends LiveStrongActivity {
	
	private WeightDiaryEntry diaryEntry;
	WaterUnits unitsPreference;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_add_weight);

        // Fetch WaterDiaryEntry from Intent Extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
    		this.diaryEntry = (WeightDiaryEntry) extras.get(WeightDiaryEntry.class.getName());
        }

        final EditText weightEditText = (EditText) findViewById(R.id.weightEditText);
        TextView weightUnitsTextView = (TextView) findViewById(R.id.weightUnitsTextView);
		
        WeightUnits weightUnits = WeightUnits.valueOf((String) DataHelper.getPref(DataHelper.PREFS_WEIGHT_UNITS, (String) null));
		if (weightUnits == WeightUnits.KILOGRAMS){
			weightUnitsTextView.setText("kg");
		} else if (weightUnits == WeightUnits.POUNDS){
			weightUnitsTextView.setText("lbs");
		} else {
			weightUnitsTextView.setText("st");
		}
		
        Button trackButton = (Button) findViewById(R.id.trackButton);
        Button deleteButton = (Button) findViewById(R.id.deleteButton);

        if (this.diaryEntry != null) {
        	trackButton.setText("Update Weight");
        	deleteButton.setVisibility(View.VISIBLE);
        	DecimalFormat decimalFormat = new DecimalFormat("#.##");
        	weightEditText.setText(decimalFormat.format(this.diaryEntry.getWeightForSelectedUnits()));
    	} else {
    		trackButton.setText("Track Weight");
        	deleteButton.setVisibility(View.GONE);
    	}

        TextView selectedDateTextView = (TextView) findViewById(R.id.timeOfDayTextView);
        selectedDateTextView.setText(MyPlateApplication.getPrettyWorkingDate());
        
        trackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String weightString = weightEditText.getText().toString();
				if (weightString.equals("")){
					weightString = "0";
				}
				double weight = Double.parseDouble(weightString);
				
		    	// Not coming from the Diary; but maybe we already have a water diary entry for this day?
				DiaryEntries entries = DataHelper.getDailyDiaryEntries(MyPlateApplication.getWorkingDateStamp(), null);
				WeightDiaryEntry e = entries.getWeightEntry(MyPlateApplication.getWorkingDateStamp());
				if (e == null) {
					e = new WeightDiaryEntry(weight, MyPlateApplication.getWorkingDateStamp());
				} else {
					e.setWeight(weight);
				}

				FlurryAgent.logEvent(Constants.Flurry.TRACKED_WEIGHT_EVENT);
				setResult(Activity.RESULT_OK);
				
	            DataHelper.saveDiaryEntry(e, AddWeightActivity.this);
	            finish();
			}
		});

    	deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
	            DataHelper.deleteDiaryEntry(AddWeightActivity.this.diaryEntry, AddWeightActivity.this);
	            finish();
			}
		});

	}
	
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        // -> onResume()
        FlurryAgent.logEvent(Constants.Flurry.MY_WEIGHT_ACTIVITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // The activity was stopped, and is about to be started again. It was not destroyed, so all members are intact.
        // -> onStart()
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        if (isFinishing()) {
        	// Someone called finish()
        } else {
        	// System is temporarily destroying this instance of the activity to save space
        }
    }
}
