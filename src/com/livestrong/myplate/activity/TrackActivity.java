package com.livestrong.myplate.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.admarvel.android.ads.AdMarvelView;
import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplate.back.models.FoodDiaryEntry.TimeOfDay;
import com.livestrong.myplate.constants.BuildValues;
import com.livestrong.myplate.utilities.AdvertisementHelper;
import com.livestrong.myplatelite.R;
import com.sessionm.api.SessionM;

public class TrackActivity extends LiveStrongFragmentActivity {
	
	private LinearLayout breakfastItem, lunchItem, dinnerItem, snacksItem, exerciseItem, waterItem, weightItem;
	private AdvertisementHelper adHelper;
	
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
		
		// Light version contains ads
 		if (BuildValues.IS_LIGHT) {
 			initializeAdvertisement();
 			this.adHelper.startAdvertising();
 		}
	}

	private void initializeAdvertisement() {
		// Request AdM
		AdMarvelView admarvelView = (AdMarvelView) findViewById(R.id.ad);
		adHelper = new AdvertisementHelper();
		adHelper.initWithAdMarvelViewAndActivity(admarvelView, this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
		// bubble up the result
		if (resultCode == Activity.RESULT_OK && null != intent) {
			Intent resultIntent = new Intent();
			if (intent.getExtras() != null) {
				for (String key : intent.getExtras().keySet()) {
					resultIntent.putExtra(key, intent.getExtras().getString(key));
				}
			}
			
			setResult(resultCode, resultIntent);
			this.adHelper.stopAdvertising();
			finish();
		}
	}
}
