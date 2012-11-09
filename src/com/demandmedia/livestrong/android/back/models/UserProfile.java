package com.demandmedia.livestrong.android.back.models;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import android.database.Cursor;
import android.util.Log;

import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.DataHelper.DistanceUnits;
import com.demandmedia.livestrong.android.back.DataHelper.WeightUnits;
import com.demandmedia.livestrong.android.back.api.models.AbstractLiveStrongApiObject;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "UserProfile")
public class UserProfile extends AbstractLiveStrongApiObject {

	public final static double CM_PER_INCH = 2.54;
	
	@DatabaseField(id = true)
	private int userId;

	@DatabaseField
	private String username;
	
	@DatabaseField
	private Date dob;
	
	public enum Gender { UNKNOWN, MALE, FEMALE }
	@DatabaseField
	private Gender gender = Gender.UNKNOWN;
	
	@DatabaseField
	private double height;
	
	@DatabaseField
	private double weight;
	
	@DatabaseField
	private double activityLevel;
	
	@DatabaseField
	private double calories;
	
	@DatabaseField
	private double goal; // Weight lose goal
	
	public static double goals [] = new double[]{-2, -1.5, -1, 0, 1, 1.5, 2}; 
	
	public enum GoalMode { CALCULATED, OVERRIDDEN }
	@DatabaseField
	private GoalMode mode = GoalMode.CALCULATED;

	@DatabaseField
	private boolean dirty = false; 
	
	public UserProfile() {
        // ORMLite needs a no-args constructor 
    }

	// For deserialization (from API)

	public UserProfile(boolean createDefaultProfile){
		if (createDefaultProfile){
			this.gender = Gender.MALE;
			this.mode = GoalMode.CALCULATED;
			this.dob = new Date(1980-1900, 1, 1);
			this.height = 70.0;
			this.weight = 160.0;
		}
	}
	
	public UserProfile(Cursor cursor)
	{
		username = cursor.getString(cursor.getColumnIndex("username"));
		try {
			dob = new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(cursor.getColumnIndex("dob")));
		} catch (ParseException e) {
			Log.e("UserProfile", e.getMessage());
		}
		setGender(cursor.getString(cursor.getColumnIndex("gender")));
		height = cursor.getDouble(cursor.getColumnIndex("height"));
		weight = cursor.getDouble(cursor.getColumnIndex("weight"));
		activityLevel = cursor.getDouble(cursor.getColumnIndex("activityLevel"));
		calories = cursor.getDouble(cursor.getColumnIndex("calories"));
		goal = cursor.getDouble(cursor.getColumnIndex("goal"));
		mode = GoalMode.valueOf(cursor.getString(cursor.getColumnIndex("mode")));
		dirty = cursor.getShort(cursor.getColumnIndex("dirty")) > 0; 
	}
	
	public void setProfileDefaults(){
		this.height = 70.0;
		this.weight = 160.0;
		DataHelper.saveUserProfile(this, null);
	}
	
	public void setGender(String string) {
		this.gender = Gender.valueOf(string.toUpperCase());
	}
	
	public void setMode(String string) {
		this.mode = GoalMode.valueOf(string.toUpperCase());
	}
	
	public void setDob(Date dob){
		this.dob = dob;
	}
	
	// For serialization (to API)

	public String getSerializableGender() {
		return this.gender.toString().toLowerCase();
	}

	public String getSerializableMode() {
		return this.mode.toString().toLowerCase();
	}

	public String getSerializableDob() {
		return new SimpleDateFormat("yyyy-MM-dd").format(this.dob);
	}

	public Integer getSerializableUserId() {
		// Don't serialize this field, when it's sent to the API
		return null;
	}
	
	public String getSerializableUsername() {
		// Don't serialize this field, when it's sent to the API
		return null;
	}
	
	public Boolean getSerializableDirty() {
		// Don't serialize this field, when it's sent to the API
		return null;
	}

	public WeightDiaryEntry getSerializableWeightEntry() {
		// Don't serialize this field, when it's sent to the API
		return null;
	}

	// For local profile editing
	public void editDob(Date dob) {
		this.dob = dob;
		setDirty();
	}
	
	public void editWeight(double weight){
		this.weight = WeightDiaryEntry.convertWeightFromSelectedUnits(weight);
		setDirty();
		
		// Let's also create or update a diary entry for weight
		DiaryEntries entries = DataHelper.getDailyDiaryEntries(new Date(), null);
		WeightDiaryEntry e = entries.getWeightEntry(new Date());
		if (e == null) {
			e = new WeightDiaryEntry(weight, new Date());
		} else {
			e.setWeight(weight);
		}
        DataHelper.saveDiaryEntry(e, null);
	}
	
	public void  editHeight(double feet, double inches) {
		this.editHeight(feet * 12 + inches);
	}
	
	public void editHeight(double height) {
		DistanceUnits unitsPreference = DistanceUnits.valueOf((String) DataHelper.getPref(DataHelper.PREFS_DISTANCE_UNITS, DistanceUnits.METERS.toString()));
		
		switch (unitsPreference) {
			case METERS:
				this.height = height / CM_PER_INCH;
				break;
			case MILES:
			default:
				this.height = height;
				break;
		}
		
		setDirty();
	}

	public void editGender(Gender gender) {
		this.gender = gender;
		setDirty();
	}

	public void editActivityLevel(double activityLevel) {
		this.activityLevel = activityLevel;
		setDirty();
	}
	
	public void editGoal(double goal){
		this.goal = goal;
		setDirty();
	}

	public void editMode(GoalMode goalMode){
		this.mode = goalMode;
		setDirty();
	}

	public void editCalories(double calories) {
		this.calories = calories;
		setDirty();
	}

	public void editCaloriesMode(GoalMode mode) {
		this.mode = mode;
		setDirty();
	}

	private void setDirty() {
		this.dirty = true;
	}
	
	public void wasSynced() {
		this.dirty = false;
	}

	public int getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public Date getDob() {
		return dob;
	}

	public Gender getGender() {
		return gender;
	}

	public double getHeight() {
		return height;
	}
	
	public String getMetricHeight(){
		double height = getHeight() * CM_PER_INCH; 
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		return decimalFormat.format(height);
	}
	
	public int getFeet(){
		return (int)Math.floor(this.height/12);
	}
	
	public int getInches(){
		return (int)this.height - (12 * this.getFeet());
	}

	public double getActivityLevel() {
		return activityLevel;
	}

	public int getAge() {
		if (this.getDob() == null) {
			return 0;
		}
		Calendar today = Calendar.getInstance();
		Calendar dob = Calendar.getInstance();
		dob.setTime(this.getDob());
		int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
	    if (dob.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR) > 3 || dob.get(Calendar.MONTH) > today.get(Calendar.MONTH)) {
			// If birth date is greater than todays date (after 2 days adjustment of leap year) then decrement age one year   
	        age--;
	    } else if (dob.get(Calendar.MONTH) == today.get(Calendar.MONTH) && dob.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH)) {
		     // If birth date and todays date are of same month and birth day of month is greater than todays day of month then decrement age
	        age--;
	    }
		return age;
	}

	public int getCaloriesGoal(){
		return getCaloriesGoal(-1);
	}
	
	public int getCaloriesGoal(double weight) {
		if (getMode().equals(GoalMode.OVERRIDDEN)) {
			return (int) Math.round(this.calories);
		} else {
			double pounds;
			if (weight == -1){
				pounds = getWeight();
			} else {
				pounds = weight;
			}
			
			if (pounds <= 0) {
				return (int) Math.round(this.calories);
			}
			// "calculated", AKA automatic, based on weight, height and age (plus weight loss goal)
		    double kilos = pounds * WeightDiaryEntry.KG_PER_POUND;
		    int age = getAge();
		    
		    double goal = this.goal;
		    double activity = getActivityLevel();
		    double centimeters = getHeight() * CM_PER_INCH;
		    
		    double weight_factor = 0;
		    double height_factor = 0;
		    double age_factor= 0;
		    
		    // magic from the server
		    if (getGender().equals(Gender.MALE)) {
		        weight_factor = 66.5 + (13.75 * kilos);
		        height_factor = centimeters * 5.003;
		        age_factor = 6.775 * age;
		    } else {
		        weight_factor = 655.1 + (9.563 * kilos);
		        height_factor = centimeters * 1.85;
		        age_factor = 4.676 * age;
		    }
		    
		    double calories = (weight_factor + height_factor - age_factor) * activity;
		    double calories_to_lose_per_day = 3500 * goal / 7.0;
		    this.calories = calories - calories_to_lose_per_day;
		    
		    if (this.calories == 0) {
		    	this.calories = 2000;
		    } else if (this.calories < 800) {
		    	this.calories = 800;
		    } else if (this.calories > 9000) {
		    	this.calories = 9000;
		    }
		    
		    return (int) Math.round(this.calories);
		}
	}

	public GoalMode getMode() {
		return this.mode;
	}

	public boolean isDirty() {
		return dirty;
	}

	public String getWeightWithUnits() {
		return WeightDiaryEntry.getWeightWithUnits(getWeight());
	}
	
	public String getWeightForSelectedUnits(){
		double weight = WeightDiaryEntry.getWeightForSelectedUnits(getWeight());
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		return decimalFormat.format(weight);
	}
		
	public double getWeight() {
		RuntimeExceptionDao<WeightDiaryEntry, Integer> diaryDao = DataHelper.getDatabaseHelper().getDiaryDao(WeightDiaryEntry.class);

		List<WeightDiaryEntry> entries;
		try {
			entries = diaryDao.queryBuilder()
					.orderBy(DiaryEntry.DATESTAMP_FIELD_NAME, true)
					.where()
					.eq(DiaryEntry.DELETED_FIELD_NAME, false)
					.query();

			if (entries != null && entries.size() > 0) {
				WeightDiaryEntry weightEntry = (WeightDiaryEntry) entries.get(entries.size() - 1);
				this.weight = weightEntry.getWeight();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return this.weight;
	}
	
	public double getBmi() {
		return (getWeight() * 703) / Math.pow(getHeight(), 2);
	}

	public TreeMap<Double, String> getWeightGoals(){
		TreeMap<Double, String> weightGoals = new TreeMap<Double, String>();
		
		double factor = 1;
		String unitsString = "";
		WeightUnits weightUnits = WeightUnits.valueOf((String)DataHelper.getPref(DataHelper.PREFS_WEIGHT_UNITS,(String)WeightUnits.POUNDS.toString())); 
		switch (weightUnits) {
			case KILOGRAMS:
				factor = WeightDiaryEntry.KG_PER_POUND;
				unitsString = "kgs/week";
				break;
			case POUNDS:
				factor = 1;
				unitsString = "lbs/week";
				break;
			case STONES:
				factor = 1 / WeightDiaryEntry.POUND_PER_STONES;
				unitsString = "st/week";
				break;
		}
		
		for (double goal:goals){
			if (goal == 0){
				weightGoals.put(goal, "Maintain my weight");
			} else if (goal > 0){
				weightGoals.put(goal, String.format("Lose %.2f%s", goal * factor, unitsString));
			} else if (goal < 0){
				weightGoals.put(goal, String.format("Gain %.2f%s", -goal * factor, unitsString));
			}
		}
		
		return weightGoals;
	}
	
	public double getGoal() {
		return this.goal;
	}
}
