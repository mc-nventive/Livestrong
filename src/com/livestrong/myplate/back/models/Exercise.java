package com.livestrong.myplate.back.models;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.livestrong.myplate.Constants;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.api.models.AbstractLiveStrongApiObject;

@DatabaseTable(tableName = "Exercise")
public class Exercise extends AbstractLiveStrongApiObject implements LiveStrongDisplayableListItem, Serializable {
	private static final long serialVersionUID = -4405977108711504601L;
	
	public static final String ID_FIELD_NAME = "id";
	public static final String TITLE_FIELD_NAME = "exercise";
	public static final String EXERCISE_ID_FIELD_NAME = "exerciseId";
	public static final String CUSTOM_FIELD_NAME = "custom";

	public final static int CUSTOM_EXERCISE_ID = 87;
	
	static {
		units = new HashMap<String, String>() {
			private static final long serialVersionUID = 6775390878060734546L;
			{
			}
		};
	}

	@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
	private int id;

	@DatabaseField(columnName = EXERCISE_ID_FIELD_NAME)
	@SerializedName("fitness_id")
	private int exerciseId;

	@DatabaseField(columnName = TITLE_FIELD_NAME)
	private String exercise;

	@DatabaseField
	private String metricExercise;

	@DatabaseField
	private String description;

	@DatabaseField
	private double calFactor = 0;

	@DatabaseField
	private double distance = 0;

	@DatabaseField
	private double cph = 0;

	@DatabaseField
	private double active = 0;

	@DatabaseField
	private int calsPerHour = 0;

	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private HashMap<Integer, String> images;

	@DatabaseField(columnName = CUSTOM_FIELD_NAME)
	private boolean custom = false; // User saved this as a custom food

	public Exercise() {
	}

	public Exercise(boolean isCustom) {
		this.custom = isCustom;
		this.exerciseId = CUSTOM_EXERCISE_ID;
		this.calFactor = 0.0;
		if (isCustom) {
			this.exercise = "My Custom Exercise " + getNextAvailableCustomId();
		} else {
			this.exercise = "My Manual Exercise " + getNextAvailableManualId();
		}
	} 
	
	public Exercise(Boolean isCustom, String name, int calsPerHour){
		this.custom = isCustom;
		this.exercise = name;
		this.exerciseId = CUSTOM_EXERCISE_ID;
		this.calsPerHour = calsPerHour;
	}
	
	public String getTitle() {
		return getExercise();
	}

	public String getDescription() {
		return getCalsPerHourWithUnits();
	}
	
	public int getId() {
		return this.id;
	}
	
	private long getNextAvailableCustomId() {
		long numCustomFoods = 0;
		try {
			RuntimeExceptionDao<Exercise, Integer>dao = DataHelper.getDatabaseHelper().getExerciseDao();
			numCustomFoods = dao.countOf(dao.queryBuilder()
					.setCountOf(true)
					.where()
					.eq(EXERCISE_ID_FIELD_NAME, CUSTOM_EXERCISE_ID)
					.and()
					.eq(CUSTOM_FIELD_NAME, true)
					.prepare()
			);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return numCustomFoods + 1;
	}

	private long getNextAvailableManualId() {
		long numCustomFoods = 0;
		try {
			RuntimeExceptionDao<Exercise, Integer>dao = DataHelper.getDatabaseHelper().getExerciseDao();
			numCustomFoods = dao.countOf(dao.queryBuilder()
					.setCountOf(true)
					.where()
					.eq(EXERCISE_ID_FIELD_NAME, CUSTOM_EXERCISE_ID)
					.and()
					.eq(CUSTOM_FIELD_NAME, false)
					.prepare()
			);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return numCustomFoods + 1;
	}
	
	public int getExerciseId() {
		return exerciseId;
	}

	public String getExercise() {
		return exercise;
	}

	public String getMetricExercise() {
		return metricExercise;
	}

	public String getExerciseDescription() {
		return description;
	}

	public double getCalFactor() {
		return calFactor;
	}

	public double getDistance() {
		return distance;
	}

	public double getCph() {
		return cph;
	}

	public double getActive() {
		return active;
	}

	public boolean isCustom() {
		return custom;
	}
	
	public boolean isManual() {
		return this.exerciseId == CUSTOM_EXERCISE_ID;
	}
	
	public boolean isGeneric() {
		return isManual() && !isCustom() && getCalsPerHour() == 0;
	}

	public HashMap<Integer, String> getImages() {
		return images;
	}

	public String getSmallImage() {
		if (images != null) {
			return images.get(Constants.FOOD_IMAGE_SMALL);
		} else {
			return "";
		}
	}

	public String getCalsPerHourWithUnits() {
	    return Math.round(getCalsPerHour()) + " calories per hour";
	}

	public double getCalsPerHour() {
		UserProfile userProfile = DataHelper.getUserProfile(null);
		if (userProfile == null){
			return this.calsPerHour;
		}
		
		double userWeight = userProfile.getWeight();
		if (isCustom() || userWeight <= 0) {
			return this.calsPerHour;
		}
		double caloriesBurnedPerSecond = getCalsBurnedPerSecond(userWeight);
		double caloriesBurnedPerHour = caloriesBurnedPerSecond * 60.0 * 60.0;
		this.calsPerHour = (int) Math.round(caloriesBurnedPerHour);
		return caloriesBurnedPerHour;
	}

	private double getCalsBurnedPerSecond(double userWeight) {
		// calculate calories burned, this is kinda complicated due to some oddities in the data
		double caloriesBurnedPerSecond = 0;
		if (this.exerciseId > 2500 && this.exerciseId < 5000) {
			// for these records, the weight needs to be in KG and the duration in seconds
			caloriesBurnedPerSecond = (this.calFactor / 60.0 / 60.0) * (userWeight * WeightDiaryEntry.KG_PER_POUND);
		} else {
			// everything else uses weight in pounds and duration in minutes
			caloriesBurnedPerSecond = (this.calFactor / 60.0) * userWeight;
		}
		return caloriesBurnedPerSecond;
	}

	// For serialization (to API)
	public Integer getSerializableId() {
		// Don't serialize this field, when it's sent to the API
		return null;
	}
	
	public Boolean getSerializableCustom() {
		// Don't serialize this field, when it's sent to the API
		return null;
	}
}
