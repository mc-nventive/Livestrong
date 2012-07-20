package com.livestrong.myplate.back.models;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.table.DatabaseTable;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.api.LiveStrongDataObjectTypeAdapter;
import com.livestrong.myplate.back.api.models.AbstractLiveStrongApiObject;

@DatabaseTable(tableName = "Meal")
public class Meal extends AbstractLiveStrongApiObject implements LiveStrongDisplayableListItem, Serializable {
	private static final long serialVersionUID = -9005024991955044135L;

	public static final String MEAL_NAME_FIELD_NAME = "mealName";
	
	@DatabaseField(id = true)
	private int mealId;

	@DatabaseField(columnName = MEAL_NAME_FIELD_NAME)
	private String mealName;

	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private MealNutritionInfo nutritionInfo;

	@DatabaseField
	private double cals = 0;

	@ForeignCollectionField(eager = true)
	private Collection<MealItem> items;

	public String getTitle() {
		return mealName;
	}

	public String getDescription() {
		return Math.round(getCals()) + " calories";
	}
	
	public void setNutritionInfo(String json) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(MealNutritionInfo.class, new LiveStrongDataObjectTypeAdapter(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES));
		this.nutritionInfo = gsonBuilder.create().fromJson(json, MealNutritionInfo.class);
	}

	public int getMealId() {
		return mealId;
	}

	public String getMealName() {
		return mealName;
	}

	public MealNutritionInfo getNutritionInfo() {
		return nutritionInfo;
	}

	public double getCals() {
		return cals;
	}

	public Collection<MealItem> getItems() {
		return items;
	}

	public void save() {
		RuntimeExceptionDao<Meal, Integer> mealDao = DataHelper.getDatabaseHelper().getMealDao();
		RuntimeExceptionDao<MealNutritionInfo, Integer> mealNutritionInfoDao = DataHelper.getDatabaseHelper().getMealNutritionInfoDao();
		RuntimeExceptionDao<MealItem, Integer> mealItemDao = DataHelper.getDatabaseHelper().getMealItemDao();

		// Delete old nutrition info entry
		try {
			Meal oldMeal = mealDao.queryForId(this.mealId);
			if (oldMeal != null && oldMeal.getNutritionInfo() != null && oldMeal.getNutritionInfo().getId() > 0) {
				DeleteBuilder<MealNutritionInfo, Integer> builder = mealNutritionInfoDao.deleteBuilder();
				builder.where().eq("id", oldMeal.getNutritionInfo().getId());
				mealNutritionInfoDao.delete(builder.prepare());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Insert new nutrition info entry
		MealNutritionInfo info = getNutritionInfo();
		mealNutritionInfoDao.createOrUpdate(info);
		
		// Insert or update the meal entry
		mealDao.createOrUpdate(this);
		
		// Delete old meal item entries
		try {
			DeleteBuilder<MealItem, Integer> builder = mealItemDao.deleteBuilder();
			builder.where().eq("meal_id", this.mealId);
			mealItemDao.delete(builder.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Insert new meal item entries
		for (MealItem item : getItems()) {
			item.setMeal(this);
			item.loadFood();
			mealItemDao.createOrUpdate(item);
		}
	}

	@Override
	public String getSmallImage() {
		// TODO Auto-generated method stub
		return null;
	}
}
