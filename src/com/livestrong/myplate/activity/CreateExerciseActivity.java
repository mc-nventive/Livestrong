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
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.models.Exercise;
import com.livestrong.myplate.utilities.SessionMHelper;
import com.livestrong.myplatelite.R;

public class CreateExerciseActivity extends LiveStrongActivity {

	EditText nameEditText, caloriesEditText;
	Button cancelButton, doneButton;
	Exercise customExercise;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
			// UI elements states are restored automatically by super.onCreate()
		}

		// The activity is being created; create views, bind data to lists, etc.
		setContentView(R.layout.activity_create_exercise);

		// Hook up outlets
		this.nameEditText = (EditText) findViewById(R.id.nameEditText);
		this.caloriesEditText = (EditText) findViewById(R.id.caloriesEditText);
		this.cancelButton = (Button) findViewById(R.id.cancelButton);
		this.doneButton = (Button) findViewById(R.id.doneButton);

		this.customExercise = new Exercise(true);

		this.initializeEditTexts();
		this.initializeButtons();

	}

	public void initializeEditTexts() {
		this.nameEditText.setText(this.customExercise.getTitle());
		this.caloriesEditText.setText(Math.round(this.customExercise.getCalsPerHour()) + "");
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
					
					String name = CreateExerciseActivity.this.nameEditText.getText().toString();
										
					int calories;
					if (CreateExerciseActivity.this.caloriesEditText.getText().toString().equals("")){
						calories = 0;
					} else {
						calories = Integer.parseInt(CreateExerciseActivity.this.caloriesEditText.getText().toString());
					}
					
					CreateExerciseActivity.this.customExercise = new Exercise(true, name, calories);
					
					RuntimeExceptionDao<Exercise, Integer> dao = DataHelper.getDatabaseHelper().getExerciseDao();
					dao.create(CreateExerciseActivity.this.customExercise);
					
		        	Intent intent = new Intent(CreateExerciseActivity.this, AddExerciseActivity.class);
		        	intent.putExtra(CreateExerciseActivity.this.customExercise.getClass().getName(), CreateExerciseActivity.this.customExercise);
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
			String exerciseName = data.getExtras().getString(AddExerciseActivity.INTENT_EXERCISE_NAME);
			String sessionM = data.getExtras().getString(SessionMHelper.INTENT_SESSIONM);
			
			Intent resultIntent = new Intent();
			resultIntent.putExtra(AddExerciseActivity.INTENT_EXERCISE_NAME, exerciseName);
			
			if (sessionM != null)
				resultIntent.putExtra(SessionMHelper.INTENT_SESSIONM, sessionM);
			
			setResult(Activity.RESULT_OK, resultIntent);
			
            finish();
		}
	}
	
	private void hideKeyboard() {
		// Hide Keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.nameEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(this.caloriesEditText.getWindowToken(), 0);
	}
	
	private boolean validateFields(){
		String errorMessage = "";
		if (this.nameEditText.getText().toString().equals("")){
			errorMessage += "A name must be entered.\n";
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
