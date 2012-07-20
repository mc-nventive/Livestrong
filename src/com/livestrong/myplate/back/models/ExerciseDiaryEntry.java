package com.livestrong.myplate.back.models;

import java.util.Date;
import java.util.LinkedHashMap;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.livestrong.myplate.back.DataHelper;

@DatabaseTable(tableName = "Diary_Exercise")
public class ExerciseDiaryEntry extends DiaryEntry {
	private static final long serialVersionUID = -8845100600494457508L;

	public final static LinkedHashMap<String, Integer> hoursPickerValues = new LinkedHashMap<String, Integer>() {
		private static final long serialVersionUID = 4305750926349218536L;
		{
			for (int i=0; i<=23; i++) {
	            put(i+"", i);
			}
        };
	};

	public final static LinkedHashMap<String, Integer> minutesPickerValues = new LinkedHashMap<String, Integer>() {
		private static final long serialVersionUID = 8824599242790051736L;
		{
			for (int i=0; i<=59; i += 5) {
	            put(i+"", i);
			}
        };
	};

	public final static String EXERCISE_ID_FIELD_NAME = "exerciseId";
	
	@DatabaseField(columnName = EXERCISE_ID_FIELD_NAME)
	@SerializedName("fitness_id")
	private int exerciseId;
	
	@DatabaseField
	private String label;
	
	@DatabaseField
	private double minutes;
	
	@DatabaseField
	private int caloriesBurned = 0;

	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private Exercise exercise;

	public ExerciseDiaryEntry() {}
	
	public ExerciseDiaryEntry(Exercise exercise, double minutes, Date datestamp) {
		super(datestamp, exercise);
		this.exercise = exercise;
		this.exerciseId = exercise.getExerciseId();
		this.minutes = minutes;
		getCals(); // will set this.caloriesBurned
	}
	
	public String getTitle() {
		if (label != null) {
			return this.label;
		}
		return getExercise().getExercise();
	}

	public String getDescription() {
		int hours = (int) Math.floor(this.minutes / 60);
		int minutes = (int) this.minutes - hours * 60;
		return (hours > 1 ? hours + " hours " : (hours == 1 ? "1 hour " : "")) + minutes + " minutes, " + getCals() + " calories";
	}

	public void setMinutes(String string) {
		this.minutes = Float.parseFloat(string);
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
		wasModified();
	}

	public void setCaloriesBurned(int caloriesBurned) {
		this.caloriesBurned = caloriesBurned;
		wasModified();
	}
	
	public Exercise getExercise() {
		if (this.exercise == null) {
			this.exercise = DataHelper.getExercise(this.exerciseId, null);
		}
		return this.exercise;
	}

	public int getExerciseId() {
		return exerciseId;
	}

	public double getMinutes() {
		return minutes;
	}

	public int getCals() {
		if (this.caloriesBurned != 0) {
			return -1 * this.caloriesBurned;
		}
		return -1 * (int) Math.round(getExercise().getCalsPerHour() * this.minutes / 60.0);
	}

	@Override
	public String getSmallImage() {
		return this.exercise.getSmallImage();
	}
	
	// For serialization (to API)
	public int getSerializableCaloriesBurned() {
		return -1 * getCals();
	}

	public Exercise getSerializableExercise() {
		// Don't serialize this field, when it's sent to the API
		return null;
	}
	
	public String getSerializableLabel() {
		if (getExercise().isManual()) {
			return getExercise().getTitle();
		}
		return null;
	}
}
