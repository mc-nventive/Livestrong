package com.livestrong.myplate.providers;

import com.livestrong.myplate.back.DataHelper;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MyPlateDataProvider extends ContentProvider {

	private static final int ACTIVITY_LEVELS = 1;
	
	private static final int DIARY_EXERCISE = 2;
	
	private static final int DIARY_FOOD = 3;
	
	private static final int DIARY_WATER = 4;
	
	private static final int DIARY_WEIGHT = 5;
	
	private static final int EXERCISE = 6;
	
	private static final int FOOD = 7;
	
	private static final int MEAL = 8;
	
	private static final int MEAL_ITEM = 9;
	
	private static final int MEAL_NUTRITION_INFO = 10;
	
	private static final int USER_PROFILE = 11;
	
	private static final int USER_PREFERENCES = 12;
	
	private static final String AUTHORITY = "com.livestrong.myplate.myplatedataprovider";
	
	private static UriMatcher __uriMatcher = new  UriMatcher(UriMatcher.NO_MATCH);
	
	static
	{
		__uriMatcher.addURI(AUTHORITY, "activity_levels", ACTIVITY_LEVELS);
		__uriMatcher.addURI(AUTHORITY, "diary_exercise", DIARY_EXERCISE);
		__uriMatcher.addURI(AUTHORITY, "diary_food", DIARY_FOOD );
		__uriMatcher.addURI(AUTHORITY, "diary_water", DIARY_WATER);
		__uriMatcher.addURI(AUTHORITY, "diary_weight", DIARY_WEIGHT);
		__uriMatcher.addURI(AUTHORITY, "exercise", EXERCISE);
		__uriMatcher.addURI(AUTHORITY, "food", FOOD);
		__uriMatcher.addURI(AUTHORITY, "meal", MEAL);
		__uriMatcher.addURI(AUTHORITY, "meal_item", MEAL_ITEM);
		__uriMatcher.addURI(AUTHORITY, "meal_nutrition_info", MEAL_NUTRITION_INFO);
		__uriMatcher.addURI(AUTHORITY, "userprofile", USER_PROFILE);
		__uriMatcher.addURI(AUTHORITY, "userpreferences", USER_PREFERENCES);
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() 
	{
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		if (!DataHelper.INITIALIZED) {
			DataHelper.initialize(getContext());
		}
		
		SQLiteDatabase database = DataHelper.getDatabaseHelper().getReadableDatabase();
		
		switch(__uriMatcher.match(uri))
		{
		case ACTIVITY_LEVELS:
			return database.rawQuery(sqlQuery("activity_levels", null) , null);
		case DIARY_EXERCISE:
			return database.rawQuery(sqlQuery("diary_exercise", "exerciseId"), null);
		case DIARY_FOOD:
			return database.rawQuery(sqlQuery("diary_food", "mealId"), null);
		case DIARY_WATER:
			return database.rawQuery(sqlQuery("diary_water", "modified") , null);
		case DIARY_WEIGHT:
			return database.rawQuery(sqlQuery("diary_weight", null), null);
		case EXERCISE:
			return database.rawQuery(sqlQuery("exercise", null), null);
		case FOOD:
			return database.rawQuery(sqlQuery("food", null), null);
		case MEAL:
			return database.rawQuery(sqlQuery("meal", "mealId"), null);
		case MEAL_ITEM:
			return database.rawQuery(sqlQuery("mealitem", null), null);
		case MEAL_NUTRITION_INFO:
			return database.rawQuery(sqlQuery("mealnutritioninfo", null), null);
		case USER_PROFILE:
			return database.rawQuery(sqlQuery("userprofile", null), null);
		case USER_PREFERENCES:
			return preferencesCursor();
		default:
			return new MatrixCursor(new String[]{ });
		}
	}
	
	private Cursor preferencesCursor() 
	{
		String[] preferencesFields = new String[]
		{
			// User configurable Preferences
			DataHelper.PREFS_WEIGHT_UNITS,
			DataHelper.PREFS_DISTANCE_UNITS,
			DataHelper.PREFS_WATER_UNITS,
			DataHelper.PREFS_DAILY_REMINDER,
			DataHelper.PREFS_DAILY_REMINDER_TIME,
		};
		
		
		MatrixCursor cursor = new MatrixCursor(preferencesFields);
		
		cursor.addRow(new Object[]
		{
			DataHelper.getPref(DataHelper.PREFS_WEIGHT_UNITS, (String) null),
			DataHelper.getPref(DataHelper.PREFS_DISTANCE_UNITS, (String)null),
			DataHelper.getPref(DataHelper.PREFS_WATER_UNITS, (String)null),
			DataHelper.getPref(DataHelper.PREFS_DAILY_REMINDER, (Boolean)null),
			DataHelper.getPref(DataHelper.PREFS_DAILY_REMINDER_TIME, (Long) null),
		});
		
		return cursor;
	}

	private String sqlQuery(String tableName, String orderingColumn)
	{
		String format = "SELECT * FROM %1$s";
		
		if(null != orderingColumn)	format.concat(" ORDER BY %2$s ASC");
		
		if (null == orderingColumn) 
		{
			return String.format(format, tableName);
		}
		else
		{
			return String.format(format, tableName, orderingColumn);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}
