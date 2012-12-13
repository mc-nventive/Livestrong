package com.demandmedia.livestrong.android.back.models;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.database.Cursor;

import com.demandmedia.livestrong.android.back.DataHelper;
import com.demandmedia.livestrong.android.back.DataHelper.WaterUnits;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Diary_Water")
public class WaterDiaryEntry extends DiaryEntry implements LiveStrongDisplayableListItem {
	private static final long serialVersionUID = 6350182980525110160L;

	public final static LinkedHashMap<String, Integer> metricWaterPickerValues = new LinkedHashMap<String, Integer>() {
		private static final long serialVersionUID = 1119891256808288014L;
		{
			for (int i=ML_PER_GLASS; i<=MAX_ML_PER_DAY; i+=ML_PER_GLASS) {
	            put(i+"", i);
			}
        };
	};

	public final static LinkedHashMap<String, Integer> imperialWaterPickerValues = new LinkedHashMap<String, Integer>() {
		private static final long serialVersionUID = -1502013476888802512L;
		{
			for (int i=ONCES_PER_GLASS; i<=MAX_ONCES_PER_DAY; i+=ONCES_PER_GLASS) {
	            put(i+"", i);
			}
        };
	};

	public final static double ML_PER_OUNCE = 29.57;
	public final static int ONCES_PER_GLASS = 8;
	public final static int ML_PER_GLASS = 237;
	public final static int MAX_GLASS_PER_DAY = 43;
	public final static int MAX_ML_PER_DAY = MAX_GLASS_PER_DAY * ML_PER_GLASS;
	public final static int MAX_ONCES_PER_DAY = MAX_GLASS_PER_DAY * ONCES_PER_GLASS;
	
	@DatabaseField
	private double glasses;

	public WaterDiaryEntry() {}
	
	public WaterDiaryEntry(double onces, Date datestamp) {
		super(datestamp, null);
		addOnces(onces);
	}
	
	public WaterDiaryEntry(Cursor cursor)
	{
		super(cursor);
		this.glasses = cursor.getInt(cursor.getColumnIndex("glasses"));
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
				return metricWaterPickerValues.values().toArray()[getMetricWaterIndex(onces)] + " ml";
			default:
				return Math.round(onces) + " ounces";
		}
	}

	public void setOnces(double onces) {
		setGlasses((int)((int)onces / ONCES_PER_GLASS));
	}
	
	public void setGlasses(double glasses) {
		this.glasses = glasses;
		wasModified();
	}

	public double getGlasses() {
		return this.glasses;
	}
	
	public void addOnces(double onces) {
		this.glasses += (int)((int)onces * 1/ONCES_PER_GLASS);
		wasModified();
	}
	
	public double getOnces() {
		return this.glasses * ONCES_PER_GLASS;
	}

	@Override
	public String getSmallImage() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static int getOnceWater(double ml) {
		int glasses = (int) Math.round(ml / WaterDiaryEntry.ML_PER_GLASS);

		// To know what value to select, we will choose the one that has the smallest difference (distance) with the value from the DB
		int index = 0;
		Map<Integer, Integer> distances = new HashMap<Integer, Integer>();
		for (double ozValue : WaterDiaryEntry.imperialWaterPickerValues.values()) {
			distances.put(index, Math.abs(glasses - (int)Math.round(ozValue / WaterDiaryEntry.ONCES_PER_GLASS)));
			index++;
		}

		// Find the index (key) of the smallest distance
		Entry<Integer, Integer> min = null;
		for (Entry<Integer, Integer> entry : distances.entrySet()) {
		    if (min == null || min.getValue() > entry.getValue()) {
		        min = entry;
		    }
		}
		
		return WaterDiaryEntry.ONCES_PER_GLASS * (null != min ? 1 + min.getKey() : 1);
	}
	
	public static int getMetricWaterIndex(double onces) {
		int mlGlasses = (int) Math.round(onces / WaterDiaryEntry.ONCES_PER_GLASS);

		// To know what value to select, we will choose the one that has the smallest difference (distance) with the value from the DB
		int index = 0;
		Map<Integer, Integer> distances = new HashMap<Integer, Integer>();
		for (double mlValue : WaterDiaryEntry.metricWaterPickerValues.values()) {
			distances.put(index, Math.abs(mlGlasses - (int)Math.round(mlValue / WaterDiaryEntry.ML_PER_GLASS)));
			index++;
		}

		// Find the index (key) of the smallest distance
		Entry<Integer, Integer> min = null;
		for (Entry<Integer, Integer> entry : distances.entrySet()) {
		    if (min == null || min.getValue() > entry.getValue()) {
		        min = entry;
		    }
		}
		
		return null != min ? min.getKey() : 0;
	}
}
