package com.livestrong.myplate.back.api.models;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.api.ISO8601DateParser;
import com.livestrong.myplate.back.models.DiaryEntries;
import com.livestrong.myplate.back.models.DiaryEntry;
import com.livestrong.myplate.back.models.ExerciseDiaryEntry;
import com.livestrong.myplate.back.models.FoodDiaryEntry;
import com.livestrong.myplate.back.models.Meal;
import com.livestrong.myplate.back.models.MealItem;
import com.livestrong.myplate.back.models.WaterDiaryEntry;
import com.livestrong.myplate.back.models.WeightDiaryEntry;

public class SyncDiaryObject extends DiaryEntries implements LiveStrongApiObject {
	@SerializedName("SyncKey")
	private Date syncToken;

	@SerializedName("Meals")
	private List<Meal> meals;
	
	public SyncDiaryObject() {
		super();
		this.meals = new ArrayList<Meal>();
	}
	
	public void setSyncToken(String string) {
	    try {
			this.syncToken = ISO8601DateParser.parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	protected Collection<Integer> getAllFoodIds() {
		Collection<Integer> foodIds = new HashSet<Integer>();
		for (Meal meal : getMeals()) {
			for (MealItem item : meal.getItems()) {
				foodIds.add(item.getItemId());
			}
		}
		for (FoodDiaryEntry diaryEntry : getFoodEntries()) {
			foodIds.add(diaryEntry.getItemId());
		}
		return foodIds;
	}
	
	protected Collection<Integer> getAllExerciseIds() {
		Collection<Integer> exerciseIds = new HashSet<Integer>();
		for (ExerciseDiaryEntry diaryEntry : getExerciseEntries()) {
			exerciseIds.add(diaryEntry.getExerciseId());
		}
		return exerciseIds;
	}

	@SuppressWarnings("unchecked")
	public <D extends DiaryEntry> void saveAll() {
		Map<Class<D>, Collection<D>> allEntries = new HashMap<Class<D>, Collection<D>>();
		allEntries.put((Class<D>) ExerciseDiaryEntry.class, (Collection<D>) getExerciseEntries());
		allEntries.put((Class<D>) FoodDiaryEntry.class, (Collection<D>) getFoodEntries());
		allEntries.put((Class<D>) WaterDiaryEntry.class, (Collection<D>) getWaterEntries());
		allEntries.put((Class<D>) WeightDiaryEntry.class, (Collection<D>) getWeightEntries());

		// Fetch & cache Food objects
		DataHelper.getFoods(getAllFoodIds(), null);

		// Fetch & cache Exercise objects
		DataHelper.getExercises(getAllExerciseIds(), null);

		// Diary entries
		for (Class<D> clazz : allEntries.keySet()) {
			try {
				RuntimeExceptionDao<DiaryEntry, Integer> diaryDao = (RuntimeExceptionDao<DiaryEntry, Integer>) DataHelper.getDatabaseHelper().getDiaryDao(clazz);
				Collection<DiaryEntry> thoseEntries = (Collection<DiaryEntry>) allEntries.get(clazz);
				for (DiaryEntry diaryEntry : thoseEntries) {
					if (clazz.equals(FoodDiaryEntry.class)) {
						((FoodDiaryEntry) diaryEntry).getFood();
					} else if (clazz.equals(ExerciseDiaryEntry.class)) {
						((ExerciseDiaryEntry) diaryEntry).getExercise();
					}
					diaryDao.createOrUpdate(diaryEntry);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Meals
		for (Meal meal : getMeals()) {
			meal.save();
		}
	}

	public void loadDirtyEntries() {
		@SuppressWarnings("unchecked")
		Class<DiaryEntry>[] classes = new Class[] { ExerciseDiaryEntry.class, FoodDiaryEntry.class, WaterDiaryEntry.class, WeightDiaryEntry.class };
		
		for (Class<DiaryEntry> clazz : classes) {
			RuntimeExceptionDao<DiaryEntry, Integer> diaryDao = DataHelper.getDatabaseHelper().getDiaryDao(clazz);
			try {
				Collection<DiaryEntry> dirtyFoodEntries = diaryDao.queryBuilder()
						.where()
						.eq(DiaryEntry.DIRTY_FIELD_NAME, true)
						.query();
				for (DiaryEntry diaryEntry : dirtyFoodEntries) {
					diaryEntry.wasSynced();
					diaryDao.update(diaryEntry);
					addEntry(diaryEntry);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void removeDeletedEntries() {
		removeDeletedEntries(this.getExerciseEntries().iterator());
		removeDeletedEntries(this.getFoodEntries().iterator());
		removeDeletedEntries(this.getWaterEntries().iterator());
		removeDeletedEntries(this.getWeightEntries().iterator());
	}

	private <D extends DiaryEntry> void removeDeletedEntries(Iterator<D> it) {
		while (it.hasNext()) {
			DiaryEntry e = it.next();
			if (e.isDeleted()) {
				@SuppressWarnings("unchecked")
				Class<DiaryEntry> clazz = (Class<DiaryEntry>) e.getClass();
				RuntimeExceptionDao<DiaryEntry, Integer> diaryDao = DataHelper.getDatabaseHelper().getDiaryDao(clazz);
				diaryDao.update(e);

				it.remove();
			}
		}
	}
	
	public Date getSyncToken() {
		return this.syncToken;
	}

	public List<Meal> getMeals() {
		return this.meals;
	}
}
