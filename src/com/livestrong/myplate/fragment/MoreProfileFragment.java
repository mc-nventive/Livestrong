package com.livestrong.myplate.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.livestrong.myplate.R;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.DataHelper.DistanceUnits;
import com.livestrong.myplate.back.DataHelper.WeightUnits;
import com.livestrong.myplate.back.models.ActivityLevels;
import com.livestrong.myplate.back.models.UserProfile;
import com.livestrong.myplate.back.models.UserProfile.Gender;
import com.livestrong.myplate.back.models.UserProfile.GoalMode;

public class MoreProfileFragment extends FragmentDataHelperDelegate {
	
	private LinearLayout view;
	private Date birthDate;
	private Dialog dialog;
	private UserProfile userProfile;
	private EditText birthdayEditText, heightEditText, feetEditText, inchesEditText, weightEditText, calorieGoalEditText;
	private Spinner genderSpinner, activityLevelSpinner, weightGoalSpinner;
	private CheckBox automaticCalorieGoalCheckBox;
	private LinearLayout calorieGoalContainer;
	private ScrollView scrollView;
	private TextView weightUnitsTextView;
	private ActivityLevels activityLevels;
	private TreeMap<Double, String> weightGoalsMap;
	
	private class UserProfileTask extends AsyncTask<Void, Void, UserProfile>
	{
		
		@Override
		protected UserProfile doInBackground(Void... params) 
		{
			return DataHelper.getUserProfile(null);
		}
		
		protected void onPostExecute(UserProfile profile) 
		{
			if (profile != null) 
			{
				userProfile = profile;
				birthDate = userProfile.getDob();
			}
		};

	};
		
	public MoreProfileFragment(){
		new UserProfileTask().execute(new Void[]{});
	}
	
	public void createNewUserProfile(){
		this.userProfile = new UserProfile(true);
		if (userProfile != null){
			this.birthDate = userProfile.getDob();	
		}
		DataHelper.saveUserProfile(this.userProfile, null);
	}
	
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
		this.view 					= (LinearLayout) inflater.inflate(R.layout.fragment_more_profile, container, false);
		this.birthdayEditText 		= (EditText) this.view.findViewById(R.id.birthdayEditText);
		this.heightEditText 		= (EditText) this.view.findViewById(R.id.heightEditText);
		this.feetEditText 			= (EditText) this.view.findViewById(R.id.feetEditText);
		this.inchesEditText 		= (EditText) this.view.findViewById(R.id.inchesEditText);
		this.weightEditText 		= (EditText) this.view.findViewById(R.id.weightEditText);
		this.weightUnitsTextView 	= (TextView) this.view.findViewById(R.id.weightUnitsTextView);	
		this.calorieGoalEditText	= (EditText) this.view.findViewById(R.id.calorieGoalEditText);
		this.genderSpinner 			= (Spinner) this.view.findViewById(R.id.genderSpinner);
		this.activityLevelSpinner 	= (Spinner) this.view.findViewById(R.id.activityLevelSpinner);
		this.weightGoalSpinner 		= (Spinner) this.view.findViewById(R.id.weightGoalSpinner);
		this.calorieGoalContainer 	= (LinearLayout) this.view.findViewById(R.id.calorieGoalContainer);
		this.automaticCalorieGoalCheckBox = (CheckBox) this.view.findViewById(R.id.automaticCalorieGoalCheckBox);
		this.scrollView 			= (ScrollView) this.view.findViewById(R.id.scrollView);
				
		this.initializeBirthdayEditText();
		this.initalizeCheckBox();
		
		return this.view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d("MoreProfile", "key RESUME");
		new UserProfileTask().execute(new Void[]{});
		
		if (this.view != null){
			this.initializeFragmentBasedOnPreferences();
		}
		
		this.initializeEditTexts();
		this.initializeSpinners();
	}
	
	private void initializeFragmentBasedOnPreferences(){
		DistanceUnits distanceUnits = DistanceUnits.valueOf((String) DataHelper.getPref(DataHelper.PREFS_DISTANCE_UNITS, DistanceUnits.MILES));
		LinearLayout container;
		if (distanceUnits == DistanceUnits.METERS){
			container = (LinearLayout) this.view.findViewById(R.id.heightImperialContainer);
		} else {
			container = (LinearLayout) this.view.findViewById(R.id.heightMetricContainer);
		}
		
		container.getLayoutParams().height = 0;
		container.requestLayout();
		
		WeightUnits weightUnits = WeightUnits.valueOf((String) DataHelper.getPref(DataHelper.PREFS_WEIGHT_UNITS, WeightUnits.POUNDS));
		if (weightUnits == WeightUnits.KILOGRAMS){
			this.weightUnitsTextView.setText("kgs");
		} else if (weightUnits == WeightUnits.POUNDS){
			this.weightUnitsTextView.setText("lbs");
		} else {
			this.weightUnitsTextView.setText("st");
		}
	}
	
	private void initializeEditTexts(){
		if (this.userProfile != null){
			this.weightEditText.setText(this.userProfile.getWeightForSelectedUnits());
			this.heightEditText.setText(this.userProfile.getMetricHeight() + "");
			this.feetEditText.setText(this.userProfile.getFeet() + "");
			this.inchesEditText.setText(this.userProfile.getInches() + "");
			this.calorieGoalEditText.setText(this.userProfile.getCaloriesGoal() + "");
			
			this.refreshBirthdayEditText();
		}
	}
	
	private void initializeSpinners(){
		ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.gender, R.layout.spinner_item);
		genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.genderSpinner.setAdapter(genderAdapter);
		Gender gender = userProfile.getGender();
		if (gender == Gender.MALE){
			this.genderSpinner.setSelection(0);
		} else if (gender == Gender.FEMALE){
			this.genderSpinner.setSelection(1);
		}
		
		this.activityLevels = DataHelper.getActivityLevels(null);
		HashMap<Float, String> levels;
		if (this.activityLevels == null){
			levels = new HashMap<Float, String>();
		} else {
			levels = this.activityLevels.getLevels();
		}
		
		Set<Float>keys =  levels.keySet();
		ArrayList<Float> sortedKeys = new ArrayList<Float>(keys);
		Collections.sort(sortedKeys);
		
		CharSequence levelsStrings[] = new CharSequence[levels.values().size()];
		levelsStrings = levels.values().toArray(levelsStrings);			
		int i = 0;
		int selectedIndex = 0;
		for (Float key:sortedKeys){
			levelsStrings[i] = levels.get(key);
			// equals for double numbers with 3 decimals.....
			if (Math.round(this.userProfile.getActivityLevel()*1000) == Math.round(key*1000)) {
				selectedIndex = i;
			}
			i++;
		}
		
		ArrayAdapter<CharSequence> activityLevelAdapter = new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_item, levelsStrings);
		activityLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.activityLevelSpinner.setAdapter(activityLevelAdapter);
		this.activityLevelSpinner.setSelection(selectedIndex);
				
		
		this.weightGoalsMap = this.userProfile.getWeightGoals();
		Set<Double>weightKeys = weightGoalsMap.keySet();
		ArrayList<Double> sortedWeightKeys = new ArrayList<Double>(weightKeys);
		Collections.sort(sortedWeightKeys);

		String weightStrings[] = new String[weightGoalsMap.size()];
		weightStrings = weightGoalsMap.values().toArray(weightStrings);
		i = 0;
		selectedIndex = 0;
		for (Double key : sortedWeightKeys){
			weightStrings[i] = weightGoalsMap.get(key);
			if (this.userProfile.getGoal() == (double)key){
				selectedIndex = i;
			}
			i++;
		}
		
		//ArrayAdapter<CharSequence> weightGoalAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.weightGoal, R.layout.spinner_item);
		ArrayAdapter<CharSequence> weightGoalAdapter = new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_item, weightStrings);
		weightGoalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.weightGoalSpinner.setAdapter(weightGoalAdapter);
		this.weightGoalSpinner.setSelection(selectedIndex);
	}
	
	private void initializeBirthdayEditText(){
		
		birthdayEditText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (dialog == null){
					dialog = new Dialog(getActivity());
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog.setContentView(R.layout.dialog_birthday); 
					DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.birthdayDatePicker);
					datePicker.init(birthDate.getYear() + 1900, birthDate.getMonth(), birthDate.getDate(), null);
					
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
							DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.birthdayDatePicker);
							birthDate = new Date(datePicker.getYear() - 1900, datePicker.getMonth(), datePicker.getDayOfMonth());
								
							refreshBirthdayEditText();
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
	
	private void refreshBirthdayEditText(){
		SimpleDateFormat formatter = new SimpleDateFormat("d MMMM, yyyy");
		if (this.birthDate == null) {
			this.birthdayEditText.setText("");
		} else {
			this.birthdayEditText.setText(formatter.format(this.birthDate));
		}
	}
	
	private void initalizeCheckBox(){				
		if (this.userProfile != null && this.userProfile.getMode().equals(GoalMode.CALCULATED)) {
			this.automaticCalorieGoalCheckBox.setChecked(true);
			this.calorieGoalContainer.getLayoutParams().height = 0;
		} else {
			this.automaticCalorieGoalCheckBox.setChecked(false);
			this.calorieGoalContainer.getLayoutParams().height = LayoutParams.WRAP_CONTENT;			
		}
		this.automaticCalorieGoalCheckBox.requestLayout();
		this.calorieGoalContainer.requestLayout();
		
		this.automaticCalorieGoalCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					calorieGoalContainer.getLayoutParams().height = 0;
				} else {
					calorieGoalContainer.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
				}
				calorieGoalContainer.requestLayout();
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
	
	public void saveProfile(){
		if (this.userProfile == null || this.heightEditText == null || this.feetEditText == null ||
				this.inchesEditText == null || this.genderSpinner == null || this.weightEditText == null ||
				this.automaticCalorieGoalCheckBox == null || this.activityLevelSpinner == null || this.activityLevels == null ||
				this.weightGoalSpinner == null){
			return;
		}
		
		this.userProfile.setDob(this.birthDate);
		
		// Save height
		DistanceUnits distanceUnits = DistanceUnits.valueOf((String) DataHelper.getPref(DataHelper.PREFS_DISTANCE_UNITS, DistanceUnits.MILES));
		if (distanceUnits == DistanceUnits.METERS){
			String heightString = this.heightEditText.getText().toString();
			heightString = heightString.replace(",", ".");
			if (heightString.length() > 0){
				this.userProfile.editHeight(Double.parseDouble(heightString));	
			}		
		} else {
			String feetString = this.feetEditText.getText().toString();
			feetString = feetString.replace(",", ".");
			String inchesString = this.inchesEditText.getText().toString();
			inchesString = inchesString.replace(",", ".");
			
			if (feetString.length() == 0){
				feetString = "0";
			} 
			if (inchesString.length() == 0){
				inchesString = "0";
			}
			this.userProfile.editHeight(Double.parseDouble(feetString), Double.parseDouble(inchesString));
		}
				
		// Save gender
		if (this.genderSpinner.getSelectedItemPosition() == 0){
			this.userProfile.editGender(Gender.MALE);
		} else {
			this.userProfile.editGender(Gender.FEMALE);
		}
		
		// Save Weight
		String weightString = this.weightEditText.getText().toString();
		weightString = weightString.replace(",", ".");
		if (weightString.length() > 0){
			this.userProfile.editWeight(Double.parseDouble(weightString));
		}	
		
		// Save Automatic calorie goal settings	
		if (this.automaticCalorieGoalCheckBox.isChecked()){
			this.userProfile.editMode(GoalMode.CALCULATED);
		} else {
			this.userProfile.editMode(GoalMode.OVERRIDDEN);
			String goalStr = this.calorieGoalEditText.getText().toString();
			if (goalStr.length() > 0){
				this.userProfile.editCalories(Double.parseDouble(goalStr));
			}
		}	
		
		// Save ActivityLevel
		HashMap<Float, String> levels = this.activityLevels.getLevels();
		Set<Float>keys =  levels.keySet();
		for (Float key:keys){
			if (levels.get(key).equals(this.activityLevelSpinner.getSelectedItem())){
				this.userProfile.editActivityLevel((double) key);
				break;
			}
		}	
		
		// Save Weight Goal
		Set<Double>weightKeys =  this.weightGoalsMap.keySet();
		for (Double key:weightKeys){
			if (this.weightGoalsMap.get(key).equals(this.weightGoalSpinner.getSelectedItem())){
				this.userProfile.editGoal(key);
				break;
			}
		}			
		
		DataHelper.saveUserProfile(this.userProfile, null);
	}
		
	@Override
	public void onPause() {
		hideKeyboard();
		super.onPause();
	}
	
	private void hideKeyboard(){
		if (heightEditText == null){
			return;
		}
		// Hide Keyboard
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(heightEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(feetEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(inchesEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(weightEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(calorieGoalEditText.getWindowToken(), 0);
	}
}
