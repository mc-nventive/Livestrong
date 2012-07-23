package com.livestrong.myplate.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.livestrong.myplatelite.R;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.models.Food;

public class NutritionFactsActivity extends LiveStrongActivity {
	
	private static int caloriesGoal;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
        	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
            // UI elements states are restored automatically by super.onCreate()
        }

        // The activity is being created; create views, bind data to lists, etc.
        setContentView(R.layout.activity_nutrition_facts);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	Food food = (Food) extras.get(Food.class.getName());

            ((TextView) findViewById(R.id.servingSizeTextView)).setText(food.getServingSize());
            ((TextView) findViewById(R.id.caloriesTextView)).setText(food.getCals()+"");
            ((TextView) findViewById(R.id.caloriesFromFatTextView)).setText(food.getCalsFromFat()+"");

            ((TextView) findViewById(R.id.totalFatTextView)).setText(Math.round(food.getFat())+"g");
            ((TextView) findViewById(R.id.satFatTextView)).setText(Math.round(food.getSatFat())+"g");
            ((TextView) findViewById(R.id.cholesterolTextView)).setText(Math.round(food.getCholesterol())+"mg");
            ((TextView) findViewById(R.id.sodiumTextView)).setText(Math.round(food.getSodium())+"mg");
            ((TextView) findViewById(R.id.carbsTextView)).setText(Math.round(food.getCarbs())+"g");
            ((TextView) findViewById(R.id.dietaryFiberTextView)).setText(Math.round(food.getDietaryFiber())+"g");
            ((TextView) findViewById(R.id.sugarsTextView)).setText(Math.round(food.getSugars())+"g");
            ((TextView) findViewById(R.id.proteinTextView)).setText(Math.round(food.getProtein())+"g");

            // Daily values
            caloriesGoal = DataHelper.getUserProfile(null).getCaloriesGoal();
            ((TextView) findViewById(R.id.totalFatDailyTextView)).setText(getDailyValue(food.getFat(), 65)+"%");
            ((TextView) findViewById(R.id.satFatDailyTextView)).setText(getDailyValue(food.getSatFat(), 20)+"%");
            ((TextView) findViewById(R.id.cholesterolDailyTextView)).setText(getDailyValue(food.getCholesterol(), 300)+"%");
            ((TextView) findViewById(R.id.sodiumDailyTextView)).setText(getDailyValue(food.getSodium(), 2300)+"%");
            ((TextView) findViewById(R.id.carbsDailyTextView)).setText(getDailyValue(food.getCarbs(), 300)+"%");
            ((TextView) findViewById(R.id.dietaryFiberDailyTextView)).setText(getDailyValue(food.getDietaryFiber(), 25)+"%");
            ((TextView) findViewById(R.id.proteinDailyTextView)).setText(getDailyValue(food.getProtein(), 50)+"%");

            ((TextView) findViewById(R.id.fatPercTextView)).setText(Math.round(food.getCalsPercFat())+"%");
            ((TextView) findViewById(R.id.carbsPercTextView)).setText(Math.round(food.getCalsPercCarbs())+"%");
            ((TextView) findViewById(R.id.proteinPercTextView)).setText(Math.round(food.getCalsPercProtein())+"%");
        }
    }
	
	private int getDailyValue(double grams, double rdv) {
		double dailyValueRatio = caloriesGoal / 2000.0;
		return (int) Math.round(100 * (grams / (rdv * dailyValueRatio)));
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
