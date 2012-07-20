package com.livestrong.myplate.activity;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplate.R;
import com.livestrong.myplate.adapters.MealListAdapter;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.DataHelperDelegate;
import com.livestrong.myplate.back.models.FoodDiaryEntry;
import com.livestrong.myplate.back.models.FoodDiaryEntry.TimeOfDay;
import com.livestrong.myplate.back.models.Meal;
import com.livestrong.myplate.back.models.MealItem;

public class AddMealActivity extends LiveStrongActivity implements DataHelperDelegate {
	
	private Meal meal;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_add_meal);

        // Fetch Meal from Intent Extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	this.meal = (Meal) extras.get(Meal.class.getName());
        }
		
        MealListAdapter listAdapter = new MealListAdapter(this, this.meal);
        ListView listView = (ListView) findViewById(R.id.mealListView);
        listView.setAdapter(listAdapter);
        
        Button iAteThisButton = (Button) findViewById(R.id.doneButton);
        iAteThisButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View buttonView) {
				if (meal != null){
					Collection<MealItem> mealItems = meal.getItems();
					
					TimeOfDay timeOfDay = MyPlateApplication.getWorkingTimeOfDay();
					Date workingDate = MyPlateApplication.getWorkingDateStamp();
					
					for (MealItem item: mealItems){
						FoodDiaryEntry entry = new FoodDiaryEntry(item.getFood(), meal.getMealId(), timeOfDay, item.getServings(), workingDate);
						DataHelper.saveDiaryEntry(entry, AddMealActivity.this);
					}
					
					Intent resultIntent = new Intent();
					resultIntent.putExtra(AddFoodActivity.INTENT_FOOD_NAME, meal.getTitle());
					
					setResult(Activity.RESULT_OK, resultIntent);
					
    	            finish();	
				}			
			}
		});
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

	@Override
	public void dataReceived(Method methodCalled, Object data) {
		
		
	}
}
