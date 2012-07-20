package com.livestrong.myplate.back.models;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.models.FoodDiaryEntry.TimeOfDay;
import com.livestrong.myplate.utilities.SimpleDate;

public class DiaryEntries {
	public static enum DiaryEntryType {BREAKFAST, LUNCH, DINNER, SNACKS, EXERCISE, WATER};
	
	@SerializedName("Fitness")
	private List<ExerciseDiaryEntry> exerciseEntries;

	@SerializedName("Food")
	private List<FoodDiaryEntry> foodEntries;

	@SerializedName("Water")
	private List<WaterDiaryEntry> waterEntries;
	private transient Map<SimpleDate, WaterDiaryEntry> waterEntriesMap;

	@SerializedName("Weight")
	@SuppressWarnings("unused") // For de/serialization only
	private List<WeightDiaryEntry> weightEntries;
	private transient Map<SimpleDate, WeightDiaryEntry> weightEntriesMap;
	
	public DiaryEntries() {
		this.exerciseEntries = new ArrayList<ExerciseDiaryEntry>();
		this.foodEntries = new ArrayList<FoodDiaryEntry>();
		this.waterEntriesMap = new LinkedHashMap<SimpleDate, WaterDiaryEntry>();
		this.weightEntriesMap = new LinkedHashMap<SimpleDate, WeightDiaryEntry>();
	}

	public void loadEntriesForDates(Date from, Date to) {
		from = new SimpleDate(from);
		to = new SimpleDate(to);

		// SQLite BETWEEN clause includes test values, but we don't want to include the 'to', just the from. So let's subtract 1s to the to date:
		//Calendar toForSQL = Calendar.getInstance();
		//toForSQL.setTime(to);
		//toForSQL.add(Calendar.SECOND, -1);

		loadEntriesForExactDates(from, to);
	}

	public void loadEntriesForExactDates(Date from, Date to) {
		// This is because the between clause excludes the last date
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(to);
		calendar.add(Calendar.DATE, -1);
		to = calendar.getTime();

 		@SuppressWarnings("unchecked")
		Class<DiaryEntry>[] classes = new Class[] { ExerciseDiaryEntry.class, FoodDiaryEntry.class, WaterDiaryEntry.class, WeightDiaryEntry.class };
		
		for (Class<DiaryEntry> clazz : classes) {
			try {
				RuntimeExceptionDao<DiaryEntry, Integer> diaryDao = DataHelper.getDatabaseHelper().getDiaryDao(clazz);
				Collection<DiaryEntry> diaryEntries = diaryDao.queryBuilder()
						.where()
						.between(DiaryEntry.DATESTAMP_FIELD_NAME, from, to)
						.and()
						.eq(DiaryEntry.DELETED_FIELD_NAME, false)
						.query();
				for (DiaryEntry diaryEntry : diaryEntries) {
					addEntry(diaryEntry);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void loadEntriesForDay(Date date) {
		SimpleDate day = new SimpleDate(date);
		Calendar nextDay = Calendar.getInstance();
		nextDay.setTime(day);
		nextDay.add(Calendar.DATE, 1);
		loadEntriesForDates(day, nextDay.getTime());
	}
	
	/**
	 * Calculates the percentage of fat, carbs & protein of all the FoodDiaryEntries.
	 * @return Map<String, Double> 
	 */
	public Map<String, Double> getFatCarbsProtein() {
		Map<String, Double> response = new LinkedHashMap<String, Double>();
		
		double fat = 0;
		double carbs = 0;
		double protein = 0;
		for (FoodDiaryEntry diaryEntry : getFoodEntries()) {
			fat += diaryEntry.getFat();
			carbs += diaryEntry.getCarbs();
			protein += diaryEntry.getProtein();
		}
		fat = fat * Food.CALORIES_PER_FAT; // in calories
		carbs = carbs * Food.CALORIES_PER_CARBS; // in calories
		protein = protein * Food.CALORIES_PER_PROTEIN; // in calories
		
		double total = fat + carbs + protein;

		if (total == 0) {
			total = 1;
		}

		response.put("Fat", fat / total);
		response.put("Carbs", carbs / total);
		response.put("Protein", protein / total);
		
		return response;
	}
	
	public Map<DiaryEntryType, String> getDiarySummaryPerType() {
		Map<DiaryEntryType, String> summaryPerType = new HashMap<DiaryEntryType, String>();
		summaryPerType.put(DiaryEntryType.BREAKFAST, sumCalories(DiaryEntryType.BREAKFAST));
		summaryPerType.put(DiaryEntryType.LUNCH, sumCalories(DiaryEntryType.LUNCH));
		summaryPerType.put(DiaryEntryType.DINNER, sumCalories(DiaryEntryType.DINNER));
		summaryPerType.put(DiaryEntryType.SNACKS, sumCalories(DiaryEntryType.SNACKS));
		summaryPerType.put(DiaryEntryType.EXERCISE, sumCalories(DiaryEntryType.EXERCISE));
		summaryPerType.put(DiaryEntryType.WATER, sumCalories(DiaryEntryType.WATER));
		return summaryPerType;
	}
	
	private String sumCalories(DiaryEntryType entryType) {
		switch (entryType) {
			case BREAKFAST:
				return sumCalories(TimeOfDay.BREAKFAST) + "";
			case LUNCH:
				return sumCalories(TimeOfDay.LUNCH) + "";
			case DINNER:
				return sumCalories(TimeOfDay.DINNER) + "";
			case SNACKS:
				return sumCalories(TimeOfDay.SNACKS) + "";
			case EXERCISE:
				int totalCalories = 0;
				for (ExerciseDiaryEntry entry : getExerciseEntries()) {
					totalCalories += entry.getCals();
				}
				return totalCalories + "";
			case WATER:
				double totalOnces = 0;
				for (WaterDiaryEntry entry : getWaterEntries()) {
					totalOnces += entry.getOnces();
				}
				if (totalOnces == 0) {
					return "0";
				}
				return WaterDiaryEntry.getAmountWithUnits(totalOnces);
		}
		return null;
	}
	
	private int sumCalories(TimeOfDay timeOfDay) {
		int totalCalories = 0;
		List<FoodDiaryEntry> entries = getFoodEntriesPerTimeOfDay().get(timeOfDay);
		if (entries != null) {
			for (FoodDiaryEntry entry : entries) {
				totalCalories += entry.getCals();
			}
		}
		return totalCalories;
	}
	
	public int getNetCalories(Date date) {
		// getExerciseCalories() returns a negative number, so we add it here to decrease the net calories consumed
		return getCaloriesConsumed(date) + getCaloriesBurned(date);
	}

	public int getCaloriesConsumed(Date date) {
		SimpleDate day = new SimpleDate(date);
		int dailyCals = 0;
		for (FoodDiaryEntry diaryEntry : getFoodEntries()) {
			if (diaryEntry.getDatestamp().equals(day)) {
				dailyCals += diaryEntry.getCals();
			}
		}
		return dailyCals;
	}

	public int getCaloriesBurned(Date date) {
		SimpleDate day = new SimpleDate(date);
		int dailyCals = 0;
		for (ExerciseDiaryEntry diaryEntry : getExerciseEntries()) {
			if (diaryEntry.getDatestamp().equals(day)) {
				dailyCals += diaryEntry.getCals(); // getCals() return negative numbers
			}
		}
		return dailyCals;
	}
	
	public Map<SimpleDate, Double> getDailyWeight() {
		Map<SimpleDate, Double> dailySummary = new LinkedHashMap<SimpleDate, Double>();
		
		for (WeightDiaryEntry diaryEntry : getWeightEntries()) {
			dailySummary.put(diaryEntry.getDatestamp(), diaryEntry.getWeight());
		}
		return dailySummary;
	}
	
	public Map<SimpleDate, Double> getDailyWeightForSelectedUnits(){
		Map<SimpleDate, Double> dailySummary = new LinkedHashMap<SimpleDate, Double>();
		
		for (WeightDiaryEntry diaryEntry : getWeightEntries()) {
			dailySummary.put(diaryEntry.getDatestamp(), diaryEntry.getWeightForSelectedUnits());
		}
		return dailySummary;
	}

	public void addEntry(DiaryEntry entry) {
		if (entry instanceof ExerciseDiaryEntry) {
			this.exerciseEntries.add((ExerciseDiaryEntry) entry);
		}
		else if (entry instanceof FoodDiaryEntry) {
			this.foodEntries.add((FoodDiaryEntry) entry);
		}
		else if (entry instanceof WaterDiaryEntry) {
			this.waterEntriesMap.put(entry.getDatestamp(), (WaterDiaryEntry) entry);
		}
		else if (entry instanceof WeightDiaryEntry) {
			this.weightEntriesMap.put(entry.getDatestamp(), (WeightDiaryEntry) entry);
		}
	}
	
	public List<ExerciseDiaryEntry> getExerciseEntries() {
		return this.exerciseEntries;
	}

	public List<FoodDiaryEntry> getFoodEntries() {
		return this.foodEntries;
	}

	public Map<TimeOfDay, List<FoodDiaryEntry>> getFoodEntriesPerTimeOfDay() {
		Map<TimeOfDay, List<FoodDiaryEntry>> foodEntriesPerTimeOfDay = new HashMap<TimeOfDay, List<FoodDiaryEntry>>();
		
		// Initialize Map
		foodEntriesPerTimeOfDay.put(TimeOfDay.BREAKFAST, new ArrayList<FoodDiaryEntry>());
		foodEntriesPerTimeOfDay.put(TimeOfDay.LUNCH, new ArrayList<FoodDiaryEntry>());
		foodEntriesPerTimeOfDay.put(TimeOfDay.DINNER, new ArrayList<FoodDiaryEntry>());
		foodEntriesPerTimeOfDay.put(TimeOfDay.SNACKS, new ArrayList<FoodDiaryEntry>());		
		
		for (FoodDiaryEntry entry : this.foodEntries) {
			TimeOfDay timeOfDay = entry.getTimeOfDay();
			List<FoodDiaryEntry> entries = foodEntriesPerTimeOfDay.get(timeOfDay);
			entries.add(entry);
		}
		return foodEntriesPerTimeOfDay;
	}

	public Collection<WaterDiaryEntry> getWaterEntries() {
		if (this.waterEntriesMap != null) {
			return this.waterEntriesMap.values();
		}
		return null;
	}

	public WaterDiaryEntry getWaterEntry(Date date) {
		if (this.waterEntriesMap != null) {
			return this.waterEntriesMap.get(new SimpleDate(date));
		}
		return null;
	}

	public Collection<WeightDiaryEntry> getWeightEntries() {
		if (this.weightEntriesMap != null) {
			return this.weightEntriesMap.values();
		}
		return null;
	}

	public WeightDiaryEntry getWeightEntry(Date date) {
		if (this.weightEntriesMap != null) {
			return this.weightEntriesMap.get(new SimpleDate(date));
		}
		return null;
	}

	public boolean hasEntries(){
		if (this.foodEntries != null && this.foodEntries.size() > 0){
			return true;
		}
		if (this.exerciseEntries != null && this.exerciseEntries.size() > 0){
			return true;
		}
		if (this.waterEntries != null && this.waterEntries.size() > 0){
			return true;
		}
		return false;
	}
	
	// For deserialization (from API)
	public void setWaterEntries(List<WaterDiaryEntry> entries) {
		for (WaterDiaryEntry entry : entries) {
			this.waterEntriesMap.put(entry.getDatestamp(), entry);
		}
	}

	public void setWeightEntries(List<WeightDiaryEntry> entries) {
		for (WeightDiaryEntry entry : entries) {
			this.weightEntriesMap.put(entry.getDatestamp(), entry);
		}
	}

	// For serialization (to API)
	public Collection<WeightDiaryEntry> getSerializableWeightEntries() {
		return this.weightEntriesMap.values();
	}

	public Collection<WaterDiaryEntry> getSerializableWaterEntries() {
		return this.waterEntriesMap.values();
	}

	public Date getSerializableFromDate() {
		// Don't serialize this field, when it's sent to the API
		return null;
	}

	public Date getSerializableToDate() {
		// Don't serialize this field, when it's sent to the API
		return null;
	}
}
