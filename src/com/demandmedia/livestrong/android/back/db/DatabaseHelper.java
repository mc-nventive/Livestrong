package com.demandmedia.livestrong.android.back.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentProvider;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.demandmedia.livestrong.android.R;
import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.api.ApiHelper;
import com.demandmedia.livestrong.android.back.api.models.SyncDiaryObject;
import com.demandmedia.livestrong.android.back.models.ActivityLevels;
import com.demandmedia.livestrong.android.back.models.DiaryEntry;
import com.demandmedia.livestrong.android.back.models.Exercise;
import com.demandmedia.livestrong.android.back.models.ExerciseDiaryEntry;
import com.demandmedia.livestrong.android.back.models.Food;
import com.demandmedia.livestrong.android.back.models.FoodDiaryEntry;
import com.demandmedia.livestrong.android.back.models.Meal;
import com.demandmedia.livestrong.android.back.models.MealItem;
import com.demandmedia.livestrong.android.back.models.MealNutritionInfo;
import com.demandmedia.livestrong.android.back.models.UserProfile;
import com.demandmedia.livestrong.android.back.models.WaterDiaryEntry;
import com.demandmedia.livestrong.android.back.models.WeightDiaryEntry;
import com.demandmedia.livestrong.android.constants.BuildValues;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	public static final String DATABASE_NAME = "livestrong_myplate.db";
	private static final int DATABASE_VERSION = 6;

	private static Collection<Class<?>> databaseTables;

	static {
		databaseTables = new ArrayList<Class<?>>();
		databaseTables.add(ActivityLevels.class);
		databaseTables.add(Exercise.class);
		databaseTables.add(ExerciseDiaryEntry.class);
		databaseTables.add(Food.class);
		databaseTables.add(FoodDiaryEntry.class);
		databaseTables.add(Meal.class);
		databaseTables.add(MealItem.class);
		databaseTables.add(MealNutritionInfo.class);
		databaseTables.add(UserProfile.class);
		databaseTables.add(WaterDiaryEntry.class);
		databaseTables.add(WeightDiaryEntry.class);
	}

	public DatabaseHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			for (Class<?> clazz : databaseTables) {
				TableUtils.createTable(connectionSource, clazz);
			}

			// Remove the syncToken, in case it was there. The DB is empty now!
			DataHelper.setPref(DataHelper.PREFS_LAST_SYNC_TOKEN, null);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			if (BuildValues.IS_STAGING) {
				for (Class<?> clazz : databaseTables) {
					TableUtils.dropTable(connectionSource, clazz, true);
				}
				// after we drop the old databases, we create the new ones
				onCreate(db, connectionSource);
			}
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}
	
	public void emptyAllTables() {
		try {
			for (Class<?> clazz : databaseTables) {
				TableUtils.clearTable(this.connectionSource, clazz);
			}
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't clear databases", e);
			throw new RuntimeException(e);
		}
	}

	public RuntimeExceptionDao<Food, Integer> getFoodDao() {
		return getRuntimeExceptionDao(Food.class);
	}
	
	public RuntimeExceptionDao<Exercise, Integer> getExerciseDao() {
		return getRuntimeExceptionDao(Exercise.class);
	}
	
	public RuntimeExceptionDao<Meal, Integer> getMealDao() {
		return getRuntimeExceptionDao(Meal.class);
	}
	
	public RuntimeExceptionDao<MealNutritionInfo, Integer> getMealNutritionInfoDao() {
		return getRuntimeExceptionDao(MealNutritionInfo.class);
	}
	
	public RuntimeExceptionDao<MealItem, Integer> getMealItemDao() {
		return getRuntimeExceptionDao(MealItem.class);
	}
	
	public RuntimeExceptionDao<UserProfile, Integer> getUserProfileDao() {
		return getRuntimeExceptionDao(UserProfile.class);
	}
	
	public <D extends DiaryEntry> RuntimeExceptionDao<D, Integer> getDiaryDao(Class<D> clazz) {
		return getRuntimeExceptionDao(clazz);
	}
	
	public RuntimeExceptionDao<ActivityLevels, Integer> getActivityLevelsDao() {
		return getRuntimeExceptionDao(ActivityLevels.class);
	}
	
	@SuppressWarnings("unchecked")
	public static void persistData(Method methodCalled, Object data) {
		synchronized (DataHelper.getDatabaseHelper()) {
			if (methodCalled.equals(DataHelper.METHOD_GET_USER_PROFILE)) {
				if (data instanceof UserProfile) {
					ApiHelper.persistAuthData();
					DataHelper.getDatabaseHelper().getUserProfileDao().createOrUpdate((UserProfile) data);
				}
			}
			else if (methodCalled.equals(DataHelper.METHOD_GET_FOODS)) {
				if (data instanceof List) {
					RuntimeExceptionDao<Food, Integer> dao = DataHelper.getDatabaseHelper().getFoodDao();
					for (Food food : (List<Food>) data) {
						dao.createOrUpdate(food);
					}
				}
			}
			else if (methodCalled.equals(DataHelper.METHOD_GET_EXERCISES)) {
				if (data instanceof List) {
					RuntimeExceptionDao<Exercise, Integer> dao = DataHelper.getDatabaseHelper().getExerciseDao();
					for (Exercise exercise : (List<Exercise>) data) {
						dao.createOrUpdate(exercise);
					}
				}
			}
			else if (methodCalled.equals(DataHelper.METHOD_SYNC_DIARY)) {
				if (data instanceof SyncDiaryObject) {
					// Save remote changes in DB
					((SyncDiaryObject) data).saveAll();
				}
			}
			else if (methodCalled.equals(DataHelper.METHOD_GET_ACTIVITY_LEVELS)) {
				if (data instanceof ActivityLevels) {
					DataHelper.getDatabaseHelper().getActivityLevelsDao().createOrUpdate((ActivityLevels) data);
				}
			}
		}
		Log.i("DatabaseHelper", "persistData(" + methodCalled.getName() + ", ...) > Done");
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
	}

	/**
	 * Re-order a list of objects using their primary key (AKA id in ORMLite).
	 * @param List<?> unsortedList
	 * @param List<Integer> orderBy
	 * @param Method getId
	 * @return List<?> Ordered list.
	 */
	public static List<?> reorder(List<?> unsortedList, List<Integer> orderBy, Method getId) {
		Map<Integer, Object> objectsMap = new HashMap<Integer, Object>();
		if (unsortedList.size() == 0) {
			return unsortedList;
		}

		for (Object o : unsortedList) {
			try {
				Integer id = (Integer) getId.invoke(o);
				objectsMap.put(id, o);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		List<Object> sortedList = new ArrayList<Object>();
		for (Integer id : orderBy) {
			sortedList.add(objectsMap.get(id));
		}
		return sortedList;
	}
}
