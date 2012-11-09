package com.demandmedia.livestrong.android.back.models;

import java.io.Serializable;

import android.database.Cursor;

import com.demandmedia.livestrong.android.back.api.models.AbstractLiveStrongApiObject;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

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
	
	public MealNutritionInfo()
	{
		
	}
	
	public MealNutritionInfo(Cursor cursor)
	{
		id = cursor.getInt(cursor.getColumnIndex("id"));
		cals = cursor.getDouble(cursor.getColumnIndex("cals"));
		fat = cursor.getDouble(cursor.getColumnIndex("fat"));
		cholesterol = cursor.getDouble(cursor.getColumnIndex("cholesterol"));
		sodium = cursor.getDouble(cursor.getColumnIndex("sodium"));
		carbs = cursor.getDouble(cursor.getColumnIndex("carbs"));
		sugars = cursor.getDouble(cursor.getColumnIndex("sugards"));
		dietary_fiber = cursor.getDouble(cursor.getColumnIndex("dietary_fiber"));
		protein = cursor.getDouble(cursor.getColumnIndex("protein"));
	}

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
