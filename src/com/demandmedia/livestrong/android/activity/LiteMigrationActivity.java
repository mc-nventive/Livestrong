package com.demandmedia.livestrong.android.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.db.DatabaseHelper;
import com.demandmedia.livestrong.android.back.models.ActivityLevels;
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
import com.demandmedia.livestrong.android.utilities.Utils;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.demandmedia.livestrong.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderClient;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LiteMigrationActivity extends Activity
{
	private enum MigrationTaskResultCode 
	{
		FAILED,
		NOTSUPPORTED,
		SUCCEEDED,
	}
	
	private final class MigrationTask extends AsyncTask<Void, String, MigrationTaskResultCode> 
	{
		private ProgressBar _migrationProgressBar = (ProgressBar)LiteMigrationActivity.this.findViewById(R.id.migrationProgressBar);
		private TextView _migrationText = (TextView)LiteMigrationActivity.this.findViewById(R.id.migrationProgressText);
		private Button _nextRetryButton = (Button)LiteMigrationActivity.this.findViewById(R.id.nextButton);
		
		protected void onPreExecute() 
		{
			LiteMigrationActivity.this.findViewById(R.id.migrationYesNoLayout).setVisibility(View.GONE);
			_migrationProgressBar.setVisibility(View.VISIBLE);
			_migrationText.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected void onProgressUpdate(String... values) 
		{
			super.onProgressUpdate(values);
			
			if(null != values)	_migrationText.setText(values[0]);
		}
		
		@Override
		protected MigrationTaskResultCode doInBackground(Void... params) 
		{
			MigrationTaskResultCode resultCode = MigrationTaskResultCode.FAILED;

			String contentProviderAuthority = "content://com.livestrong.myplate.myplatedataprovider";
			
			ContentProviderClient contentProviderClient = getContentResolver().acquireContentProviderClient(Uri.parse(contentProviderAuthority));
			DatabaseHelper databaseHelper = DataHelper.getDatabaseHelper();
			
			try 
			{
				if (null != contentProviderClient && null != databaseHelper) 
				{
					databaseHelper.emptyAllTables();
					
					migrateActivityLevels(contentProviderAuthority, contentProviderClient, databaseHelper);
					publishProgress(new String[] { "Successfully migrated activity levels!" });
					
					migrateExercisesEntries(contentProviderAuthority,contentProviderClient, databaseHelper);
					publishProgress(new String[] { "Successfully migrated exercise entries and diary!" });
					
					migrateFoodEntries(contentProviderAuthority, contentProviderClient, databaseHelper);
					publishProgress(new String[] { "Successfully migrated food entries and diary!" });
					
					migrateWeightEntries(contentProviderAuthority, contentProviderClient, databaseHelper);
					publishProgress(new String[] { "Successfully migrated weight diary!" });
					
					migrateWaterEntries(contentProviderAuthority, contentProviderClient, databaseHelper);
					publishProgress(new String[] { "Successfully migrated water diary!" });
					
					migrateUserProfile(contentProviderAuthority, contentProviderClient, databaseHelper);
					publishProgress(new String[] { "Successfully migrated user profile!" });
					
					migratePreferences(contentProviderAuthority, contentProviderClient);
					publishProgress(new String[] { "Successfully migrated user preferences!" });
					
					resultCode = MigrationTaskResultCode.SUCCEEDED;
				}
				else if (null == contentProviderClient)
				{
					resultCode = MigrationTaskResultCode.NOTSUPPORTED;
				}
			}
			catch(Exception e)
			{
				Log.e("LiteMigrationActivity", String.format("Lite Data migration failed %1$s : %2$s", e.toString(), e.getMessage()));
			}
			finally
			{
				if(null != contentProviderClient)	contentProviderClient.release();
			}
			
			return resultCode;
		}
		
		private void migrateWaterEntries(String contentProviderAuthority, ContentProviderClient contentProviderClient, DatabaseHelper databaseHelper) throws RemoteException 
		{
			Cursor waterCursor = contentProviderClient.query(Uri.parse(String.format("%1$s/%2$s", contentProviderAuthority, "diary_water")), null, null, null, null);
			if(null != waterCursor)
			{
				RuntimeExceptionDao<WaterDiaryEntry, Integer> waterDiaryEntries = databaseHelper.getDiaryDao(WaterDiaryEntry.class);
				while(waterCursor.moveToNext())
				{
					waterDiaryEntries.create(new WaterDiaryEntry(waterCursor));
				}
				waterCursor.close();
				
				Log.d("LiteMigrationActivity", "Successfully migrated Water diary");
			}
		}

		private void migrateWeightEntries(String contentProviderAuthority, ContentProviderClient contentProviderClient, DatabaseHelper databaseHelper) throws RemoteException 
		{
			Cursor weightCursor = contentProviderClient.query(Uri.parse(String.format("%1$s/%2$s", contentProviderAuthority, "diary_weight")), null, null, null, null);
			if(null != weightCursor)
			{
				RuntimeExceptionDao<WeightDiaryEntry, Integer> weightDiaryEntries = databaseHelper.getDiaryDao(WeightDiaryEntry.class);
				while(weightCursor.moveToNext())
				{
					weightDiaryEntries.create(new WeightDiaryEntry(weightCursor));
				}
				weightCursor.close();
				
				Log.d("LiteMigrationActivity", "Successfully migrated Weight diary");
			}
		}

		private void migratePreferences(String contentProviderAuthority,ContentProviderClient contentProviderClient) throws RemoteException 
		{
			Cursor userPreferencesCursor = contentProviderClient.query(Uri.parse(String.format("%1$s/%2$s", contentProviderAuthority, "userpreferences")), null, null, null, null);
			if(null != userPreferencesCursor && userPreferencesCursor.moveToNext())
			{
				int columnIndex = 0;
				if((columnIndex = userPreferencesCursor.getColumnIndex(DataHelper.PREFS_WEIGHT_UNITS)) >= 0)
				{
					DataHelper.setPref(userPreferencesCursor.getColumnName(columnIndex), userPreferencesCursor.getString(columnIndex));
				}
				if((columnIndex = userPreferencesCursor.getColumnIndex(DataHelper.PREFS_WATER_UNITS)) >= 0)
				{
					DataHelper.setPref(userPreferencesCursor.getColumnName(columnIndex), userPreferencesCursor.getString(columnIndex));
				}
				if((columnIndex = userPreferencesCursor.getColumnIndex(DataHelper.PREFS_DISTANCE_UNITS)) >= 0)
				{
					DataHelper.setPref(userPreferencesCursor.getColumnName(columnIndex), userPreferencesCursor.getString(columnIndex));
				}
				if((columnIndex = userPreferencesCursor.getColumnIndex(DataHelper.PREFS_DAILY_REMINDER)) >= 0)
				{
					DataHelper.setPref(userPreferencesCursor.getColumnName(columnIndex), Boolean.parseBoolean(userPreferencesCursor.getString(columnIndex)));
				}
				if((columnIndex = userPreferencesCursor.getColumnIndex(DataHelper.PREFS_DAILY_REMINDER_TIME)) >= 0)
				{
					DataHelper.setPref(userPreferencesCursor.getColumnName(columnIndex), userPreferencesCursor.getLong(columnIndex));
				}
				userPreferencesCursor.close();
				
				Log.d("LiteMigrationActivity", "Successfully migrated User Preferences");
			}
		}

		private void migrateActivityLevels(String contentProviderAuthority, ContentProviderClient contentProviderClient, DatabaseHelper databaseHelper) throws RemoteException 
		{
			Cursor activityLevelCursor = contentProviderClient.query(Uri.parse(String.format("%1$s/%2$s", contentProviderAuthority, "activity_levels")), null, null, null, null);
			if (null != activityLevelCursor) 
			{
				//Only one table of activity levels
				if (activityLevelCursor.moveToNext()) 
				{
					databaseHelper.getActivityLevelsDao().create(new ActivityLevels(activityLevelCursor));
				}
				activityLevelCursor.close();
				
				Log.d("LiteMigrationActivity", "Successfully migrated Activity Levels");
			}
		}

		private void migrateUserProfile(String contentProviderAuthority, ContentProviderClient contentProviderClient, DatabaseHelper databaseHelper) throws RemoteException 
		{
			Cursor userProfileCursor = contentProviderClient.query(Uri.parse(String.format("%1$s/%2$s", contentProviderAuthority, "userprofile")), null, null, null, null);
			if (null != userProfileCursor) 
			{
				//Only one user profile
				if (userProfileCursor.moveToNext()) 
				{
					databaseHelper.getUserProfileDao().create(new UserProfile(userProfileCursor));
				}
				userProfileCursor.close();
				
				Log.d("LiteMigrationActivity", "Successfully migrated User Profile");
			}
		}

		private void migrateFoodEntries(String contentProviderAuthority, ContentProviderClient contentProviderClient, DatabaseHelper databaseHelper) throws RemoteException 
		{
			Map<Integer, Food> foodItems = migrateFoodItems(contentProviderAuthority, contentProviderClient, databaseHelper);
			Map<Integer, MealNutritionInfo> nutritionInfoMap = migrateMealNutritionInfo(contentProviderAuthority, contentProviderClient, databaseHelper);
			
			Cursor mealsCursor = contentProviderClient.query(Uri.parse(String.format("%1$s/%2$s", contentProviderAuthority, "meal")), null, null, null, null);
			Cursor mealItemsCursor = contentProviderClient.query(Uri.parse(String.format("%1$s/%2$s", contentProviderAuthority, "mealitem")), null, null, null, null);
			Map<Integer, Meal> mealsList = new TreeMap<Integer, Meal>();
			
			RuntimeExceptionDao<Meal, Integer> mealEntries = databaseHelper.getMealDao();
			RuntimeExceptionDao<MealItem, Integer> mealItemEntries = databaseHelper.getMealItemDao();
			
			if(null != mealsCursor && null != mealItemsCursor)
			{
				int mealItemIdColumnIndex = mealItemsCursor.getColumnIndex("id");
				int foodItemIdColumndIndexInMealItemsTable = mealItemsCursor.getColumnIndex("food_id");
				int nutrionInfoColumnIndexInMealTable = mealsCursor.getColumnIndex("nutritioninfo_id");
				while(mealsCursor.moveToNext())
				{
					Collection<MealItem> currentMealMealItems = new ArrayList<MealItem>();
					Meal meal = new Meal(mealsCursor, null, nutritionInfoMap.get(mealsCursor.getInt(nutrionInfoColumnIndexInMealTable)));
					while(mealItemsCursor.moveToNext())
					{
						if(mealItemsCursor.getInt(mealItemIdColumnIndex) == meal.getMealId())
						{
							MealItem mealItem = new MealItem(mealItemsCursor, meal, foodItems.get(mealItemsCursor.getInt(foodItemIdColumndIndexInMealItemsTable)));
							currentMealMealItems.add(mealItem);
							mealItemEntries.create(mealItem);
						}
					}
					meal.setItems(currentMealMealItems);
					mealEntries.create(meal);
					mealsList.put(meal.getMealId(), meal);
				}
				mealsCursor.close();
				mealItemsCursor.close();
				
				Log.d("LiteMigrationActivity", "Successfully migrated Meals");
			}
			
			migrateFoodDiaries(contentProviderAuthority, contentProviderClient, databaseHelper, foodItems);
		}

		private void migrateFoodDiaries(String contentProviderAuthority, ContentProviderClient contentProviderClient, DatabaseHelper databaseHelper, Map<Integer, Food> foodItems) 
				throws RemoteException 
		{
			Cursor foodDiaryCursor = contentProviderClient.query(Uri.parse(String.format("%1$s/%2$s", contentProviderAuthority, "diary_food")), null, null, null, null);
			if(null != foodDiaryCursor)
			{
				RuntimeExceptionDao<FoodDiaryEntry, Integer> foodDiaryEntries = databaseHelper.getDiaryDao(FoodDiaryEntry.class);
				int foodIdColumnIndexInFoodDiaryTable = foodDiaryCursor.getColumnIndex("food_id");
				while(foodDiaryCursor.moveToNext())
				{
					foodDiaryEntries.create(new FoodDiaryEntry(foodDiaryCursor, foodItems.get(foodDiaryCursor.getInt(foodIdColumnIndexInFoodDiaryTable))));
				}
				foodDiaryCursor.close();
				Log.d("LiteMigrationActivity", "Successfully migrated Food diary entries");
			}
		}

		private Map<Integer, MealNutritionInfo> migrateMealNutritionInfo(String contentProviderAuthority, ContentProviderClient contentProviderClient, DatabaseHelper databaseHelper) throws RemoteException 
		{
			RuntimeExceptionDao<MealNutritionInfo, Integer> nutritionInfoEntries = databaseHelper.getMealNutritionInfoDao();
			Cursor nutritionInfoCursor = contentProviderClient.query(Uri.parse(String.format("%1$s/%2s", contentProviderAuthority, "mealnutritioninfo")), null, null, null, null);
			int nutritionInfoColumnIndex = nutritionInfoCursor.getColumnIndex("id");
			Map<Integer, MealNutritionInfo> nutritionInfoMap = new TreeMap<Integer, MealNutritionInfo>();
			while(nutritionInfoCursor.moveToNext())
			{
				MealNutritionInfo nutritionInfo = new MealNutritionInfo(nutritionInfoCursor);
				nutritionInfoMap.put(nutritionInfoCursor.getInt(nutritionInfoColumnIndex), nutritionInfo);
				nutritionInfoEntries.create(nutritionInfo);
			}
			nutritionInfoCursor.close();
			
			return nutritionInfoMap;
		}

		private Map<Integer, Food> migrateFoodItems(String contentProviderAuthority, ContentProviderClient contentProviderClient, DatabaseHelper databaseHelper) throws RemoteException 
		{
			Cursor foodCursor = contentProviderClient.query(Uri.parse(String.format("%1$s/%2$s", contentProviderAuthority, "food")), null, null, null, null);
			Map<Integer, Food> foodItems = new TreeMap<Integer, Food>();
			if(null != foodCursor)
			{
				int foodIdColumnIndex = foodCursor.getColumnIndex("id");
				RuntimeExceptionDao<Food, Integer> foodEntries = databaseHelper.getFoodDao();
				while (foodCursor.moveToNext())
				{
					Food food = new Food(foodCursor);
					foodItems.put(foodCursor.getInt(foodIdColumnIndex), new Food(foodCursor));
					foodEntries.create(food);
				}
				foodCursor.close();
			}
			return foodItems;
		}

		private void migrateExercisesEntries(String contentProviderAuthority, ContentProviderClient contentProviderClient, DatabaseHelper databaseHelper) throws RemoteException 
		{
			Cursor exercisesCursor = contentProviderClient.query(Uri.parse(String.format("%1$s/%2$s", contentProviderAuthority, "exercise")), null, null, null, null);
			Cursor exerciseDiaryEntriesCursor = getContentResolver().query(Uri.parse(String.format("%1$s/%2$s", contentProviderAuthority, "diary_exercise")), null, null, null, null);
			
			if(null != exercisesCursor && null != exerciseDiaryEntriesCursor)
			{
				Map<Integer, Exercise> exercises = new TreeMap<Integer, Exercise>();
				int idColumnIndex = exercisesCursor.getColumnIndex("id");
				RuntimeExceptionDao<Exercise, Integer> exercisesDao = databaseHelper.getExerciseDao();
				while(exercisesCursor.moveToNext())
				{
					Exercise exercise = new Exercise(exercisesCursor);
					exercises.put(exercisesCursor.getInt(idColumnIndex), exercise);
					exercisesDao.createOrUpdate(exercise);
				}
				exercisesCursor.close();
				
				int exerciseIdColumnIndex = exerciseDiaryEntriesCursor.getColumnIndex("exercise_id");
				RuntimeExceptionDao<ExerciseDiaryEntry, Integer> exerciseDiaryEntriesDao = 
						(RuntimeExceptionDao<ExerciseDiaryEntry, Integer>) databaseHelper.getDiaryDao(ExerciseDiaryEntry.class);
				while(exerciseDiaryEntriesCursor.moveToNext())
				{
					exerciseDiaryEntriesDao.create(new ExerciseDiaryEntry(exerciseDiaryEntriesCursor
						, exercises.get(new Integer(exerciseDiaryEntriesCursor.getInt(exerciseIdColumnIndex)))));
					
				}
				exerciseDiaryEntriesCursor.close();
				
				Log.d("LiteMigrationActivity", "Successfully migrated Exercises");
			}
		}

		protected void onPostExecute(MigrationTaskResultCode result) 
		{
			_migrationProgressBar.setVisibility(View.GONE);
			
			switch(result) 
			{
			case SUCCEEDED:
				_migrationText.setText("Data migration succeeded!");
				_nextRetryButton.setVisibility(View.VISIBLE);	
				_nextRetryButton.setText("Done");
				_nextRetryButton.setOnClickListener(new OnClickListener() 
				{
					@Override
					public void onClick(View v) 
					{
						LiteMigrationActivity.this.SkipMigration();
					}
				});
				
				DataHelper.setPref(EntryPageActivity.SHOULD_PROMPT_FOR_MIGRATION, false);
				break;
			case FAILED:
				_migrationText.setText("Data migration failed!");
				_nextRetryButton.setText("Retry");
				_nextRetryButton.setVisibility(View.VISIBLE);
				_nextRetryButton.setOnClickListener(new OnClickListener() 
				{
					public void onClick(View v) 
					{
						LiteMigrationActivity.this.PerformMigration();
					}
				});
				break;
			case NOTSUPPORTED:
				default:
					_migrationText.setText("MyPlate Data migration is supported only with Calorie Tracker Lite version 1.4 and above. Please upgrade your Calorie Tracker Lite and try again.");
					_nextRetryButton.setText("Open Google Play");
					_nextRetryButton.setVisibility(View.VISIBLE);
					_nextRetryButton.setOnClickListener(new OnClickListener() 
					{
						public void onClick(View v) 
						{
							Utils.openPlayStore(LiteMigrationActivity.this);
						}
					});
				break;
			}
		}
	}

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_migrate_data);

		yesToMigration();
		
		notNowToMigration();
		
		noNeverToMigration();
	}

	private void noNeverToMigration() 
	{
		((Button)findViewById(R.id.noThanksButton)).setOnClickListener(new OnClickListener()
		 {
			@Override
			public void onClick(View v) 
			{
				LiteMigrationActivity.this.ForgetAboutMigration();
			}
		 });
	}

	private void notNowToMigration() {
		((Button)findViewById(R.id.noButton)).setOnClickListener(new OnClickListener()
		 {
			@Override
			public void onClick(View v) 
			{
				LiteMigrationActivity.this.SkipMigration();
			}
		 });
	}

	private void yesToMigration() {
		((Button)findViewById(R.id.yesButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				new AlertDialog.Builder(LiteMigrationActivity.this)
					.setMessage("Are you sure you want to replace your existing data with data from Calorie Tracker Lite?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							LiteMigrationActivity.this.PerformMigration();
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							LiteMigrationActivity.this.SkipMigration();
						}
					})
					.show();
			}
		});
	}
	
	private void ForgetAboutMigration()
	{
		DataHelper.setPref(EntryPageActivity.SHOULD_PROMPT_FOR_MIGRATION, false);
		SkipMigration();
	}
	
	private void SkipMigration()
	{
		startActivity(new Intent(LiteMigrationActivity.this, WelcomeActivity.class));
	}
	
	private void PerformMigration()
	{
		new MigrationTask().execute(new Void[]{});
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
	}
	
}
