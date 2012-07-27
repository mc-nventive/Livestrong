package com.livestrong.myplate.back;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.SelectArg;
import com.livestrong.myplate.activity.LoginActivity;
import com.livestrong.myplate.back.api.ApiHelper;
import com.livestrong.myplate.back.api.models.ExerciseSearchResponse;
import com.livestrong.myplate.back.api.models.FoodSearchResponse;
import com.livestrong.myplate.back.api.models.NewCommentResponse;
import com.livestrong.myplate.back.api.models.NewMessageResponse;
import com.livestrong.myplate.back.api.models.SyncDiaryObject;
import com.livestrong.myplate.back.db.DatabaseHelper;
import com.livestrong.myplate.back.models.ActivityLevels;
import com.livestrong.myplate.back.models.CommunityMessage;
import com.livestrong.myplate.back.models.CommunityMessageComment;
import com.livestrong.myplate.back.models.DiaryEntries;
import com.livestrong.myplate.back.models.DiaryEntries.DiaryEntryType;
import com.livestrong.myplate.back.models.DiaryEntry;
import com.livestrong.myplate.back.models.Exercise;
import com.livestrong.myplate.back.models.ExerciseDiaryEntry;
import com.livestrong.myplate.back.models.Food;
import com.livestrong.myplate.back.models.FoodDiaryEntry;
import com.livestrong.myplate.back.models.FoodDiaryEntry.TimeOfDay;
import com.livestrong.myplate.back.models.Meal;
import com.livestrong.myplate.back.models.UserProfile;
import com.livestrong.myplate.back.models.UserProfile.Gender;
import com.livestrong.myplate.back.models.UserProgress;
import com.livestrong.myplate.back.models.WeightDiaryEntry;
import com.livestrong.myplate.utilities.SimpleDate;

public class DataHelper {

	public final static String PREFS_NAME = "LiveStrong";
	public final static String PREFS_LAST_ONLOAD_SYNC = "onloadSync";
	public final static String PREFS_LAST_SYNC_TOKEN = "lastSyncToken";
	public final static String PREFS_USERNAME = "username";
	public final static String PREFS_PASSWORD = "password";
	public final static String PREFS_ACCESS_TOKEN = "accessToken";
	public final static String PREFS_REFRESH_TOKEN = "refreshToken";
	public final static String PREFS_LAST_SYNC = "lastSync";
	
	// User configurable Preferences
	public final static String PREFS_WEIGHT_UNITS = "weightUnits";
	public final static String PREFS_DISTANCE_UNITS = "distanceUnits";
	public final static String PREFS_WATER_UNITS = "waterUnits";
	public final static String PREFS_DAILY_REMINDER = "dailyReminder";
	public final static String PREFS_DAILY_REMINDER_TIME = "dailyReminderTime";
	
	// UI Saved state Preferences
	public final static String PREFS_SELECTED_TAB = "selectedTab";
	public final static String PREFS_PROGRESS_BAR_WIDTH = "progressBarWidth";
	public final static String PREFS_DIARY_SELECTED_TAB = "diarySelectedTab";
	public final static String PREFS_DIARY_SELECTED_DATE = "diarySelectedDATE";
	public final static String PREFS_PROGRESS_SELECTED_TAB = "progressSelectedTab";
	public final static String PREFS_COMMUNITY_SELECTED_TAB = "communitySelectedTab";
	public final static String PREFS_MORE_SELECTED_TAB = "moreSelectedTab";

	public static enum WeightUnits {KILOGRAMS, POUNDS, STONES};
	public static enum DistanceUnits {METERS, MILES};
	public static enum WaterUnits {MILLILITERS, ONCES};
	
	private static volatile Stack<DatabaseHelper> databaseHelper;

	private static Context context;

	public static Method METHOD_REGISTER_USER;
	public static Method METHOD_GET_USER_PROFILE;
	public static Method METHOD_GET_ACTIVITY_LEVELS;
	public static Method METHOD_SEARCH_FOODS;
	public static Method METHOD_SEARCH_EXERCISES;
	public static Method METHOD_GET_FOODS;
	public static Method METHOD_GET_EXERCISES;
	public static Method METHOD_GET_COMMUNITY_MESSAGES;
	public static Method METHOD_GET_USER_OWN_COMMUNITY_MESSAGES;
	public static Method METHOD_GET_COMMUNITY_MESSAGE_COMMENTS;
	public static Method METHOD_GET_RECENT_FOODS;
	public static Method METHOD_GET_FAVORITE_FOODS;
	public static Method METHOD_GET_RECENT_EXERCISES;
	public static Method METHOD_GET_FAVORITE_EXERCISES;
	public static Method METHOD_SYNC_DIARY;
	public static Method METHOD_SYNC_USER_PROFILE;
	public static Method METHOD_POST_NEW_MESSAGE;
	public static Method METHOD_POST_NEW_COMMENT;
	public static Method METHOD_GET_DAILY_DIARY_ENTRIES_FOR_DATES;
	public static Method METHOD_GET_DAILY_DIARY_ENTRIES_FOR_DAY;
	public static Method METHOD_GET_TODAY_NUTRIENTS;

	static {
		try {
			METHOD_REGISTER_USER = DataHelper.class.getMethod("registerUserSynchronous", String.class, String.class, String.class, String.class, Date.class, Gender.class, String.class, DataHelperDelegate.class);
			METHOD_GET_USER_PROFILE = DataHelper.class.getMethod("getUserProfileSynchronous", DataHelperDelegate.class);
			METHOD_SEARCH_FOODS = DataHelper.class.getMethod("searchFoodsSynchronous", String.class, boolean.class, DataHelperDelegate.class);
			METHOD_SEARCH_EXERCISES = DataHelper.class.getMethod("searchExercisesSynchronous", String.class, boolean.class, DataHelperDelegate.class);
			METHOD_GET_FOODS = DataHelper.class.getMethod("getFoodsSynchronous", Collection.class, DataHelperDelegate.class);
			METHOD_GET_RECENT_FOODS = DataHelper.class.getMethod("getRecentFoods", TimeOfDay.class, DataHelperDelegate.class);
			METHOD_GET_FAVORITE_FOODS = DataHelper.class.getMethod("getFavoriteFoods", TimeOfDay.class, DataHelperDelegate.class);
			METHOD_GET_EXERCISES = DataHelper.class.getMethod("getExercisesSynchronous", Collection.class, DataHelperDelegate.class);
			METHOD_GET_RECENT_EXERCISES = DataHelper.class.getMethod("getRecentExercises", DataHelperDelegate.class);
			METHOD_GET_FAVORITE_EXERCISES = DataHelper.class.getMethod("getFavoriteExercises", DataHelperDelegate.class);
			METHOD_GET_ACTIVITY_LEVELS = DataHelper.class.getMethod("getActivityLevelsSynchronous", DataHelperDelegate.class);
			METHOD_GET_COMMUNITY_MESSAGES = DataHelper.class.getMethod("getCommunityMessagesSynchronous", int.class, DataHelperDelegate.class);
			METHOD_GET_USER_OWN_COMMUNITY_MESSAGES = DataHelper.class.getMethod("getUserOwnCommunityMessagesSynchronous", int.class, DataHelperDelegate.class);
			METHOD_GET_COMMUNITY_MESSAGE_COMMENTS = DataHelper.class.getMethod("getCommunityMessageCommentsSynchronous", int.class, int.class, DataHelperDelegate.class);
			METHOD_POST_NEW_MESSAGE = DataHelper.class.getMethod("postNewMessageSynchronous", String.class, DataHelperDelegate.class);
			METHOD_POST_NEW_COMMENT = DataHelper.class.getMethod("postNewCommentSynchronous", int.class, String.class, DataHelperDelegate.class);
			METHOD_SYNC_DIARY = DataHelper.class.getMethod("syncDiarySynchronous", SyncDiaryObject.class, DataHelperDelegate.class);
			METHOD_SYNC_USER_PROFILE = DataHelper.class.getMethod("syncUserProfileSynchronous", UserProfile.class, DataHelperDelegate.class);
			METHOD_GET_DAILY_DIARY_ENTRIES_FOR_DAY = DataHelper.class.getMethod("getDailyDiaryEntries", Date.class, DataHelperDelegate.class);
			METHOD_GET_DAILY_DIARY_ENTRIES_FOR_DATES = DataHelper.class.getMethod("getDailyDiaryEntries", Date.class, Date.class, DataHelperDelegate.class);
			METHOD_GET_TODAY_NUTRIENTS = DataHelper.class.getMethod("getTodayNutrients", DataHelperDelegate.class);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	private DataHelper() {
		// Nobody should instantiate this class.
		// Use initialize(Context) instead.
	}

	public static void initialize(Context ctx) {
		context = ctx;
		new DatabaseHelper(ctx);
		ApiHelper.initialize(ctx);
		databaseHelper = new Stack<DatabaseHelper>();
		
		DataHelper.initializePreferences();
	}

	private static void initializePreferences(){
		// Initialize User Units Prefs
		if (DataHelper.getPref(DataHelper.PREFS_WEIGHT_UNITS, (String) null) == null){
			DataHelper.setPref(DataHelper.PREFS_WEIGHT_UNITS, WeightUnits.POUNDS.toString());	
		}
		if (DataHelper.getPref(DataHelper.PREFS_WATER_UNITS, (String) null) == null){
			DataHelper.setPref(DataHelper.PREFS_WATER_UNITS, WaterUnits.ONCES.toString());	
		}
		if (DataHelper.getPref(DataHelper.PREFS_DISTANCE_UNITS, (String) null) == null){
			DataHelper.setPref(DataHelper.PREFS_DISTANCE_UNITS, DistanceUnits.MILES.toString());	
		}
		if (DataHelper.getPref(DataHelper.PREFS_DAILY_REMINDER, (Boolean) null) == null){
			DataHelper.setPref(DataHelper.PREFS_DAILY_REMINDER, true);	
		}
		if (DataHelper.getPref(DataHelper.PREFS_DAILY_REMINDER_TIME, (Integer) null) == null){
			Date reminderTime = new Date();
			reminderTime.setHours(18);
			reminderTime.setMinutes(0);
			
			DataHelper.setPref(DataHelper.PREFS_DAILY_REMINDER_TIME, reminderTime.getTime());	
		}	
	}
	
	public static void setDatabaseHelper(DatabaseHelper helper) {
		if (helper == null) {
			databaseHelper.pop();
		} else {
			databaseHelper.push(helper);
		}
	}

	@SuppressWarnings("deprecation")
	public static DatabaseHelper getDatabaseHelper() {
		if (databaseHelper.size() == 0) {
			setDatabaseHelper((DatabaseHelper) OpenHelperManager.getHelper(context));
		}
		return databaseHelper.lastElement();
	}

	/**
	 * Tells you if the user has logged in or not. The UI and API calls will change depending on this.
	 * @return boolean isLoggedIn
	 */
	public static boolean isLoggedIn() {
		return DataHelper.getPref(DataHelper.PREFS_USERNAME, (String) null) != null; 
	}

	public static void forceRefreshData(DataHelperDelegate delegate) {
		if (!isLoggedIn()) {
			return;
		}
		// Remove the sync token, and refresh
		setPref(PREFS_LAST_SYNC_TOKEN, null);
		refreshData(delegate);
	}

	public static void refreshData(DataHelperDelegate delegate) {
		// Save synchronization date
		DataHelper.setPref(DataHelper.PREFS_LAST_SYNC, (new Date()).getTime());
		
		// This will just cache the available activity levels into the local DB.
		DataHelper.getActivityLevels(delegate);

		if (!isLoggedIn()) {
			return;
		}

		// Let's not refresh the UserProfile that we just received from logging in! :)
		if (!(delegate instanceof LoginActivity)) {
			// Sync the user profile, if there is network connectivity
			DataHelper.syncUserProfile(delegate);
		}

		// Sync the diary, if there is network connectivity
		DataHelper.syncDiary(delegate);
	}

	/**
	 * Search the local (when autoComplete = true) or remote database to find Food objects.
	 * @param query: the string to search for
	 * @param autoComplete: When true, will load search results from the local database. When false, will query the remote server.
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return FoodSearchResponse
	 * @example FoodSearchResponse foodSearchResult = DataHelper.searchFoods("sandwich", isAutoComplete, null); 
	 */
	@SuppressWarnings("unchecked")
	public static FoodSearchResponse searchFoods(String query, boolean autoComplete, DataHelperDelegate delegate) {
		if (delegate == null) {
			return searchFoodsSynchronous(query, autoComplete, delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_SEARCH_FOODS);
		asyncTask.execute(query, autoComplete, delegate);
		return null;
	}

	public static FoodSearchResponse searchFoodsSynchronous(String query, boolean autoComplete, DataHelperDelegate delegate) {
		if (autoComplete) {
			RuntimeExceptionDao<Food, Integer> foodDao = getDatabaseHelper().getFoodDao();
			List<Food> foodCatalog = new ArrayList<Food>();

			// Perform query on FoodDao
			try {
				foodCatalog = foodDao.queryBuilder().distinct().limit((long) 50).where().like(Food.ITEM_TITLE_FIELD_NAME, new SelectArg("%" + query + "%")).query();

				FoodSearchResponse response = new FoodSearchResponse(query, 0, Integer.MAX_VALUE, foodCatalog.size(), foodCatalog);
				if (delegate != null) {
					delegate.dataReceivedThreaded(METHOD_SEARCH_FOODS, response);
					return null;
				} else {
					return response;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return ApiHelper.searchFood(query, delegate);
		}
	}

	/**
	 * Search the local (when autoComplete = true) or remote database to find Exercise objects.
	 * @param query: the string to search for
	 * @param autoComplete: When true, will load search results from the local database. When false, will query the remote server.
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return ExerciseSearchResponse
	 * @example ExerciseSearchResponse exerciseSearchResult = DataHelper.searchExercises("run", isAutoComplete, null); 
	 */
	@SuppressWarnings("unchecked")
	public static ExerciseSearchResponse searchExercises(String query, boolean autoComplete, DataHelperDelegate delegate) {
		if (delegate == null) {
			return searchExercisesSynchronous(query, autoComplete, delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_SEARCH_EXERCISES);
		asyncTask.execute(query, autoComplete, delegate);
		return null;
	}

	public static ExerciseSearchResponse searchExercisesSynchronous(String query, boolean autoComplete, DataHelperDelegate delegate) {
		if (autoComplete) {
			RuntimeExceptionDao<Exercise, Integer> exerciseDao = getDatabaseHelper().getExerciseDao();
			List<Exercise> exercisesCatalog = new ArrayList<Exercise>();

			// Perform query on ExerciseDao
			try {
				exercisesCatalog = exerciseDao.queryBuilder().distinct().limit((long) 50).where().like(Exercise.TITLE_FIELD_NAME, new SelectArg("%" + query + "%")).query();

				ExerciseSearchResponse response = new ExerciseSearchResponse(query, 0, Integer.MAX_VALUE, exercisesCatalog.size(), exercisesCatalog);
				if (delegate != null) {
					delegate.dataReceivedThreaded(METHOD_SEARCH_EXERCISES, response);
					return null;
				} else {
					return response;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return ApiHelper.searchExercises(query, delegate);
		}
	}

	/**
	 * Load the currently logged user profile, either from the local DB or from the remote server.
	 * 
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return UserProfile
	 * @example UserProfile userProfile = DataHelper.getUserProfile(null);
	 */
	@SuppressWarnings("unchecked")
	public static UserProfile getUserProfile(DataHelperDelegate delegate) {
		if (delegate == null) {
			return getUserProfileSynchronous(delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_GET_USER_PROFILE);
		asyncTask.execute(delegate);
		return null;
	}

	public static UserProfile getUserProfileSynchronous(DataHelperDelegate delegate) {
		RuntimeExceptionDao<UserProfile, Integer> userProfileDao = getDatabaseHelper().getUserProfileDao();

		// Is this food already in the local DB?
		List<UserProfile> userProfiles = userProfileDao.queryForAll();

		UserProfile userProfile = null;
		if (userProfiles != null && userProfiles.size() > 0) {
			userProfile = userProfiles.get(0);
			// TODO Check if it's too old; if it is, refresh from the API.
			// if (userProfile.isTooOld()) {
			// userProfile = null;
			// }
		}

		if (userProfile == null) {
			if (!isLoggedIn()) {
				return null;
			}
			userProfile = ApiHelper.getUserProfile(delegate);
		}

		persist(userProfile, METHOD_GET_USER_PROFILE);

		return userProfile;
	}
	
	@SuppressWarnings("unchecked")
	public static UserProfile registerUser(String username, String password, String name, String email, Date birthday, Gender gender, String postalCode, DataHelperDelegate delegate) {
		if (delegate == null) {
			return registerUserSynchronous(username, password, name, email, birthday, gender, postalCode, delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_REGISTER_USER);
		asyncTask.execute(username, password, name, email, birthday, gender, postalCode, delegate);
		return null;

	}

	public static UserProfile registerUserSynchronous(String username, String password, String name, String email, Date birthday, Gender gender, String postalCode, DataHelperDelegate delegate) {
		username = ApiHelper.registerUser(username, password, name, email, birthday, gender, postalCode, delegate);
		if (username != null) {
			return ApiHelper.authenticate(username, password, delegate);
		}
		return null;
	}

	/**
	 * Load complete Food information from an ID (foodId), either from the local DB or from the remote server.
	 * 
	 * @param foodId
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return Food
	 * @example Food food = DataHelper.getFood(825791, null);
	 */
	public static Food getFood(int foodId, DataHelperDelegate delegate) {
		List<Integer> foodIds = new ArrayList<Integer>(1);
		foodIds.add(foodId);
		List<Food> foods = getFoods(foodIds, delegate);
		if (foods != null && foods.size() > 0) {
			return foods.get(0);
		}
		return null;
	}

	/**
	 * Load complete Food information from multiple food ID (foodId), either from the local DB or from the remote server.
	 * 
	 * @param foodIds
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return List<Food>
	 * @example List<Food> foods = DataHelper.getFood(foodIds, null);
	 */
	@SuppressWarnings("unchecked")
	public static List<Food> getFoods(Collection<Integer> foodIds, DataHelperDelegate delegate) {
		if (delegate == null) {
			return getFoodsSynchronous(foodIds, delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_GET_FOODS);
		asyncTask.execute(foodIds, delegate);
		return null;
	}

	public static List<Food> getFoodsSynchronous(Collection<Integer> foodIds, DataHelperDelegate delegate) {
		List<Food> foods = null;
		if (foodIds == null || foodIds.size() == 0) {
			return new ArrayList<Food>();
		}

		RuntimeExceptionDao<Food, Integer> foodDao = getDatabaseHelper().getFoodDao();

		// Is this food already in the local DB?
		try {
			foods = foodDao.queryBuilder().where().in(Food.FOOD_ID_FIELD_NAME, foodIds).query();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (foods != null) {
			// Remove the loaded Food ids from foodIds; anything left in there will be loaded from the API.
			for (Food food : foods) {
				if (food.getFoodId() != Food.CUSTOM_FOOD_ID){ // Do not remove custom foods (Force fetch from API)
					foodIds.remove(new Integer(food.getFoodId()));	
				}				
			}
			// TODO Check if it's too old; if it is, refresh from the API.
			// if (food.isTooOld()) {
			// food = null;
			// }
		}

		if (foodIds.size() > 0) {
			// Not; load it from the API.
			foods = ApiHelper.getFoods(foodIds, delegate);
		}

		persist(foods, METHOD_GET_FOODS);

		return foods;
	}

	/**
	 * Load the recently used Food from the local DB.
	 * 
	 * @param timeOfDay The TimeOfDay for which you want the recently used food.
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return List<Food>
	 * @example List<Food> foods = DataHelper.getRecentFoods(TimeOfDay.BREAKFAST, null);
	 */
	public static List<Food> getRecentFoods(TimeOfDay timeOfDay, DataHelperDelegate delegate) {
		return getFavoriteFoods(timeOfDay, delegate, "MAX(" + DiaryEntry.MODIFIED_FIELD_NAME + ") DESC");
	}

	/**
	 * Load favorite Foods information from either from the local DB or the remote server.
	 * 
	 * @param timeOfDay The TimeOfDay for which you want the favorite food.
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return List<Food>
	 * @example List<Food> food = DataHelper.getFavoriteFoods(TimeOfDay.BREAKFAST, null);
	 */
	public static List<Food> getFavoriteFoods(TimeOfDay timeOfDay, DataHelperDelegate delegate) {
		return getFavoriteFoods(timeOfDay, delegate, "COUNT(*) DESC");
	}

	@SuppressWarnings("unchecked")
	public static List<Food> getFavoriteFoods(TimeOfDay timeOfDay, DataHelperDelegate delegate, String orderBy) {
		List<Food> favoriteFoods = null;
		try {
			RuntimeExceptionDao<FoodDiaryEntry, Integer> diaryDao = getDatabaseHelper().getDiaryDao(FoodDiaryEntry.class);

			List<FoodDiaryEntry> frequentDiaryEntries = diaryDao.queryBuilder()
					.selectColumns("food_id")
					.orderByRaw(orderBy)
					.groupBy("food_id")
					.limit((long) 50)
					.where()
					.eq(FoodDiaryEntry.TIME_OF_DAY_FIELD_NAME, timeOfDay)
					.and()
					.eq(DiaryEntry.DELETED_FIELD_NAME, false)
					.query();

			List<Integer> foodIds = new ArrayList<Integer>();
			for (DiaryEntry diaryEntry : frequentDiaryEntries) {
				Food food = ((FoodDiaryEntry) diaryEntry).getFood();
				if (food == null || food.isGeneric()) {
					// Generic calorie coming from the API
					continue;
				}
				foodIds.add(food.getId());
			}

			RuntimeExceptionDao<Food, Integer> foodDao = getDatabaseHelper().getFoodDao();
			List<Food> favoriteFoodsUnsorted = foodDao.queryBuilder().where().in(Food.ID_FIELD_NAME, foodIds).query();

			// Re-order favoriteFoodsUnsorted according to frequentDiaryEntries/foodIds
			favoriteFoods = (List<Food>) DatabaseHelper.reorder(favoriteFoodsUnsorted, foodIds, Food.class.getMethod("getId"));
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return favoriteFoods;
	}

	/**
	 * Load complete Exercise information from an ID (exerciseId), either from the local DB or from the remote server.
	 * 
	 * @param exerciseId
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return Exercise
	 * @example Exercise exercise = DataHelper.getExercise(3236, null);
	 */
	public static Exercise getExercise(int exerciseId, DataHelperDelegate delegate) {
		List<Integer> exerciseIds = new ArrayList<Integer>(1);
		exerciseIds.add(exerciseId);
		List<Exercise> exercises = getExercises(exerciseIds, delegate);
		if (exercises != null && exercises.size() > 0) {
			return exercises.get(0);
		}
		return null;
	}

	/**
	 * Load complete Exercise information from multiple ID (exerciseId), either from the local DB or from the remote server.
	 * 
	 * @param exerciseIds
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return List<Exercise>
	 * @example List<Exercise> exercises = DataHelper.getExercises(exerciseIds, null);
	 */
	@SuppressWarnings("unchecked")
	public static List<Exercise> getExercises(Collection<Integer> exerciseIds, DataHelperDelegate delegate) {
		if (delegate == null) {
			return getExercisesSynchronous(exerciseIds, delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_GET_EXERCISES);
		asyncTask.execute(exerciseIds, delegate);
		return null;
	}

	public static List<Exercise> getExercisesSynchronous(Collection<Integer> exerciseIds, DataHelperDelegate delegate) {
		List<Exercise> exercises = null;
		if (exerciseIds == null || exerciseIds.size() == 0) {
			return new ArrayList<Exercise>();
		}
		try {
			RuntimeExceptionDao<Exercise, Integer> exerciseDao = getDatabaseHelper().getExerciseDao();

			// Is this exercise already in the local DB?
			exercises = exerciseDao.queryBuilder().where().in(Exercise.EXERCISE_ID_FIELD_NAME, exerciseIds).query();

			if (exercises != null) {
				// Remove the loaded Exercise IDs from exerciseIds; anything left in there will be loaded from the API.
				for (Exercise exercise : exercises) {
					exerciseIds.remove(new Integer(exercise.getExerciseId()));
				}
				// TODO Check if it's too old; if it is, refresh from the API.
				// if (exercise.isTooOld()) {
				// exercise = null;
				// }
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (exerciseIds.size() > 0) {
			// Not; load it from the API.
			exercises = ApiHelper.getExercises(exerciseIds, delegate);
		}

		persist(exercises, METHOD_GET_EXERCISES);

		return exercises;
	}

	/**
	 * Load the recently used Exercise from the local DB.
	 * 
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return List<Exercise>
	 * @example List<Exercise> recentExercises = DataHelper.getRecentExercises(null);
	 */
	public static List<Exercise> getRecentExercises(DataHelperDelegate delegate) {
		return getRecentOrFavoriteExercises(delegate, "MAX(" + DiaryEntry.MODIFIED_FIELD_NAME + ") DESC");
	}

	/**
	 * Load favorite (frequently user) Exercise information from either from the local DB.
	 * 
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return List<Exercise>
	 * @example List<Exercise> favoriteExercises = DataHelper.getFavoriteExercises(null);
	 */
	public static List<Exercise> getFavoriteExercises(DataHelperDelegate delegate) {
		return getRecentOrFavoriteExercises(delegate, "COUNT(*) DESC");
	}

	@SuppressWarnings("unchecked")
	private static List<Exercise> getRecentOrFavoriteExercises(DataHelperDelegate delegate, String orderBy) {
		List<Exercise> recentExercises = null;
		try {
			RuntimeExceptionDao<ExerciseDiaryEntry, Integer> diaryDao = getDatabaseHelper().getDiaryDao(ExerciseDiaryEntry.class);

			List<ExerciseDiaryEntry> recentDiaryEntries = diaryDao.queryBuilder()
					.selectColumns("exercise_id")
					.orderByRaw(orderBy)
					.groupBy("exercise_id")
					.limit((long) 50)
					.where()
					.eq(DiaryEntry.DELETED_FIELD_NAME, false)
					.query();

			List<Integer> exerciseIds = new ArrayList<Integer>();
			for (DiaryEntry diaryEntry : recentDiaryEntries) {
				Exercise exercise = ((ExerciseDiaryEntry) diaryEntry).getExercise();
				if (exercise == null || exercise.isGeneric()) {
					// Generic exercise coming from the API
					continue;
				}
				exerciseIds.add(exercise.getId());
			}

			RuntimeExceptionDao<Exercise, Integer> exerciseDao = getDatabaseHelper().getExerciseDao();
			List<Exercise> recentExercisesUnsorted = exerciseDao.queryBuilder()
					.where()
					.in(Exercise.ID_FIELD_NAME, exerciseIds)
					.query();

			// Re-order recentExercisesUnsorted according to frequentDiaryEntries/exerciseIds
			recentExercises = (List<Exercise>) DatabaseHelper.reorder(recentExercisesUnsorted, exerciseIds, Exercise.class.getMethod("getId"));
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return recentExercises;
	}

	/**
	 * Load Community messages from the remote server.
	 * @param pageNum
	 * @return List<CommunityMessage>
	 * @example List<CommunityMessage> messages = DataHelper.getCommunityMessages(1, null);
	 */
	@SuppressWarnings("unchecked")
	public static List<CommunityMessage> getCommunityMessages(int pageNum, DataHelperDelegate delegate) {
		if (delegate == null) {
			return getCommunityMessagesSynchronous(pageNum, delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_GET_COMMUNITY_MESSAGES);
		asyncTask.execute(pageNum, delegate);
		return null;
	}

	public static List<CommunityMessage> getCommunityMessagesSynchronous(int pageNum, DataHelperDelegate delegate) {
		return ApiHelper.getCommunityMessages(pageNum, delegate);
	}

	/**
	 * Load the user's own Community messages from the remote server.
	 * @param pageNum
	 * @return List<CommunityMessage>
	 * @example List<CommunityMessage> messages = DataHelper.getUserOwnCommunityMessages(1, null);
	 */
	@SuppressWarnings("unchecked")
	public static List<CommunityMessage> getUserOwnCommunityMessages(int pageNum, DataHelperDelegate delegate) {
		if (delegate == null) {
			return getUserOwnCommunityMessagesSynchronous(pageNum, delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_GET_USER_OWN_COMMUNITY_MESSAGES);
		asyncTask.execute(pageNum, delegate);
		return null;
	}
	
	public static List<CommunityMessage> getUserOwnCommunityMessagesSynchronous(int pageNum, DataHelperDelegate delegate) {
		if (!isLoggedIn()) {
			return null;
		}
		return ApiHelper.getUserOwnCommunityMessages(pageNum, delegate);
	}

	/**
	 * Load the comments on a specific Community message from the remote server.
	 * @param postId
	 * @param pageNum
	 * @return List<CommunityMessageComment>
	 * @example List<CommunityMessageComment> comments = DataHelper.getCommunityMessageComments(418891, null);
	 */
	@SuppressWarnings("unchecked")
	public static List<CommunityMessageComment> getCommunityMessageComments(int postId, int pageNum, DataHelperDelegate delegate) {
		if (delegate == null) {
			return getCommunityMessageCommentsSynchronous(postId, pageNum, delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_GET_COMMUNITY_MESSAGE_COMMENTS);
		asyncTask.execute(postId, pageNum, delegate);
		return null;
	}

	public static List<CommunityMessageComment> getCommunityMessageCommentsSynchronous(int postId, int pageNum, DataHelperDelegate delegate) {
		return ApiHelper.getCommunityMessageComments(postId, pageNum, delegate);
	}

	@SuppressWarnings("unchecked")
	public static <D extends DiaryEntry> void saveDiaryEntry(D diaryEntry, DataHelperDelegate delegate) {
		RuntimeExceptionDao<DiaryEntry, Integer> diaryDao = (RuntimeExceptionDao<DiaryEntry, Integer>) getDatabaseHelper().getDiaryDao(diaryEntry.getClass());
		diaryDao.createOrUpdate(diaryEntry);
	}
	
	public static void saveUserProfile(UserProfile userProfile, DataHelperDelegate delegate) {
		RuntimeExceptionDao<UserProfile, Integer> userProfileDao = (RuntimeExceptionDao<UserProfile, Integer>) getDatabaseHelper().getUserProfileDao();
		userProfileDao.createOrUpdate(userProfile);
	}

	@SuppressWarnings("unchecked")
	public static <D extends DiaryEntry> void deleteDiaryEntry(D diaryEntry, DataHelperDelegate delegate) {
		RuntimeExceptionDao<DiaryEntry, Integer> diaryDao = (RuntimeExceptionDao<DiaryEntry, Integer>) getDatabaseHelper().getDiaryDao(diaryEntry.getClass());
		diaryEntry.setDeleted(true);
		diaryDao.createOrUpdate(diaryEntry);
	}

	@SuppressWarnings("unchecked")
	private static SyncDiaryObject syncDiary(SyncDiaryObject localChanges, DataHelperDelegate delegate) {
		if (delegate == null) {
			return syncDiarySynchronous(localChanges, delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_SYNC_DIARY);
		asyncTask.execute(localChanges, delegate);
		return null;
	}

	public static SyncDiaryObject syncDiarySynchronous(SyncDiaryObject localChanges, DataHelperDelegate delegate) {
		if (!isLoggedIn()) {
			return null;
		}
		SyncDiaryObject remoteChanges = ApiHelper.syncDiary(localChanges, delegate);
		persist(remoteChanges, METHOD_SYNC_DIARY);
		return remoteChanges;
	}

	public static SyncDiaryObject syncDiary(DataHelperDelegate delegate) {
		if (!isLoggedIn()) {
			return null;
		}
		if (ApiHelper.isOnline()) {
			SyncDiaryObject localChanges = new SyncDiaryObject();
			localChanges.loadDirtyEntries();
			return syncDiary(localChanges, delegate);
		}
		Log.i(DataHelper.class.getName(), "Not online; skipping diary sync for now.");
		return null;
	}

	@SuppressWarnings("unchecked")
	public static UserProfile syncUserProfile(UserProfile modifiedUserProfile, DataHelperDelegate delegate) {
		if (delegate == null) {
			return syncUserProfileSynchronous(modifiedUserProfile, delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_SYNC_USER_PROFILE);
		asyncTask.execute(modifiedUserProfile, delegate);
		return null;
	}

	public static UserProfile syncUserProfileSynchronous(UserProfile modifiedUserProfile, DataHelperDelegate delegate) {
		if (!isLoggedIn()) {
			return modifiedUserProfile;
		}

		ApiHelper.syncUserProfile(modifiedUserProfile, delegate);

		modifiedUserProfile.wasSynced();
		getDatabaseHelper().getUserProfileDao().update(modifiedUserProfile);

		UserProfile newUserProfile = getUserProfile(null);
		return newUserProfile;
	}

	public static UserProfile syncUserProfile(DataHelperDelegate delegate) {
		if (ApiHelper.isOnline()) {
			UserProfile localUserProfile = getUserProfile(null);
			if (localUserProfile == null) {
				Log.w(DataHelper.class.getName(), "An error occurred while trying to get the user profile from the API. Skipping user sync for now.");
			} else if (localUserProfile.isDirty()) {
				// POST local changes
				return syncUserProfile(localUserProfile, delegate);
			} else {
				// Just do a GET: delete from local DB and getUserProfile()
				getDatabaseHelper().getUserProfileDao().delete(localUserProfile);
				localUserProfile = getUserProfile(delegate);
			}
			return localUserProfile;
		}
		Log.i(DataHelper.class.getName(), "Not online; skipping user sync for now.");
		return null;
	}

	/**
	 * Load the available activity levels, either from the local DB or from the remote server.
	 * 
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return ActivityLevels
	 * @example ActivityLevels activityLevels = DataHelper.getActivityLevels(null);
	 */
	@SuppressWarnings("unchecked")
	public static ActivityLevels getActivityLevels(DataHelperDelegate delegate) {
		if (delegate == null) {
			return getActivityLevelsSynchronous(delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_GET_ACTIVITY_LEVELS);
		asyncTask.execute(delegate);
		return null;
	}
	
	public static ActivityLevels getActivityLevelsSynchronous(DataHelperDelegate delegate) {
		ActivityLevels activityLevels = null;
		if (isLoggedIn() == false){
			return new ActivityLevels();
		}
		
		try {
			RuntimeExceptionDao<ActivityLevels, Integer> activityLevelsDao = getDatabaseHelper().getActivityLevelsDao();

			// Is this info already in the local DB?
			activityLevels = activityLevelsDao.queryBuilder().queryForFirst();

			if (activityLevels != null) {
				// TODO Check if it's too old; if it is, refresh from the API.
				// if (activityLevels.isTooOld()) {
				// activityLevels = null;
				// }
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (activityLevels == null) {
			// Not; load it from the API.
			activityLevels = ApiHelper.getActivityLevels(delegate);
			if (activityLevels == null) {
				// Use defaults, until we can get them from the API
				return new ActivityLevels();
			}
		}

		persist(activityLevels, METHOD_GET_ACTIVITY_LEVELS);

		return activityLevels;
	}

	/**
	 * Load the diary entries for a specific day.
	 * 
	 * @param day
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return DiaryEntries
	 * @example DiaryEntries diaryEntries = DataHelper.getDailyDiaryEntries(new SimpleDateFormat("yyyy-MM-dd").parse("2012-03-28"), null);
	 */
	@SuppressWarnings("unchecked")
	public static DiaryEntries getDailyDiaryEntries(Date day, DataHelperDelegate delegate) {
		if (delegate != null) {
			AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_GET_DAILY_DIARY_ENTRIES_FOR_DAY);
			asyncTask.execute(day, null);
			return null;
		}

		DiaryEntries dailyDiary = new DiaryEntries();
		dailyDiary.loadEntriesForDay(day);
		return dailyDiary;
	}

	/**
	 * Load the diary entries for a specific date interval.
	 * 
	 * @param Date from
	 * @param Date to
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return DiaryEntries
	 * @example DiaryEntries diaryEntries = DataHelper.getDailyDiaryEntries(new SimpleDateFormat("yyyy-MM-dd").parse("2012-03-01"), new SimpleDateFormat("yyyy-MM-dd").parse("2012-04-30"), null);
	 */
	@SuppressWarnings("unchecked")
	public static DiaryEntries getDailyDiaryEntries(Date from, Date to, DataHelperDelegate delegate) {
		if (delegate != null) {
			AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_GET_DAILY_DIARY_ENTRIES_FOR_DATES);
			asyncTask.execute(from, to, null);
			return null;
		}

		DiaryEntries dailyDiary = new DiaryEntries();
		dailyDiary.loadEntriesForExactDates(from, to);
		return dailyDiary;
	}

	/**
	 * Load today's nutrients (fat, carbs, protein).
	 * 
	 * @param delegate (Optional) To make this call asynchronous, send an object that implements DataHelperDelegate.
	 * @return Map<String, Double>
	 * @example Map<String, Double> todayNutrients = DataHelper.getTodayNutrients(null);
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Double> getTodayNutrients(DataHelperDelegate delegate) {
		if (delegate != null) {
			AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_GET_TODAY_NUTRIENTS);
			asyncTask.execute((DataHelperDelegate) null);
			return null;
		}

		DiaryEntries diaryEntries = DataHelper.getDailyDiaryEntries(new Date(), delegate);
		return diaryEntries.getFatCarbsProtein();
	}

	/**
	 * Load the diary entries for a specific day, and calculate the percentage of fat, carbs & protein for those entries.
	 * 
	 * @param day
	 * @return Map<String, Double>
	 * @example Map<String, Double> percentages = DataHelper.getDailyFatCarbsProtein(new Date());
	 */
	public static Map<String, Double> getDailyFatCarbsProtein(Date day) {
		DiaryEntries dailyDiary = new DiaryEntries();
		dailyDiary.loadEntriesForDay(day);
		return dailyDiary.getFatCarbsProtein();
	}

	/**
	 * Load the diary entries for a specific date, and sum the calories, per type.
	 * 
	 * @param Date day
	 * @return Map<DiaryEntryType, String>
	 * @example Map<DiaryEntryType, String> diarySummaryPerType = DataHelper.getDiarySummaryPerType(new Date());
	 */
	public static Map<DiaryEntryType, String> getDiarySummaryPerType(Date day) {
		DiaryEntries dailyDiary = new DiaryEntries();
		dailyDiary.loadEntriesForDay(day);
		return dailyDiary.getDiarySummaryPerType();
	}

	public static SortedSet<SimpleDate> getAllDates(Date fromDate, Date toDate) {
		SortedSet<SimpleDate> allDates = new TreeSet<SimpleDate>();

		Date day = new SimpleDate(fromDate);
		Calendar nextDay = Calendar.getInstance();

		while (day.before(toDate)) {
			allDates.add(new SimpleDate(day));

			nextDay.setTime(day);
			nextDay.add(Calendar.DATE, 1);
			day = nextDay.getTime();
		}

		return allDates;
	}

	public static Map<SimpleDate, Integer> getDailyCaloriesSum(Date fromDate, Date toDate) {
		Map<SimpleDate, Integer> dailySummary = new LinkedHashMap<SimpleDate, Integer>();
		
		fromDate = new SimpleDate(fromDate);
		toDate = new SimpleDate(toDate);

		SortedSet<SimpleDate> allDates = getAllDates(fromDate, toDate);
		for (SimpleDate day : allDates) {
			dailySummary.put(day, 0);
		}
		
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
		
		Cursor c = DataHelper.getDatabaseHelper().getReadableDatabase().rawQuery("SELECT d.datestamp, SUM(d.servings * f.cals) AS cals " +
				"FROM Diary_Food d JOIN Food f ON (d.food_id = f.id) " +
				"WHERE d.datestamp BETWEEN ? AND ? " +
				"AND d.deleted = 0 " +
				"GROUP BY d.datestamp", new String[] { formater.format(fromDate), formater.format(toDate) });

		c.moveToFirst();
		do {
			try {
				if (c.getCount() == 0) {
					break;
				}
		        SimpleDate datestamp = new SimpleDate(formater.parse(c.getString(0).substring(0, 10)));
		        Double cals = c.getDouble(1);
		        int dailyTotal = dailySummary.get(datestamp) + cals.intValue();
				dailySummary.put(datestamp, dailyTotal);
			} catch (ParseException e) {
				e.printStackTrace();
			}
	    } while (c.moveToNext());
		
		String userWeightQuery = "(SELECT weight FROM Diary_Weight WHERE datestamp <= d.datestamp ORDER BY datestamp DESC LIMIT 1)";
		c = DataHelper.getDatabaseHelper().getReadableDatabase().rawQuery("SELECT d.datestamp, " +
				"-1 * SUM(" +
				"CASE " +
					"WHEN d.caloriesBurned > 0 THEN d.caloriesBurned " +
					"WHEN e.calsPerHour > 0 THEN e.calsPerHour / 60.0 * d.minutes " +
					"WHEN e.exerciseId > 2500 AND e.exerciseId < 5000 THEN e.calFactor * " + userWeightQuery + " * " + WeightDiaryEntry.KG_PER_POUND + " / 60.0 * d.minutes " + // calFactor is calories per hour per kg
					"ELSE e.calFactor * " + userWeightQuery + "  * d.minutes " + // calFactor is calories per minute per lb
				"END) AS cals " +
				"FROM Diary_Exercise d JOIN Exercise e ON (d.exercise_id = e.id) " +
				"WHERE d.datestamp BETWEEN ? AND ? " +
				"AND d.deleted = 0 " +
				"GROUP BY d.datestamp " +
				"ORDER BY d.datestamp", new String[] { formater.format(fromDate), formater.format(toDate) });

		c.moveToFirst();
		do {
			try {
				if (c.getCount() == 0) {
					break;
				}
		        SimpleDate datestamp = new SimpleDate(formater.parse(c.getString(0).substring(0, 10)));
		        Double cals = c.getDouble(1);
		        int dailyTotal = dailySummary.get(datestamp) + cals.intValue();
				dailySummary.put(datestamp, dailyTotal);
			} catch (ParseException e) {
				e.printStackTrace();
			}
	    } while (c.moveToNext());
		
		return dailySummary;
	}

	public static Map<SimpleDate, Integer> getDailyCaloriesGoals(Date fromDate, Date toDate) {
		
		fromDate = new SimpleDate(fromDate);
		toDate = new SimpleDate(toDate);
		
		Map<SimpleDate, Integer> dailySummary = new LinkedHashMap<SimpleDate, Integer>();

		UserProfile userProfile = DataHelper.getUserProfile(null);

		SortedSet<SimpleDate> allDates = getAllDates(fromDate, toDate);

		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
		
		Map<SimpleDate, Double> allWeights = new HashMap<SimpleDate, Double>();
		
		Cursor c = DataHelper.getDatabaseHelper().getReadableDatabase().rawQuery("SELECT d.datestamp, d.weight " +
				"FROM Diary_Weight d " +
				"WHERE d.datestamp BETWEEN ? AND ? " +
				"AND d.deleted = 0 " + 
				"ORDER BY d.datestamp", new String[] { formater.format(fromDate), formater.format(toDate) });

		Double userWeight = null;
		c.moveToFirst();
		do {
			try {
				if (c.getCount() == 0) {
					break;
				}
		        SimpleDate datestamp = new SimpleDate(formater.parse(c.getString(0).substring(0, 10)));
		        Double weight = c.getDouble(1);
		        allWeights.put(datestamp, weight);
		        if (userWeight == null) {
		        	userWeight = weight;
		        }
			} catch (ParseException e) {
				e.printStackTrace();
			}
	    } while (c.moveToNext());
		
		if (userWeight == null) {
			userWeight = userProfile.getWeight();
		}
		for (SimpleDate day : allDates) {
			if (allWeights.get(day) != null) {
				userWeight = allWeights.get(day);
			}
			
			dailySummary.put(day, userProfile.getCaloriesGoal(userWeight));
		}

		return dailySummary;
	}

	/**
	 * Load the diary entries for a specific date, and sum the calories, per type.
	 * 
	 * @return int
	 * @example int dailyCaloriesGoal = DataHelper.getUserDailyCaloriesGoal();
	 */
	public static int getUserDailyCaloriesGoal() {
		UserProfile userProfile = DataHelper.getUserProfile(null);
		if (userProfile != null) {
			return (int) Math.round(userProfile.getCaloriesGoal());
		}
		return 0;
	}

	/**
	 * Return a progress report for the specified day, vs the current daily calories goal of the user.
	 * 
	 * @param Date day
	 * @return UserProgress
	 * @example UserProgress userProgress = DataHelper.getUserCaloriesProgress(new Date());
	 */
	public static UserProgress getUserCaloriesProgress(Date day, int dailyCaloriesGoal) {
		return new UserProgress(day, dailyCaloriesGoal);
	}

	@SuppressWarnings("unchecked")
	public static NewMessageResponse postNewMessage(String message, DataHelperDelegate delegate) {
		if (delegate == null) {
			postNewMessageSynchronous(message, delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_POST_NEW_MESSAGE);
		asyncTask.execute(message, delegate);
		return null;
	}
	
	public static NewMessageResponse postNewMessageSynchronous(String message, DataHelperDelegate delegate) {
		return ApiHelper.postNewMessage(message, delegate);
	}
	
	public static void resetDatabase() {
		getDatabaseHelper().emptyAllTables();
		SharedPreferences settings = context.getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
		settings.edit().clear().commit();
		
		DataHelper.initializePreferences();
		ApiHelper.resetAuthData();
	}

	@SuppressWarnings("unchecked")
	public static NewCommentResponse postNewComment(int messageId, String comment, DataHelperDelegate delegate) {
		if (delegate == null) {
			postNewCommentSynchronous(messageId, comment, delegate);
		}
		AsyncDataHelper asyncTask = new AsyncDataHelper(delegate, DataHelper.METHOD_POST_NEW_COMMENT);
		asyncTask.execute(messageId, comment, delegate);
		return null;
	}

	public static NewCommentResponse postNewCommentSynchronous(int messageId, String comment, DataHelperDelegate delegate) {
		return ApiHelper.postNewComment(messageId, comment, delegate);
	}
	
	public static List<Meal> getMeals() {
		try {
			return getDatabaseHelper().getMealDao().queryBuilder()
					.orderBy(Meal.MEAL_NAME_FIELD_NAME, true)
					.query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<Food> getCustomFoods() {
		try {
			return getDatabaseHelper().getFoodDao().queryBuilder()
					.orderBy(Food.ITEM_TITLE_FIELD_NAME, true)
					.where()
					.eq(Food.FOOD_ID_FIELD_NAME, Food.CUSTOM_FOOD_ID)
					.and()
					.eq(Food.CUSTOM_FIELD_NAME, true)
					.query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<Exercise> getCustomExercises() {
		try {
			return getDatabaseHelper().getExerciseDao().queryBuilder()
					.orderBy(Exercise.TITLE_FIELD_NAME, true)
					.where()
					.eq(Exercise.EXERCISE_ID_FIELD_NAME, Exercise.CUSTOM_EXERCISE_ID)
					.and()
					.eq(Exercise.CUSTOM_FIELD_NAME, true)
					.query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getPref(String name, String defaultValue) {
		return ((String) getPref(name, (Object) defaultValue));
	}

	public static long getPref(String name, long defaultValue) {
		return ((Long) getPref(name, (Object) defaultValue)).longValue();
	}

	public static int getPref(String name, int defaultValue) {
		return ((Integer) getPref(name, (Object) defaultValue)).intValue();
	}

	public static float getPref(String name, float defaultValue) {
		return ((Float) getPref(name, defaultValue)).intValue();
	}

	public static boolean getPref(String name, boolean defaultValue) {
		return ((Boolean) getPref(name, (Object) (Object) defaultValue)).booleanValue();
	}

	public static Object getPref(String name, Object defaultValue) {
		SharedPreferences settings = context.getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
		Object value = settings.getAll().get(name);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

//	public static UnitsPreference getPref(String name, UnitsPreference defaultValue) {
//		return UnitsPreference.valueOf(getPref(name, defaultValue.toString()));
//	}

	public static void setPref(String name, Object value) {
		SharedPreferences settings = context.getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
		if (value == null) {
			settings.edit().remove(name).commit();
		} else if (value instanceof Long) {
			settings.edit().putLong(name, (Long) value).commit();
		} else if (value instanceof String) {
			settings.edit().putString(name, (String) value).commit();
		} else if (value instanceof Integer) {
			settings.edit().putInt(name, (Integer) value).commit();
		} else if (value instanceof Float) {
			settings.edit().putFloat(name, (Float) value).commit();
		} else if (value instanceof Boolean) {
			settings.edit().putBoolean(name, (Boolean) value).commit();
		}
	}

	private static void persist(Object responseData, Method methodCalled) {
		if (responseData != null) {
			// Data was fetched from the remote server; persist it.
			DatabaseHelper.persistData(methodCalled, responseData);
		}
	}
}
