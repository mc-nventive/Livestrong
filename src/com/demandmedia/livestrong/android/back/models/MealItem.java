package com.demandmedia.livestrong.android.back.models;

import java.io.Serializable;

import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.api.models.AbstractLiveStrongApiObject;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "MealItem")
public class MealItem extends AbstractLiveStrongApiObject implements LiveStrongDisplayableListItem, Serializable {
	private static final long serialVersionUID = 133250407815271974L;

	@DatabaseField(generatedId = true)
	@SuppressWarnings("unused")
	private int id;
	
	@DatabaseField(foreign = true)
	private Meal meal;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private Food food;

	// Refers to Food.foodId
	private int itemId;
	
	@DatabaseField
	private double servings;
	
	@DatabaseField
	private double cals;
	
	@DatabaseField
	private String itemTitle;

	@DatabaseField
	private String itemBrand;

	@DatabaseField
	private String itemShortcut;

	@DatabaseField
	private String brandShortcut;
	
	public String getTitle() {
		return itemTitle;
	}

	public String getDescription() {
		return this.servings + " servings, " + getCals() + " calories";
	}
	
	public void setMeal(Meal meal) {
		this.meal = meal;
	}
	
	public void loadFood() {
		this.food = DataHelper.getFood(this.itemId, null);
	}

	public Meal getMeal() {
		return meal;
	}

	public Food getFood() {
		if (this.food == null){
			loadFood();
		}
		return food;
	}

	public int getItemId() {
		return itemId;
	}

	public double getServings() {
		return servings;
	}

	public double getCals() {
		return cals;
	}

	public String getItemTitle() {
		return itemTitle;
	}

	public String getItemBrand() {
		return itemBrand;
	}

	public String getItemShortcut() {
		return itemShortcut;
	}

	public String getBrandShortcut() {
		return brandShortcut;
	}

	@Override
	public String getSmallImage() {
		if (this.food == null){
			loadFood();
		}
		return this.food.getSmallImage();
	}
}
