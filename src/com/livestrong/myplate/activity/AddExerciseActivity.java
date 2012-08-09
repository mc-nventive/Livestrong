package com.livestrong.myplate.activity;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.admarvel.android.ads.AdMarvelView;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.models.Exercise;
import com.livestrong.myplate.back.models.ExerciseDiaryEntry;
import com.livestrong.myplate.utilities.AdvertisementHelper;
import com.livestrong.myplate.utilities.ImageLoader;
import com.livestrong.myplate.utilities.SessionMHelper;
import com.livestrong.myplate.utilities.picker.NumberPicker;
import com.livestrong.myplate.utilities.picker.NumberPicker.OnChangedListener;
import com.livestrong.myplatelite.R;
import com.sessionm.api.SessionM;

public class AddExerciseActivity extends LiveStrongActivity {
	
	public static String INTENT_EXERCISE_NAME = "exerciseName";
	
	private Exercise exercise;
	private ExerciseDiaryEntry diaryEntry;
	private NumberPicker hoursPicker, minutesPicker;
	private TextView caloriesTextView;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_add_exercise);
        
        this.initializePickers();
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	this.exercise = (Exercise) extras.get(Exercise.class.getName());

        	this.caloriesTextView = (TextView)findViewById(R.id.caloriesTextView);
        	
        	if (this.exercise != null) {
        		this.diaryEntry = null;
				RuntimeExceptionDao<Exercise, Integer> dao = DataHelper.getDatabaseHelper().getExerciseDao();
				dao.createOrUpdate(this.exercise);
		    	caloriesTextView.setText(Math.round(this.exercise.getCalsPerHour()) + "");
        	} else {
        		this.diaryEntry = (ExerciseDiaryEntry) extras.get(ExerciseDiaryEntry.class.getName());
        		this.exercise = this.diaryEntry.getExercise();
        		setPickers((int) Math.round(this.diaryEntry.getMinutes()));
        		caloriesTextView.setText(this.diaryEntry.getCals() + "");
        	}

	    	TextView tv = (TextView) findViewById(R.id.exerciseNameTextView);
	    	if (this.diaryEntry != null) {
		    	tv.setText(this.diaryEntry.getTitle());
	    	} else {
		    	tv.setText(this.exercise.getTitle());
	    	}
	    	
	    	tv = (TextView) findViewById(R.id.descriptionTextView);
	    	if (this.diaryEntry != null) {
		    	tv.setText(this.diaryEntry.getCals() + " calories per hour");
	    	} else {
		    	tv.setText(this.exercise.getCalsPerHourWithUnits());
	    	}

	    	tv = (TextView) findViewById(R.id.timeOfDayTextView);
	    	tv.setText(MyPlateApplication.getPrettyWorkingDate());
	    	
	    	ImageLoader imageLoader = new ImageLoader(this);
	    	ImageView imageView = (ImageView)findViewById(R.id.foodImageView);
	    	imageView.setImageResource(R.drawable.icon_fitness);
	    	imageLoader.DisplayImage(this.exercise.getSmallImage(), imageView);
	    	
	        Button iDidThisButton = (Button) findViewById(R.id.iDidThisButton);
	        Button deleteButton = (Button) findViewById(R.id.deleteButton);

	        if (this.diaryEntry != null) {
	        	iDidThisButton.setText("Update");
	        	deleteButton.setVisibility(View.VISIBLE);
	    	} else {
	    		iDidThisButton.setText("I Did This");
	        	deleteButton.setVisibility(View.GONE);
	    	}

	        iDidThisButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					double pickerMinutes = getPickersMinutes();
				 	
					if (AddExerciseActivity.this.diaryEntry != null) {
						if (pickerMinutes == 0.0){
							DataHelper.deleteDiaryEntry(AddExerciseActivity.this.diaryEntry, AddExerciseActivity.this);
						} else {
							AddExerciseActivity.this.diaryEntry.setMinutes((int) getPickersMinutes());
							DataHelper.saveDiaryEntry(AddExerciseActivity.this.diaryEntry, AddExerciseActivity.this);
						}
					} else {
						ExerciseDiaryEntry e = new ExerciseDiaryEntry(
				           		AddExerciseActivity.this.exercise,
				           		getPickersMinutes(),
				           		MyPlateApplication.getWorkingDateStamp());

				        DataHelper.saveDiaryEntry(e, AddExerciseActivity.this);		
					}

					if (pickerMinutes > 0.0){						
						Intent resultIntent = new Intent();
						resultIntent.putExtra(AddExerciseActivity.INTENT_EXERCISE_NAME, AddExerciseActivity.this.exercise.getTitle());
						
						if(null != AddExerciseActivity.this.diaryEntry){
							resultIntent.putExtra(SessionMHelper.INTENT_SESSIONM, "trackedExercise");
						}
						
						setResult(Activity.RESULT_OK, resultIntent);
					}
					
					finish();
				}
			});

        	deleteButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
		            DataHelper.deleteDiaryEntry(AddExerciseActivity.this.diaryEntry, AddExerciseActivity.this);
		            finish();
				}
			});

        }
        
		// Initialize advertisements
		AdvertisementHelper.requestAd((AdMarvelView) findViewById(R.id.ad), this);
    }

	private double getPickersMinutes() {
		int hours = this.hoursPicker.getCurrent();
		int minutes = this.minutesPicker.getCurrent() * 5;
		return 60 * hours + minutes;
	}

	private void setPickers(int minutes) {
		int hours = (int) Math.floor(minutes / 60);
		minutes -= hours * 60;
		this.hoursPicker.setCurrent(hours);
		this.minutesPicker.setCurrent(minutes / 5);
	}
	
	private void updateCaloriesLabel(){
		double minutes = getPickersMinutes();
		double cals = minutes * exercise.getCalsPerHour() / 60.0;
		DecimalFormat decimalFormat = new DecimalFormat("#");
		caloriesTextView.setText(decimalFormat.format(Math.round(cals)) + "");
	}

	private void initializePickers() {
		// Hook up outlets
		this.hoursPicker = (NumberPicker) findViewById(R.id.hoursPicker);
        this.minutesPicker = (NumberPicker) findViewById(R.id.minutesPicker);
		
        this.hoursPicker.setOnChangeListener(new OnChangedListener() {
			@Override
			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
				updateCaloriesLabel();
			}
		});
        this.minutesPicker.setOnChangeListener(new OnChangedListener() {
			@Override
			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
				updateCaloriesLabel();
			}
		});
        
        
		String[] hoursValues = ExerciseDiaryEntry.hoursPickerValues.keySet().toArray(new String[ExerciseDiaryEntry.hoursPickerValues.size()]);
        this.hoursPicker.setRange(0, hoursValues.length - 1, hoursValues);
        this.hoursPicker.setFocusable(false);
        this.hoursPicker.setCurrent(1);
        
        String[] minuteValues = ExerciseDiaryEntry.minutesPickerValues.keySet().toArray(new String[ExerciseDiaryEntry.minutesPickerValues.size()]);
        this.minutesPicker.setRange(0, minuteValues.length - 1, minuteValues);
        this.minutesPicker.setFocusable(false);
	}
	
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        // -> onResume()
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
