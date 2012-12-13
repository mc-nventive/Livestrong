package com.demandmedia.livestrong.android.back.models;

import java.text.DecimalFormat;
import java.util.Date;

import android.database.Cursor;

import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.DataHelper.WeightUnits;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Diary_Weight")
public class WeightDiaryEntry extends DiaryEntry implements LiveStrongDisplayableListItem {
	private static final long serialVersionUID = -3010100787044836596L;

	
	public final static double KG_PER_POUND = 0.45359237;
	public final static double POUND_PER_STONES = 14;
	
	@DatabaseField
	private double weight;
	
	@DatabaseField
	private double calories;

	public WeightDiaryEntry(){
		
	}
	
	public WeightDiaryEntry(double weight, Date datestamp) {
		super(datestamp, null);
		this.weight = convertWeightFromSelectedUnits(weight);
	}
	
	public WeightDiaryEntry(Cursor cursor)
	{
		super(cursor);
		this.weight = cursor.getDouble(cursor.getColumnIndex("weight"));
		this.calories = cursor.getDouble(cursor.getColumnIndex("calories"));
	}
	
	public String getTitle() {
		return "Tracked weight";
	}

	public String getDescription() {
		return getWeightWithUnits();
	}

	public static String getWeightWithUnits(double weight) {
		WeightUnits unitsPreference = WeightUnits.valueOf( (String) DataHelper.getPref(DataHelper.PREFS_WEIGHT_UNITS, WeightUnits.POUNDS.toString()));

		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		
		switch (unitsPreference) {
			case KILOGRAMS:
				return decimalFormat.format(weight * KG_PER_POUND) + "kg";
			case POUNDS:
				return decimalFormat.format(weight) + " pounds";
			case STONES:
				double stones = weight / POUND_PER_STONES;
				return decimalFormat.format(stones) + " stones"; // This is not a typo; the plural of stone is stone (no s), when used as a measurement unit. Ref: http://en.wikipedia.org/wiki/Stone_(Imperial_mass)#Current_use
		}
		return "Unknown";
	}
	
	public static double getWeightForSelectedUnits(double weight){
		WeightUnits unitsPreference = WeightUnits.valueOf( (String) DataHelper.getPref(DataHelper.PREFS_WEIGHT_UNITS, WeightUnits.POUNDS.toString()));
		switch (unitsPreference) {
			case KILOGRAMS:
				return (weight * KG_PER_POUND);
			case POUNDS:
				return weight;
			case STONES:
			default:
				return (double) weight / POUND_PER_STONES;
		}
	}
	
	public static double convertWeightFromSelectedUnits(double weight){
		WeightUnits unitsPreference = WeightUnits.valueOf( (String) DataHelper.getPref(DataHelper.PREFS_WEIGHT_UNITS, WeightUnits.POUNDS.toString()));
		switch (unitsPreference) {
			case KILOGRAMS:
				return (weight / KG_PER_POUND);
			case POUNDS:
				return weight;
			case STONES:
			default:
				return (double) weight * POUND_PER_STONES;
		}
	}

	public String getWeightWithUnits() {
		return getWeightWithUnits(this.weight);
	}

	public void setWeight(double weight) {
		this.weight = convertWeightFromSelectedUnits(weight);
		wasModified();
	}

	public void setCaloriesGoal(double calories) {
		this.calories = calories;
		wasModified();
	}

	public double getWeight() {
		return this.weight;
		
		
	}
	
	public double getWeightForSelectedUnits(){
		return getWeightForSelectedUnits(this.weight);
	}

	public double getCaloriesGoal() {
		return this.calories;
	}

	@Override
	public String getSmallImage() {
		// TODO Auto-generated method stub
		return null;
	}
}
