package com.demandmedia.livestrong.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.demandmedia.livestrong.android.MyPlateApplication;
import com.demandmedia.livestrong.android.R;
import com.demandmedia.livestrong.android.back.models.FoodDiaryEntry.TimeOfDay;

public class TrackActivity extends LiveStrongFragmentActivity {
	
	private LinearLayout breakfastItem, lunchItem, dinnerItem, snacksItem, exerciseItem, waterItem, weightItem;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_track);
        
        // Hook up Outlets
        this.breakfastItem 	= (LinearLayout) findViewById(R.id.breakfastItem);
        this.lunchItem 		= (LinearLayout) findViewById(R.id.lunchItem);
        this.dinnerItem 	= (LinearLayout) findViewById(R.id.dinnerItem);
        this.snacksItem 	= (LinearLayout) findViewById(R.id.snacksItem);
        this.waterItem 		= (LinearLayout) findViewById(R.id.waterItem);
        this.exerciseItem 	= (LinearLayout) findViewById(R.id.exerciseItem);
        this.weightItem 	= (LinearLayout) findViewById(R.id.weightItem);
        
        OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {				
				Intent intent = null;
				
				switch (view.getId()) {
					case R.id.breakfastItem:
						intent = new Intent(TrackActivity.this, FoodSelectorActivity.class);
						MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.BREAKFAST);
						break;
					case R.id.lunchItem:
						intent = new Intent(TrackActivity.this, FoodSelectorActivity.class);
						MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.LUNCH);
						break;
					case R.id.dinnerItem:
						intent = new Intent(TrackActivity.this, FoodSelectorActivity.class);
						MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.DINNER);
						break;
					case R.id.snacksItem:
						intent = new Intent(TrackActivity.this, FoodSelectorActivity.class);
						MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.SNACKS);
						break;
					case R.id.exerciseItem:
						intent = new Intent(TrackActivity.this, ExerciseSelectorActivity.class);
						break;
					case R.id.waterItem:
						intent = new Intent(TrackActivity.this, AddWaterActivity.class);
						break;
					case R.id.weightItem:
						intent = new Intent(TrackActivity.this, AddWeightActivity.class);
						break;
				}
				if (intent != null){
					startActivityForResult(intent, 1);	
					finish();
				}				
			}
		};
		
		this.breakfastItem.setOnClickListener(onClickListener);
		this.lunchItem.setOnClickListener(onClickListener);
		this.dinnerItem.setOnClickListener(onClickListener);
		this.snacksItem.setOnClickListener(onClickListener);
		this.waterItem.setOnClickListener(onClickListener);
		this.exerciseItem.setOnClickListener(onClickListener);
		this.weightItem.setOnClickListener(onClickListener);		
	}
}
