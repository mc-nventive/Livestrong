package com.livestrong.myplate.back.models;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.livestrong.myplate.back.api.models.AbstractLiveStrongApiObject;

@DatabaseTable(tableName = "MealNutritionInfo")
public class MealNutritionInfo extends AbstractLiveStrongApiObject implements Serializable{
	private static final long serialVersionUID = -4541546848425543268L;

	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField
	private double cals;
	
	@DatabaseField
	private double fat;
	
	@DatabaseField
	private double cholesterol;
	
	@DatabaseField
	private double sodium;
	
	@DatabaseField
	private double carbs;
	
	@DatabaseField
	private double sugars;
	
	@DatabaseField
	private double dietary_fiber;
	
	@DatabaseField
	private double protein;

	public int getId() {
		return id;
	}

	public double getCals() {
		return cals;
	}

	public double getFat() {
		return fat;
	}

	public double getCholesterol() {
		return cholesterol;
	}

	public double getSodium() {
		return sodium;
	}

	public double getCarbs() {
		return carbs;
	}

	public double getSugars() {
		return sugars;
	}

	public double getDietary_fiber() {
		return dietary_fiber;
	}

	public double getProtein() {
		return protein;
	}

}
