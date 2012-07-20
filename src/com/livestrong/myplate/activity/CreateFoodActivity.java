package com.livestrong.myplate.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.livestrong.myplate.MyPlateApplication;
import com.livestrong.myplate.R;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.models.Food;

public class CreateFoodActivity extends LiveStrongActivity {

	EditText nameEditText, servingSizeEditText, caloriesEditText, fatEditText, carbsEditText, proteinEditText;
	Button cancelButton, doneButton;
	Food customFood;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
			// UI elements states are restored automatically by super.onCreate()
		}

		// The activity is being created; create views, bind data to lists, etc.
		setContentView(R.layout.activity_create_food);

		// Hook up outlets
		this.nameEditText = (EditText) findViewById(R.id.nameEditText);
		this.servingSizeEditText = (EditText) findViewById(R.id.servingSizeEditText);
		this.caloriesEditText = (EditText) findViewById(R.id.caloriesEditText);
		this.fatEditText = (EditText) findViewById(R.id.fatEditText);
		this.carbsEditText = (EditText) findViewById(R.id.carbsEditText);
		this.proteinEditText = (EditText) findViewById(R.id.proteinEditText);
		this.cancelButton = (Button) findViewById(R.id.cancelButton);
		this.doneButton = (Button) findViewById(R.id.doneButton);

		// this.nameEditText.requestFocus();

		this.customFood = new Food(true);

		this.initializeEditTexts();
		this.initializeButtons();

	}

	public void initializeEditTexts() {
		this.nameEditText.setText(this.customFood.getTitle());
		this.servingSizeEditText.setText(this.customFood.getServingSize());
		this.caloriesEditText.setText(this.customFood.getCals() + "");
	}

	public void initializeButtons() {
		View.OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View button) {
				int btnId = button.getId();

				switch (btnId) {
				case R.id.cancelButton:
					hideKeyboard();
					finish();
					break;
				case R.id.doneButton:
					// Create Food & redirect to I Ate This activity
					if (!validateFields()){
						return;
					}
					
					String name = CreateFoodActivity.this.nameEditText.getText().toString();
					String servingSize = CreateFoodActivity.this.servingSizeEditText.getText().toString();
					
					int calories;
					if (CreateFoodActivity.this.caloriesEditText.getText().toString().equals("")){
						calories = 0;
					} else {
						calories = Integer.parseInt(CreateFoodActivity.this.caloriesEditText.getText().toString());
					}
					
					int fat;
					if (CreateFoodActivity.this.fatEditText.getText().toString().equals("")){
						fat = 0;
					} else {
						fat = Integer.parseInt(CreateFoodActivity.this.fatEditText.getText().toString());
					}
					
					int carbs;
					if (CreateFoodActivity.this.carbsEditText.getText().toString().equals("")){
						carbs = 0;
					} else {
						carbs = Integer.parseInt(CreateFoodActivity.this.carbsEditText.getText().toString());
					}
					
					int protein;
					if (CreateFoodActivity.this.proteinEditText.getText().toString().equals("")){
						protein = 0;
					} else {
						protein = Integer.parseInt(CreateFoodActivity.this.proteinEditText.getText().toString());
					}
					
					CreateFoodActivity.this.customFood = new Food(true, name, servingSize, calories, fat, carbs, protein);
					
					RuntimeExceptionDao<Food, Integer> dao = DataHelper.getDatabaseHelper().getFoodDao();
					dao.create(CreateFoodActivity.this.customFood);
					
		        	Intent intent = new Intent(CreateFoodActivity.this, AddFoodActivity.class);
		        	intent.putExtra(CreateFoodActivity.this.customFood.getClass().getName(), CreateFoodActivity.this.customFood);
					startActivityForResult(intent, 1);
					
					break;
				}
			}
		};
		this.cancelButton.setOnClickListener(onClickListener);
		this.doneButton.setOnClickListener(onClickListener);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Activity.RESULT_OK){
			String foodName = data.getExtras().getString(AddFoodActivity.INTENT_FOOD_NAME);
			
			Intent resultIntent = new Intent();
			resultIntent.putExtra(AddFoodActivity.INTENT_FOOD_NAME, foodName);
			
			setResult(Activity.RESULT_OK, resultIntent);
			
            finish();	
		}
	}

	private void hideKeyboard() {
		// Hide Keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.nameEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(this.servingSizeEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(this.caloriesEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(this.fatEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(this.carbsEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(this.proteinEditText.getWindowToken(), 0);
	}
	
	private boolean validateFields(){
		String errorMessage = "";
		if (this.nameEditText.getText().toString().equals("")){
			errorMessage += "A name must be entered.\n";			
		}
		if (this.servingSizeEditText.getText().toString().equals("")){
			errorMessage += "A serving size must be entered.\n";
		}
		if (this.caloriesEditText.getText().toString().equals("")){
			errorMessage += "The number of calories must be entered.\n";
		}
		
		if (!errorMessage.equals("")){
			new AlertDialog.Builder(MyPlateApplication.getFrontMostActivity())
		      .setMessage(errorMessage)
		      .setNeutralButton(android.R.string.ok,
		         new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int whichButton){}
		         })
		      .show();
			return false;
		}		
		return true;
	}
}
