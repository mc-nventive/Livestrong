package com.livestrong.myplate.back.models;

import java.util.Date;
import java.util.LinkedHashMap;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.livestrong.myplate.back.DataHelper;
import com.livestrong.myplate.back.DataHelper.WaterUnits;

@DatabaseTable(tableName = "Diary_Water")
public class WaterDiaryEntry extends DiaryEntry implements LiveStrongDisplayableListItem {
	private static final long serialVersionUID = 6350182980525110160L;

	public final static LinkedHashMap<String, Integer> metricWaterPickerValues = new LinkedHashMap<String, Integer>() {
		private static final long serialVersionUID = 1119891256808288014L;
		{
			for (int i=0; i<=1000; i+=50) {
	            put(i+"", i);
			}
        };
	};

	public final static LinkedHashMap<String, Integer> imperialWaterPickerValues = new LinkedHashMap<String, Integer>() {
		private static final long serialVersionUID = -1502013476888802512L;
		{
			for (int i=0; i<=600; i++) {
	            put(i+"", i);
			}
        };
	};

	public final static double ML_PER_OUNCE = 29.57;
	public final static double ONCES_PER_GLASS = 8;
	
	@DatabaseField
	private double glasses;

	public WaterDiaryEntry() {}
	
	public WaterDiaryEntry(double onces, Date datestamp) {
		super(datestamp, null);
		addOnces(onces);
	}
	
	public String getTitle() {
		return "Total consumed";
	}

	public String getDescription() {
		return getAmountWithUnits(getOnces());
	}
	
	public static String getAmountWithUnits(double onces) {
		WaterUnits waterUnits = WaterUnits.valueOf((String) DataHelper.getPref(DataHelper.PREFS_WATER_UNITS, WaterUnits.MILLILITERS.toString()));
		switch (waterUnits) {
			case MILLILITERS:
				return Math.round(onces * ML_PER_OUNCE) + " ml";
			default:
				return Math.round(onces) + " ounces";
		}
	}

	public void setOnces(double onces) {
		setGlasses(onces / ONCES_PER_GLASS);
	}
	
	public void setGlasses(double glasses) {
		this.glasses = glasses;
		wasModified();
	}

	public double getGlasses() {
		return this.glasses;
	}
	
	public void addOnces(double onces) {
		this.glasses += onces * 1/ONCES_PER_GLASS;
	}
	
	public double getOnces() {
		return this.glasses * ONCES_PER_GLASS;
	}

	@Override
	public String getSmallImage() {
		// TODO Auto-generated method stub
		return null;
	}
}
